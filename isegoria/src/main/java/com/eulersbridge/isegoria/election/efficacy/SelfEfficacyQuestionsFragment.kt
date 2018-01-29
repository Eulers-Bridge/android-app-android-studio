package com.eulersbridge.isegoria.election.efficacy

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

import com.eulersbridge.isegoria.MainActivity
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.util.ui.TitledFragment

class SelfEfficacyQuestionsFragment : Fragment(), TitledFragment, MainActivity.TabbedFragment {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.self_efficacy_questions_fragment, container, false)

        val sliderBar1 = rootView.findViewById<SelfEfficacySliderBar>(R.id.selfEfficacySliderBar1)
        val sliderBar2 = rootView.findViewById<SelfEfficacySliderBar>(R.id.selfEfficacySliderBar2)
        val sliderBar3 = rootView.findViewById<SelfEfficacySliderBar>(R.id.selfEfficacySliderBar3)
        val sliderBar4 = rootView.findViewById<SelfEfficacySliderBar>(R.id.selfEfficacySliderBar4)

        val viewModel = ViewModelProviders.of(this).get(EfficacyQuestionsViewModel::class.java)

        viewModel.score1.observe(this, Observer {
            if (it != null)
                sliderBar1.score = it.toInt()
        })
        viewModel.score2.observe(this, Observer {
            if (it != null)
                sliderBar2.score = it.toInt()
        })
        viewModel.score3.observe(this, Observer {
            if (it != null)
                sliderBar3.score = it.toInt()
        })
        viewModel.score4.observe(this, Observer {
            if (it != null)
                sliderBar4.score = it.toInt()
        })

        val doneButton = rootView.findViewById<Button>(R.id.selfEfficacyDoneButton)
        doneButton.setOnClickListener { view ->
            view.isEnabled = false

            viewModel.addUserEfficacy().observe(this, Observer { success ->
                if (success == true) {
                    if (activity != null)
                        activity!!.supportFragmentManager.popBackStack()

                } else {
                    view.isEnabled = true
                }
            })
        }

        return rootView
    }

    override fun getTitle(context: Context?): String? {
        return context?.getString(R.string.section_title_self_efficacy_questions)
    }

    override fun setupTabLayout(tabLayout: TabLayout) {
        tabLayout.visibility = View.GONE
    }
}
