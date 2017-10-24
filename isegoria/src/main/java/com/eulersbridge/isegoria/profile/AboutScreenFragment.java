package com.eulersbridge.isegoria.profile;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.R;

/**
 * Created by Anthony on 06/04/2015.
 */
public class AboutScreenFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.about_screen_fragment, container, false);
    }
}
