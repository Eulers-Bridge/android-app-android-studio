package com.eulersbridge.isegoria.login;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.models.UserPersonality;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.network.SimpleCallback;

import retrofit2.Response;

/**
 * Created by Anthony on 01/04/2015.
*/
public class PersonalityScreen2Fragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.personality_screen2_fragment, container, false);

        MainActivity mainActivity = (MainActivity) getActivity();

        PersonalitySliderBar personalitySliderBar1 = rootView.findViewById(R.id.personalitySliderBar1);
        PersonalitySliderBar personalitySliderBar2 = rootView.findViewById(R.id.personalitySliderBar2);
        PersonalitySliderBar personalitySliderBar3 = rootView.findViewById(R.id.personalitySliderBar3);
        PersonalitySliderBar personalitySliderBar4 = rootView.findViewById(R.id.personalitySliderBar4);
        PersonalitySliderBar personalitySliderBar5 = rootView.findViewById(R.id.personalitySliderBar5);
        PersonalitySliderBar personalitySliderBar6 = rootView.findViewById(R.id.personalitySliderBar6);
        PersonalitySliderBar personalitySliderBar7 = rootView.findViewById(R.id.personalitySliderBar7);
        PersonalitySliderBar personalitySliderBar8 = rootView.findViewById(R.id.personalitySliderBar8);
        PersonalitySliderBar personalitySliderBar9 = rootView.findViewById(R.id.personalitySliderBar9);
        PersonalitySliderBar personalitySliderBar10 = rootView.findViewById(R.id.personalitySliderBar10);

        Button donePersonalityQuestions = rootView.findViewById(R.id.donePersonalityQuestions);
        donePersonalityQuestions.setOnClickListener(v -> {
            float extraversion = (personalitySliderBar1.getScore() + (8-personalitySliderBar6.getScore()))/2;
            float agreeableness = (personalitySliderBar7.getScore() + (8-personalitySliderBar2.getScore()))/2;
            float conscientiousness = (personalitySliderBar3.getScore() + (8-personalitySliderBar8.getScore()))/2;
            float emotionalStability = (personalitySliderBar9.getScore() + (8-personalitySliderBar4.getScore()))/2;
            float opennessToExperiences = (personalitySliderBar5.getScore() + (10-personalitySliderBar6.getScore()))/2;

            UserPersonality personality = new UserPersonality(agreeableness, conscientiousness,
                    emotionalStability, extraversion, opennessToExperiences);

            Isegoria isegoria = mainActivity.getIsegoriaApplication();

            String userEmail = isegoria.getLoggedInUser().email;

            isegoria.getAPI()
                    .addUserPersonality(userEmail, personality)
                    .enqueue(new SimpleCallback<Void>() {
                        @Override
                        protected void handleResponse(Response response) {
                            mainActivity.getSupportFragmentManager().popBackStack();
                            mainActivity.setToolbarTitle(getString(R.string.section_title_profile));
                            mainActivity.setNavigationEnabled(true);
                        }
                    });
        });

        return rootView;
    }
}
