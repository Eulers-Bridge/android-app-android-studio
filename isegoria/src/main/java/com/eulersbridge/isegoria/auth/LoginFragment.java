package com.eulersbridge.isegoria.auth;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.graphics.Typeface;

import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.common.Constant;
import com.eulersbridge.isegoria.common.TitledFragment;
import com.eulersbridge.isegoria.common.Utils;
import com.securepreferences.SecurePreferences;

public class LoginFragment extends Fragment implements TitledFragment {

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

        final LinearLayout loginContainer = rootView.findViewById(R.id.loginContainer);
		Bitmap original = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.tumblr_static_aphc);

        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();

        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;

		Bitmap backgroundBitmap = Bitmap.createScaledBitmap(original, (int) dpWidth, (int) dpHeight /2, false);
		final Drawable backgroundDrawable = new BitmapDrawable(getActivity().getResources(), Utils.fastBlur(backgroundBitmap, 25));
        loginContainer.post(() -> loginContainer.setBackground(backgroundDrawable));

        TextView appNameLabel = rootView.findViewById(R.id.isegoria_label);
        Typeface appNameFont = Typeface.createFromAsset(mainActivity.getAssets(),
                "MuseoSansRounded-300.otf");
        appNameLabel.setTypeface(appNameFont);

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

    public void setTabLayout(TabLayout tabLayout) {
        tabLayout.setVisibility(View.GONE);
    }

}
