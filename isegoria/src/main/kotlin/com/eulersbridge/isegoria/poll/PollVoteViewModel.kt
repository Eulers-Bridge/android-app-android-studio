package com.eulersbridge.isegoria.poll

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.Contact
import com.eulersbridge.isegoria.network.api.model.Poll
import com.eulersbridge.isegoria.network.api.model.PollResult
import com.eulersbridge.isegoria.util.BaseViewModel
import com.eulersbridge.isegoria.util.data.SingleLiveData
import com.eulersbridge.isegoria.util.extension.map
import com.eulersbridge.isegoria.util.extension.subscribeSuccess
import com.eulersbridge.isegoria.util.extension.toLiveData
import javax.inject.Inject

class PollVoteViewModel @Inject constructor (private val repository: Repository) : BaseViewModel() {

    internal val poll = MutableLiveData<Poll>()
    internal val pollResults = MutableLiveData<List<PollResult>>()

    internal val pollCreator: LiveData<Contact?> = Transformations.switchMap(poll) {
        return@switchMap if (it.creator == null && !it.creatorEmail.isNullOrBlank()) {
            repository.getContact(it.creatorEmail!!).toLiveData().map { it.value }

        } else {
            SingleLiveData(it?.creator)
        }
    }

    internal fun voteForPollOption(optionIndex: Int) {
        val currentPoll = poll.value ?: return

        val (id) = currentPoll.options[optionIndex]

        repository.answerPoll(currentPoll.id, id)
                .andThen(repository.getPollResults(currentPoll.id))
                .subscribeSuccess { results ->
                    val updatedPollOptions = currentPoll.options
                            .mapIndexed { index, pollOption ->
                                pollOption.copy(result = results[index])
                            }

                    val updatedPoll = currentPoll.copy(options = updatedPollOptions)
                    poll.postValue(updatedPoll)
                    pollResults.postValue(results)
                }.addToDisposable()
    }
}
