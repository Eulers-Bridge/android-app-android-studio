package com.eulersbridge.isegoria.feed;

import android.graphics.Bitmap;
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

import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.Network;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.Photo;
import com.eulersbridge.isegoria.utilities.TimeConverter;

public class PhotoViewFragment extends Fragment {
	private View rootView;

	private ImageView photoView;
    private ImageView photoStar;
    private TextView photoLikes;

	private DisplayMetrics displayMetrics;

    private int photoPath;

    private Network network;
    private Bitmap imageBitmap;
    private boolean setLiked = false;
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.photo_view_fragment, container, false);

		Bundle bundle = this.getArguments();
		photoPath = bundle.getInt("PhotoId");
        Photo photo = bundle.getParcelable("photo");

		displayMetrics = getActivity().getResources().getDisplayMetrics();

        photoView = rootView.findViewById(R.id.photoView);
        photoStar = rootView.findViewById(R.id.photoFlag);
        photoLikes = rootView.findViewById(R.id.photoLikes);

        addPhoto("", imageBitmap);

        final ImageView starView = rootView.findViewById(R.id.photoStar);
        starView.setOnClickListener(view -> {
            setLiked = !setLiked;

            if (setLiked) {
                starView.setImageResource(R.drawable.star);
                network.likePhoto(photoPath);
                int likes = Integer.parseInt(String.valueOf(photoLikes.getText())) + 1;
                photoLikes.setText(String.valueOf(likes));
            }
            else {
                starView.setImageResource(R.drawable.stardefault);
                network.unlikePhoto(photoPath);
                int likes = Integer.parseInt(String.valueOf(photoLikes.getText())) - 1;
                photoLikes.setText(String.valueOf(likes));
            }
        });

        MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();

        if (photo == null) {
            network.getPhoto(photoPath, new Network.PhotoListener() {
                @Override
                public void onFetchSuccess(Photo photo) {
                    populatePhotoData(photo.getTitle(), photo.getDateTimestamp(), photo.hasInappropriateContent(), photo.getLikeCount());
                }

                @Override
                public void onFetchFailure(long photoId, Exception e) {}
            });

            network.getPhotoLiked(photoPath, new Network.PhotoLikedListener() {
                @Override
                public void onFetchSuccess(long photoId, boolean liked) {
                    if (liked) initiallyLiked();
                }

                @Override
                public void onFetchFailure(Exception e) {}
            });

        } else {
            populatePhotoData(photo.getTitle(), photo.getDateTimestamp(), photo.hasInappropriateContent(), photo.getLikeCount());
        }

		return rootView;
	}

    public void initiallyLiked() {
	    if (getActivity() != null) {
	        getActivity().runOnUiThread(() -> {
                final ImageView starView = rootView.findViewById(R.id.photoStar);
                starView.setImageResource(R.drawable.star);
            });
        }
    }

    public boolean isSetLiked() {
        return setLiked;
    }

    public int getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(int photoPath) {
        this.photoPath = photoPath;
    }

    public void setImageBitmap(Bitmap bitmap) {
        this.imageBitmap = bitmap;
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
	
	private void addPhoto(final String title, final Bitmap bitmap) {
        getActivity().runOnUiThread(() -> {
            TextView photoTitle = rootView.findViewById(R.id.photoTitle);
            photoTitle.setText(title);

            try {
                photoView.setScaleType(ScaleType.CENTER_CROP);
                photoView.setImageBitmap(bitmap);
                photoView.getLayoutParams().width = displayMetrics.widthPixels;
                photoView.getLayoutParams().height = (int) (displayMetrics.heightPixels / 2.5);
                photoView.setPadding(0, 0, 0, (displayMetrics.heightPixels / 20));
                photoView.setContentDescription(title);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
	}
}
