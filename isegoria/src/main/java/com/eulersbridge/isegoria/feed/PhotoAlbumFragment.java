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

import com.eulersbridge.isegoria.Constant;
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

		long albumId = getArguments().getLong(Constant.ACTIVITY_EXTRA_PHOTO_ALBUM_ID);

        adapter = new PhotoAdapter(this);

        RecyclerView photosGridView = rootView.findViewById(R.id.album_photos_grid_view);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 4, LinearLayoutManager.VERTICAL, false);
        photosGridView.setLayoutManager(layoutManager);
        photosGridView.setAdapter(adapter);

        Isegoria isegoria = (Isegoria)getActivity().getApplication();

        isegoria.getAPI().getAlbumPhotos(albumId).enqueue(new SimpleCallback<PhotosResponse>() {
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
}
