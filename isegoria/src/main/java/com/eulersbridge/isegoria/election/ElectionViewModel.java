package com.eulersbridge.isegoria.election;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;

import com.eulersbridge.isegoria.IsegoriaApp;
import com.eulersbridge.isegoria.network.api.models.Election;
import com.eulersbridge.isegoria.util.data.SingleLiveData;
import com.eulersbridge.isegoria.util.data.RetrofitLiveData;

import java.util.List;

@SuppressWarnings("WeakerAccess")
public class ElectionViewModel extends AndroidViewModel {

    private LiveData<Election> election;

    public ElectionViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<Boolean> userCompletedEfficacyQuestions() {
        IsegoriaApp app = getApplication();
        return Transformations.switchMap(app.loggedInUser, user ->
                new SingleLiveData<>(user != null && user.hasPPSEQuestions)
        );
    }

    LiveData<Election> getElection() {
        if (election != null)
            return election;

        IsegoriaApp app = getApplication();
        return Transformations.switchMap(app.loggedInUser, user -> {
            if (user != null && user.institutionId != null) {
                LiveData<List<Election>> electionsList = new RetrofitLiveData<>(app.getAPI().getElections(user.institutionId));

                election = Transformations.switchMap(electionsList, elections -> {
                    if (elections != null && elections.size() > 0) {
                        return new SingleLiveData<>(elections.get(0));

                    } else {
                        return new SingleLiveData<>(null);
                    }
                });

                return election;
            }

            return new SingleLiveData<>(null);
        });
    }
}
