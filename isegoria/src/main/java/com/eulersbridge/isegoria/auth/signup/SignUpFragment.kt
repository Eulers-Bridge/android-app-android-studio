package com.eulersbridge.isegoria.auth.signup

import android.arch.lifecycle.ViewModelProviders
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.view.updatePadding
import com.eulersbridge.isegoria.*
import com.eulersbridge.isegoria.auth.AuthViewModel
import com.eulersbridge.isegoria.network.api.models.Country
import com.eulersbridge.isegoria.network.api.models.Institution
import kotlinx.android.synthetic.main.sign_up_fragment.*
import java.util.*

class SignUpFragment : Fragment() {

    private lateinit var countryAdapter: ArrayAdapter<Country>
    private lateinit var institutionAdapter: ArrayAdapter<Institution>

    private val viewModel: SignUpViewModel by lazy {
        ViewModelProviders.of(this).get(SignUpViewModel::class.java)
    }

    private val authViewModel: AuthViewModel by lazy {
        ViewModelProviders.of(activity!!).get(AuthViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater.inflate(R.layout.sign_up_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val hasTranslucentStatusBar = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        if (hasTranslucentStatusBar) {
            val additionalTopPadding = Math.round(22 * resources.displayMetrics.density)
            container.updatePadding(top = container.paddingTop + additionalTopPadding)
        }

        countryAdapter = ArrayAdapter(activity!!, android.R.layout.simple_spinner_item)
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        countrySpinner.apply {
            isEnabled = false
            adapter = countryAdapter
        }

        institutionAdapter = ArrayAdapter(activity, android.R.layout.simple_spinner_item)
        institutionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        institutionSpinner.adapter = institutionAdapter

        val spinnerGenderArrayAdapter = ArrayAdapter(activity, android.R.layout.simple_spinner_item, arrayOf("Male", "Female"))
        spinnerGenderArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = spinnerGenderArrayAdapter

        val spinnerYearOfBirthArrayAdapter = ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item)

        val year = Calendar.getInstance().get(Calendar.YEAR)

        // Allow users to be in the age range 12 to 100
        for (i in year - 100..year - 12) {
            spinnerYearOfBirthArrayAdapter.add(i.toString())

            if (i == 1990) birthYearSpinner.setSelection(i)
        }
        spinnerYearOfBirthArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        birthYearSpinner.adapter = spinnerYearOfBirthArrayAdapter

        setupViewListeners()

        observe(viewModel.getCountries()) {
            it?.let { setCountries(it) }
        }

        activity?.setKeyboardVisible(true)
    }

    private fun setupViewListeners() {
        backButton.setOnClickListener {
            activity?.let {
                authViewModel.onSignUpBackPressed()
                it.onBackPressed()
            }
        }

        nextButton.setOnClickListener {
            it.isEnabled = false
            backButton.isEnabled = false

            val newUser = viewModel.signUpUser
            if (newUser == null) {
                backButton.isEnabled = true
                it.isEnabled = true

            } else {
                authViewModel.signUpUser.value = newUser
            }
        }

        givenName.onTextChanged { viewModel.givenName.value = it }
        familyName.onTextChanged { viewModel.familyName.value = it }
        email.onTextChanged { viewModel.email.value = it }
        newPassword.onTextChanged { viewModel.password.value = it }
        confirmNewPassword.onTextChanged { viewModel.confirmPassword.value = it }

        countrySpinner.onItemSelected { position -> updateInstitutionSpinner(position) }

        institutionSpinner.onItemSelected { position -> viewModel.onInstitutionSelected(position) }

        birthYearSpinner.onItemSelected {
            val birthYear = birthYearSpinner.selectedItem as String
            viewModel.selectedBirthYear.value = birthYear
        }

        genderSpinner.onItemSelected {
            val gender = genderSpinner.selectedItem as String
            viewModel.selectedGender.value = gender
        }
    }

    private fun updateInstitutionSpinner(position: Int) {
        institutionSpinner.isEnabled = false

        viewModel.onCountrySelected(position)?.let { institutions ->
            institutionAdapter.clear()
            institutionAdapter.addAll(institutions)
        }

        institutionSpinner.isEnabled = true
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
