package com.eulersbridge.isegoria.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.eulersbridge.isegoria.R;

public final class Utils {

    private Utils() {
        // Hide implicit public constructor
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
        }

        return false;
    }

    //Change colour, use default title (app name)
    public static void setMultitaskColour(Activity activity, @ColorInt int colour) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setMultitaskDescription(activity, activity.getString(R.string.app_name), colour);
        }
    }

    private static void setMultitaskDescription(Activity activity, String title, @ColorInt int colour) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            final Bitmap icon = BitmapFactory.decodeResource(activity.getResources(), R.drawable.app_icon);

            activity.setTaskDescription(new ActivityManager.TaskDescription(title, icon, colour));

            icon.recycle();
        }
    }

    public static void setStatusBarColour(Activity activity, @ColorInt int colour) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(colour);
        }
    }

    public static void hideKeyboard(Activity activity) {
        final View currentFocus = activity.getCurrentFocus();
        if (currentFocus != null) {

            final InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            }
        }
    }

    public static void showKeyboard(Window window) {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }
}
