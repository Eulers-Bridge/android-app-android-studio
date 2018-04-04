package com.eulersbridge.isegoria.election.efficacy

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.view.isGone
import com.eulersbridge.isegoria.MainActivity
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.observe
import com.eulersbridge.isegoria.util.ui.TitledFragment
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class SelfEfficacyQuestionsFragment : Fragment(), TitledFragment, MainActivity.TabbedFragment {

    @Inject
    lateinit var modelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: EfficacyViewModel

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        viewModel = ViewModelProviders.of(this, modelFactory)[EfficacyViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.self_efficacy_questions_fragment, container, false)

        val sliderBar1 = rootView.findViewById<EfficacySliderBar>(R.id.selfEfficacySliderBar1)
        val sliderBar2 = rootView.findViewById<EfficacySliderBar>(R.id.selfEfficacySliderBar2)
        val sliderBar3 = rootView.findViewById<EfficacySliderBar>(R.id.selfEfficacySliderBar3)
        val sliderBar4 = rootView.findViewById<EfficacySliderBar>(R.id.selfEfficacySliderBar4)

        val pairs = mapOf(
            viewModel.score1 to sliderBar1,
            viewModel.score2 to sliderBar2,
            viewModel.score3 to sliderBar3,
            viewModel.score4 to sliderBar4
        )
        for ((liveData, sliderBar) in pairs) {
            observe(liveData) {
                if (it != null)
                    sliderBar.score = it.toInt()
            }
        }

        val doneButton = rootView.findViewById<Button>(R.id.selfEfficacyDoneButton)
        doneButton.setOnClickListener {
            it.isEnabled = false

            observe(viewModel.addUserEfficacy()) { success ->
                if (success == true) {
                    activity?.supportFragmentManager?.popBackStack()

                } else {
                    it.isEnabled = true
                }
            }
        }

        return rootView
    }

    override fun getTitle(context: Context?) =
        context?.getString(R.string.section_title_self_efficacy_questions)

    override fun setupTabLayout(tabLayout: TabLayout) {
        tabLayout.isGone = true
    }
}
