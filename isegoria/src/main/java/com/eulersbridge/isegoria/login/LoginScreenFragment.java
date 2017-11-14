package com.eulersbridge.isegoria.login;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.graphics.Typeface;

import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.utilities.TitledFragment;
import com.eulersbridge.isegoria.utilities.Utils;
import com.securepreferences.SecurePreferences;

public class LoginScreenFragment extends Fragment implements TitledFragment {

    @SuppressWarnings("deprecation")
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.user_login_screen_fragment, container, false);

        final MainActivity mainActivity = (MainActivity) getActivity();
        Utils.setStatusBarColour(mainActivity, Color.BLACK);
        mainActivity.setToolbarVisible(false);

        rootView.findViewById(R.id.login_signup_button).setOnClickListener(view -> {
            if (mainActivity != null) mainActivity.signupClicked();
        });

        final LinearLayout loginContainer = rootView.findViewById(R.id.loginContainer);
		Bitmap original = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.tumblr_static_aphc);

        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();

        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;

		Bitmap backgroundBitmap = Bitmap.createScaledBitmap(original, (int) dpWidth, (int) dpHeight /2, false);
		final Drawable backgroundDrawable = new BitmapDrawable(getActivity().getResources(), Utils.fastBlur(backgroundBitmap, 25));
        loginContainer.post(() -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                loginContainer.setBackground(backgroundDrawable);
            } else {
                loginContainer.setBackgroundDrawable(backgroundDrawable);
            }
        });

        TextView appNameLabel = rootView.findViewById(R.id.isegoria_label);
        Typeface appNameFont = Typeface.createFromAsset(mainActivity.getAssets(),
                "MuseoSansRounded-300.otf");
        appNameLabel.setTypeface(appNameFont);

        final EditText emailField = rootView.findViewById(R.id.login_email);
        final EditText passwordField = rootView.findViewById(R.id.login_password);

        Button loginButton = rootView.findViewById(R.id.login_button);
        loginButton.setOnClickListener(view -> mainActivity.login(emailField.getText().toString(), passwordField.getText().toString()));

        // If the user previously logged in, pre-fill the email and password fields
        String userEmail = new SecurePreferences(getContext()).getString("userEmail", null);
        if (userEmail != null) {
            emailField.setText(userEmail);

        } else {
            emailField.requestFocus();
            Utils.showKeyboard(mainActivity.getWindow());
        }

        String userPassword = new SecurePreferences(getContext()).getString("userPassword", null);
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

    @Override
    public String getTitle() {
        return null;
    }

    public void setTabLayout(TabLayout tabLayout) {
        tabLayout.setVisibility(View.GONE);
    }

}
