package com.eulersbridge.isegoria

import com.eulersbridge.isegoria.network.api.model.User

sealed class LoginState {
    class LoggingIn : LoginState()
    class LoggedIn(val user: User): LoginState()
    class LoginFailure : LoginState()
    class LoggedOut : LoginState()
}