package com.eulersbridge.isegoria.feed;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.models.UserProfile;
import com.eulersbridge.isegoria.network.IgnoredCallback;
import com.eulersbridge.isegoria.network.LikedResponse;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.Photo;
import com.eulersbridge.isegoria.utilities.TimeConverter;

import org.parceler.Parcels;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhotoViewFragment extends Fragment {
	private View rootView;

	private ImageView photoView;
    private ImageView photoStar;
    private TextView photoLikes;

	private DisplayMetrics displayMetrics;

    private long photoId;

    private boolean setLiked = false;
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.photo_view_fragment, container, false);

		Bundle bundle = this.getArguments();
		photoId = bundle.getInt("PhotoId");
        Photo photo = Parcels.unwrap(bundle.getParcelable("photo"));

        Isegoria isegoria = (Isegoria)getActivity().getApplication();
        UserProfile loggedInUser = isegoria.getLoggedInUser();

        displayMetrics = getActivity().getResources().getDisplayMetrics();

        photoView = rootView.findViewById(R.id.photoView);
        photoStar = rootView.findViewById(R.id.photoFlag);
        photoLikes = rootView.findViewById(R.id.photoLikes);

        setupPhotoView();

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

        if (photo == null) {
            isegoria.getAPI().getPhoto(photoId).enqueue(new Callback<Photo>() {
                @Override
                public void onResponse(Call<Photo> call, Response<Photo> response) {
                    Photo photo = response.body();
                    if (photo != null) {
                        populatePhotoData(photo.title, photo.dateTimestamp, photo.hasInappropriateContent, photo.likeCount);
                    }
                }

                @Override
                public void onFailure(Call<Photo> call, Throwable t) {
                    t.printStackTrace();
                }
            });

            isegoria.getAPI().getPhotoLiked(photoId, loggedInUser.email).enqueue(new Callback<LikedResponse>() {
                @Override
                public void onResponse(Call<LikedResponse> call, Response<LikedResponse> response) {
                    LikedResponse  likedResponse = response.body();
                    if (likedResponse != null && likedResponse.liked) {
                        initiallyLiked();
                    }
                }

                @Override
                public void onFailure(Call<LikedResponse> call, Throwable t) {
                    t.printStackTrace();
                }
            });

        } else {
            populatePhotoData(photo.title, photo.dateTimestamp, photo.hasInappropriateContent, photo.likeCount);

            GlideApp.with(this).load(photo.thumbnailUrl).into(photoView);
        }

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
    private void populatePhotoData(String title, long date,
                        final boolean inappropriateContent, int numOfLikes) {
        String dateStr = TimeConverter.convertTimestampToString(date);

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
	
	private void setupPhotoView() {
        getActivity().runOnUiThread(() -> {

            try {
                photoView.setScaleType(ScaleType.CENTER_CROP);
                photoView.getLayoutParams().width = displayMetrics.widthPixels;
                photoView.getLayoutParams().height = (int) (displayMetrics.heightPixels / 2.5);
                photoView.setPadding(0, 0, 0, (displayMetrics.heightPixels / 20));

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
	}
}
