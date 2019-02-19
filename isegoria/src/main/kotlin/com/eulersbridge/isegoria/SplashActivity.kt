package com.eulersbridge.isegoria

import android.content.Intent
import android.os.Bundle
import com.eulersbridge.isegoria.auth.AuthActivity
import com.eulersbridge.isegoria.data.LoginState
import com.eulersbridge.isegoria.data.Repository
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class SplashActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var repository: Repository

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val savedEmail = repository.getSavedEmail()
        val savedPassword = repository.getSavedPassword()

        if (savedEmail != null && savedPassword != null) {
            repository.login(savedEmail, savedPassword)

        } else {
            showLoginActivity()
        }
    }

    private fun observeLoginState() {
        repository.getLoginState().subscribe {
            when (it) {
                is LoginState.LoggedIn -> showMainActivity()
                is LoginState.LoginFailure -> showLoginActivity()
                is LoginState.LoginUnauthorised -> showLoginActivity()
            }
        }.addTo(compositeDisposable)
    }

    override fun onResume() {
        super.onResume()
        observeLoginState()
    }

    override fun onPause() {
        compositeDisposable.clear()
        super.onPause()
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
