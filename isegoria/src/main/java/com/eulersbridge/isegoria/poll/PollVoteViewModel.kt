package com.eulersbridge.isegoria.poll

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import com.eulersbridge.isegoria.network.api.API
import com.eulersbridge.isegoria.network.api.model.Contact
import com.eulersbridge.isegoria.network.api.model.Poll
import com.eulersbridge.isegoria.network.api.model.PollResult
import com.eulersbridge.isegoria.util.data.SingleLiveData
import com.eulersbridge.isegoria.util.extension.subscribeSuccess
import com.eulersbridge.isegoria.util.extension.toLiveData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class PollVoteViewModel
@Inject constructor (
    private val api: API
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    internal val poll = MutableLiveData<Poll>()
    internal val pollResults = MutableLiveData<List<PollResult>>()

    internal val pollCreator: LiveData<Contact?> = Transformations.switchMap(poll) {
        return@switchMap if (it.creator == null && !it.creatorEmail.isNullOrBlank()) {
            api.getContact(it.creatorEmail!!).toLiveData() as LiveData<Contact?>

        } else {
            SingleLiveData(it?.creator)
        }
    }

    internal fun voteForPollOption(optionIndex: Int) {
        val currentPoll = poll.value ?: return

        val (id) = currentPoll.options[optionIndex]

        api.answerPoll(currentPoll.id, id)
                .onErrorComplete()
                .andThen(api.getPollResults(currentPoll.id))
                .subscribeSuccess { response ->
                    response.results?.let { results ->
                        val updatedPollOptions = currentPoll.options
                                .mapIndexed { index, pollOption ->
                                    pollOption.copy(result = results[index])
                                }

                        val updatedPoll = currentPoll.copy(options = updatedPollOptions)
                        poll.postValue(updatedPoll)
                        pollResults.postValue(results)
                    }
                }.addTo(compositeDisposable)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
    }
}
