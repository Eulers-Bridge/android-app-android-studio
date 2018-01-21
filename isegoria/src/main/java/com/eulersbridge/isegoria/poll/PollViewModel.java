package com.eulersbridge.isegoria.poll;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.eulersbridge.isegoria.IsegoriaApp;
import com.eulersbridge.isegoria.network.api.API;
import com.eulersbridge.isegoria.network.api.models.Contact;
import com.eulersbridge.isegoria.network.api.models.Poll;
import com.eulersbridge.isegoria.network.api.models.PollOption;
import com.eulersbridge.isegoria.network.api.models.PollResult;
import com.eulersbridge.isegoria.network.api.responses.PollResultsResponse;
import com.eulersbridge.isegoria.util.data.SingleLiveData;
import com.eulersbridge.isegoria.util.data.RetrofitLiveData;
import com.eulersbridge.isegoria.util.network.SimpleCallback;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings("WeakerAccess")
public class PollViewModel extends AndroidViewModel {

    final MutableLiveData<Poll> poll = new MutableLiveData<>();
    final MutableLiveData<List<PollResult>> pollResults = new MutableLiveData<>();

    final LiveData<Contact> pollCreator = Transformations.switchMap(poll, thePoll -> {
        if (thePoll != null) {
            if (thePoll.creator == null && !TextUtils.isEmpty(thePoll.creatorEmail)) {
                IsegoriaApp isegoriaApp = getApplication();
                return new RetrofitLiveData<>(isegoriaApp.getAPI().getContact(thePoll.creatorEmail));

            } else if (thePoll.creator != null) {
                return new SingleLiveData<>(thePoll.creator);
            }
        }

        return new SingleLiveData<>(null);
    });

    public PollViewModel(@NonNull Application application) {
        super(application);
    }

    void setPoll(Poll poll) {
        this.poll.setValue(poll);
    }

    void voteForPollOption(int optionIndex) {
        Poll currentPoll = poll.getValue();
        if (currentPoll == null) return;

        PollOption option = currentPoll.options.get(optionIndex);

        IsegoriaApp isegoriaApp = getApplication();
        API api = isegoriaApp.getAPI();
        api.answerPoll(currentPoll.id, option.id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // After voting, fetch poll results

                api.getPollResults(currentPoll.id).enqueue(new SimpleCallback<PollResultsResponse>() {
                    @Override
                    protected void handleResponse(Response<PollResultsResponse> response) {
                        PollResultsResponse body = response.body();
                        if (body != null && body.results != null) {
                            List<PollResult> results = body.results;

                            Poll updatedPoll = new Poll(currentPoll);

                            int resultsCount = results.size();
                            for (int i = 0; i < resultsCount; i++) {
                                PollOption pollOption = updatedPoll.options.get(i);
                                pollOption.setResult(results.get(i));
                            }

                            poll.setValue(updatedPoll);
                            pollResults.setValue(body.results);
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @Override
    protected void onCleared() {
        if (pollCreator instanceof RetrofitLiveData)
            ((RetrofitLiveData) pollCreator).cancel();
    }
}
