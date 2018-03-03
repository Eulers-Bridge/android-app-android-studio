package com.eulersbridge.isegoria.poll

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.network.api.models.Poll
import com.eulersbridge.isegoria.network.api.models.User
import com.eulersbridge.isegoria.util.data.RetrofitLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData

class PollsViewModel(application: Application) : AndroidViewModel(application) {

    private var polls: LiveData<List<Poll>?>? = null

    internal fun getPolls(): LiveData<List<Poll>?> {
        val app = getApplication<IsegoriaApp>()

        return if (polls?.value == null) {
            Transformations.switchMap<User, List<Poll>>(app.loggedInUser) { user ->

                if (user?.institutionId == null)
                    return@switchMap SingleLiveData(null)

                val pollsResponse = RetrofitLiveData(app.api.getPolls(user.institutionId!!))

                Transformations.switchMap(pollsResponse) { response ->
                    polls = if (response != null && response.totalPolls > 0) {
                        SingleLiveData(response.polls)

                    } else {
                        SingleLiveData(null)
                    }

                    polls
                }
            }
        } else polls!!
    }
}
