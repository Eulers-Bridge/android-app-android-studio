package com.eulersbridge.isegoria.login;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
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
import com.eulersbridge.isegoria.utilities.Utils;
import com.securepreferences.SecurePreferences;

public class LoginScreenFragment extends Fragment {

    @SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.user_login_screen_fragment, container, false);

        final MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.setToolbarVisible(false);

        final LinearLayout loginContainer = rootView.findViewById(R.id.loginContainer);
		Bitmap original = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.tumblr_static_aphc);

        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();

        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;

		Bitmap backgroundBitmap = Bitmap.createScaledBitmap(original, (int) dpWidth, (int) dpHeight /2, false);
		final Drawable backgroundDrawable = new BitmapDrawable(getActivity().getResources(), Utils.fastBlur(backgroundBitmap, 25));
        loginContainer.post(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    loginContainer.setBackground(backgroundDrawable);
                } else {
                    loginContainer.setBackgroundDrawable(backgroundDrawable);
                }
            }
        });

        TextView appNameLabel = rootView.findViewById(R.id.isegoriaLabel);
        Typeface appNameFont = Typeface.createFromAsset(mainActivity.getAssets(),
                "MuseoSansRounded-300.otf");
        appNameLabel.setTypeface(appNameFont);

        final EditText emailField = rootView.findViewById(R.id.username);
        final EditText passwordField = rootView.findViewById(R.id.password);

        Button loginButton = rootView.findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.login(emailField.getText().toString(), passwordField.getText().toString());
            }
        });

        // If the user previously logged in, pre-fill the email and password fields
        String userEmail = new SecurePreferences(getContext()).getString("userEmail", null);
        if (userEmail != null) {
            emailField.setText(userEmail);
        }

        String userPassword = new SecurePreferences(getContext()).getString("userPassword", null);
        if (userPassword != null) {
            passwordField.setText(userPassword);

            loginButton.requestFocus();

        } else {
            //No password stored for whatever reason, focus the password field
            rootView.findViewById(R.id.password).requestFocus();
        }
		
		return rootView;
	}
}
