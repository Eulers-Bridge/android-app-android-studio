package com.eulersbridge.isegoria.auth.signup

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import com.eulersbridge.isegoria.network.api.API
import com.eulersbridge.isegoria.network.api.models.Country
import com.eulersbridge.isegoria.network.api.models.Institution
import com.eulersbridge.isegoria.util.data.RetrofitLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData
import javax.inject.Inject

class SignUpViewModel
@Inject constructor(private val api: API) : ViewModel() {

    private var countries: LiveData<List<Country>>? = null

    val givenName = MutableLiveData<String>()
    val familyName = MutableLiveData<String>()
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val confirmPassword = MutableLiveData<String>()
    val gender = MutableLiveData<String>()
    private val selectedCountry = MutableLiveData<Country>()
    private val selectedInstitution = MutableLiveData<Institution?>()
    val selectedBirthYear = MutableLiveData<String>()

    fun getCountries(): LiveData<List<Country>?> {
        val generalInfo = RetrofitLiveData(api.getGeneralInfo())

        return Transformations.switchMap(generalInfo) {
            return@switchMap SingleLiveData(it?.countries)
        }
    }

    fun onCountrySelected(index: Int) : List<Institution>? {
        selectedInstitution.value = null

        countries?.value?.let {
            val country = it[index]
            selectedCountry.value = country

            return country.institutions
        }

        return null
    }

    fun onInstitutionSelected(index: Int) {
        countries?.value?.let { countries ->
            val institution = countries[index].institutions?.get(index)
            selectedInstitution.value = institution
        }
    }

    val signUpUser: SignUpUser? by lazy {
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

        val country = selectedCountry.value
        val countryValid = country != null

        val institution = selectedInstitution.value
        val institutionValid = institution != null

        val birthYear = selectedBirthYear.value
        val birthYearValid = birthYear != null

        val gender = gender.value
        val genderValid = gender != null

        val allFieldsValid = givenNameValid && familyNameValid && emailValid
                && passwordValid && passwordsMatch && countryValid
                && institutionValid && birthYearValid && genderValid

        if (allFieldsValid)
            SignUpUser(givenName!!, familyName!!, gender!!, country!!.name, birthYear!!,
                    email!!, password!!, institution!!.getName())

        null
    }

}