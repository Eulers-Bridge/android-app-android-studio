package com.eulersbridge.isegoria.auth

import android.arch.lifecycle.Observer
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
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
import com.eulersbridge.isegoria.util.transformation.BlurTransformation

class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_auth)

        GlideApp.with(this)
                .load(R.drawable.tumblr_static_aphc)
                .transforms(CenterCrop(), BlurTransformation(this, 5))
                .priority(Priority.HIGH)
                .into(object: SimpleTarget<Drawable>() {
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                        window.setBackgroundDrawable(resource)
                    }
                })

        presentRootContent(LoginFragment())

        val viewModel = AuthViewModel.create(this)

        viewModel.signUpVisible.observe(this, Observer { visible ->
            /* If makeVisible is false, allow default behaviour of back button & fragment manager
                to pop the stack, removing the sign up fragment. */
            if (visible == true)
                presentContent(SignUpFragment())
        })

        viewModel.signUpUser.observe(this, Observer { user ->
            if (user != null)
                presentContent(ConsentAgreementFragment())
        })

        viewModel.signUpConsentGiven.observe(this, Observer { consent ->
            if (consent == false)

                viewModel.signUp().observe(this, Observer { success ->
                    if (success == false)
                        presentRootContent(LoginFragment())
                })
        })

        viewModel.userLoggedIn.observe(this, Observer { success ->
            if (success == true) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        })
    }

    private fun presentRootContent(fragment: Fragment) {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.activity_auth_container, fragment)
                .commit()
    }

    private fun presentContent(fragment: Fragment) {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.activity_auth_container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit()
    }

    /*private fun onSignUpFailure() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.user_sign_up_error_title))
                .setMessage(getString(R.string.user_sign_up_error_message))
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }*/

}
