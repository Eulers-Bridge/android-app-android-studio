package com.eulersbridge.isegoria.election;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;

import com.eulersbridge.isegoria.IsegoriaApp;
import com.eulersbridge.isegoria.network.api.models.Election;
import com.eulersbridge.isegoria.util.data.FixedData;
import com.eulersbridge.isegoria.util.data.RetrofitLiveData;

import java.util.List;

@SuppressWarnings("WeakerAccess")
public class ElectionViewModel extends AndroidViewModel {

    private LiveData<Election> election;

    public ElectionViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<Boolean> userCompletedEfficacyQuestions() {
        IsegoriaApp isegoriaApp = getApplication();
        return Transformations.switchMap(isegoriaApp.loggedInUser, user ->
                new FixedData<>(user != null && user.hasPPSEQuestions)
        );
    }

    LiveData<Election> getElection() {
        if (election != null)
            return election;

        IsegoriaApp isegoriaApp = getApplication();
        return Transformations.switchMap(isegoriaApp.loggedInUser, user -> {
            if (user != null && user.institutionId != null) {
                LiveData<List<Election>> electionsList = new RetrofitLiveData<>(isegoriaApp.getAPI().getElections(user.institutionId));

                election = Transformations.switchMap(electionsList, elections -> {
                    if (elections != null && elections.size() > 0) {
                        return new FixedData<>(elections.get(0));

                    } else {
                        return new FixedData<>(null);
                    }
                });

                return election;
            }

            return new FixedData<>(null);
        });
    }
}
