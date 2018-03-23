package com.eulersbridge.isegoria.poll

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import com.eulersbridge.isegoria.enqueue
import com.eulersbridge.isegoria.network.NetworkService
import com.eulersbridge.isegoria.network.api.models.Contact
import com.eulersbridge.isegoria.network.api.models.Poll
import com.eulersbridge.isegoria.network.api.models.PollOption
import com.eulersbridge.isegoria.network.api.models.PollResult
import com.eulersbridge.isegoria.onSuccess
import com.eulersbridge.isegoria.util.data.RetrofitLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData
import javax.inject.Inject

class PollViewModel
@Inject constructor (
    private val networkService: NetworkService
) : ViewModel() {

    internal val poll = MutableLiveData<Poll>()
    internal val pollResults = MutableLiveData<List<PollResult>>()

    internal val pollCreator: LiveData<Contact?> = Transformations.switchMap(poll) { thePoll ->

        return@switchMap if (thePoll.creator == null && !thePoll.creatorEmail.isNullOrBlank()) {
            RetrofitLiveData(networkService.api.getContact(thePoll.creatorEmail!!)) as RetrofitLiveData<Contact?>

        } else {
            SingleLiveData(thePoll?.creator)
        }
    }

    internal fun voteForPollOption(optionIndex: Int) {
        val currentPoll = poll.value ?: return

        val (id) = currentPoll.options!![optionIndex]

        networkService.api.answerPoll(currentPoll.id, id).enqueue({

            // After voting, fetch poll results
            networkService.api.getPollResults(currentPoll.id) .onSuccess { response ->
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
