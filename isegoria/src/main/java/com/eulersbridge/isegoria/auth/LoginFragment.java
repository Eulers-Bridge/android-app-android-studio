package com.eulersbridge.isegoria.auth;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.common.BlurTransformation;
import com.eulersbridge.isegoria.common.Constant;
import com.eulersbridge.isegoria.common.TitledFragment;
import com.eulersbridge.isegoria.common.Utils;
import com.securepreferences.SecurePreferences;

public class LoginFragment extends Fragment implements TitledFragment, MainActivity.TabbedFragment {

    private TextInputLayout emailLayout;
    private EditText emailField;

    private TextInputLayout passwordLayout;
    private EditText passwordField;

    private Button loginButton;
    private Button signUpButton;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.user_login_fragment, container, false);

        final MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            Utils.setStatusBarColour(mainActivity, Color.BLACK);
            mainActivity.setToolbarVisible(false);
        }

        rootView.findViewById(R.id.login_signup_button).setOnClickListener(view -> {
            if (getActivity() != null) ((MainActivity)getActivity()).onSignUpClicked();
        });

        final ConstraintLayout loginContainer = rootView.findViewById(R.id.loginContainer);

        GlideApp.with(this)
                .load(R.drawable.tumblr_static_aphc)
                //DiskCacheStrategy.RESOURCE causes the image to be blurred multiple times
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .transforms(new CenterCrop(), new BlurTransformation(getContext(),5))
                .priority(Priority.HIGH)
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                        if (isAdded() && !isDetached())
                            loginContainer.post(() -> loginContainer.setBackground(resource));
                    }
                });

        emailField = rootView.findViewById(R.id.login_email);
        emailLayout = rootView.findViewById(R.id.login_email_layout);

        passwordField = rootView.findViewById(R.id.login_password);
        passwordLayout = rootView.findViewById(R.id.login_password_layout);

        loginButton = rootView.findViewById(R.id.login_button);
        loginButton.setOnClickListener(view -> onLoginClicked());

        signUpButton = rootView.findViewById(R.id.login_signup_button);

        // If the user previously logged in, pre-fill the email and password fields
        String userEmail = new SecurePreferences(getContext()).getString(Constant.USER_EMAIL_KEY, null);
        if (userEmail != null) {
            emailField.setText(userEmail);

        } else {
            emailField.requestFocus();
            Utils.showKeyboard(mainActivity.getWindow());
        }

        String userPassword = new SecurePreferences(getContext()).getString(Constant.USER_PASSWORD_KEY, null);
        if (userPassword != null) {
            passwordField.setText(userPassword);

            loginButton.requestFocus();

        } else if (userEmail != null) {
            //No password stored for whatever reason, focus the password field
            passwordField.requestFocus();
            Utils.showKeyboard(mainActivity.getWindow());
        }
		
		return rootView;
	}

	private void onLoginClicked() {

        MainActivity mainActivity = getActivity() == null? null : (MainActivity)getActivity();

        if (mainActivity != null && !Utils.isNetworkAvailable(getContext())) {
            // No network connection
            mainActivity.runOnUiThread(() ->
                    Snackbar.make(mainActivity.getCoordinatorLayout(), getString(R.string.connection_error_message), Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.connection_error_action), view -> onLoginClicked())
                            .setActionTextColor(getResources().getColor(R.color.white))
                            .show());

            return;
        }

	    String email = emailField.getText().toString();
	    String password = passwordField.getText().toString();

	    boolean emailValid = Utils.validEmail(email);

        if (!emailValid) {
            emailLayout.setError(getString(R.string.user_login_email_error_required));
            emailLayout.setErrorEnabled(true);
        } else {
            emailLayout.setErrorEnabled(false);
        }

	    boolean passwordValid = !TextUtils.isEmpty(password);

        if (!passwordValid) {
            passwordLayout.setError(getString(R.string.user_login_password_error_required));
            passwordLayout.setErrorEnabled(true);
        } else {
            passwordLayout.setErrorEnabled(false);
        }

        if (mainActivity != null && emailValid && passwordValid) {
            Isegoria application = (Isegoria) mainActivity.getApplication();

            application.login(email, password);

            emailLayout.setEnabled(false);
            passwordLayout.setEnabled(false);
            loginButton.setEnabled(false);
            signUpButton.setEnabled(false);
        }
    }

    @Override
    public String getTitle(Context context) {
        return null;
    }

    @Override
    public void setupTabLayout(TabLayout tabLayout) {
        tabLayout.setVisibility(View.GONE);
    }

}
