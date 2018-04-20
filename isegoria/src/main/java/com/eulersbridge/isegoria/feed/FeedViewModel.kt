package com.eulersbridge.isegoria.feed

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.eulersbridge.isegoria.AppRouter

class FeedViewModel(application: Application) : AndroidViewModel(application) {

    internal fun showFriends() {
        val app = getApplication<Application>() as? AppRouter
        app!!.setFriendsScreenVisible(true)
    }
}
