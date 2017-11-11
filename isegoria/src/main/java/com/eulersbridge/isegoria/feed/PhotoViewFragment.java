package com.eulersbridge.isegoria.feed;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.models.User;
import com.eulersbridge.isegoria.network.IgnoredCallback;
import com.eulersbridge.isegoria.network.LikedResponse;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.Photo;
import com.eulersbridge.isegoria.utilities.Utils;


import org.parceler.Parcels;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhotoViewFragment extends Fragment {
	private View rootView;

	private SubsamplingScaleImageView photoView;
    private ImageView photoStar;
    private TextView photoLikes;

    private boolean setLiked = false;

    private long photoId;
	
	@SuppressLint("ClickableViewAccessibility")
    @Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.photo_view_fragment, container, false);

		Photo photo = Parcels.unwrap(getArguments().getParcelable("photo"));
        photoId = photo.id;

        Isegoria isegoria = (Isegoria)getActivity().getApplication();
        User loggedInUser = isegoria.getLoggedInUser();

        photoView = rootView.findViewById(R.id.photoView);
        photoStar = rootView.findViewById(R.id.photoFlag);
        photoLikes = rootView.findViewById(R.id.photoLikes);

        final ImageView starView = rootView.findViewById(R.id.photoStar);
        starView.setOnClickListener(view -> {
            setLiked = !setLiked;

            if (setLiked) {
                isegoria.getAPI().likePhoto(photoId, loggedInUser.email).enqueue(new IgnoredCallback<>());

                starView.setImageResource(R.drawable.star);

                int likes = Integer.parseInt(String.valueOf(photoLikes.getText())) + 1;
                photoLikes.setText(String.valueOf(likes));
            }
            else {
                isegoria.getAPI().unlikePhoto(photoId, loggedInUser.email).enqueue(new IgnoredCallback<>());

                starView.setImageResource(R.drawable.stardefault);

                int likes = Integer.parseInt(String.valueOf(photoLikes.getText())) - 1;
                photoLikes.setText(String.valueOf(likes));
            }
        });

        populatePhotoData(photo.title, photo.dateTimestamp, photo.hasInappropriateContent, photo.likeCount);

        GlideApp.with(this)
                .asBitmap()
                .load(photo.thumbnailUrl)
                .override(Target.SIZE_ORIGINAL)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        photoView.setImage(ImageSource.bitmap(resource));
                    }
                });

        isegoria.getAPI().getPhotoLiked(photoId, loggedInUser.email).enqueue(new Callback<LikedResponse>() {
            @Override
            public void onResponse(Call<LikedResponse> call, Response<LikedResponse> response) {
                if (response.isSuccessful()) {
                    LikedResponse likedResponse = response.body();
                    if (likedResponse != null && likedResponse.liked) {
                        initiallyLiked();
                    }
                }
            }

            @Override
            public void onFailure(Call<LikedResponse> call, Throwable t) {
                // Ignored (404 = user has not liked)
            }
        });

		return rootView;
	}

    private void initiallyLiked() {
	    if (getActivity() != null) {
	        getActivity().runOnUiThread(() -> {
                final ImageView starView = rootView.findViewById(R.id.photoStar);
                starView.setImageResource(R.drawable.star);
            });
        }
    }

    @UiThread
    private void populatePhotoData(String title, long date, boolean inappropriateContent,
                                   int numOfLikes) {
        String dateStr = Utils.convertTimestampToString(getContext(), date);

        TextView photoTitleTextView = rootView.findViewById(R.id.photoTitle);
        TextView photoDateTextView = rootView.findViewById(R.id.photoDate);
        TextView photoLikesTextView = rootView.findViewById(R.id.photoLikes);
        ImageView flagged = rootView.findViewById(R.id.photoFlag);

        if (!inappropriateContent) flagged.setImageResource(R.drawable.flag);

        photoTitleTextView.setText(title);
        photoView.setContentDescription(title);

        photoDateTextView.setText(dateStr);
        photoLikesTextView.setText(String.valueOf(numOfLikes));
    }
}
