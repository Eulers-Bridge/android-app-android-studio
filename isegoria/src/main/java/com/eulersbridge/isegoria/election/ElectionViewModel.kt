package com.eulersbridge.isegoria.election

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.network.api.models.Election
import com.eulersbridge.isegoria.network.api.models.User
import com.eulersbridge.isegoria.util.data.RetrofitLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData

class ElectionViewModel(application: Application) : AndroidViewModel(application) {

    private var election: LiveData<Election?>? = null

    internal fun userCompletedEfficacyQuestions(): LiveData<Boolean> {
        val app = getApplication<IsegoriaApp>()

        return Transformations.switchMap<User, Boolean>(app.loggedInUser) {
            return@switchMap SingleLiveData(it != null && it.hasPPSEQuestions)
        }
    }

    internal fun getElection(): LiveData<Election?> {
        if (election != null)
            return election!!

        val app = getApplication<IsegoriaApp>()
        return Transformations.switchMap<User, Election>(app.loggedInUser) { user ->
            if (user?.institutionId != null) {
                val electionsList = RetrofitLiveData(app.api.getElections(user.institutionId!!))

                election = Transformations.switchMap(electionsList) {
                    SingleLiveData(it?.firstOrNull())
                }

                return@switchMap election
            }

            SingleLiveData(null)
        }
    }
}
