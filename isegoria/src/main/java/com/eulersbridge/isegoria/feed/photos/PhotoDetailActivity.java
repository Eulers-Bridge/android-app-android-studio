package com.eulersbridge.isegoria.feed.photos;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Priority;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.network.api.models.Photo;
import com.eulersbridge.isegoria.util.Constants;
import com.eulersbridge.isegoria.util.Strings;

import org.parceler.Parcels;

import java.util.ArrayList;

public class PhotoDetailActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    private ViewPager pager;

    private ConstraintLayout detailsContainer;
    private TextView titleTextView;
    private TextView dateTextView;
    private TextView likesTextView;
    private ImageView starImageView;
    private ImageView flagImageView;

    private boolean userLikedCurrentPhoto;

    private PhotoDetailViewModel viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.photo_detail_activity);

        viewModel = ViewModelProviders.of(this).get(PhotoDetailViewModel.class);
        setupModelObservers();

        ArrayList<Photo> photos = Parcels.unwrap(getIntent().getParcelableExtra(Constants.ACTIVITY_EXTRA_PHOTOS));
        int startIndex = getIntent().getIntExtra(Constants.ACTIVITY_EXTRA_PHOTOS_POSITION, 0);

        viewModel.setPhotos(photos, startIndex);

        pager = findViewById(R.id.photo_detail_view_pager);

        detailsContainer = findViewById(R.id.photo_detail_details_container);

        titleTextView = findViewById(R.id.photo_detail_title_text_view);
        dateTextView = findViewById(R.id.photo_detail_date_text_view);
        likesTextView = findViewById(R.id.photo_detail_likes_text_view);
        starImageView = findViewById(R.id.photo_detail_star_image_view);
        flagImageView = findViewById(R.id.photo_detail_flag_image_view);

        starImageView.setOnClickListener(view -> {
            userLikedCurrentPhoto = !userLikedCurrentPhoto;

            view.setEnabled(false);

            if (userLikedCurrentPhoto) {
                viewModel.likePhoto().observe(this, success -> {
                    view.setEnabled(true);

                    if (success != null && success) {
                        starImageView.setColorFilter(ContextCompat.getColor(this, R.color.star_active));

                        int likes = Integer.parseInt(String.valueOf(likesTextView.getText())) + 1;
                        likesTextView.setText(String.valueOf(likes));
                    }
                });
            }
            else {
                viewModel.unlikePhoto().observe(this, success -> {
                    view.setEnabled(true);

                    if (success != null && success) {
                        starImageView.setColorFilter(null);

                        int likes = Integer.parseInt(String.valueOf(likesTextView.getText())) - 1;
                        likesTextView.setText(String.valueOf(likes));
                    }
                });
            }
        });

        setupPager(startIndex);
    }

    private void setupModelObservers() {
        viewModel.currentPhoto.observe(this, photo -> {
            userLikedCurrentPhoto = false;

            runOnUiThread(() -> {
                if (photo != null) {
                    titleTextView.setText(photo.title);

                    String dateStr = Strings.fromTimestamp(this, photo.dateTimestamp);
                    dateTextView.setText(dateStr.toUpperCase());

                    likesTextView.setText(String.valueOf(photo.likeCount));

                    @DrawableRes int flagImage = photo.hasInappropriateContent? R.drawable.flag : R.drawable.flagdefault;
                    flagImageView.setImageResource(flagImage);
                }
            });
        });

        viewModel.photoLikes.observe(this, likes -> {
            if (likes != null)
                runOnUiThread(() -> likesTextView.setText(String.valueOf(likes.size())));
        });

        viewModel.photoLikedByUser.observe(this, likedByUser -> {
            if (likedByUser != null && likedByUser) {
                runOnUiThread(() -> {
                    userLikedCurrentPhoto = true;
                    starImageView.setImageResource(R.drawable.star);
                });
            }
        });
    }

    private void setupPager(int startIndex) {
        PagerAdapter pagerAdapter = new PagerAdapter() {

            @Override
            @NonNull
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                Photo photo = viewModel.currentPhoto.getValue();

                Context context = PhotoDetailActivity.this;

                SubsamplingScaleImageView imageView = new SubsamplingScaleImageView(context);

                ViewPager.LayoutParams layoutParams = new ViewPager.LayoutParams();
                imageView.setLayoutParams(layoutParams);

                if (photo != null) {
                    GlideApp.with(context)
                            .asBitmap()
                            .load(photo.thumbnailUrl)
                            .priority(Priority.HIGH)
                            .placeholder(R.color.black)
                            .override(Target.SIZE_ORIGINAL)
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                                    imageView.setImage(ImageSource.bitmap(resource));
                                }
                            });
                }

                container.addView(imageView);

                return imageView;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView((View)object);
            }

            @Override
            public int getCount() {
                return viewModel.getPhotoCount();
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }
        };
        pager.setAdapter(pagerAdapter);

        final int marginDp = 8;
        final int marginPixels = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, marginDp, getResources().getDisplayMetrics()));
        pager.setPageMargin(marginPixels);

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
        viewModel.changePhoto(position);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

    @Override
    public void onPageScrollStateChanged(int state) {

        final boolean dragging = (state == ViewPager.SCROLL_STATE_DRAGGING);
        final long durationMillis = dragging? 100 : 250;
        final float alpha = dragging? 0.3f : 1.0f;

        detailsContainer.animate().setDuration(durationMillis).alpha(alpha);
    }
}