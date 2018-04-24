package com.eulersbridge.isegoria.profile

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.*
import com.eulersbridge.isegoria.util.BaseViewModel
import com.eulersbridge.isegoria.util.data.SingleLiveData
import com.eulersbridge.isegoria.util.extension.map
import com.eulersbridge.isegoria.util.extension.subscribeSuccess
import com.eulersbridge.isegoria.util.extension.toBooleanSingle
import com.eulersbridge.isegoria.util.extension.toLiveData
import javax.inject.Inject

class ProfileViewModel @Inject constructor(private val repository: Repository) : BaseViewModel() {

    internal val friendsScreenVisible = MutableLiveData<Boolean>()
    internal val currentSectionIndex = MutableLiveData<Int>()
    internal val user = MutableLiveData<GenericUser>()

    private var targetBadgeLevel = 0
    private var institutionName: String? = null

    internal val totalXp = MutableLiveData<Long>()
    internal val contactsCount = MutableLiveData<Long>()
    internal val totalTasksCount = MutableLiveData<Long>()

    init {
        friendsScreenVisible.value = false
        currentSectionIndex.value = 0
    }

    internal fun viewFriends() {
        val isAnotherUser = user.value is Contact

        if (!isAnotherUser) {
            friendsScreenVisible.value = true
            friendsScreenVisible.value = false
        }
    }

    fun setTargetBadgeLevel(targetBadgeLevel: Int?) {
        this.targetBadgeLevel = targetBadgeLevel ?: 0
    }

    internal fun onSectionIndexChanged(newIndex: Int) {
        currentSectionIndex.value = newIndex
    }

    internal fun viewTasksProgress() {
        currentSectionIndex.value = 1
    }

    internal fun logOut() {
        repository.logOut()
                .toBooleanSingle()
                .subscribe()
                .addToDisposable()
    }

    internal fun setUser(user: GenericUser) {
        this.user.value = user
    }

    internal fun fetchUserStats() {
        val (_, _, email) = getUser() ?: return

        repository.getContact(email).subscribeSuccess { result ->
            result.value?.let {
                contactsCount.postValue(it.contactsCount)
                totalTasksCount.postValue(it.totalTasksCount)
            }
        }.addToDisposable()
    }

    private fun getUser(): User? {
        return if (user.value is User) {
            user.value as User?

        } else {
            null
        }
    }

    /**
     * @return A list of the badges the user has yet to complete, regardless of the badges'
     * or the user's level.
     */
    internal fun getRemainingBadges(): LiveData<List<Badge>> {
        return getRemainingBadges(false)
    }

    /**
     * @param limitToLevel Whether to only include badges matching the user's target level.
     * @return A list of the badges the user has yet to complete, optionally filtered
     * by the user's target level.
     */
    fun getRemainingBadges(limitToLevel: Boolean): LiveData<List<Badge>> {
        return getUser()?.getId()?.let {
            return repository.getUserRemainingBadges(it)
                    .map {
                        if (limitToLevel) {
                            it.filter { it.level == targetBadgeLevel }
                        } else {
                            it
                        }
                    }
                    .toLiveData()

        } ?: SingleLiveData(emptyList())
    }

    /**
     * @return A list of the user's completed badges, *filtered by those matching their target
     * badge level*.
     */
    fun getCompletedBadges(): LiveData<List<Badge>>? {
        return getUser()?.getId()?.let {
            return repository.getUserCompletedBadges(it)
                    .map { it.filter { it.level == targetBadgeLevel } }
                    .toLiveData()
        } ?: SingleLiveData(emptyList())
    }

    internal fun getInstitutionName(): LiveData<String?> {
        if (institutionName == null) {

            getUser()?.institutionId?.let {
                repository.getInstitutionName(it)
                        .toLiveData()
                        .map { it.value ?: "" }

            } ?: SingleLiveData(null)
        }

        return SingleLiveData(institutionName)
    }

    internal fun getTasks(): LiveData<List<Task>> {
        return repository.getTasks().toLiveData()
    }

    internal fun getRemainingTasks(): LiveData<List<Task>?>? {
        return repository.getRemainingTasks().map {
            val totalXpValue = it.fold(0L) { sum, task -> sum + task.xpValue }
            totalXp.postValue(totalXpValue)
            it
        }.toLiveData()
    }

    internal fun getCompletedTasks(): LiveData<List<Task>> {
        return repository.getCompletedTasks().toLiveData()
    }

    internal fun getUserPhoto(): LiveData<Photo?> {
        return repository.getUserPhoto().toLiveData().map { it.value }
    }
}
