package com.eulersbridge.isegoria.poll

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.network.api.models.Contact
import com.eulersbridge.isegoria.network.api.models.Poll
import com.eulersbridge.isegoria.network.api.models.PollResult
import com.eulersbridge.isegoria.network.api.responses.PollResultsResponse
import com.eulersbridge.isegoria.util.data.RetrofitLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData
import com.eulersbridge.isegoria.util.network.SimpleCallback
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

        api.answerPoll(currentPoll.id, id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                // After voting, fetch poll results

                api.getPollResults(currentPoll.id)
                    .enqueue(object : SimpleCallback<PollResultsResponse>() {
                        override fun handleResponse(response: Response<PollResultsResponse>) {
                            response.body()?.results?.let { results ->

                                val updatedPollOptions = currentPoll.options!!
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

            override fun onFailure(call: Call<Void>, t: Throwable) = t.printStackTrace()
        })
    }

    override fun onCleared() {
        (pollCreator as? RetrofitLiveData)?.cancel()
    }
}
