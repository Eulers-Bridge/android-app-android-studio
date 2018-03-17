package com.eulersbridge.isegoria.personality

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.eulersbridge.isegoria.R
import kotlinx.android.synthetic.main.personality_screen1_fragment.*

class PersonalityPermissionFragment : Fragment() {

    private val viewModel: PersonalityViewModel by lazy {
        ViewModelProviders.of(activity!!).get(PersonalityViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
        = inflater.inflate(R.layout.personality_screen1_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        takePersonalityButton.setOnClickListener { viewModel.userContinuedQuestions.value = true }
    }
}
