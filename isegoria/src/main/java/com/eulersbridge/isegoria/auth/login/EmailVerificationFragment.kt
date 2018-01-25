package com.eulersbridge.isegoria.auth.login

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.eulersbridge.isegoria.R
import kotlinx.android.synthetic.main.email_verification.*

class EmailVerificationFragment : Fragment() {

    private lateinit var viewModel: EmailVerificationViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.email_verification, container, false)

        viewModel = ViewModelProviders.of(this).get(EmailVerificationViewModel::class.java)

        verifiedButton.setOnClickListener {
            it.isEnabled = false

            viewModel.userVerified().observe(this, Observer { success ->
                if (success == null || success == false)
                    verifiedButton.isEnabled = true
            })
        }

        resendVerificationButton.setOnClickListener { button ->
            button.isEnabled = false

            viewModel.resendVerification().observe(this, Observer {
                button.isEnabled = true
            })
        }

        return rootView
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.onExit()
    }
}