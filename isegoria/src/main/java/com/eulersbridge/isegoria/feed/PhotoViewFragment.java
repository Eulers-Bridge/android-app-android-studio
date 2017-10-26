package com.eulersbridge.isegoria.feed;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.eulersbridge.isegoria.utilities.TimeConverter;

public class PhotoViewFragment extends Fragment {
	private View rootView;

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

		displayMetrics = getActivity().getResources().getDisplayMetrics();

        addPhoto("", imageBitmap);

        MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();
        network.getPhoto(this, photoPath);
        network.getPhotoLiked(this);

        photoStar = rootView.findViewById(R.id.photoFlag);
        photoLikes = rootView.findViewById(R.id.photoLikes);

        final ImageView starView = rootView.findViewById(R.id.photoStar);
        starView.setOnClickListener(view -> {
            if(!setLiked) {
                setLiked = true;
                starView.setImageResource(R.drawable.star);
                network.likePhoto(photoPath, PhotoViewFragment.this);
                int likes = Integer.parseInt(String.valueOf(photoLikes.getText()));
                likes = likes + 1;
                photoLikes.setText(String.valueOf(likes));
            }
            else {
                setLiked = false;
                starView.setImageResource(R.drawable.stardefault);
                network.unlikePhoto(photoPath, PhotoViewFragment.this);
                int likes = Integer.parseInt(String.valueOf(photoLikes.getText()));
                likes = likes - 1;
                photoLikes.setText(String.valueOf(likes));
            }
        });

		return rootView;
	}

    public void initiallyLiked() {
        final ImageView starView = rootView.findViewById(R.id.photoStar);
        starView.setImageResource(R.drawable.star);
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

    public void setData(String title, long date,
                        final boolean inappropriateContent, int numOfLikes) {
        String dateStr = TimeConverter.convertTimestampToString(date);

        TextView photoTileTextView = rootView.findViewById(R.id.photoTitle);
        TextView photoDateTextView = rootView.findViewById(R.id.photoDate);
        TextView photoLikesTextView = rootView.findViewById(R.id.photoLikes);
        ImageView flagged = rootView.findViewById(R.id.photoFlag);

        if(!inappropriateContent) {
            flagged.setImageResource(R.drawable.flag);
        }

        photoTileTextView.setText(title);
        photoDateTextView.setText(dateStr);
        photoLikesTextView.setText(String.valueOf(numOfLikes));
    }
	
	private void addPhoto(final String title, final Bitmap bitmap) {
		try {
			getActivity().runOnUiThread(() -> {
                TextView photoTitle = rootView.findViewById(R.id.photoTitle);
                photoTitle.setText(title);
                ImageView photoImageView = rootView.findViewById(R.id.photoView);
                try {
                    photoImageView.setScaleType(ScaleType.CENTER_CROP);
                    photoImageView.setImageBitmap(bitmap);
                    photoImageView.getLayoutParams().width = displayMetrics.widthPixels;
                    photoImageView.getLayoutParams().height = (int) (displayMetrics.heightPixels / 2.5);
                    photoImageView.setPadding(0, 0, 0, (displayMetrics.heightPixels / 20));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
		} catch(Exception ignored) {
			
		}
	}
}
