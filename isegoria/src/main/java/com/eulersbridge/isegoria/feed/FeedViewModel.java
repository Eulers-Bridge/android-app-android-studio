package com.eulersbridge.isegoria.feed;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import com.eulersbridge.isegoria.IsegoriaApp;

@SuppressWarnings("WeakerAccess")
public class FeedViewModel extends AndroidViewModel {

    public FeedViewModel(@NonNull Application application) {
        super(application);
    }

    void showFriends() {
        IsegoriaApp app = getApplication();
        app.friendsVisible.setValue(true);
    }
}
