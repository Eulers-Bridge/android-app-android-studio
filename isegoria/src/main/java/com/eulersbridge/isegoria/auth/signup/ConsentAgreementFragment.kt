package com.eulersbridge.isegoria.auth.signup

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.auth.AuthViewModel
import kotlinx.android.synthetic.main.sign_up_fragment.*

class ConsentAgreementFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.consent_agreement_fragment, container, false)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val authViewModel = ViewModelProviders.of(activity!!).get(AuthViewModel::class.java)

        nextButton.setOnClickListener {
            it.isEnabled = false

            authViewModel.signUpConsentGiven.value = true
        }
    }
}
