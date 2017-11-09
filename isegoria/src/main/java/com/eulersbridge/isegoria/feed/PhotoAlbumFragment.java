package com.eulersbridge.isegoria.feed;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.Photo;
import com.eulersbridge.isegoria.network.PhotosResponse;

import org.parceler.Parcels;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhotoAlbumFragment extends Fragment {
    private TableLayout photosAlbumTableLayout;
	private TableRow tr;

	private int photosPerRow = -1;
	private int fitPerRow = 0;
	private int squareSize;
	private int dividerPadding = 0;

	private boolean insertedFirstRow = false;

    private PhotoViewPagerFragment photoViewPagerFragment;

	public PhotoAlbumFragment() {
		insertedFirstRow = false;
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.photo_album_fragment, container, false);

		int photoAlbumId = getArguments().getInt("albumId");

		DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
		photosAlbumTableLayout = rootView.findViewById(R.id.photosAlbumTableLayout);

        squareSize = displayMetrics.widthPixels / 4 - (10/4);
        fitPerRow = 4;
        dividerPadding = (10/4);

        tr = new TableRow(getActivity());

        photoViewPagerFragment = new PhotoViewPagerFragment();

        Isegoria isegoria = (Isegoria)getActivity().getApplication();

        isegoria.getAPI().getAlbumPhotos(photoAlbumId).enqueue(new Callback<PhotosResponse>() {
            @Override
            public void onResponse(Call<PhotosResponse> call, Response<PhotosResponse> response) {
                PhotosResponse body = response.body();
                if (body != null) {
                    addPhotoThumbs(body.photos);
                }
            }

            @Override
            public void onFailure(Call<PhotosResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });

		return rootView;
	}

	private void addPhotoThumbs(List<Photo> photos) {
	    if (getActivity() != null && photos.size() > 0) {
	        getActivity().runOnUiThread(() -> {
                for (Photo photo : photos)
                    addTableRow(photo);
            });
        }
    }

	private void addTableRow(Photo photo) {
        try {
            int paddingMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    (float) 6.666666667, getResources().getDisplayMetrics());

            photosPerRow = photosPerRow + 1;
            if (photosPerRow == fitPerRow) {
                photosPerRow = 0;
                tr = new TableRow(getActivity());
                if (!insertedFirstRow) {
                    insertedFirstRow = true;
                    tr.setPadding(dividerPadding, dividerPadding, dividerPadding, dividerPadding);
                } else {
                    tr.setPadding(dividerPadding, 0, dividerPadding, dividerPadding);
                }
                photosAlbumTableLayout.addView(tr);
            }

            LinearLayout viewLinearLayout = new LinearLayout(getActivity());
            viewLinearLayout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
            viewLinearLayout.setBackgroundColor(Color.parseColor("#000000"));

            ImageView view = new ImageView(getActivity());
            //view.setColorFilter(Color.argb(125, 35, 35, 35));
            view.setLayoutParams(new LinearLayout.LayoutParams(squareSize, squareSize, 1.0f));
            view.setScaleType(ScaleType.CENTER_CROP);
            view.setBackgroundColor(Color.GRAY);

            viewLinearLayout.addView(view);

            LinearLayout linearLayout = new LinearLayout(getActivity());
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setGravity(Gravity.CENTER_VERTICAL);
            linearLayout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
            linearLayout.setPadding(paddingMargin, 0, 0, 0);

            PhotoViewFragment detailFragment = new PhotoViewFragment();
            Bundle args = new Bundle();
            args.putParcelable("photo", Parcels.wrap(photo));
            detailFragment.setArguments(args);

            final int index = photoViewPagerFragment.addFragment(detailFragment);

            GlideApp.with(this)
                    .load(photo.thumbnailUrl)
                    .into(view);

            view.setOnClickListener(view1 -> {

                photoViewPagerFragment.setPosition(index);

                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.photosFrameLayout, photoViewPagerFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack(null)
                        .commit();

                /*FragmentManager fragmentManager2 = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();

                PhotoViewFragment fragment21 = new PhotoViewFragment();
                Bundle args1 = new Bundle();
                args1.putString("PhotoName", String.valueOf(photo.id));
                //fragment21.setArguments(args1);

                fragmentTransaction2.addToBackStack(null);
                fragmentTransaction2.add(R.id.photosFrameLayout, photoViewPagerFragment);
                photoViewPagerFragment.setPosition(index);
                fragmentTransaction2.commit();*/
            });

            tr.addView(viewLinearLayout);
            tr.addView(linearLayout);

        } catch(Exception ignored) {

        }
	}
}
