package com.eulersbridge.isegoria.auth.signup

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.view.updatePadding
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.auth.AuthViewModel
import com.eulersbridge.isegoria.auth.onTextChanged
import com.eulersbridge.isegoria.network.api.model.Country
import com.eulersbridge.isegoria.network.api.model.Institution
import com.eulersbridge.isegoria.util.extension.observe
import com.eulersbridge.isegoria.util.extension.onItemSelected
import com.eulersbridge.isegoria.util.extension.setKeyboardVisible
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.sign_up_fragment.*
import java.util.*
import javax.inject.Inject

class SignUpFragment : Fragment() {

    @Inject
    internal lateinit var modelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: SignUpViewModel

    private val authViewModel: AuthViewModel by lazy {
        ViewModelProviders.of(requireActivity())[AuthViewModel::class.java]
    }

    private lateinit var countryAdapter: ArrayAdapter<Country>
    private lateinit var institutionAdapter: ArrayAdapter<Institution>

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        viewModel = ViewModelProviders.of(this, modelFactory)[SignUpViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater.inflate(R.layout.sign_up_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val hasTranslucentStatusBar = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        if (hasTranslucentStatusBar) {
            val additionalTopPadding = Math.round(22 * resources.displayMetrics.density)
            container.updatePadding(top = container.paddingTop + additionalTopPadding)
        }

        countryAdapter = ArrayAdapter(activity, android.R.layout.simple_spinner_item)
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        countrySpinner.apply {
            isEnabled = false
            adapter = countryAdapter
        }

        institutionAdapter = ArrayAdapter(activity, android.R.layout.simple_spinner_item)
        institutionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        loginInstitutionSpinner.adapter = institutionAdapter

        val spinnerYearOfBirthArrayAdapter = ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item)

        val year = Calendar.getInstance()[Calendar.YEAR]

        // Allow users to be in the age range 12 to 100
        for (i in year - 100..year - 12) {
            spinnerYearOfBirthArrayAdapter.add(i.toString())

            if (i == 1990)
                birthYearSpinner.setSelection(i)
        }

        spinnerYearOfBirthArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        birthYearSpinner.adapter = spinnerYearOfBirthArrayAdapter

        createViewListeners()

        observe(viewModel.countries) {
            setCountries(it!!)
        }

        activity?.setKeyboardVisible(true)
    }

    private fun createViewListeners() {
        backButton.setOnClickListener {
            activity?.let {
                authViewModel.onSignUpBackPressed()
                it.onBackPressed()
            }
        }

        nextButton.setOnClickListener {
            it.isEnabled = false
            backButton.isEnabled = false

            val newUser = viewModel.getSignUpUser()
            if (newUser == null) {
                backButton.isEnabled = true
                it.isEnabled = true

            } else {
                authViewModel.signUpUser.value = newUser
            }
        }

        givenName.onTextChanged { if (it != null) viewModel.setGivenName(it) }
        familyName.onTextChanged { if (it != null) viewModel.setFamilyName(it) }
        email.onTextChanged { if (it != null) viewModel.setEmail(it) }
        newPassword.onTextChanged { if (it != null) viewModel.setPassword(it) }
        confirmNewPassword.onTextChanged { if (it != null) viewModel.setConfirmPassword(it) }
        gender.onTextChanged { if (it != null) viewModel.setGender(it) }

        countrySpinner.onItemSelected { position -> updateInstitutionSpinner(position) }

        loginInstitutionSpinner.onItemSelected { position -> viewModel.onInstitutionSelected(position) }

        birthYearSpinner.onItemSelected {
            val birthYear = birthYearSpinner.selectedItem as String
            viewModel.setBirthYear(birthYear)
        }
    }

    private fun updateInstitutionSpinner(position: Int) {
        loginInstitutionSpinner.isEnabled = false

        viewModel.onCountrySelected(position)?.let { institutions ->
            institutionAdapter.clear()
            institutionAdapter.addAll(institutions)
        }

        loginInstitutionSpinner.isEnabled = true
    }

    private fun setCountries(newCountries: List<Country>) {
        countryAdapter.apply {
            clear()
            addAll(newCountries)
        }

        updateInstitutionSpinner(0)

        countrySpinner.isEnabled = true
    }
}
