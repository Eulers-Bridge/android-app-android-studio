package com.eulersbridge.isegoria

import io.reactivex.Observable

interface AppRouter {
    fun setUserVerificationScreenVisible(visible: Boolean)
    fun getUserVerificationScreenVisible(): Observable<Boolean>

    fun getFriendsScreenVisible(): Observable<Boolean>
    fun setFriendsScreenVisible(visible: Boolean)
}