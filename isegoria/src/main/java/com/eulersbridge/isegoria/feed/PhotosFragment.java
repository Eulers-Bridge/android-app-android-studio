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
import com.eulersbridge.isegoria.models.User;
import com.eulersbridge.isegoria.network.NewsFeedResponse;
import com.eulersbridge.isegoria.network.SimpleCallback;

import java.util.List;

import retrofit2.Response;

public class PhotosFragment extends Fragment {

    private Isegoria isegoria;

    private final PhotoAlbumAdapter adapter = new PhotoAlbumAdapter(this);
    private SwipeRefreshLayout refreshLayout;

    private boolean fetchedPhotos = false;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.photos_fragment, container, false);

        isegoria = (Isegoria)getActivity().getApplication();

        refreshLayout = rootView.findViewById(R.id.swipeContainerPhotos);
        refreshLayout.setOnRefreshListener(() -> {
            refreshLayout.setRefreshing(true);

            refresh();

            refreshLayout.postDelayed(() -> refreshLayout.setRefreshing(false), 6000);
        });

        RecyclerView photoAlbumsListView = rootView.findViewById(R.id.photo_albums_list_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        photoAlbumsListView.setLayoutManager(layoutManager);
        photoAlbumsListView.setAdapter(adapter);

        refresh();

		return rootView;
	}

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (getView() != null && isegoria != null && !fetchedPhotos) {
            refresh();
        }
    }

    private void refresh() {
        fetchedPhotos = true;

	    long newsFeedId = isegoria.getLoggedInUser().getNewsFeedId();

        User loggedInUser = isegoria.getLoggedInUser();
        if (newsFeedId == 0) {
            isegoria.getAPI().getInstitutionNewsFeed(loggedInUser.institutionId).enqueue(new SimpleCallback<NewsFeedResponse>() {
                @Override
                protected void handleResponse(Response<NewsFeedResponse> response) {
                    NewsFeedResponse body = response.body();

                    if (body != null) {
                        loggedInUser.setNewsFeedId(body.newsFeedId);

                        fetchPhotoAlbums(loggedInUser);
                    }
                }
            });

        } else {
            fetchPhotoAlbums(loggedInUser);
        }
	}

	private void fetchPhotoAlbums(User loggedInUser) {
        isegoria.getAPI().getPhotoAlbums(loggedInUser.getNewsFeedId()).enqueue(new SimpleCallback<List<PhotoAlbum>>() {
            @Override
            protected void handleResponse(Response<List<PhotoAlbum>> response) {
                if (refreshLayout != null) refreshLayout.post(() -> refreshLayout.setRefreshing(false));

                List<PhotoAlbum> albums = response.body();
                if (albums != null) {
                    addPhotoAlbums(albums);
                }
            }
        });
    }

	private void addPhotoAlbums(@NonNull List<PhotoAlbum> albums) {
        adapter.replaceItems(albums);
        adapter.notifyDataSetChanged();
	}
}
