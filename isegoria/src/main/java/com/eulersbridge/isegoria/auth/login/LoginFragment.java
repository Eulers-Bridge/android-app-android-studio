package com.eulersbridge.isegoria.auth.login;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
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

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.util.Utils;
import com.eulersbridge.isegoria.util.data.SimpleTextWatcher;
import com.eulersbridge.isegoria.util.transformation.BlurTransformation;
import com.eulersbridge.isegoria.util.ui.TitledFragment;

public class LoginFragment extends Fragment implements TitledFragment, MainActivity.TabbedFragment {

    private TextInputLayout emailLayout;
    private EditText emailField;

    private TextInputLayout passwordLayout;
    private EditText passwordField;

    private Button loginButton;
    private Button signUpButton;

    private LoginViewModel viewModel;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.user_login_fragment, container, false);

        final MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            Utils.setStatusBarColour(mainActivity, Color.BLACK);
            mainActivity.setToolbarVisible(false);
        }

        rootView.findViewById(R.id.login_signup_button).setOnClickListener(view -> {
            if (getActivity() != null)
                ((MainActivity)getActivity()).onSignUpClicked();
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
                    public void onResourceReady(@NonNull Drawable resource, Transition<? super Drawable> transition) {
                        if (isAdded() && !isDetached())
                            loginContainer.post(() -> loginContainer.setBackground(resource));
                    }
                });

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

	private void showForgotPasswordDialog() {
	    Boolean canContinue = viewModel.canShowPasswordResetDialog.getValue();
	    if (canContinue != null && !canContinue) return;

	    View alertView = getLayoutInflater().inflate(R.layout.alert_dialog_input_forgot_password, null);
        final EditText alertEmailInput = alertView.findViewById(R.id.alert_dialog_email_address_input);

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
        String forgottenAccountEmail = editable.toString();

        boolean validEmail = viewModel.requestPasswordRecoveryEmail(forgottenAccountEmail);

        if (validEmail) {
            MainActivity mainActivity = (MainActivity) getActivity();
            if (mainActivity != null) {
                CoordinatorLayout coordinatorLayout = mainActivity.getCoordinatorLayout();
                String message = getString(R.string.forgot_password_email_sent, forgottenAccountEmail);
                Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG).show();
            }
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
                    MainActivity mainActivity = getActivity() == null? null : (MainActivity)getActivity();

                    if (mainActivity != null)
                        mainActivity.runOnUiThread(() ->
                                Snackbar.make(mainActivity.getCoordinatorLayout(), getString(R.string.connection_error_message), Snackbar.LENGTH_LONG)
                                        .setAction(getString(R.string.connection_error_action), view -> onLoginClicked())
                                        .setActionTextColor(getResources().getColor(R.color.white))
                                        .show());

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
        viewModel.login();
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
