package com.eulersbridge.isegoria.poll


import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.Observer
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.Contact
import com.eulersbridge.isegoria.network.api.model.Poll
import com.eulersbridge.isegoria.network.api.model.PollOption
import com.eulersbridge.isegoria.util.data.Optional
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PollVoteViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var dummyOpenUnvotedPoll: Poll
    private lateinit var dummyOpenVotedPoll: Poll
    private lateinit var dummyClosedUnvotedPoll: Poll

    private lateinit var repository: Repository
    private lateinit var  pollVoteViewModel: PollVoteViewModel

    @Before
    fun setUp() {
        dummyOpenUnvotedPoll = Poll(123,
                "executive@cis-gres.org",
                null,
                "What's your favourite colour?",
                listOf(PollOption(0, "Blue", null, 0, false, null)),
                false
        )
        dummyClosedUnvotedPoll = dummyOpenUnvotedPoll.copy(closed = true)

        dummyOpenVotedPoll = dummyOpenUnvotedPoll.copy(options = dummyOpenUnvotedPoll.options.map { it.copy(hasVoted = true) })

        repository = mock {
            on { getContact(any()) } doReturn Single.just(Optional<Contact>(null))
        }
        pollVoteViewModel = PollVoteViewModel(repository)
    }

    @Test
    fun `view model populates question from given poll`() {
        val questionObserver = mock<Observer<String>>()
        pollVoteViewModel.pollQuestion.observeForever(questionObserver)

        pollVoteViewModel.setPoll(dummyOpenUnvotedPoll)

        then(questionObserver).should(times(1)).onChanged(dummyOpenUnvotedPoll.question)
        then(questionObserver).shouldHaveNoMoreInteractions()
    }

    @Test
    fun `view model populates options from given poll`() {
        val optionsObserver = mock<Observer<List<PollOption>>>()
        pollVoteViewModel.pollOptions.observeForever(optionsObserver)

        pollVoteViewModel.setPoll(dummyOpenUnvotedPoll)

        then(optionsObserver).should(times(1)).onChanged(dummyOpenUnvotedPoll.options)
        then(optionsObserver).shouldHaveNoMoreInteractions()
    }

    @Test
    fun `view model enables voting if user has not voted on poll`() {
        val votingEnabledObserver = mock<Observer<Boolean>>()
        pollVoteViewModel.votingEnabled.observeForever(votingEnabledObserver)

        pollVoteViewModel.setPoll(dummyOpenUnvotedPoll)

        then(votingEnabledObserver).should(times(1)).onChanged(true)
        then(votingEnabledObserver).shouldHaveNoMoreInteractions()
    }

    @Test
    fun `view model does not allow voting if user has already voted on poll`() {
        val votingEnabledObserver = mock<Observer<Boolean>>()
        pollVoteViewModel.votingEnabled.observeForever(votingEnabledObserver)

        pollVoteViewModel.setPoll(dummyOpenVotedPoll)

        then(votingEnabledObserver).should(times(1)).onChanged(false)
        then(votingEnabledObserver).shouldHaveNoMoreInteractions()
    }

    @Test
    fun `view model does not allow voting if the poll is closed`() {
        val votingEnabledObserver = mock<Observer<Boolean>>()
        pollVoteViewModel.votingEnabled.observeForever(votingEnabledObserver)

        pollVoteViewModel.setPoll(dummyClosedUnvotedPoll)

        then(votingEnabledObserver).should(times(1)).onChanged(false)
        then(votingEnabledObserver).shouldHaveNoMoreInteractions()
    }

}