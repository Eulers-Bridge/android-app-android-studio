package com.eulersbridge.isegoria.auth.verification

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.auth.AuthViewModel
import com.eulersbridge.isegoria.ifTrue
import com.eulersbridge.isegoria.observe
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.email_verification.*
import javax.inject.Inject

class EmailVerificationFragment : Fragment() {

    @Inject
    lateinit var modelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: EmailVerificationViewModel

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)

        viewModel = ViewModelProviders.of(this, modelFactory)[EmailVerificationViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
        = inflater.inflate(R.layout.email_verification, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        verifiedButton.setOnClickListener {
            it.isEnabled = false

            val authViewModel = ViewModelProviders.of(requireActivity())[AuthViewModel::class.java]
            authViewModel.verificationComplete.value = true

            ifTrue(viewModel.userVerified()) {
                verifiedButton.isEnabled = true
            }
        }

        resendVerificationButton.setOnClickListener { button ->
            button.isEnabled = false

            observe(viewModel.resendVerification()) {
                button.isEnabled = true
            }
        }
    }

    override fun onDestroy() {
        viewModel.onExit()

        super.onDestroy()
    }
}