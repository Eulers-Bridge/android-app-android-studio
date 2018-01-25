package com.eulersbridge.isegoria.auth.signup

import android.app.Application
import android.arch.lifecycle.*
import android.support.v4.app.Fragment
import android.text.TextUtils
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.network.api.models.Country
import com.eulersbridge.isegoria.network.api.models.Institution
import com.eulersbridge.isegoria.util.data.RetrofitLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData

class SignUpViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        fun create(fragment: Fragment): SignUpViewModel {
            return ViewModelProviders.of(fragment).get(SignUpViewModel::class.java)
        }
    }

    private var countries: LiveData<List<Country>>? = null

    val givenName = MutableLiveData<String>()
    val familyName = MutableLiveData<String>()
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val confirmPassword = MutableLiveData<String>()
    private val selectedCountry = MutableLiveData<Country>()
    private val selectedInstitution = MutableLiveData<Institution?>()
    val selectedBirthYear = MutableLiveData<String>()
    val selectedGender = MutableLiveData<String>()

    fun getCountries(): LiveData<List<Country>?> {
        val app: IsegoriaApp = getApplication()
        val generalInfo = RetrofitLiveData(app.api.generalInfo)

        return Transformations.switchMap(generalInfo) {
            SingleLiveData(it?.countries)
        }
    }

    fun onCountrySelected(index: Int) : List<Institution>? {
        selectedInstitution.value = null

        countries?.value?.let {
            val country = it[index]
            selectedCountry.value = country

            country.institutions
        }

        return null
    }

    fun onInstitutionSelected(index: Int) {
        countries?.value?.let {
            val institution = it[index].institutions[index]
            selectedInstitution.value = institution
        }
    }

    val signUpUser: SignUpUser? by lazy {
        val givenName = givenName.value
        val givenNameValid = !TextUtils.isEmpty(givenName)

        val familyName = familyName.value
        val familyNameValid = !TextUtils.isEmpty(familyName)

        val email = email.value
        val emailValid = !TextUtils.isEmpty(email)

        val password = password.value
        val passwordValid = !TextUtils.isEmpty(password)

        val confirmPassword = confirmPassword.value
        val passwordsMatch = passwordValid && password == confirmPassword

        val country = selectedCountry.value
        val countryValid = country != null

        val institution = selectedInstitution.value
        val institutionValid = institution != null

        val birthYear = selectedBirthYear.value
        val birthYearValid = birthYear != null

        val gender = selectedGender.value
        val genderValid = gender != null

        val allFieldsValid = givenNameValid && familyNameValid && emailValid
                && passwordValid && passwordsMatch && countryValid
                && institutionValid && birthYearValid && genderValid

        if (allFieldsValid)
            SignUpUser(givenName!!, familyName!!, gender!!, country!!.name, birthYear!!,
                    email!!, password!!, institution!!.name)

        null
    }

}