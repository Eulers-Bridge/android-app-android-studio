package com.eulersbridge.isegoria.auth

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AlertDialog
import com.bumptech.glide.Priority
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.eulersbridge.isegoria.GlideApp
import com.eulersbridge.isegoria.MainActivity
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.auth.login.LoginFragment
import com.eulersbridge.isegoria.auth.signup.ConsentAgreementFragment
import com.eulersbridge.isegoria.auth.signup.SignUpFragment
import com.eulersbridge.isegoria.auth.verification.EmailVerificationFragment
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.util.extension.ifTrue
import com.eulersbridge.isegoria.util.extension.observe
import com.eulersbridge.isegoria.util.transformation.BlurTransformation
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class AuthActivity : DaggerAppCompatActivity() {

    @Inject
    internal lateinit var repository: Repository

    private lateinit var viewModel: AuthViewModel

    private class ViewModelProviderFactory(private val repository: Repository) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(repository) as T
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_auth)

        GlideApp.with(this)
                .load(R.drawable.tumblr_static_aphc)
                .transforms(CenterCrop(), BlurTransformation(this, 5.0))
                .priority(Priority.HIGH)
                .into(object: SimpleTarget<Drawable>() {
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                        window.setBackgroundDrawable(resource)
                    }
                })

        viewModel = ViewModelProviders.of(this, ViewModelProviderFactory(repository))[AuthViewModel::class.java]

        presentRootContent(LoginFragment())

        observe(viewModel.authFinished) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        ifTrue(viewModel.signUpVisible) {
            presentContent(SignUpFragment())
        }

        observe(viewModel.signUpUser) { user ->
            val consentValue = viewModel.signUpConsentGiven.value
            val consentRequired = consentValue == null || !consentValue
            if (user != null && consentRequired)
                presentContent(ConsentAgreementFragment())
        }

        ifTrue(viewModel.signUpConsentGiven) {
            viewModel.signUp()
        }

        observe(viewModel.signRequestComplete) { signUpSuccess ->
            if (signUpSuccess == true) {
                presentContent(EmailVerificationFragment())
            } else {
                presentContent(SignUpFragment())

                AlertDialog.Builder(this)
                        .setTitle(getString(R.string.user_sign_up_error_title))
                        .setMessage(getString(R.string.user_sign_up_error_message))
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
            }
        }
    }

    private fun presentRootContent(fragment: Fragment) =
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.activity_auth_container, fragment)
                .commit()

    private fun presentContent(fragment: Fragment) {
        supportFragmentManager.apply {
            if (backStackEntryCount > 0)
                popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

            beginTransaction()
                .replace(R.id.activity_auth_container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit()
        }
    }
}
