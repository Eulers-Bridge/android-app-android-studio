package com.eulersbridge.isegoria.feed.photos;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.IsegoriaApp;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.util.ui.TitledFragment;
import com.eulersbridge.isegoria.network.api.models.User;
import com.eulersbridge.isegoria.network.api.responses.NewsFeedResponse;
import com.eulersbridge.isegoria.util.network.SimpleCallback;

import retrofit2.Response;

public class PhotosFragment extends Fragment implements TitledFragment {

    private IsegoriaApp isegoriaApp = null;

    private final PhotoAlbumAdapter adapter = new PhotoAlbumAdapter(this);
    private SwipeRefreshLayout refreshLayout;

    private PhotoAlbumsViewModel viewModel;

    private boolean fetchedPhotos = false;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.photos_fragment, container, false);

		viewModel = ViewModelProviders.of(this).get(PhotoAlbumsViewModel.class);

		if (getActivity() != null)
            isegoriaApp = (IsegoriaApp)getActivity().getApplication();

        refreshLayout = rootView.findViewById(R.id.photos_refresh_layout);
        refreshLayout.setOnRefreshListener(() -> {
            refreshLayout.setRefreshing(true);

            refresh();

            refreshLayout.postDelayed(() -> refreshLayout.setRefreshing(false), 6000);
        });

        RecyclerView photoAlbumsListView = rootView.findViewById(R.id.photo_albums_list_view);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(photoAlbumsListView.getContext(), LinearLayoutManager.VERTICAL);
        photoAlbumsListView.addItemDecoration(dividerItemDecoration);

        photoAlbumsListView.setAdapter(adapter);

        refresh();

		return rootView;
	}

    @Override
    public String getTitle(Context context) {
        return "Photos";
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (getView() != null && isegoriaApp != null && !fetchedPhotos)
            refresh();
    }

    private void refresh() {
        fetchedPhotos = true;

        User user = isegoriaApp.loggedInUser.getValue();
        if (user != null && user.institutionId != null) {

            long newsFeedId = user.getNewsFeedId();
            if (newsFeedId == 0) {
                isegoriaApp.getAPI().getInstitutionNewsFeed(user.institutionId).enqueue(new SimpleCallback<NewsFeedResponse>() {
                    @Override
                    protected void handleResponse(Response<NewsFeedResponse> response) {
                        NewsFeedResponse body = response.body();

                        if (body != null) {
                            User updatedUser = new User(user);
                            updatedUser.setNewsFeedId(body.newsFeedId);
                            isegoriaApp.updateLoggedInUser(updatedUser);

                            fetchPhotoAlbums();
                        }
                    }
                });

            } else {
                fetchPhotoAlbums();
            }
        }
	}

	private void fetchPhotoAlbums() {
        viewModel.getPhotoAlbums().observe(this, albums -> {
            if (albums != null) {
                adapter.setLoading(false);
                adapter.replaceItems(albums);
            }
        });
    }
}
