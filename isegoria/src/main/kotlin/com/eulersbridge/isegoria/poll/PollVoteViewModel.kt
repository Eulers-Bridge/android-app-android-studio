package com.eulersbridge.isegoria.poll

import android.arch.lifecycle.MutableLiveData
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.Contact
import com.eulersbridge.isegoria.network.api.model.Poll
import com.eulersbridge.isegoria.network.api.model.PollOption
import com.eulersbridge.isegoria.network.api.model.PollResult
import com.eulersbridge.isegoria.util.BaseViewModel
import com.eulersbridge.isegoria.util.data.Optional
import com.eulersbridge.isegoria.util.extension.subscribeSuccess
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

class PollVoteViewModel @Inject constructor (private val repository: Repository) : BaseViewModel() {

    private var poll: Poll? = null
    internal val pollQuestion = MutableLiveData<String>()
    internal val pollCreator = MutableLiveData<Contact?>()
    internal val pollOptions = MutableLiveData<List<PollOption>>()

    internal val votingEnabled = MutableLiveData<Boolean>()
    internal val votingError = MutableLiveData<Boolean>()

    internal val pollResults = MutableLiveData<List<PollResult>>()

    internal fun setPoll(poll: Poll?) {
        this.poll = poll

        poll?.let {
            pollQuestion.value = it.question
            pollOptions.value = it.options

            // Enable voting if the poll is not closed and the user has not voted for any option
            votingEnabled.value = !it.closed  &&it.options.none { it.hasVoted }

            fetchPollCreator()
        }
    }

    private fun fetchPollCreator() {
        this.poll?.let { poll ->
            if (poll.creator == null && !poll.creatorEmail.isNullOrBlank()) {
                repository.getContact(poll.creatorEmail!!)
                        .onErrorReturnItem(Optional(poll.creator))
                        .subscribeSuccess {
                            pollCreator.postValue(it.value)
                        }
                        .addToDisposable()
            } else {
                pollCreator.value = poll.creator
            }
        }
    }

    internal fun voteForPollOption(optionIndex: Int) {
        votingEnabled.value = false
        votingError.value = false

        val currentPoll = poll ?: return

        val (id) = currentPoll.options[optionIndex]

        repository.answerPoll(currentPoll.id, id)
                .andThen(repository.getPollResults(currentPoll.id))
                .map {
                    if (it.size == currentPoll.options.size) {
                        it
                    } else {
                        throw Exception("Missing results for all poll options")
                    }
                }
                .subscribeBy(
                        onSuccess = { results ->
                            votingEnabled.postValue(true)

                            val updatedPollOptions = currentPoll.options
                                    .mapIndexed { index, pollOption ->
                                        if (index < results.size - 1) {
                                            pollOption.copy(result = results[index])
                                        } else {
                                            pollOption
                                        }
                                    }

                            pollOptions.postValue(updatedPollOptions)
                            pollResults.postValue(results)
                        },
                        onError = {
                            votingEnabled.postValue(true)
                            votingError.postValue(true)
                        }
                )
                .addToDisposable()
    }
}
