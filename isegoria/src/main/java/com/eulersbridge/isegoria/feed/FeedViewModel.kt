package com.eulersbridge.isegoria.feed

import android.app.Application
import android.arch.lifecycle.AndroidViewModel

import com.eulersbridge.isegoria.IsegoriaApp

class FeedViewModel(application: Application) : AndroidViewModel(application) {

    internal fun showFriends() {
        val app = getApplication<IsegoriaApp>()
        app.friendsVisible.value = true
    }
}
