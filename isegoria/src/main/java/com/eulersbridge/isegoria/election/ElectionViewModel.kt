package com.eulersbridge.isegoria.election

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.Election
import com.eulersbridge.isegoria.util.extension.map
import com.eulersbridge.isegoria.util.extension.toLiveData
import javax.inject.Inject

class ElectionViewModel
@Inject constructor(
        private val repository: Repository
) : ViewModel() {

    internal fun userCompletedEfficacyQuestions()
        = repository.getUser().hasPPSEQuestions

    internal fun getElection(): LiveData<Election?> {
        return repository.getLatestElection().toLiveData().map { it.value }
    }
}
