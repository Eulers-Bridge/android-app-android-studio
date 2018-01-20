package com.eulersbridge.isegoria.feed.photos;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.eulersbridge.isegoria.IsegoriaApp;
import com.eulersbridge.isegoria.network.api.models.Like;
import com.eulersbridge.isegoria.network.api.models.Photo;
import com.eulersbridge.isegoria.network.api.models.User;
import com.eulersbridge.isegoria.network.api.responses.LikeResponse;
import com.eulersbridge.isegoria.util.data.RetrofitLiveData;
import com.eulersbridge.isegoria.util.data.FixedData;

import java.util.List;

@SuppressWarnings("WeakerAccess")
public class PhotoDetailViewModel extends AndroidViewModel {

    private List<Photo> photos;
    private final MutableLiveData<Integer> visibleIndex = new MutableLiveData<>();

    final LiveData<Photo> currentPhoto = Transformations.switchMap(visibleIndex, index -> {
        if (photos != null && index >= 0 && index < photos.size()) {
            return new FixedData<>(photos.get(index));
        } else {
            return new FixedData<>(null);
        }
    });

    final LiveData<List<Like>> photoLikes = Transformations.switchMap(currentPhoto, photo -> {
        IsegoriaApp isegoriaApp = getApplication();

        final long photoId = photo.id;

        LiveData<List<Like>> photoLikes = new RetrofitLiveData<>(isegoriaApp.getAPI().getPhotoLikes(photoId));

        return Transformations.switchMap(photoLikes, likes -> {
            if (currentPhoto.getValue() != null && photoId == currentPhoto.getValue().id) {
                return new FixedData<>(likes);

            } else {
                return new FixedData<>(null);
            }
        });
    });

    final LiveData<Boolean> photoLikedByUser = Transformations.switchMap(photoLikes, likes -> {
        if (likes != null) {
            IsegoriaApp isegoriaApp = getApplication();
            User user = isegoriaApp.loggedInUser.getValue();

            if (user != null)
                for (Like like : likes)
                    if (like.email.equals(user.email))
                        return new FixedData<>(true);
        }

        return new FixedData<>(false);
    });

    public PhotoDetailViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected void onCleared() {
        if (photoLikes instanceof RetrofitLiveData)
            ((RetrofitLiveData) photoLikes).cancel();
    }

    void changePhoto(@IntRange(from = 0) int position) {

        /* Cancel fetching likes for old photo if started,
            but give like/unlike requests a chance to finish */
        if (photoLikes instanceof RetrofitLiveData)
            ((RetrofitLiveData) photoLikes).cancel();


        visibleIndex.setValue(position);
    }

    void setPhotos(List<Photo> photos, int startIndex) {
        this.photos = photos;
        visibleIndex.setValue(startIndex);
    }

    int getPhotoCount() {
        return (photos == null)? 0 : photos.size();
    }

    /**
     * @return Boolean.TRUE on success, Boolean.FALSE on failure
     */
    LiveData<Boolean> likePhoto() {
        final IsegoriaApp isegoriaApp = getApplication();

        final User user = isegoriaApp.loggedInUser.getValue();

        if (user != null) {
            return Transformations.switchMap(currentPhoto, article -> {
                LiveData<LikeResponse> newsArticleLike = new RetrofitLiveData<>(isegoriaApp.getAPI().likePhoto(article.id, user.email));

                return Transformations.switchMap(newsArticleLike,
                        likeResponse -> new FixedData<>(likeResponse != null && likeResponse.success));
            });
        }

        return new FixedData<>(false);
    }

    /**
     * @return Boolean.TRUE on success, Boolean.FALSE on failure
     */
    LiveData<Boolean> unlikePhoto() {
        IsegoriaApp isegoriaApp = getApplication();

        User user = isegoriaApp.loggedInUser.getValue();

        if (user != null) {
            return Transformations.switchMap(currentPhoto,
                    photo -> {
                        LiveData<Void> unlike = new RetrofitLiveData<>(isegoriaApp.getAPI().unlikePhoto(photo.id, user.email));

                        return Transformations.switchMap(unlike, __ -> new FixedData<>(true));
                    });
        }

        return new FixedData<>(false);
    }
}
