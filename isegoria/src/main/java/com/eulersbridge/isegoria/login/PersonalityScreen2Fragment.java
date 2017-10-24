package com.eulersbridge.isegoria.login;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.Network;
import com.eulersbridge.isegoria.R;

/**
 * Created by Anthony on 01/04/2015.
*/
public class PersonalityScreen2Fragment extends Fragment {
    private Network network;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.personality_screen2_fragment, container, false);

        final MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();

        final PersonalitySliderBar personalitySliderBar1 = rootView.findViewById(R.id.personalitySliderBar1);
        final PersonalitySliderBar personalitySliderBar2 = rootView.findViewById(R.id.personalitySliderBar2);
        final PersonalitySliderBar personalitySliderBar3 = rootView.findViewById(R.id.personalitySliderBar3);
        final PersonalitySliderBar personalitySliderBar4 = rootView.findViewById(R.id.personalitySliderBar4);
        final PersonalitySliderBar personalitySliderBar5 = rootView.findViewById(R.id.personalitySliderBar5);
        final PersonalitySliderBar personalitySliderBar6 = rootView.findViewById(R.id.personalitySliderBar6);
        final PersonalitySliderBar personalitySliderBar7 = rootView.findViewById(R.id.personalitySliderBar7);
        final PersonalitySliderBar personalitySliderBar8 = rootView.findViewById(R.id.personalitySliderBar8);
        final PersonalitySliderBar personalitySliderBar9 = rootView.findViewById(R.id.personalitySliderBar9);
        final PersonalitySliderBar personalitySliderBar10 = rootView.findViewById(R.id.personalitySliderBar10);

        Button donePersonalityQuestions = rootView.findViewById(R.id.donePersonalityQuestions);
        donePersonalityQuestions.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                float extraversion = (personalitySliderBar1.getScore() + (8-personalitySliderBar6.getScore()))/2;
                float agreeableness = (personalitySliderBar7.getScore() + (8-personalitySliderBar2.getScore()))/2;
                float conscientiousness = (personalitySliderBar3.getScore() + (8-personalitySliderBar8.getScore()))/2;
                float emotionalStability = (personalitySliderBar9.getScore() + (8-personalitySliderBar4.getScore()))/2;
                float opennessToExperiences = (personalitySliderBar5.getScore() + (10-personalitySliderBar6.getScore()))/2;

                network.answerPersonality(extraversion, agreeableness, conscientiousness,
                        emotionalStability, opennessToExperiences);
            }
        });

        return rootView;
    }
}
