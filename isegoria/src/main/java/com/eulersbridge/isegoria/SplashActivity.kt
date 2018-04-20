package com.eulersbridge.isegoria

import android.content.Intent
import android.os.Bundle
import com.eulersbridge.isegoria.auth.AuthActivity
import com.securepreferences.SecurePreferences
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class SplashActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var repository: Repository
    @Inject
    lateinit var securePreferences: SecurePreferences

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        repository.loginState.subscribe {
            when (it) {
                is LoginState.LoggedIn -> {
                    showMainActivity()
                }
                is LoginState.LoginFailure -> {
                    showLoginActivity()
                }
            }
        }.addTo(compositeDisposable)

        val savedEmail = securePreferences.getString(USER_EMAIL_KEY, null)
        val savedPassword = securePreferences.getString(USER_PASSWORD_KEY, null)

        if (savedEmail != null && savedPassword != null) {
            repository.login(savedEmail, savedPassword)

        } else {
            showLoginActivity()
        }
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    private fun showLoginActivity() {
        startActivity(Intent(this, AuthActivity::class.java))
    }

    private fun showMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
    }


}
