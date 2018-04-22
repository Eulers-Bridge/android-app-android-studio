
import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.eulersbridge.isegoria.auth.signup.SignUpUser
import com.eulersbridge.isegoria.auth.signup.SignUpViewModel
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.Country
import com.eulersbridge.isegoria.network.api.model.Institution
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SignUpUnitTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var repository: Repository
    private lateinit var signUpViewModel: SignUpViewModel

    @Before
    fun setUp() {

        val mockInstitution = Institution(26, -1, "CIS-GReS", null, null, null, null)
        val mockCountry = Country(6228, "Australia", listOf(mockInstitution))

        repository = mock {
            on { getSignUpCountries() } doReturn Single.just(listOf(mockCountry)).subscribeOn(Schedulers.trampoline())
        }

        signUpViewModel = SignUpViewModel(repository)
    }

    @Test
    fun `initial view model state prevents sign up`() {
        assert(signUpViewModel.getSignUpUser() == null)
    }

    @Test
    fun `the view model should return a SignUpUser given valid form information`() {
        val password = "thi\$,i5af4ntasticp4S5w0rd!"
        val signUpUser = SignUpUser(
                "Jane",
                "Doe",
                "Female",
                "Australia",
                "1995",
                "jane@doe.com",
                password,
                "CIS-GReS"
        )


        signUpViewModel.setGivenName(signUpUser.givenName)
        signUpViewModel.setFamilyName(signUpUser.familyName)
        signUpViewModel.setEmail(signUpUser.email)
        signUpViewModel.setPassword(password)
        signUpViewModel.setConfirmPassword(password)
        signUpViewModel.setGender(signUpUser.gender)
        signUpViewModel.setBirthYear(signUpUser.yearOfBirth)
        signUpViewModel.onCountrySelected(0)
        signUpViewModel.onInstitutionSelected(0)

        assert(signUpViewModel.getSignUpUser() == signUpUser)
    }
}