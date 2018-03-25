package com.eulersbridge.isegoria.personality

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.eulersbridge.isegoria.R
import kotlinx.android.synthetic.main.personality_screen1_fragment.*

class PersonalityPermissionFragment : Fragment() {

    private lateinit var viewModel: PersonalityViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
        = inflater.inflate(R.layout.personality_screen1_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        takePersonalityButton.setOnClickListener { this.onContinue() }
    }

    fun setViewModel(viewModel: PersonalityViewModel) {
        this.viewModel = viewModel
    }

    private fun onContinue() {
        viewModel.questionsContinued.value = true
    }
}
