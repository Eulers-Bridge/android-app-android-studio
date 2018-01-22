package com.eulersbridge.isegoria.election.efficacy;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.util.ui.TitledFragment;

public class SelfEfficacyQuestionsFragment extends Fragment implements TitledFragment, MainActivity.TabbedFragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.self_efficacy_questions_fragment, container, false);

        SelfEfficacySliderBar sliderBar1 = rootView.findViewById(R.id.selfEfficacySliderBar1);
        SelfEfficacySliderBar sliderBar2 = rootView.findViewById(R.id.selfEfficacySliderBar2);
        SelfEfficacySliderBar sliderBar3 = rootView.findViewById(R.id.selfEfficacySliderBar3);
        SelfEfficacySliderBar sliderBar4 = rootView.findViewById(R.id.selfEfficacySliderBar4);

        EfficacyQuestionsViewModel viewModel = ViewModelProviders.of(this).get(EfficacyQuestionsViewModel.class);

        viewModel.score1.observe(this, score1 -> {
            if (score1 != null)
                sliderBar1.setScore(score1);
        });
        viewModel.score2.observe(this, score2 -> {
            if (score2 != null)
                sliderBar2.setScore(score2);
        });
        viewModel.score3.observe(this, score3 -> {
            if (score3 != null)
                sliderBar3.setScore(score3);
        });
        viewModel.score4.observe(this, score4 -> {
            if (score4 != null)
                sliderBar4.setScore(score4);
        });

        Button doneButton = rootView.findViewById(R.id.selfEfficacyDoneButton);
        doneButton.setOnClickListener(view -> {
            view.setEnabled(false);

            viewModel.addUserEfficacy().observe(this, success -> {
                if (success != null && success) {

                    if (getActivity() != null)
                        getActivity().getSupportFragmentManager().popBackStack();

                } else {
                    view.setEnabled(true);
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
