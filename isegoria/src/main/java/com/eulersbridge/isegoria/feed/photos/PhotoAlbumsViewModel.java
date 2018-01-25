package com.eulersbridge.isegoria.feed.photos;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;

import com.eulersbridge.isegoria.IsegoriaApp;
import com.eulersbridge.isegoria.network.api.models.PhotoAlbum;
import com.eulersbridge.isegoria.util.data.SingleLiveData;
import com.eulersbridge.isegoria.util.data.RetrofitLiveData;

import java.util.List;

@SuppressWarnings("WeakerAccess")
public class PhotoAlbumsViewModel extends AndroidViewModel {

    private LiveData<List<PhotoAlbum>> photoAlbumsList;

    public PhotoAlbumsViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<List<PhotoAlbum>> getPhotoAlbums() {
        IsegoriaApp app = getApplication();

        return Transformations.switchMap(app.loggedInUser, user -> {
            if (user != null) {
                photoAlbumsList = new RetrofitLiveData<>(app.getAPI().getPhotoAlbums(user.getNewsFeedId()));
                return photoAlbumsList;
            }

            return new SingleLiveData<>(null);
        });
    }

    @Override
    protected void onCleared() {
        if (photoAlbumsList != null && photoAlbumsList instanceof RetrofitLiveData)
            ((RetrofitLiveData) photoAlbumsList).cancel();
    }

}
