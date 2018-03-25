package com.eulersbridge.isegoria.auth

import android.arch.lifecycle.ViewModelProviders
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
import com.eulersbridge.isegoria.*
import com.eulersbridge.isegoria.auth.login.LoginFragment
import com.eulersbridge.isegoria.auth.signup.ConsentAgreementFragment
import com.eulersbridge.isegoria.auth.signup.SignUpFragment
import com.eulersbridge.isegoria.auth.verification.EmailVerificationFragment
import com.eulersbridge.isegoria.util.transformation.BlurTransformation
import dagger.android.support.DaggerAppCompatActivity

class AuthActivity : DaggerAppCompatActivity() {

    private lateinit var viewModel: AuthViewModel

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

        viewModel = ViewModelProviders.of(this)[AuthViewModel::class.java]

        presentRootContent(LoginFragment())

        ifTrue(viewModel.signUpVisible) {
            presentContent(SignUpFragment())
        }

        observe(viewModel.signUpUser) {
            if (it != null && viewModel.signUpConsentGiven.value == false)
                presentContent(ConsentAgreementFragment())
        }

        ifTrue(viewModel.signUpConsentGiven) {
            observeBoolean(viewModel.signUp()) { signUpSuccess ->
                if (signUpSuccess) {
                    presentRootContent(LoginFragment())
                    presentContent(EmailVerificationFragment())

                } else {
                    presentRootContent(LoginFragment())
                    presentContent(SignUpFragment())

                    AlertDialog.Builder(this)
                        .setTitle(getString(R.string.user_sign_up_error_title))
                        .setMessage(getString(R.string.user_sign_up_error_message))
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                }
            }
        }

        ifTrue(viewModel.verificationComplete) {
            presentRootContent(LoginFragment())
        }

        ifTrue(viewModel.userLoggedIn) {
            finish()
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
