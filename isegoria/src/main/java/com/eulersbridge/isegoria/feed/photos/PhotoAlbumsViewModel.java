package com.eulersbridge.isegoria.feed.photos;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.eulersbridge.isegoria.IsegoriaApp;
import com.eulersbridge.isegoria.network.api.models.PhotoAlbum;
import com.eulersbridge.isegoria.network.api.models.User;
import com.eulersbridge.isegoria.util.data.RetrofitLiveData;
import com.eulersbridge.isegoria.util.data.FixedData;

import java.util.List;

@SuppressWarnings("WeakerAccess")
public class PhotoAlbumsViewModel extends AndroidViewModel {

    private LiveData<List<PhotoAlbum>> photoAlbumsList;

    public PhotoAlbumsViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<List<PhotoAlbum>> getPhotoAlbums() {
        IsegoriaApp isegoriaApp = getApplication();

        User user = isegoriaApp.getLoggedInUser();

        if (user != null) {
            photoAlbumsList = new RetrofitLiveData<>(isegoriaApp.getAPI().getPhotoAlbums(user.getNewsFeedId()));
        } else {
            photoAlbumsList = new FixedData<>(null);
        }

        return photoAlbumsList;
    }

    @Override
    protected void onCleared() {
        if (photoAlbumsList != null && photoAlbumsList instanceof RetrofitLiveData)
            ((RetrofitLiveData) photoAlbumsList).cancel();
    }

}
