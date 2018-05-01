package com.eulersbridge.isegoria.election.efficacy

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.util.extension.observe
import com.eulersbridge.isegoria.util.extension.observeBoolean
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.self_efficacy_questions_fragment.*
import javax.inject.Inject

// TODO: Convert into Activity, as this fragment is an app-level modal
class SelfEfficacyQuestionsFragment : Fragment() {

    @Inject
    internal lateinit var modelFactory: ViewModelProvider.Factory
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
    ): View?
        = inflater.inflate(R.layout.self_efficacy_questions_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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

        doneButton.setOnClickListener {
            viewModel.score1.value = sliderBar1.score
            viewModel.score2.value = sliderBar2.score
            viewModel.score3.value = sliderBar3.score
            viewModel.score4.value = sliderBar4.score
            viewModel.onDone()
        }

        createViewModelObservers()
    }

    private fun createViewModelObservers() {
        observeBoolean(viewModel.doneButtonEnabled) {
            doneButton.isEnabled = it
        }

        observe(viewModel.efficacyComplete) {
            activity!!.supportFragmentManager.popBackStack()
        }
    }
}
