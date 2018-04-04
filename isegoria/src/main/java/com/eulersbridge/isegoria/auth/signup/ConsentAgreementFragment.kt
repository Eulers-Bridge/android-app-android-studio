package com.eulersbridge.isegoria.auth.signup

import android.arch.lifecycle.ViewModelProviders
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.auth.AuthViewModel
import kotlinx.android.synthetic.main.consent_agreement_fragment.*


class ConsentAgreementFragment : Fragment() {

    private val authViewModel: AuthViewModel by lazy {
        ViewModelProviders.of(requireActivity())[AuthViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.consent_agreement_fragment, container, false)

        val hasTranslucentStatusBar = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        if (hasTranslucentStatusBar) {
            val additionalTopPadding = Math.round(22 * resources.displayMetrics.density)
            scrollContainer.updatePadding(top = scrollContainer.paddingTop + additionalTopPadding)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        agreeButton.setOnClickListener {
            it.isEnabled = false
            authViewModel.signUpConsentGiven.value = true
        }

        disagreeButton.setOnClickListener {
            activity?.onBackPressed()
        }
    }
}
