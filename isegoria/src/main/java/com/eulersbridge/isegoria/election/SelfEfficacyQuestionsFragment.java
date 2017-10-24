package com.eulersbridge.isegoria.election;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.Network;
import com.eulersbridge.isegoria.R;

/**
 * Created by Seb on 20/10/2017.
 */

public class SelfEfficacyQuestionsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.self_efficacy_questions_fragment, container, false);

        MainActivity mainActivity = (MainActivity) getActivity();

        ((MainActivity)getActivity()).setToolbarTitle(getString(R.string.section_title_self_efficacy_questions));

        final Network network = mainActivity.getIsegoriaApplication().getNetwork();

        final SelfEfficacySliderBar sliderBar1 = rootView.findViewById(R.id.selfEfficacySliderBar1);
        final SelfEfficacySliderBar sliderBar2 = rootView.findViewById(R.id.selfEfficacySliderBar2);
        final SelfEfficacySliderBar sliderBar3 = rootView.findViewById(R.id.selfEfficacySliderBar3);
        final SelfEfficacySliderBar sliderBar4 = rootView.findViewById(R.id.selfEfficacySliderBar4);

        Button doneButton = rootView.findViewById(R.id.selfEfficacyDoneButton);
        doneButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                network.answerEfficacy(
                        sliderBar1.getScore() + 1,
                        sliderBar2.getScore() + 1,
                        sliderBar3.getScore() + 1,
                        sliderBar4.getScore() + 1
                );
            }
        });

        return rootView;
    }

    public void setTabLayout(TabLayout tabLayout) {
        tabLayout.setVisibility(View.GONE);
    }

}
