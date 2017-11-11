package com.eulersbridge.isegoria.feed;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.Photo;
import com.eulersbridge.isegoria.network.PhotosResponse;
import com.eulersbridge.isegoria.network.SimpleCallback;

import java.util.List;

import retrofit2.Response;

public class PhotoAlbumFragment extends Fragment {

    private PhotoAdapter adapter;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.photo_album_fragment, container, false);

		long photoAlbumId = getArguments().getLong("albumId");

        adapter = new PhotoAdapter(this);

        RecyclerView photosGridView = rootView.findViewById(R.id.album_photos_grid_view);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 4, LinearLayoutManager.VERTICAL, false);
        photosGridView.setLayoutManager(layoutManager);
        photosGridView.setAdapter(adapter);

        Isegoria isegoria = (Isegoria)getActivity().getApplication();

        isegoria.getAPI().getAlbumPhotos(photoAlbumId).enqueue(new SimpleCallback<PhotosResponse>() {
            @Override
            protected void handleResponse(Response<PhotosResponse> response) {
                PhotosResponse body = response.body();
                if (body != null) {
                    setPhotos(body.photos);
                }
            }
        });

		return rootView;
	}

	private void setPhotos(@NonNull List<Photo> photos) {
        adapter.replaceItems(photos);
        adapter.notifyDataSetChanged();
    }

	/*private void addTableRow(Photo photo) {
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
            /*});

            tr.addView(viewLinearLayout);
            tr.addView(linearLayout);

        } catch(Exception ignored) {

        }
	}*/
}
