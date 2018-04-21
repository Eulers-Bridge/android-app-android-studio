package com.eulersbridge.isegoria.auth.signup

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.eulersbridge.isegoria.network.api.API
import com.eulersbridge.isegoria.network.api.model.Country
import com.eulersbridge.isegoria.network.api.model.Institution
import com.eulersbridge.isegoria.util.data.SingleLiveData
import com.eulersbridge.isegoria.util.extension.toLiveData
import javax.inject.Inject

class SignUpViewModel
@Inject constructor(private val api: API) : ViewModel() {

    private var countries: List<Country>? = null

    val givenName = MutableLiveData<String>()
    val familyName = MutableLiveData<String>()
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val confirmPassword = MutableLiveData<String>()
    val gender = MutableLiveData<String>()
    private var selectedCountry: Country? = null
    private var selectedInstitution: Institution? = null
    val selectedBirthYear = MutableLiveData<String>()

    fun getCountries(): LiveData<List<Country>?> {
        countries?.let {
            return SingleLiveData(it)
        }

        return api.getGeneralInfo()
                .doOnSuccess { countries = it.countries }
                .map { it.countries }
                .toLiveData()
    }

    fun onCountrySelected(index: Int) : List<Institution>? {
        selectedInstitution = null

        return countries?.let {
            val country = it[index]
            selectedCountry = country

            country.institutions
        }
    }

    fun onInstitutionSelected(index: Int) {
        countries?.let { countries ->
            val institution = countries[index].institutions?.get(index)
            selectedInstitution = institution
        }
    }

    fun getSignUpUser(): SignUpUser? {
        val givenName = givenName.value
        val givenNameValid = !givenName.isNullOrBlank()

        val familyName = familyName.value
        val familyNameValid = !familyName.isNullOrBlank()

        val email = email.value
        val emailValid = !email.isNullOrBlank()

        val password = password.value
        val passwordValid = !password.isNullOrBlank()

        val confirmPassword = confirmPassword.value
        val passwordsMatch = passwordValid && password == confirmPassword

        val countryValid = selectedCountry != null
        val institutionValid = selectedInstitution != null

        val birthYear = selectedBirthYear.value
        val birthYearValid = birthYear != null

        val gender = gender.value
        val genderValid = gender != null

        val allFieldsValid = givenNameValid && familyNameValid && emailValid
                && passwordValid && passwordsMatch && countryValid
                && institutionValid && birthYearValid && genderValid

        if (allFieldsValid)
            return SignUpUser(givenName!!, familyName!!, gender!!, selectedCountry!!.name, birthYear!!,
                    email!!, password!!, selectedInstitution!!.getName())

        return null
    }

}