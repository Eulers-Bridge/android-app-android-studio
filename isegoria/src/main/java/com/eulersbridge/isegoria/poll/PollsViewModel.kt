package com.eulersbridge.isegoria.poll

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import com.eulersbridge.isegoria.network.api.API
import com.eulersbridge.isegoria.network.api.models.Poll
import com.eulersbridge.isegoria.network.api.models.User
import com.eulersbridge.isegoria.toLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData
import javax.inject.Inject

class PollsViewModel
@Inject constructor(
    private val userData: LiveData<User>,
    private val api: API
) : ViewModel() {

    private var polls: List<Poll>? = null

    internal fun getPolls(): LiveData<List<Poll>?> {
        return polls?.let {
            SingleLiveData<List<Poll>?>(it)

        } ?: Transformations.switchMap<User, List<Poll>>(userData) { user ->
            if (user?.institutionId == null) {
                SingleLiveData(null)

            } else {
                api.getPolls(user.institutionId!!)
                        .map { (polls, totalPolls) ->
                            if (totalPolls > 0) {
                                this.polls = polls
                            }

                            this.polls
                        }.toLiveData()
            }
        }
    }
}
