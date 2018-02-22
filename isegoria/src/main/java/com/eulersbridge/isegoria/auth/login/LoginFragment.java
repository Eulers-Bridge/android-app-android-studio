package com.eulersbridge.isegoria.auth.login;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.auth.AuthViewModel;
import com.eulersbridge.isegoria.util.data.SimpleTextWatcher;

public class LoginFragment extends Fragment {

    private CoordinatorLayout coordinatorLayout;

    private TextInputLayout emailLayout;
    private EditText emailField;

    private TextInputLayout passwordLayout;
    private EditText passwordField;

    private Button loginButton;
    private Button signUpButton;

    private LoginViewModel viewModel;
    private AuthViewModel authViewModel;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.login_fragment, container, false);

        //noinspection ConstantConditions
        authViewModel = ViewModelProviders.of(getActivity()).get(AuthViewModel.class);

        rootView.findViewById(R.id.login_signup_button).setOnClickListener(view ->
            authViewModel.signUpVisible.setValue(true)
        );

        boolean hasTranslucentStatusBar = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
        if (hasTranslucentStatusBar) {
            View logo = rootView.findViewById(R.id.login_image_logo);

            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) logo.getLayoutParams();
            params.topMargin += Math.round(22.0f * getResources().getDisplayMetrics().density);
        }

        coordinatorLayout = rootView.findViewById(R.id.login_container);

        emailField = rootView.findViewById(R.id.login_email);
        emailField.addTextChangedListener(new SimpleTextWatcher(value ->
            viewModel.setEmail(value.toString())
        ));
        emailLayout = rootView.findViewById(R.id.login_email_layout);

        passwordField = rootView.findViewById(R.id.login_password);
        passwordField.addTextChangedListener(new SimpleTextWatcher(value ->
            viewModel.setPassword(value.toString())
        ));
        passwordLayout = rootView.findViewById(R.id.login_password_layout);

        loginButton = rootView.findViewById(R.id.login_button);
        loginButton.setOnClickListener(view -> onLoginClicked());

        signUpButton = rootView.findViewById(R.id.login_signup_button);

        TextView forgotPasswordView = rootView.findViewById(R.id.login_forgot_password);
        forgotPasswordView.setOnClickListener(view -> showForgotPasswordDialog());

        viewModel = ViewModelProviders.of(this).get(LoginViewModel.class);
        setupViewModelObservers();
		
		return rootView;
	}

    @Override
    public void onDestroy() {
	    if (viewModel != null)
            viewModel.onExit();

        super.onDestroy();
    }

    private void showForgotPasswordDialog() {
	    Boolean canContinue = viewModel.canShowPasswordResetDialog.getValue();
	    if (canContinue != null && !canContinue) return;

        @SuppressLint("InflateParams")
	    final View alertView = getLayoutInflater().inflate(R.layout.alert_dialog_input_forgot_password, null);
        final EditText alertEmailInput = alertView.findViewById(R.id.alert_dialog_email_address_input);

        //noinspection ConstantConditions
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.forgot_password_title)
                .setMessage(R.string.forgot_password_message)
                .setView(R.layout.alert_dialog_input_forgot_password)
                .setPositiveButton(android.R.string.ok,
                        (dialog, choice) -> resetPassword(alertEmailInput.getText()))
                .setNegativeButton(android.R.string.cancel, (dialog, __) -> dialog.cancel())
                .show();
    }

    private void resetPassword(Editable editable) {
        final String forgottenAccountEmail = editable.toString();

        final boolean validEmail = viewModel.requestPasswordRecoveryEmail(forgottenAccountEmail);

        if (validEmail) {
            final String message = getString(R.string.forgot_password_email_sent, forgottenAccountEmail);
            Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG).show();
        }
    }

	private void setupViewModelObservers() {
        String email = viewModel.email.getValue();
        if (email != null)
            emailField.setText(email);

        String password = viewModel.password.getValue();
        if (password != null)
            passwordField.setText(password);

        viewModel.emailError.observe(this, hasError -> {
            if (hasError != null && hasError) {
                emailLayout.setError(getString(R.string.user_login_email_error_required));

                emailLayout.setErrorEnabled(true);
                emailField.requestFocus();

            } else {
                emailLayout.setErrorEnabled(false);
            }
        });

        viewModel.passwordError.observe(this, hasError -> {
            if (hasError != null && hasError) {
                passwordLayout.setError(getString(R.string.user_login_password_error_required));
                passwordLayout.setErrorEnabled(true);

            } else {
                passwordLayout.setErrorEnabled(false);
            }
        });

        viewModel.networkError.observe(this, hasError -> {
                if (hasError != null && hasError) {
                    Snackbar.make(coordinatorLayout, getString(R.string.connection_error_message), Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.connection_error_action), view -> onLoginClicked())
                            .setActionTextColor(getResources().getColor(R.color.white))
                            .show();

                    viewModel.setNetworkErrorShown();
                }
        });

        viewModel.formEnabled.observe(this, enabled -> {
            boolean fieldsEnabled = enabled != null && enabled;

            emailLayout.setEnabled(fieldsEnabled);
            passwordLayout.setEnabled(fieldsEnabled);
            loginButton.setEnabled(fieldsEnabled);
            signUpButton.setEnabled(fieldsEnabled);
        });
    }

	private void onLoginClicked() {
        viewModel.login().observe(this, success -> {
            if (success != null && success)
                authViewModel.userLoggedIn.setValue(true);
        });
    }

}
