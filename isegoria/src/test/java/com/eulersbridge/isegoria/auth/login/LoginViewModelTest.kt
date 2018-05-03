package com.eulersbridge.isegoria.auth.login

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.Observer
import com.eulersbridge.isegoria.data.LoginState
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.API
import com.nhaarman.mockitokotlin2.*
import io.reactivex.subjects.BehaviorSubject
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var repository: Repository
    private lateinit var api: API
    private lateinit var loginViewModel: LoginViewModel

    @Before
    fun setUp() {
        repository = mock {
            on { getLoginState() } doReturn BehaviorSubject.createDefault<LoginState>(LoginState.LoggedOut())
        }
        api = mock()

        loginViewModel = LoginViewModel(repository, api)
    }

    @Test
    fun `view model does not present a password error given a valid password`() {
        val observer = mock<Observer<Boolean>>()
        loginViewModel.passwordError.observeForever(observer)

        loginViewModel.setPassword("thi\$,i5af4ntasticp4S5w0rd!")

        then(observer).should(times(1)).onChanged(false)
        then(observer).should(never()).onChanged(true)
    }

    @Test
    fun `view model presents a password error given a blank password`() {
        val observer = mock<Observer<Boolean>>()
        loginViewModel.passwordError.observeForever(observer)

        loginViewModel.setPassword("")

        then(observer).should(times(1)).onChanged(true)
    }

    @Test
    fun `initial view model state prevents login via Repository`() {
        loginViewModel.login()

        then(repository).should(never()).login(any(), any())
    }

    @Test
    fun `view model does not login via Repository, given an email but no password`() {
        loginViewModel.setEmail("jane@doe.com")
        loginViewModel.login()

        then(repository).should(never()).login(any(), any())
    }
}