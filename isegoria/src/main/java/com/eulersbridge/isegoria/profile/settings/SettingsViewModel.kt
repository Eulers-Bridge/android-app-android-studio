package com.eulersbridge.isegoria.profile.settings

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.net.Uri
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.Photo
import com.eulersbridge.isegoria.util.extension.map
import com.eulersbridge.isegoria.util.extension.toBooleanSingle
import com.eulersbridge.isegoria.util.extension.toLiveData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

class SettingsViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

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
    }

    override fun onCleared() {
        compositeDisposable.dispose()
    }

    internal fun getProfilePhotoUrl(): String? = repository.getUserProfilePhotoUrl()

    internal fun onOptOutDataCollectionChange(isChecked: Boolean) {
        optOutDataCollectionSwitchEnabled.value = false

        repository.setUserOptedOutOfDataCollection(isChecked).subscribeBy(
                onComplete = {
                    optOutDataCollectionSwitchChecked.postValue(isChecked)
                },
                onError = {
                    optOutDataCollectionSwitchChecked.postValue(!isChecked)
                }
        ).addTo(compositeDisposable)
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
        ).addTo(compositeDisposable)
    }

    internal fun getUserPhoto(): LiveData<Photo?> {
        return repository.getUserPhoto().toLiveData().map {
            it.value
        }
    }

    internal fun updateUserPhoto(imageUri: Uri): LiveData<Boolean> {
        return repository.setUserPhoto(imageUri).toBooleanSingle().toLiveData()
    }
}
