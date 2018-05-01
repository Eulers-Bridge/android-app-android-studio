package com.eulersbridge.isegoria.profile

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.eulersbridge.isegoria.AppRouter
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.*
import com.eulersbridge.isegoria.util.BaseViewModel
import com.eulersbridge.isegoria.util.data.SingleLiveData
import com.eulersbridge.isegoria.util.extension.subscribeSuccess
import com.eulersbridge.isegoria.util.extension.toBooleanSingle
import com.eulersbridge.isegoria.util.extension.toLiveData
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
        private val repository: Repository,
        private val appRouter: AppRouter?
) : BaseViewModel() {

    data class BadgeCount(val remaining: Int, val completed: Int)

    internal val currentSectionIndex = MutableLiveData<Int>()

    internal val user = MutableLiveData<GenericUser>()
    internal val userPhoto = MutableLiveData<Photo?>()
    internal val institutionName = MutableLiveData<String?>()
    internal val remainingBadges = MutableLiveData<List<Badge>>()
    internal val badgeCount = MutableLiveData<BadgeCount?>()
    internal val tasks = MutableLiveData<List<Task>>()

    internal val personalityTestHintVisible = MutableLiveData<Boolean>()
    internal val viewProgressHintVisible = MutableLiveData<Boolean>()

    private var targetBadgeLevel = 0

    internal val totalXp = MutableLiveData<Long>()
    internal val contactsCount = MutableLiveData<Long>()
    internal val totalTasksCount = MutableLiveData<Long>()

    init {
        currentSectionIndex.value = 0
    }

    internal fun viewFriends() {
        val isLoggedInUser = user.value is User

        if (isLoggedInUser)
            appRouter!!.setFriendsScreenVisible(true)
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

        fetchUserInstitutionName(user)
        fetchUserStats(user)

        if (user is User) {
            val isLoggedInUser = user !is Contact
            val isLoggedInUserWithPersonality = user.hasPersonality

            if (!isLoggedInUser || isLoggedInUserWithPersonality)
                personalityTestHintVisible.value = false

            if (isLoggedInUser) {
                fetchBadgeCounts(user)
                viewProgressHintVisible.value = true
            }

        } else {
            viewProgressHintVisible.value = false
            personalityTestHintVisible.value = false
        }

        getTasks()
        fetchUserPhoto()
    }

    private fun fetchUserInstitutionName(user: GenericUser) {
        user.institutionId?.let { id ->
            repository.getInstitutionName(id)
                    .subscribeSuccess { result ->
                        institutionName.postValue(result.value)
                    }
                    .addToDisposable()
        }
    }

    private fun fetchUserStats(user: GenericUser) {
        repository.getContact(user.email)
                .subscribeSuccess { result ->
                    result.value?.let {
                        contactsCount.postValue(it.contactsCount)
                        totalTasksCount.postValue(it.totalTasksCount)
                    }
                }
                .addToDisposable()
    }

    private fun getUser(): User? {
        return if (user.value is User) {
            user.value as User?

        } else {
            null
        }
    }

    private fun fetchBadgeCounts(user: User) {
        user.id?.let { id ->
            repository.getUserRemainingBadges(id)
                    .subscribeSuccess { badges ->
                        remainingBadges.postValue(badges.filter { it.level == targetBadgeLevel })
                        badgeCount.postValue(BadgeCount(badges.size, user.completedBadgesCount.toInt()))
                    }
                    .addToDisposable()
        }
    }

    /**
     * @return A list of the user's completed badges, *filtered by those matching their target
     * badge level*.
     */
    fun getCompletedBadges(): LiveData<List<Badge>> {
        return getUser()?.getId()?.let {
            return repository.getUserCompletedBadges(it)
                    .map { it.filter { it.level == targetBadgeLevel } }
                    .toLiveData()
        } ?: SingleLiveData(emptyList())
    }

    private fun getTasks() {
        repository.getTasks()
                .subscribeSuccess { tasks.postValue(it) }
                .addToDisposable()
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

    private fun fetchUserPhoto() {
        repository.getUserPhoto()
                .subscribeSuccess { userPhoto.postValue(it.value) }
                .addToDisposable()
    }
}
