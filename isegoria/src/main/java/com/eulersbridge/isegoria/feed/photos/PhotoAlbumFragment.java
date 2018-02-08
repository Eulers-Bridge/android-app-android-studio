package com.eulersbridge.isegoria.feed.photos;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.IsegoriaApp;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.network.api.models.Photo;
import com.eulersbridge.isegoria.network.api.models.PhotoAlbum;
import com.eulersbridge.isegoria.network.api.responses.PhotosResponse;
import com.eulersbridge.isegoria.util.Constants;
import com.eulersbridge.isegoria.util.network.SimpleCallback;

import org.parceler.Parcels;

import java.util.List;

import retrofit2.Response;

public class PhotoAlbumFragment extends Fragment {

    private PhotoAdapter adapter;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.photo_album_fragment, container, false);

        adapter = new PhotoAdapter();

        RecyclerView photosGridView = rootView.findViewById(R.id.album_photos_grid_view);
        photosGridView.setAdapter(adapter);

        if (getActivity() != null && getArguments() != null) {
            IsegoriaApp app = (IsegoriaApp)getActivity().getApplication();

            PhotoAlbum album = Parcels.unwrap(getArguments().getParcelable(Constants.FRAGMENT_EXTRA_PHOTO_ALBUM));

            TextView titleTextView = rootView.findViewById(R.id.album_title_text_view);
            titleTextView.setText(album.name);

            TextView descriptionTextView = rootView.findViewById(R.id.album_description_text_view);
            descriptionTextView.setText(album.description);

            ImageView albumImageView = rootView.findViewById(R.id.album_thumbnail_image_view);
            GlideApp.with(this)
                    .load(album.thumbnailPhotoUrl)
                    .placeholder(R.color.lightGrey)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(albumImageView);

            app.getAPI().getAlbumPhotos(album.id).enqueue(new SimpleCallback<PhotosResponse>() {
                @Override
                protected void handleResponse(Response<PhotosResponse> response) {
                    PhotosResponse body = response.body();

                    if (body != null)
                        setPhotos(body.photos);
                }
            });
        }

		return rootView;
	}

	private void setPhotos(@NonNull List<Photo> photos) {
	    adapter.setLoading(false);
        adapter.replaceItems(photos);
    }
}
