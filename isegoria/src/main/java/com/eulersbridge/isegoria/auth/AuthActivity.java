package com.eulersbridge.isegoria.auth;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.auth.login.LoginFragment;
import com.eulersbridge.isegoria.auth.signup.ConsentAgreementFragment;
import com.eulersbridge.isegoria.auth.signup.SignUpFragment;
import com.eulersbridge.isegoria.util.transformation.BlurTransformation;

public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_auth);

        GlideApp.with(this)
                .load(R.drawable.tumblr_static_aphc)
                .transforms(new CenterCrop(), new BlurTransformation(this,5))
                .priority(Priority.HIGH)
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, Transition<? super Drawable> transition) {
                        getWindow().setBackgroundDrawable(resource);
                    }
                });

        presentRootContent(new LoginFragment());

        AuthViewModel viewModel = ViewModelProviders.of(this).get(AuthViewModel.class);

        viewModel.signUpVisible.observe(this, makeVisible -> {
            /* If makeVisible is false, allow default behaviour of back button & fragment manager
                to pop the stack, removing the sign up fragment. */
            if (makeVisible != null && makeVisible)
                presentContent(new SignUpFragment());
        });

        viewModel.signUpUser.observe(this, newUser -> {
            if (newUser != null)
                presentContent(new ConsentAgreementFragment());
        });

        viewModel.signUpConsentGiven.observe(this, consent -> {
            if (consent != null && consent)
                viewModel.signUp();
        });

        viewModel.signUpComplete.observe(this, signUpComplete -> {
            if (signUpComplete != null && signUpComplete)
                presentRootContent(new LoginFragment());
        });

        viewModel.userLoggedIn.observe(this, loggedIn -> {
            if (loggedIn != null && loggedIn) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });
    }

    private void presentRootContent(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_auth_container, fragment)
                .commit();
    }

    private void presentContent(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_auth_container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
    }

    /*private void onSignUpFailure() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.user_sign_up_error_title))
                .setMessage(getString(R.string.user_sign_up_error_message))
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }*/

}
