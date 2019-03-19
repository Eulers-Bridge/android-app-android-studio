package com.eulersbridge.isegoria.auth.verification

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.auth.AuthViewModel
import com.eulersbridge.isegoria.auth.login.LoginViewModel
import com.eulersbridge.isegoria.util.extension.observe
import com.eulersbridge.isegoria.util.extension.observeBoolean
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.email_verification.*
import javax.inject.Inject

class EmailVerificationFragment : Fragment() {

    @Inject
    internal lateinit var modelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: EmailVerificationViewModel
    private lateinit var authViewModel: AuthViewModel

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        viewModel = ViewModelProviders.of(this, modelFactory)[EmailVerificationViewModel::class.java]

        authViewModel = ViewModelProviders.of(requireActivity())[AuthViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
        = inflater.inflate(R.layout.email_verification, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        observeBoolean(viewModel.resendVerificationButtonEnabled) {
            resendVerificationButton.isEnabled = it
        }

        observeBoolean(viewModel.completeButtonEnabled) {
            verifiedButton.isEnabled = it
        }

        verifiedButton.setOnClickListener {
            authViewModel.onEmailVerified()
        }

        resendVerificationButton.setOnClickListener {
            authViewModel.onResendVerification()
        }

        observe(viewModel.loginError) {
            when (it) {
                LoginViewModel.LoginError.NotAuthorised -> {
                    Snackbar.make(emailVerificationLayout, getString(R.string.email_verification_login_error_message), Snackbar.LENGTH_LONG)
                            .show()
                }
                LoginViewModel.LoginError.UnknownFailure -> {
                    Snackbar.make(emailVerificationLayout, getString(R.string.connection_error_message), Snackbar.LENGTH_LONG)
                            .show()
                }
            }
        }

        observe(authViewModel.toasMessage) {stringResourceId ->
            if (stringResourceId != null) {
                Toast.makeText(activity, getString(stringResourceId), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        viewModel.onDestroy()
        super.onDestroy()
    }
}