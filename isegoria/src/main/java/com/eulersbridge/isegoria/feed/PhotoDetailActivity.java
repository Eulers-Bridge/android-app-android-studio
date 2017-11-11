package com.eulersbridge.isegoria.feed;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.Photo;
import com.eulersbridge.isegoria.network.API;
import com.eulersbridge.isegoria.network.IgnoredCallback;
import com.eulersbridge.isegoria.network.LikedResponse;
import com.eulersbridge.isegoria.utilities.Utils;

import org.parceler.Parcels;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhotoDetailActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    private API api;
    private String loggedInUserEmail;

    private ArrayList<Photo> photos = new ArrayList<>();

    private ViewPager pager;

    private RelativeLayout detailsContainer;
    private TextView titleTextView;
    private TextView dateTextView;
    private TextView likesTextView;
    private ImageView starImageView;
    private ImageView flagImageView;

    private boolean userLikedCurrentPhoto;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.photo_detail_activity);

        photos = Parcels.unwrap(getIntent().getParcelableExtra("photos"));
        int startIndex = getIntent().getIntExtra("position", 0);

        Isegoria isegoria = (Isegoria)getApplication();
        loggedInUserEmail = isegoria.getLoggedInUser().email;

        api = isegoria.getAPI();

        pager = findViewById(R.id.photo_detail_view_pager);

        detailsContainer = findViewById(R.id.photo_detail_details_container);

        titleTextView = findViewById(R.id.photo_detail_title_text_view);
        dateTextView = findViewById(R.id.photo_detail_date_text_view);
        likesTextView = findViewById(R.id.photo_detail_likes_text_view);
        starImageView = findViewById(R.id.photo_detail_star_image_view);
        flagImageView = findViewById(R.id.photo_detail_flag_image_view);

        starImageView.setOnClickListener(view -> {
            userLikedCurrentPhoto = !userLikedCurrentPhoto;

            long photoId = getCurrentPhoto().id;

            if (userLikedCurrentPhoto) {
                api.likePhoto(photoId, loggedInUserEmail).enqueue(new IgnoredCallback<>());

                starImageView.setImageResource(R.drawable.star);

                int likes = Integer.parseInt(String.valueOf(likesTextView.getText())) + 1;
                likesTextView.setText(String.valueOf(likes));
            }
            else {
                api.unlikePhoto(photoId, loggedInUserEmail).enqueue(new IgnoredCallback<>());

                starImageView.setImageResource(R.drawable.stardefault);

                int likes = Integer.parseInt(String.valueOf(likesTextView.getText())) - 1;
                likesTextView.setText(String.valueOf(likes));
            }
        });

        setupPager(startIndex);
    }

    private Photo getCurrentPhoto() {
        return photos.get(pager.getCurrentItem());
    }

    private void setupPager(int startIndex) {
        PagerAdapter pagerAdapter = new PagerAdapter() {

            @Override
            @NonNull
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                Photo photo = photos.get(position);

                Context context = PhotoDetailActivity.this;

                SubsamplingScaleImageView imageView = new SubsamplingScaleImageView(context);
                imageView.setContentDescription(photo.title);

                ViewPager.LayoutParams layoutParams = new ViewPager.LayoutParams();
                imageView.setLayoutParams(layoutParams);

                GlideApp.with(context)
                        .asBitmap()
                        .load(photo.thumbnailUrl)
                        .placeholder(R.color.black)
                        .override(Target.SIZE_ORIGINAL)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                imageView.setImage(ImageSource.bitmap(resource));
                            }
                        });

                container.addView(imageView);

                return imageView;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView((View)object);
            }

            @Override
            public int getCount() {
                return photos.size();
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }
        };
        pager.setAdapter(pagerAdapter);

        int marginDp = 8;
        float marginPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, marginDp, getResources().getDisplayMetrics());
        pager.setPageMargin((int)marginPixels);

        pager.addOnPageChangeListener(this);

        pager.setCurrentItem(startIndex);
        onPageSelected(startIndex);
    }

    /**
     * When a new 'page' is selected, fetch the corresponding photo and populate text fields
     * and like/flag button states.
     */
    @Override
    public void onPageSelected(int position) {
        Photo photo = getCurrentPhoto();

        userLikedCurrentPhoto = false;

        runOnUiThread(() -> {
            titleTextView.setText(photo.title);

            String dateStr = Utils.convertTimestampToString(this, photo.dateTimestamp);
            dateTextView.setText(dateStr);

            likesTextView.setText(String.valueOf(photo.likeCount));

            checkPhotoLikedByUser(photo.id);

            @DrawableRes int flagImage = photo.hasInappropriateContent? R.drawable.flag : R.drawable.flagdefault;
            flagImageView.setImageResource(flagImage);
        });
    }

    private void checkPhotoLikedByUser(final long photoId) {
        api.getPhotoLiked(photoId, loggedInUserEmail).enqueue(new Callback<LikedResponse>() {
            @Override
            public void onResponse(Call<LikedResponse> call, Response<LikedResponse> response) {
                if (response.isSuccessful()) {
                    LikedResponse likedResponse = response.body();
                    if (likedResponse != null && likedResponse.liked && photoId == getCurrentPhoto().id) {
                        runOnUiThread(() -> {
                            userLikedCurrentPhoto = true;
                            starImageView.setImageResource(R.drawable.star);
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<LikedResponse> call, Throwable t) {
                // Ignored (404 = user has not liked)
            }
        });
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

    @Override
    public void onPageScrollStateChanged(int state) {

        boolean dragging = (state == ViewPager.SCROLL_STATE_DRAGGING);
        long durationMillis = dragging? 100 : 250;
        float alpha = dragging? 0.3f : 1.0f;

        detailsContainer.animate().setDuration(durationMillis).alpha(alpha);
    }
}