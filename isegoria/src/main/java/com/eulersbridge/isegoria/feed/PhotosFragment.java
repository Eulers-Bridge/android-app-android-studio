package com.eulersbridge.isegoria.feed;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
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
import android.widget.TextView;

import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.PhotoAlbum;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhotosFragment extends Fragment {
	private TableLayout photosTableLayout;

	private boolean insertedFirstRow = false;
    private android.support.v4.widget.SwipeRefreshLayout swipeContainerPhotos;

	public PhotosFragment() {
		insertedFirstRow = false;
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.photos_fragment, container, false);

		photosTableLayout = rootView.findViewById(R.id.photosTableLayout);

		Isegoria isegoria = (Isegoria)getActivity().getApplication();

		refreshPhotoAlbums(isegoria);

        swipeContainerPhotos = rootView.findViewById(R.id.swipeContainerPhotos);
        swipeContainerPhotos.setOnRefreshListener(() -> {
            swipeContainerPhotos.setRefreshing(true);

            clearTable();

			refreshPhotoAlbums(isegoria);

            new android.os.Handler().postDelayed(() -> {
                insertedFirstRow = false;
                swipeContainerPhotos.setRefreshing(false);
            }, 7000);
        });

		return rootView;
	}

	private void refreshPhotoAlbums(Isegoria isegoria) {
		isegoria.getAPI().getPhotoAlbums(isegoria.getLoggedInUser().getNewsFeedId()).enqueue(callback);
	}

	private final Callback<List<PhotoAlbum>> callback = new Callback<List<PhotoAlbum>>() {
		@Override
		public void onResponse(Call<List<PhotoAlbum>> call, Response<List<PhotoAlbum>> response) {
			List<PhotoAlbum> albums = response.body();
			addPhotoAlbums(albums);
		}

		@Override
		public void onFailure(Call<List<PhotoAlbum>> call, Throwable t) {
			t.printStackTrace();
		}
	};

    private void clearTable() {
        photosTableLayout.removeAllViews();
    }

	private void addPhotoAlbums(List<PhotoAlbum> albums) {
    	if (getActivity() != null && albums != null && albums.size() > 0) {
    		getActivity().runOnUiThread(() -> {
                for (PhotoAlbum album : albums) {
                    addTableRow(album.id, album.name, album.description, album.thumbnailPhotoUrl);
                }
            });
		}
	}

	@UiThread
	private void addTableRow(final int albumId, String label, String caption, String bitmap) {
		try {
			TableRow tr = new TableRow(getActivity());

            int paddingMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    (float) 6.666666667, getResources().getDisplayMetrics());
            int imageSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    (float) 66.66666667, getResources().getDisplayMetrics());

			if(!insertedFirstRow) {
				insertedFirstRow = true;
				tr.setPadding(paddingMargin, paddingMargin, 0, paddingMargin);
			}
			else {
				tr.setPadding(paddingMargin, 0, 0, paddingMargin);
			}
			TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
			tr.setLayoutParams(rowParams);
			
			ImageView view = new ImageView(getActivity());
			view.setLayoutParams(new TableRow.LayoutParams(imageSize, imageSize));
			view.setScaleType(ScaleType.CENTER_CROP);
            view.setBackgroundColor(Color.GRAY);
	        LinearLayout linearLayout = new LinearLayout(getActivity());
			linearLayout.setOrientation(LinearLayout.VERTICAL);
			linearLayout.setGravity(Gravity.CENTER_VERTICAL);
			linearLayout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
			linearLayout.setPadding(paddingMargin, 0, 0, 0);

			GlideApp.with(this)
					.load(bitmap)
					.into(view);
	        
	        final TextView textViewArticle = new TextView(getActivity());
	        textViewArticle.setTextColor(Color.parseColor("#000000"));
	        textViewArticle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18.0f);
	        textViewArticle.setText(label);
	        textViewArticle.setGravity(Gravity.START);

	        textViewArticle.setOnClickListener(view12 -> {
				PhotoAlbumFragment albumFragment = new PhotoAlbumFragment();
				Bundle args = new Bundle();
				args.putInt("albumId", albumId);
				albumFragment.setArguments(args);

				getActivity().getSupportFragmentManager()
						.beginTransaction()
						.addToBackStack(null)
						.add(R.id.photosFrameLayout, albumFragment)
						.commit();
            });
	        
	       view.setOnClickListener(view1 -> {
			   PhotoAlbumFragment albumFragment = new PhotoAlbumFragment();
			   Bundle args = new Bundle();
			   args.putInt("albumId", albumId);
			   albumFragment.setArguments(args);

			   getActivity().getSupportFragmentManager()
					   .beginTransaction()
					   .addToBackStack(null)
					   .add(R.id.photosFrameLayout, albumFragment)
					   .commit();
           });
	        
	        TextView textViewArticleTime = new TextView(getActivity());
	        textViewArticleTime.setTextColor(Color.parseColor("#000000"));
	        textViewArticleTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.0f);
	        textViewArticleTime.setText(caption);
	        textViewArticleTime.setPadding(0, 0, 0, 0);
	        textViewArticleTime.setGravity(Gravity.START);
	        
	        linearLayout.addView(textViewArticle);
	        linearLayout.addView(textViewArticleTime);
	        
	        tr.addView(view);
	        tr.addView(linearLayout);	
	        photosTableLayout.addView(tr);
		} catch(Exception ignored) {
			
		}
	}
}
