package com.eulersbridge.isegoria.poll

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.enqueue
import com.eulersbridge.isegoria.network.api.models.Contact
import com.eulersbridge.isegoria.network.api.models.Poll
import com.eulersbridge.isegoria.network.api.models.PollOption
import com.eulersbridge.isegoria.network.api.models.PollResult
import com.eulersbridge.isegoria.onSuccess
import com.eulersbridge.isegoria.util.data.RetrofitLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData

class PollViewModel(application: Application) : AndroidViewModel(application) {

    internal val poll = MutableLiveData<Poll>()
    internal val pollResults = MutableLiveData<List<PollResult>>()

    internal val pollCreator = Transformations.switchMap(poll) { thePoll ->
        if (thePoll != null) {
            if (thePoll.creator == null && !thePoll.creatorEmail.isNullOrBlank()) {
                val app = getApplication<IsegoriaApp>()
                RetrofitLiveData(app.api.getContact(thePoll.creatorEmail!!))

            } else if (thePoll.creator != null) {
                SingleLiveData(thePoll.creator)
            }
        }

        SingleLiveData<Contact?>(null)
    }

    internal fun voteForPollOption(optionIndex: Int) {
        val currentPoll = poll.value ?: return

        val (id) = currentPoll.options!![optionIndex]

        val api = getApplication<IsegoriaApp>().api

        api.answerPoll(currentPoll.id, id).enqueue({

            // After voting, fetch poll results
            api.getPollResults(currentPoll.id) .onSuccess { response ->
                response.results?.let { results ->
                    val updatedPollOptions = currentPoll.options ?: listOf<PollOption>()
                        .mapIndexed { index, pollOption ->
                            pollOption.copy(result = results[index])
                        }

                    val updatedPoll = currentPoll.copy(options = updatedPollOptions)
                    poll.value = updatedPoll

                    pollResults.value = results
                }
            }
        })
    }

    override fun onCleared() {
        (pollCreator as? RetrofitLiveData)?.cancel()
    }
}
