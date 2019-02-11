package com.eulersbridge.isegoria.auth.login

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.auth.AuthViewModel
import com.eulersbridge.isegoria.auth.onTextChanged
import com.eulersbridge.isegoria.util.extension.observe
import com.eulersbridge.isegoria.util.extension.observeBoolean
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.login_fragment.*
import javax.inject.Inject

class LoginFragment : Fragment() {

    @Inject
    internal lateinit var modelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: LoginViewModel

    private val authViewModel: AuthViewModel by lazy {
        ViewModelProviders.of(requireActivity())[AuthViewModel::class.java]
    }

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        viewModel = ViewModelProviders.of(this, modelFactory)[LoginViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater.inflate(R.layout.login_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val hasTranslucentStatusBar = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        if (hasTranslucentStatusBar) {
            val params = logoImage.layoutParams as ConstraintLayout.LayoutParams
            params.topMargin += Math.round(22.0f * resources.displayMetrics.density)
        }

        emailField.onTextChanged { viewModel.setEmail(it) }
        passwordField.onTextChanged { viewModel.setPassword(it) }

        loginButton.setOnClickListener { onLoginClicked() }
        forgotPassword.setOnClickListener { showForgotPasswordDialog() }
        signUpButton.setOnClickListener { authViewModel.signUpVisible.value = true }

        createViewModelObservers()
    }

    @SuppressLint("InflateParams")
    private fun showForgotPasswordDialog() {
        if (viewModel.canShowPasswordResetDialog.value == false)
            return

        val alertView = layoutInflater.inflate(R.layout.alert_dialog_input_forgot_password, null)
        val alertEmailInput = alertView.findViewById<EditText>(R.id.alert_dialog_email_address_input)

        AlertDialog.Builder(context!!)
                .setTitle(R.string.forgot_password_title)
                .setMessage(R.string.forgot_password_message)
                .setView(alertView)
                .setPositiveButton(android.R.string.ok) { _, _ -> resetPassword(alertEmailInput.text) }
                .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }
                .show()
    }

    private fun resetPassword(editable: Editable) {
        val forgottenAccountEmail = editable.toString()
        val validEmail = viewModel.requestPasswordRecoveryEmail(forgottenAccountEmail)

        if (validEmail) {
            val message = getString(R.string.forgot_password_email_sent, forgottenAccountEmail)
            Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun createViewModelObservers() {
        viewModel.apply {
            observeBoolean(emailError) {
                if (it) {
                    emailLayout.error = getString(R.string.user_login_email_error_required)
                    emailLayout.isErrorEnabled = true
                    emailField.requestFocus()

                } else {
                    emailLayout.isErrorEnabled = false
                }
            }

            observeBoolean(passwordError) {
                if (it) {
                    passwordLayout.error = getString(R.string.user_login_password_error_required)
                    passwordLayout.isErrorEnabled = true

                } else {
                    passwordLayout.isErrorEnabled = false
                }
            }


            observe(loginError) {
                when (loginError.value) {
                    LoginViewModel.LoginError.NotAuthorised -> {
                        Snackbar.make(coordinatorLayout, getString(R.string.user_login_error_message), Snackbar.LENGTH_LONG)
                                .show()
                    }
                    LoginViewModel.LoginError.UnkownFailure -> {
                        Snackbar.make(coordinatorLayout, getString(R.string.connection_error_message), Snackbar.LENGTH_LONG)
                                .setAction(getString(R.string.connection_error_action)) { onLoginClicked() }
                                .setActionTextColor(ContextCompat.getColor(context!!, R.color.white))
                                .show()
                    }
                }

                if (loginError.value != null) {
                    clearLoginError()
                }
            }

            observeBoolean(formEnabled) { enabled ->
                arrayOf(emailLayout, passwordLayout, loginButton, signUpButton).forEach {
                    it.isEnabled = enabled
                }
            }
        }
    }

    private fun onLoginClicked() {
        viewModel.login()
    }

}
