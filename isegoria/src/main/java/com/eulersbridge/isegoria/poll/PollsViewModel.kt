package com.eulersbridge.isegoria.poll

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.network.NetworkService
import com.eulersbridge.isegoria.network.api.models.Poll
import com.eulersbridge.isegoria.network.api.models.User
import com.eulersbridge.isegoria.util.data.RetrofitLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData
import javax.inject.Inject

class PollsViewModel
@Inject constructor(
    private val app: IsegoriaApp,
    private val networkService: NetworkService
) : ViewModel() {

    private var polls: LiveData<List<Poll>?>? = null

    internal fun getPolls(): LiveData<List<Poll>?> {
        return if (polls?.value == null) {
            Transformations.switchMap<User, List<Poll>>(app.loggedInUser) { user ->
                return@switchMap if (user?.institutionId == null) {
                    SingleLiveData(null)

                } else {
                    val pollsResponse = RetrofitLiveData(networkService.api.getPolls(user.institutionId!!))

                    Transformations.switchMap(pollsResponse) { response ->
                        polls = if (response != null && response.totalPolls > 0) {
                            SingleLiveData(response.polls)

                        } else {
                            SingleLiveData(null)
                        }

                        polls
                    }
                }
            }
        } else polls!!
    }
}
