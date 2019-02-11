package com.eulersbridge.isegoria.data

import com.eulersbridge.isegoria.network.api.model.User

sealed class LoginState {
    class LoggingIn : LoginState()
    class LoggedIn(val user: User): LoginState()
    class LoginFailure : LoginState()
    class LoginUnauthorised : LoginState()
    class LoggedOut : LoginState()
}