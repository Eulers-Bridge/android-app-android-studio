package com.eulersbridge.isegoria;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

/**
 * Created by Anthony on 01/04/2015.
*/
public class PersonalityScreen2Fragment extends SherlockFragment {
    private View rootView;
    private Network network;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.personality_screen2_fragment, container, false);

        final MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();

        FragmentManager fm = ((SherlockFragmentActivity) getActivity()).getSupportFragmentManager();
        PersonalitySliderBar personalitySliderBar1 = (PersonalitySliderBar) rootView.findViewById(R.id.personalitySliderBar1);
        PersonalitySliderBar personalitySliderBar2 = (PersonalitySliderBar) rootView.findViewById(R.id.personalitySliderBar2);
        PersonalitySliderBar personalitySliderBar3 = (PersonalitySliderBar) rootView.findViewById(R.id.personalitySliderBar3);
        PersonalitySliderBar personalitySliderBar4 = (PersonalitySliderBar) rootView.findViewById(R.id.personalitySliderBar4);
        PersonalitySliderBar personalitySliderBar5 = (PersonalitySliderBar) rootView.findViewById(R.id.personalitySliderBar5);
        PersonalitySliderBar personalitySliderBar6 = (PersonalitySliderBar) rootView.findViewById(R.id.personalitySliderBar6);
        PersonalitySliderBar personalitySliderBar7 = (PersonalitySliderBar) rootView.findViewById(R.id.personalitySliderBar7);
        PersonalitySliderBar personalitySliderBar8 = (PersonalitySliderBar) rootView.findViewById(R.id.personalitySliderBar8);
        PersonalitySliderBar personalitySliderBar9 = (PersonalitySliderBar) rootView.findViewById(R.id.personalitySliderBar9);
        PersonalitySliderBar personalitySliderBar10 = (PersonalitySliderBar) rootView.findViewById(R.id.personalitySliderBar10);

        Button donePersonalityQuestions = (Button) rootView.findViewById(R.id.donePersonalityQuestions);
        donePersonalityQuestions.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                network.answerPersonality(4.0f, 4.0f, 4.0f, 4.0f, 4.0f);
            }
        });

        return rootView;
    }
}
