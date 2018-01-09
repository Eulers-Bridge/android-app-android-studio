package com.eulersbridge.isegoria.election;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.common.TitledFragment;
import com.eulersbridge.isegoria.models.UserSelfEfficacy;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.network.SimpleCallback;

import retrofit2.Response;

public class SelfEfficacyQuestionsFragment extends Fragment implements TitledFragment, MainActivity.TabbedFragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.self_efficacy_questions_fragment, container, false);

        MainActivity mainActivity = (MainActivity) getActivity();

        Isegoria isegoria = mainActivity.getIsegoriaApplication();
        String userEmail = isegoria.getLoggedInUser().email;

        SelfEfficacySliderBar sliderBar1 = rootView.findViewById(R.id.selfEfficacySliderBar1);
        SelfEfficacySliderBar sliderBar2 = rootView.findViewById(R.id.selfEfficacySliderBar2);
        SelfEfficacySliderBar sliderBar3 = rootView.findViewById(R.id.selfEfficacySliderBar3);
        SelfEfficacySliderBar sliderBar4 = rootView.findViewById(R.id.selfEfficacySliderBar4);

        Button doneButton = rootView.findViewById(R.id.selfEfficacyDoneButton);
        doneButton.setOnClickListener(v -> {

            UserSelfEfficacy answers = new UserSelfEfficacy(
                    sliderBar1.getScore(),
                    sliderBar2.getScore(),
                    sliderBar3.getScore(),
                    sliderBar4.getScore()
            );

            isegoria.getAPI().addUserEfficacy(userEmail, answers).enqueue(new SimpleCallback<Void>() {
                @Override
                protected void handleResponse(Response response) {
                    isegoria.onUserSelfEfficacyCompleted();

                    mainActivity.getSupportFragmentManager().popBackStack();
                    mainActivity.setToolbarTitle(getString(R.string.section_title_vote));
                    mainActivity.getTabLayout().setVisibility(View.VISIBLE);
                }
            });
        });

        return rootView;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.section_title_self_efficacy_questions);
    }

    @Override
    public void setupTabLayout(TabLayout tabLayout) {
        tabLayout.setVisibility(View.GONE);
    }
}
