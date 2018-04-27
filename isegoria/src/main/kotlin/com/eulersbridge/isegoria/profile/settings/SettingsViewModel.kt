package com.eulersbridge.isegoria.profile.settings

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.net.Uri
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.Photo
import com.eulersbridge.isegoria.util.BaseViewModel
import com.eulersbridge.isegoria.util.extension.subscribeSuccess
import com.eulersbridge.isegoria.util.extension.toBooleanSingle
import com.eulersbridge.isegoria.util.extension.toLiveData
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

class SettingsViewModel @Inject constructor(private val repository: Repository) : BaseViewModel() {

    internal val userPhoto = MutableLiveData<Photo?>()
    internal val profilePhotoUrl: String? = repository.getUserProfilePhotoUrl()

    internal val optOutDataCollectionSwitchChecked = MutableLiveData<Boolean>()
    internal val optOutDataCollectionSwitchEnabled = MutableLiveData<Boolean>()

    internal val doNotTrackSwitchChecked = MutableLiveData<Boolean>()
    internal val doNotTrackSwitchEnabled = MutableLiveData<Boolean>()

    init {
        optOutDataCollectionSwitchEnabled.value = false
        doNotTrackSwitchEnabled.value = false

        val user = repository.getUser()

        optOutDataCollectionSwitchChecked.value = user.isOptedOutOfDataCollection
        optOutDataCollectionSwitchEnabled.value = true

        doNotTrackSwitchChecked.value = user.trackingOff
        doNotTrackSwitchEnabled.value = true

        fetchUserPhoto()
    }

    internal fun onOptOutDataCollectionChange(isChecked: Boolean) {
        optOutDataCollectionSwitchEnabled.value = false

        repository.setUserOptedOutOfDataCollection(isChecked).subscribeBy(
                onComplete = {
                    optOutDataCollectionSwitchChecked.postValue(isChecked)
                },
                onError = {
                    optOutDataCollectionSwitchChecked.postValue(!isChecked)
                }
        ).addToDisposable()
    }

    internal fun onTrackingChange(isChecked: Boolean) {
        doNotTrackSwitchEnabled.value = false

        repository.setUserTrackingOff(isChecked).subscribeBy(
                onComplete = {
                    doNotTrackSwitchChecked.postValue(isChecked)
                },
                onError = {
                    // Restore to previous checked state
                    doNotTrackSwitchChecked.postValue(!isChecked)
                }
        ).addToDisposable()
    }

    private fun fetchUserPhoto() {
        repository.getUserPhoto().subscribeSuccess {
            userPhoto.postValue(it.value)
        }.addToDisposable()
    }

    internal fun updateUserPhoto(imageUri: Uri): LiveData<Boolean> {
        return repository.setUserPhoto(imageUri)
                .toBooleanSingle()
                .toLiveData()
    }
}
