package com.eulersbridge.isegoria.feed;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.PhotoAlbum;
import com.eulersbridge.isegoria.network.SimpleCallback;

import java.util.List;

import retrofit2.Response;

public class PhotosFragment extends Fragment {

    private Isegoria isegoria;

    private final PhotoAlbumAdapter adapter = new PhotoAlbumAdapter(this);

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.photos_fragment, container, false);

        isegoria = (Isegoria)getActivity().getApplication();

		SwipeRefreshLayout swipeContainerPhotos = rootView.findViewById(R.id.swipeContainerPhotos);
        swipeContainerPhotos.setOnRefreshListener(() -> {
            swipeContainerPhotos.setRefreshing(true);

			refreshPhotoAlbums();

            new android.os.Handler().postDelayed(() -> swipeContainerPhotos.setRefreshing(false), 6000);
        });

        RecyclerView photoAlbumsListView = rootView.findViewById(R.id.photo_albums_list_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        photoAlbumsListView.setLayoutManager(layoutManager);
        photoAlbumsListView.setAdapter(adapter);

        refreshPhotoAlbums();

		return rootView;
	}

    private void refreshPhotoAlbums() {
	    long newsFeedId = isegoria.getLoggedInUser().getNewsFeedId();

	    if (newsFeedId > 0) {
            isegoria.getAPI().getPhotoAlbums(isegoria.getLoggedInUser().getNewsFeedId()).enqueue(new SimpleCallback<List<PhotoAlbum>>() {
                @Override
                protected void handleResponse(Response<List<PhotoAlbum>> response) {
                    List<PhotoAlbum> albums = response.body();
                    if (albums != null) {
                        addPhotoAlbums(albums);
                    }
                }
            });
        }
	}

	private void addPhotoAlbums(@NonNull List<PhotoAlbum> albums) {
        adapter.replaceItems(albums);
        adapter.notifyDataSetChanged();
	}
}
