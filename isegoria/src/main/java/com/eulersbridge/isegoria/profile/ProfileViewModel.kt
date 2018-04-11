package com.eulersbridge.isegoria.profile

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.network.api.API
import com.eulersbridge.isegoria.network.api.models.*
import com.eulersbridge.isegoria.onSuccess
import com.eulersbridge.isegoria.util.data.RetrofitLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData
import javax.inject.Inject

class ProfileViewModel
@Inject constructor(
    private val app: IsegoriaApp,
    private val api: API
) : ViewModel() {

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

        if (!isAnotherUser)
            app.friendsVisible.value = true
    }

    fun setTargetBadgeLevel(targetBadgeLevel: Int?) {
        this.targetBadgeLevel.value = targetBadgeLevel ?: 0
    }

    internal fun onSectionIndexChanged(newIndex: Int) {
        currentSectionIndex.value = newIndex
    }

    internal fun viewTasksProgress() {
        currentSectionIndex.value = 1
    }

    internal fun logOut() {
        app.logOut()
    }

    internal fun setUser(user: GenericUser) {
        this.user.value = user
    }

    internal fun fetchUserStats() {
        val (_, _, email) = getUser() ?: return

        api.getContact(email).onSuccess {
            contactsCount.value = it?.contactsCount
            totalTasksCount.value = it?.totalTasksCount
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
            val user = getUser()
            if (user != null) {
                remainingBadges = RetrofitLiveData(api.getRemainingBadges(user.getId()))
            } else {
                return SingleLiveData(null)
            }
        }

        return if (limitToLevel && remainingBadges != null) {
            Transformations.switchMap(remainingBadges!!) { badges ->
                return@switchMap SingleLiveData(badges?.filter { it.level == targetBadgeLevel.value })
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
            val user = getUser()
            if (user != null)
                completedBadges = RetrofitLiveData(api.getCompletedBadges(user.getId()))
        }

        return if (completedBadges == null) {
            SingleLiveData(null)
        } else {
            Transformations.switchMap(completedBadges!!) { badges ->
                return@switchMap SingleLiveData(badges?.filter { it.level == targetBadgeLevel.value })
            }
        }
    }

    internal fun getInstitutionName(): LiveData<String?>? {
        if (institutionName == null) {
            val user = getUser()
            return if (user?.institutionId != null) {

                val institutionRequest =
                    RetrofitLiveData(api.getInstitution(user.institutionId!!))

                institutionName = Transformations.switchMap(institutionRequest) request@ { institution ->
                    return@request SingleLiveData(institution?.getName())
                }

                institutionName

            } else {
                SingleLiveData(null)
            }
        }

        return institutionName
    }

    internal fun getTasks(): LiveData<List<Task>> {
        if (tasks == null)
            tasks = RetrofitLiveData(api.getTasks())

        return tasks!!
    }

    internal fun getRemainingTasks(): LiveData<List<Task>?>? {
        if (remainingTasks?.value == null) {
            return Transformations.switchMap(app.loggedInUser) {
                return@switchMap if (it == null) {
                    SingleLiveData<List<Task>?>(null)

                } else {
                    remainingTasks = RetrofitLiveData(api.getRemainingTasks(it.getId()))
                    remainingTasks
                }
            }
        }

        return remainingTasks
    }

    internal fun getCompletedTasks(): LiveData<List<Task>?>? {
        if (completedTasks?.value == null) {
            return Transformations.switchMap(app.loggedInUser) { user ->
                return@switchMap if (user == null) {
                    SingleLiveData<List<Task>?>(null)

                } else {
                    val tasksRequest = RetrofitLiveData(api.getCompletedTasks(user.getId()))

                    completedTasks = Transformations.switchMap(tasksRequest) {
                        totalXp.value = it?.fold(0L) { sum, task -> sum + task.xpValue } ?: 0

                        SingleLiveData(it)
                    }

                    completedTasks
                }
            }
        }

        return completedTasks
    }

    internal fun getUserPhoto(): LiveData<Photo?>? {
        if (userPhoto == null) {
            return Transformations.switchMap(app.loggedInUser) { user ->
                return@switchMap if (user == null) {
                    SingleLiveData<Photo?>(null)

                } else {
                    val photosRequest = RetrofitLiveData(api.getPhotos(user.email))

                    userPhoto = Transformations.switchMap(photosRequest) request@ {
                        return@request SingleLiveData(it?.photos?.firstOrNull())
                    }

                    userPhoto
                }
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
