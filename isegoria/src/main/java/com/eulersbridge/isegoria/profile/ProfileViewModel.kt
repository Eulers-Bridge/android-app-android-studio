package com.eulersbridge.isegoria.profile

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.network.api.models.*
import com.eulersbridge.isegoria.onSuccess
import com.eulersbridge.isegoria.util.data.RetrofitLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    internal val currentSectionIndex = MutableLiveData<Int>()

    internal val user = MutableLiveData<GenericUser>()
    private val targetBadgeLevel = MutableLiveData<Int>()

    private var remainingBadges: LiveData<List<Badge>?>? = null
    private var completedBadges: LiveData<List<Badge>?>? = null

    private var institutionName: LiveData<String?>? = null

    private var tasks: LiveData<List<Task>>? = null
    private var remainingTasks: LiveData<List<Task>?>? = null
    private var completedTasks: LiveData<List<Task>?>? = null
    internal val totalXp = MutableLiveData<Long>()

    internal val contactsCount = MutableLiveData<Long>()
    internal val totalTasksCount = MutableLiveData<Long>()

    private var userPhoto: LiveData<Photo?>? = null

    init {
        currentSectionIndex.value = 0
        targetBadgeLevel.value = 0
    }

    internal fun viewFriends() {
        val isAnotherUser = user.value is Contact

        if (!isAnotherUser) {
            val app = getApplication<IsegoriaApp>()
            app.friendsVisible.value = true
        }
    }

    fun setTargetBadgeLevel(targetBadgeLevel: Int) {
        this.targetBadgeLevel.value = targetBadgeLevel
    }

    internal fun onSectionIndexChanged(newIndex: Int) {
        currentSectionIndex.value = newIndex
    }

    internal fun viewTasksProgress() {
        currentSectionIndex.value = 1
    }

    internal fun logOut() {
        val app = getApplication<IsegoriaApp>()
        app.logOut()
    }

    internal fun setUser(user: GenericUser) {
        this.user.value = user
    }

    internal fun fetchUserStats() {
        val (_, _, email) = getUser() ?: return

        val app = getApplication<IsegoriaApp>()

        app.api.getContact(email).onSuccess {
            contactsCount.value = it.contactsCount
            totalTasksCount.value = it.totalTasksCount
        }
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
    internal fun getRemainingBadges(): LiveData<List<Badge>?>? {
        return getRemainingBadges(false)
    }

    /**
     * @param limitToLevel Whether to only include badges matching the user's target level.
     * @return A list of the badges the user has yet to complete, optionally filtered
     * by the user's target level.
     */
    fun getRemainingBadges(limitToLevel: Boolean): LiveData<List<Badge>?>? {
        if (remainingBadges == null) {
            val app = getApplication<IsegoriaApp>()

            val user = getUser()
            if (user != null) {
                remainingBadges = RetrofitLiveData(app.api.getRemainingBadges(user.getId()))
            } else {
                SingleLiveData(null)
            }
        }

        return if (limitToLevel && remainingBadges != null) {
            Transformations.switchMap(remainingBadges!!) { badges ->
                SingleLiveData(badges?.filter { it.level == targetBadgeLevel.value })
            }

        } else {
            remainingBadges
        }
    }

    /**
     * @return A list of the user's completed badges, *filtered by those matching their target
     * badge level*.
     */
    fun getCompletedBadges(): LiveData<List<Badge>?>? {
        if (completedBadges == null) {
            val app = getApplication<IsegoriaApp>()

            val user = getUser()
            if (user != null)
                completedBadges = RetrofitLiveData(app.api.getCompletedBadges(user.getId()))
        }

        return if (completedBadges == null) {
            SingleLiveData(null)
        } else {
            Transformations.switchMap(completedBadges!!) { badges ->
                SingleLiveData(badges?.filter { it.level == targetBadgeLevel.value })
            }
        }
    }

    internal fun getInstitutionName(): LiveData<String?>? {
        if (institutionName == null) {
            val app = getApplication<IsegoriaApp>()

            val user = getUser()
            if (user?.institutionId != null) {

                val institutionRequest =
                    RetrofitLiveData(app.api.getInstitution(user.institutionId!!))

                institutionName = Transformations.switchMap(institutionRequest) { institution ->
                    SingleLiveData(institution?.getName())
                }

            } else {
                return SingleLiveData(null)
            }
        }

        return institutionName
    }

    internal fun getTasks(): LiveData<List<Task>> {
        if (tasks == null) {
            val app = getApplication<IsegoriaApp>()
            tasks = RetrofitLiveData(app.api.getTasks())
        }

        return tasks!!
    }

    internal fun getRemainingTasks(): LiveData<List<Task>?>? {
        if (remainingTasks == null || remainingTasks!!.value == null) {
            val app = getApplication<IsegoriaApp>()

            return Transformations.switchMap(app.loggedInUser) {
                if (it != null) {
                    remainingTasks = RetrofitLiveData(app.api.getRemainingTasks(it.getId()))
                    return@switchMap remainingTasks
                }

                return@switchMap SingleLiveData<List<Task>?>(null)
            }
        }

        return remainingTasks
    }

    internal fun getCompletedTasks(): LiveData<List<Task>?>? {
        if (completedTasks == null || completedTasks!!.value == null) {
            val app = getApplication<IsegoriaApp>()

            return Transformations.switchMap(app.loggedInUser) { user ->
                if (user == null)
                    return@switchMap SingleLiveData<List<Task>?>(null)

                val tasksRequest = RetrofitLiveData(app.api.getCompletedTasks(user.getId()))

                completedTasks = Transformations.switchMap(tasksRequest) {
                    totalXp.value = it?.fold(0L) { sum, task -> sum + task.xpValue } ?: 0

                    SingleLiveData(it)
                }

                return@switchMap completedTasks
            }
        }

        return completedTasks
    }

    internal fun getUserPhoto(): LiveData<Photo?>? {
        if (userPhoto == null) {
            val app = getApplication<IsegoriaApp>()

            return Transformations.switchMap(app.loggedInUser) { user ->
                if (user == null)
                    return@switchMap SingleLiveData<Photo?>(null)

                val photosRequest = RetrofitLiveData(app.api.getPhotos(user.email))

                userPhoto = Transformations.switchMap(photosRequest) {
                    SingleLiveData(it?.photos?.firstOrNull())
                }

                return@switchMap userPhoto
            }
        }

        return userPhoto
    }

    /**
     * Convenience method to cancel a LiveData object if it exists and is a Retrofit API request.
     */
    private fun cancelIfPossible(liveData: LiveData<*>?) {
        (liveData as? RetrofitLiveData)?.cancel()
    }

    override fun onCleared() {
        cancelIfPossible(remainingBadges)
        cancelIfPossible(completedBadges)
        cancelIfPossible(institutionName)
        cancelIfPossible(tasks)
        cancelIfPossible(remainingTasks)
        cancelIfPossible(completedTasks)
        cancelIfPossible(userPhoto)
    }
}
