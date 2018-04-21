package com.eulersbridge.isegoria.poll

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.Poll
import com.eulersbridge.isegoria.util.extension.toLiveData
import javax.inject.Inject

class PollsViewModel
@Inject constructor(private val repository: Repository) : ViewModel() {

    internal fun getPolls(): LiveData<List<Poll>> {
        return repository.getPolls().toLiveData()
    }
}
