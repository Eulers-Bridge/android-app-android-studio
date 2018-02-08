package com.eulersbridge.isegoria.poll;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;

import com.eulersbridge.isegoria.IsegoriaApp;
import com.eulersbridge.isegoria.network.api.models.Poll;
import com.eulersbridge.isegoria.network.api.responses.PollsResponse;
import com.eulersbridge.isegoria.util.data.SingleLiveData;
import com.eulersbridge.isegoria.util.data.RetrofitLiveData;

import java.util.List;

@SuppressWarnings("WeakerAccess")
public class PollsViewModel extends AndroidViewModel {

    private LiveData<List<Poll>> polls;

    public PollsViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<List<Poll>> getPolls() {
        IsegoriaApp app = getApplication();

        if (polls == null || polls.getValue() == null) {
            return Transformations.switchMap(app.loggedInUser, user -> {

                if (user == null || user.institutionId == null)
                    return new SingleLiveData<>(null);

                LiveData<PollsResponse> pollsResponse = new RetrofitLiveData<>(app.getAPI().getPolls(user.institutionId));
                return Transformations.switchMap(pollsResponse, response -> {
                    if (response != null && response.totalPolls > 0) {
                        polls = new SingleLiveData<>(response.polls);
                    } else {
                        polls = new SingleLiveData<>(null);
                    }

                    return polls;
                });
            });
        }

        return polls;
    }
}
