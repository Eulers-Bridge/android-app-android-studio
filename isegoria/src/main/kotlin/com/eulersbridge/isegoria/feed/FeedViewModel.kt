package com.eulersbridge.isegoria.feed

import com.eulersbridge.isegoria.AppRouter
import com.eulersbridge.isegoria.util.BaseViewModel

class FeedViewModel(private val appRouter: AppRouter) : BaseViewModel() {

    internal fun showFriends() {
        appRouter.setFriendsScreenVisible(true)
    }
}
