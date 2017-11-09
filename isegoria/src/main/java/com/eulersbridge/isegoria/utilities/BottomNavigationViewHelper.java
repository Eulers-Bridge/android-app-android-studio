package com.eulersbridge.isegoria.utilities;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;

import com.eulersbridge.isegoria.Constant;

import java.lang.reflect.Field;

public final class BottomNavigationViewHelper {

    @SuppressLint("RestrictedApi")
    public static void disableShiftMode(@NonNull BottomNavigationView view) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);

        if (menuView == null) return;

        try {
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");

            if (shiftingMode != null) {
                shiftingMode.setAccessible(true);
                shiftingMode.setBoolean(menuView, false);
                shiftingMode.setAccessible(false);

                for (int i = 0; i < menuView.getChildCount(); i++) {
                    BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                    //noinspection RestrictedApi
                    item.setShiftingMode(false);
                    // set once again checked value, so view will be updated
                    //noinspection RestrictedApi
                    item.setChecked(item.getItemData().isChecked());
                }
            }

        } catch (NoSuchFieldException e) {
            Log.e(Constant.TAG, "Unable to get shift mode field", e);
        } catch (IllegalAccessException e) {
            Log.e(Constant.TAG, "Unable to change value of shift mode", e);
        }
    }
}