package com.eulersbridge.isegoria.auth

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.observe
import kotlinx.android.synthetic.main.email_verification.*

class EmailVerificationFragment : Fragment() {

    private val viewModel: EmailVerificationViewModel by lazy {
        ViewModelProviders.of(this).get(EmailVerificationViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
        = inflater.inflate(R.layout.email_verification, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        verifiedButton.setOnClickListener {
            it.isEnabled = false

            val authViewModel = ViewModelProviders.of(requireActivity()).get(AuthViewModel::class.java)
            authViewModel.verificationComplete.value = true

            observe(viewModel.userVerified()) {
                if (it == null || it == false)
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