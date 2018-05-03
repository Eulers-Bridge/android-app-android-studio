package com.eulersbridge.isegoria.auth.signup

import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.Country
import com.eulersbridge.isegoria.network.api.model.Institution
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SignUpViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var repository: Repository
    private lateinit var signUpViewModel: SignUpViewModel

    @Before
    fun setUp() {

        val mockInstitution = Institution(26, -1, "CIS-GReS", null, null, null, null)
        val mockCountry = Country(6228, "Australia", listOf(mockInstitution))

        repository = mock {
            on { getSignUpCountries() } doReturn Single.just(listOf(mockCountry))
        }

        signUpViewModel = SignUpViewModel(repository)
    }

    @Test
    fun `initial view model state prevents sign up`() {
        assert(signUpViewModel.getSignUpUser() == null)
    }

    @Test
    fun `view model should return a SignUpUser given valid form information`() {
        val password = "thi\$,i5af4ntasticp4S5w0rd!"
        val expected = SignUpUser(
                "Jane",
                "Doe",
                "Female",
                "Australia",
                "1995",
                "jane@doe.com",
                password,
                "CIS-GReS"
        )

        signUpViewModel.setGivenName(expected.givenName)
        signUpViewModel.setFamilyName(expected.familyName)
        signUpViewModel.setEmail(expected.email)
        signUpViewModel.setPassword(password)
        signUpViewModel.setConfirmPassword(password)
        signUpViewModel.setGender(expected.gender)
        signUpViewModel.setBirthYear(expected.yearOfBirth)
        signUpViewModel.onCountrySelected(0)
        signUpViewModel.onInstitutionSelected(0)

        assert(signUpViewModel.getSignUpUser() == expected)
    }
}