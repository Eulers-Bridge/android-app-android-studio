package com.eulersbridge.isegoria.election

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import com.eulersbridge.isegoria.network.api.API
import com.eulersbridge.isegoria.network.api.models.Election
import com.eulersbridge.isegoria.network.api.models.User
import com.eulersbridge.isegoria.util.data.RetrofitLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData
import javax.inject.Inject

class ElectionViewModel
@Inject constructor(
    private val user: LiveData<User>,
    private val api: API
) : ViewModel() {

    private var election: LiveData<Election?>? = null

    internal fun userCompletedEfficacyQuestions(): LiveData<Boolean> {
        return Transformations.switchMap<User, Boolean>(user) {
            return@switchMap SingleLiveData(it != null && it.hasPPSEQuestions)
        }
    }

    internal fun getElection(): LiveData<Election?> {
        if (election != null)
            return election!!

        return Transformations.switchMap<User, Election>(user) { user ->

            return@switchMap if (user?.institutionId == null) {
                SingleLiveData(null)

            } else {
                val electionsList = RetrofitLiveData(api.getElections(user.institutionId!!))

                election = Transformations.switchMap(electionsList) election@ {
                    return@election SingleLiveData(it?.firstOrNull())
                }

                election
            }
        }
    }
}
