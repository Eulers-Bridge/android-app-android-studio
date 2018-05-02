
import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.Observer
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.feed.news.detail.NewsDetailViewModel
import com.eulersbridge.isegoria.network.api.model.Contact
import com.eulersbridge.isegoria.network.api.model.Like
import com.eulersbridge.isegoria.network.api.model.NewsArticle
import com.eulersbridge.isegoria.network.api.model.User
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong

class NewsDetailViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var dummyNewsArticleLikes: List<Like>
    private lateinit var dummyUser: User
    private lateinit var dummyContact: Contact
    private lateinit var dummyNewsArticle: NewsArticle

    private lateinit var repository: Repository
    private lateinit var newsDetailViewModel: NewsDetailViewModel

    @Before
    fun setUp() {
        dummyNewsArticleLikes = listOf(Like("John", "Smith", "john@smith.com"))

        dummyUser = User(
                "Female",
                null,
                "phoebe_bell@example.com",
                "Phoebe",
                "Bell",
                26,
                4,
                4500,
                1,
                1,
                "",
                0,
                true,
                true,
                true,
                false,
                false,
                "1994",
                null
        )

        dummyContact = Contact(
                "Female",
                null,
                "executive@cis-gres.org",
                "Jane",
                "Doe",
                26,
                4,
                4600,
                0,
                0,
                "",
                0,
                null,
                null
        )

        dummyNewsArticle = NewsArticle(
                37225,
                26,
                "CIS Students in the University's 3-Minute Thesis Competition",
                "Lorem ipsum",
                null,
                1507507200000,
                0,
                "executive@cis-gres.org",
                dummyContact,
                false
        )

        repository = mock {
            on { getNewsArticleLikes(anyLong()) } doReturn Single.just(dummyNewsArticleLikes)
            on { getUser() } doReturn dummyUser
        }

        newsDetailViewModel = NewsDetailViewModel(repository)
    }

    @Test
    fun `view model stores given article`() {
        val observer = mock<Observer<NewsArticle>>()
        newsDetailViewModel.newsArticle.observeForever(observer)

        newsDetailViewModel.setNewsArticle(dummyNewsArticle)

        verify(observer, times(1)).onChanged(dummyNewsArticle)
    }

    @Test
    fun `view model fetches likes for a given article`() {
        val likeCountObserver = mock<Observer<Int>>()
        newsDetailViewModel.likeCount.observeForever(likeCountObserver)

        val likedByUserObserver = mock<Observer<Boolean>>()
        newsDetailViewModel.likedByUser.observeForever(likedByUserObserver)

        newsDetailViewModel.setNewsArticle(dummyNewsArticle)

        verify(repository, times(1)).getNewsArticleLikes(dummyNewsArticle.id)
        verify(likeCountObserver, times(1)).onChanged(dummyNewsArticleLikes.size)
        verifyNoMoreInteractions(likeCountObserver)
        verify(likedByUserObserver, times(1)).onChanged(false)
        verifyNoMoreInteractions(likedByUserObserver)
    }

}