package com.eulersbridge.isegoria;

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
import android.widget.TextView;
import android.widget.LinearLayout;
import android.graphics.Typeface;

public class LoginScreenFragment extends Fragment {

    @SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.user_login_screen_fragment, container, false);

        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.setToolbarVisible(false);

        LinearLayout backgroundLinearLayout = rootView.findViewById(R.id.loginBackground);
		Bitmap original = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.tumblr_static_aphc);

        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();

        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;

		Bitmap b = Bitmap.createScaledBitmap(original, (int) dpWidth, (int) dpHeight /2, false);
		Drawable d = new BitmapDrawable(getActivity().getResources(), Utils.fastBlur(b, 25));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            backgroundLinearLayout.setBackground(d);
        } else {
            backgroundLinearLayout.setBackgroundDrawable(d);
        }

        TextView tx = rootView.findViewById(R.id.isegoriaLabel);
        Typeface custom_font = Typeface.createFromAsset(mainActivity.getAssets(),
                "MuseoSansRounded-300.otf");
        tx.setTypeface(custom_font);
		
		return rootView;
	}
}
