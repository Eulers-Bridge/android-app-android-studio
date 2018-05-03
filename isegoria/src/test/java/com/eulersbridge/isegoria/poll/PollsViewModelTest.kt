package com.eulersbridge.isegoria.poll


import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.Poll
import com.eulersbridge.isegoria.network.api.model.PollOption
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.then
import com.nhaarman.mockitokotlin2.times
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PollsViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var repository: Repository
    private lateinit var  pollsViewModel: PollsViewModel

    @Before
    fun setUp() {
        repository = mock {
            on { getPolls() } doReturn Single.just(listOf(
                    Poll(123,
                            "executive@cis-gres.org",
                            null,
                            "What's your favourite colour?",
                            listOf(PollOption(0, "Blue", null, 0, false, null)),
                            false
                    )))
        }
        pollsViewModel = PollsViewModel(repository)
    }

    @Test
    fun `view models should fetch repository polls`() {
        then(repository).should(times(1)).getPolls()
    }

}