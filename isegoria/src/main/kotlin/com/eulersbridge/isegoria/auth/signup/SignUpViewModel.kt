package com.eulersbridge.isegoria.auth.signup

import android.arch.lifecycle.MutableLiveData
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.Country
import com.eulersbridge.isegoria.network.api.model.Institution
import com.eulersbridge.isegoria.util.BaseViewModel
import com.eulersbridge.isegoria.util.extension.subscribeSuccess
import javax.inject.Inject

class SignUpViewModel
@Inject constructor(private val repository: Repository) : BaseViewModel() {

    internal val countries = MutableLiveData<List<Country>>()

    private var givenName: String? = null
    private var familyName: String? = null
    private var email: String? = null
    private var password: String? = null
    private var confirmPassword: String? = null
    private var gender: String? = null
    private var selectedCountry: Country? = null
    private var selectedInstitution: Institution? = null
    private var birthYear: String? = null

    init {
        countries.value = emptyList()

        fetchCountries()
    }

    private fun fetchCountries() {
        repository.getSignUpCountries()
                .subscribeSuccess { countries.postValue(it) }
                .addToDisposable()
    }

    fun setGivenName(value: String) {
        this.givenName = value
    }

    fun setFamilyName(value: String) {
        this.familyName = value
    }

    fun setEmail(value: String) {
        this.email = value
    }

    fun setPassword(value: String) {
        this.password = value
    }

    fun setConfirmPassword(value: String) {
        this.confirmPassword = value
    }

    fun setGender(value: String) {
        this.gender = value
    }

    fun setBirthYear(value: String) {
        this.birthYear = value
    }

    fun onCountrySelected(index: Int) : List<Institution>? {
        selectedInstitution = null

        return countries.value?.takeIf { it.isNotEmpty() }?.let {
            val country = it[index]
            selectedCountry = country

            country.institutions
        }
    }

    fun onInstitutionSelected(index: Int) {
        countries.value?.let {
            val institution = it[index].institutions?.get(index)
            selectedInstitution = institution
        }
    }

    fun getSignUpUser(): SignUpUser? {
        val givenNameValid = !givenName.isNullOrBlank()
        val familyNameValid = !familyName.isNullOrBlank()
        val emailValid = !email.isNullOrBlank()
        val passwordValid = !password.isNullOrBlank()
        val passwordsMatch = passwordValid && password == confirmPassword
        val countryValid = selectedCountry != null
        val institutionValid = selectedInstitution != null
        val birthYearValid = birthYear != null
        val genderValid = gender != null

        val allFieldsValid = givenNameValid && familyNameValid && emailValid
                && passwordValid && passwordsMatch && countryValid
                && institutionValid && birthYearValid && genderValid

        if (allFieldsValid)
            return SignUpUser(givenName!!, familyName!!, gender!!, selectedCountry!!.name, birthYear!!,
                    email!!, password!!, selectedInstitution!!.getName(), selectedInstitution!!.id)

        return null
    }

}