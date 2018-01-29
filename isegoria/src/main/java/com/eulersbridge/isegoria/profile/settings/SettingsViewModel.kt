package com.eulersbridge.isegoria.profile.settings

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.net.Uri
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.network.api.models.Photo
import com.eulersbridge.isegoria.network.api.models.UserSettings
import com.eulersbridge.isegoria.util.data.RetrofitLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private var userPhoto: LiveData<Photo?>? = null

    internal val optOutDataCollectionSwitchChecked = MutableLiveData<Boolean>()
    internal val optOutDataCollectionSwitchEnabled = MutableLiveData<Boolean>()

    internal val doNotTrackSwitchChecked = MutableLiveData<Boolean>()
    internal val doNotTrackSwitchEnabled = MutableLiveData<Boolean>()

    internal val userProfilePhotoURL: LiveData<String?>
        get() {
            val app = getApplication<IsegoriaApp>()

            return Transformations.switchMap(
                app.loggedInUser
            ) { SingleLiveData(it.profilePhotoURL) }
        }

    init {
        optOutDataCollectionSwitchEnabled.value = false
        doNotTrackSwitchEnabled.value = false

        val app = application as IsegoriaApp
        val user = app.loggedInUser.value
        if (user != null) {
            optOutDataCollectionSwitchChecked.value = user.isOptedOutOfDataCollection
            optOutDataCollectionSwitchEnabled.value = true

            doNotTrackSwitchChecked.value = user.trackingOff
            doNotTrackSwitchEnabled.value = true
        }
    }

    internal fun onOptOutDataCollectionChange(isChecked: Boolean) {
        optOutDataCollectionSwitchEnabled.value = false

        val app = getApplication<IsegoriaApp>()
        val user = app.loggedInUser.value

        if (user != null) {
            val userSettings = UserSettings(user.trackingOff, isChecked)

            app.api.updateUserDetails(user.email, userSettings).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        optOutDataCollectionSwitchChecked.value = isChecked
                        optOutDataCollectionSwitchEnabled.value = true

                        app.setOptedOutOfDataCollection(isChecked)
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    optOutDataCollectionSwitchChecked.value = !isChecked
                    optOutDataCollectionSwitchEnabled.value = true
                }
            })
        }
    }

    internal fun onTrackingChange(isChecked: Boolean) {
        doNotTrackSwitchEnabled.value = false

        val app = getApplication<IsegoriaApp>()
        val user = app.loggedInUser.value

        if (user != null) {
            val userSettings = UserSettings(isChecked, user.isOptedOutOfDataCollection)

            app.api.updateUserDetails(user.email, userSettings).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        doNotTrackSwitchChecked.value = isChecked

                        app.setTrackingOff(isChecked)

                    } else {
                        doNotTrackSwitchChecked.value = !isChecked
                    }

                    doNotTrackSwitchEnabled.value = true
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    // Restore to previous checked state
                    doNotTrackSwitchChecked.value = !isChecked
                    doNotTrackSwitchEnabled.value = true
                }
            })
        }
    }

    internal fun getUserPhoto(): LiveData<Photo?>? {
        if (userPhoto == null) {
            val app = getApplication<IsegoriaApp>()
            val user = app.loggedInUser.value

            if (user != null) {
                val photosRequest = RetrofitLiveData(app.api.getPhotos(user.email))

                userPhoto = Transformations.switchMap(photosRequest) { photosResponse ->
                    if (photosResponse != null && photosResponse.totalPhotos > 0)
                        SingleLiveData(photosResponse.photos?.get(0))

                    SingleLiveData<Photo?>(null)
                }
            }
        }

        return userPhoto
    }

    internal fun updateUserPhoto(imageUri: Uri): LiveData<Boolean> {
        val file = File(imageUri.path)

        return IsegoriaApp.networkService.uploadNewUserPhoto(file)
    }

    override fun onCleared() {
        (userPhoto as? RetrofitLiveData)?.cancel()
    }
}
