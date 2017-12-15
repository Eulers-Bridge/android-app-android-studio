package com.eulersbridge.isegoria.auth;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.models.UserPersonality;
import com.eulersbridge.isegoria.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PersonalityScreen2Fragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.personality_screen2_fragment, container, false);

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

        Button doneButton = rootView.findViewById(R.id.donePersonalityQuestions);
        doneButton.setOnClickListener(v -> {
            float extraversion = (personalitySliderBar1.getScore() + (8-personalitySliderBar6.getScore()))/2;
            float agreeableness = (personalitySliderBar7.getScore() + (8-personalitySliderBar2.getScore()))/2;
            float conscientiousness = (personalitySliderBar3.getScore() + (8-personalitySliderBar8.getScore()))/2;
            float emotionalStability = (personalitySliderBar9.getScore() + (8-personalitySliderBar4.getScore()))/2;
            float opennessToExperiences = (personalitySliderBar5.getScore() + (10-personalitySliderBar6.getScore()))/2;

            UserPersonality personality = new UserPersonality(agreeableness, conscientiousness,
                    emotionalStability, extraversion, opennessToExperiences);

            final AppCompatActivity activity = (AppCompatActivity) getActivity();

            Isegoria isegoria = (Isegoria)activity.getApplication();

            String userEmail = isegoria.getLoggedInUser().email;

            isegoria.getAPI()
                    .addUserPersonality(userEmail, personality)
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                activity.finish();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            t.printStackTrace();
                            doneButton.post(() -> doneButton.setEnabled(true));
                        }
                    });

            doneButton.setEnabled(false);
        });

        return rootView;
    }
}
