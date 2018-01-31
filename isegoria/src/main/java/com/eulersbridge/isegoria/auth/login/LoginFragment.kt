package com.eulersbridge.isegoria.auth.login

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
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
import com.eulersbridge.isegoria.onTextChanged
import kotlinx.android.synthetic.main.login_fragment.*

class LoginFragment : Fragment() {

    private val viewModel: LoginViewModel by lazy {
        ViewModelProviders.of(this).get(LoginViewModel::class.java)
    }
    private val authViewModel: AuthViewModel by lazy {
        ViewModelProviders.of(activity!!).get(AuthViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater.inflate(R.layout.login_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val hasTranslucentStatusBar = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        if (hasTranslucentStatusBar) {
            val params = logoImage.layoutParams as ConstraintLayout.LayoutParams
            params.topMargin += Math.round(22.0f * resources.displayMetrics.density)
        }

        emailField.onTextChanged { viewModel.email.value = it }
        passwordField.onTextChanged { viewModel.password.value = it }

        loginButton.setOnClickListener { onLoginClicked() }
        forgotPassword.setOnClickListener { showForgotPasswordDialog() }
        signUpButton.setOnClickListener { authViewModel.signUpVisible.value = true }

        createViewModelObservers()
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.onExit()
    }

    private fun showForgotPasswordDialog() {
        if (viewModel.canShowPasswordResetDialog.value == false)
            return

        @SuppressLint("InflateParams")
        val alertView = layoutInflater.inflate(R.layout.alert_dialog_input_forgot_password, null)
        val alertEmailInput = alertView.findViewById<EditText>(R.id.alert_dialog_email_address_input)

        AlertDialog.Builder(context!!)
                .setTitle(R.string.forgot_password_title)
                .setMessage(R.string.forgot_password_message)
                .setView(R.layout.alert_dialog_input_forgot_password)
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
            email.value?.let {
                emailField.setText(it)
            }

            password.value?.let {
                passwordField.setText(it)
            }

            emailError.observe(this@LoginFragment, Observer {
                if (it == true) {
                    emailLayout.error = getString(R.string.user_login_email_error_required)
                    emailLayout.isErrorEnabled = true
                    emailField.requestFocus()

                } else {
                    emailLayout.isErrorEnabled = false
                }
            })

            passwordError.observe(this@LoginFragment, Observer {
                if (it == true) {
                    passwordLayout.error = getString(R.string.user_login_password_error_required)
                    passwordLayout.isErrorEnabled = true

                } else {
                    passwordLayout.isErrorEnabled = false
                }
            })

            networkError.observe(this@LoginFragment, Observer {
                if (it == true) {
                    Snackbar.make(coordinatorLayout, getString(R.string.connection_error_message), Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.connection_error_action)) { onLoginClicked() }
                        .setActionTextColor(ContextCompat.getColor(context!!, R.color.white))
                        .show()

                    setNetworkErrorShown()
                }
            })

            formEnabled.observe(this@LoginFragment, Observer { enabled ->
                arrayOf(emailLayout, passwordLayout, loginButton, signUpButton).forEach {
                    it.isEnabled = enabled == true
                }
            })
        }
    }

    private fun onLoginClicked() {
        viewModel.login().observe(this, Observer { success ->
            if (success == true)
                authViewModel.userLoggedIn.value = true
        })
    }

}
