package com.eulersbridge.isegoria.profile.settings;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.util.transformation.BlurTransformation;
import com.eulersbridge.isegoria.util.transformation.TintTransformation;
import com.theartofdev.edmodo.cropper.CropImage;

public class SettingsActivity extends AppCompatActivity {

    private ImageView backgroundImageView;
    private ImageView photoImageView;

    private Button changePhotoButton;

    private Switch doNotTrackSwitch;
    private Switch optOutDataCollectionSwitch;

    private SettingsViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_activity);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        viewModel = ViewModelProviders.of(this).get(SettingsViewModel.class);

        findViewById(R.id.settings_button_back).setOnClickListener(view -> onBackPressed());

        backgroundImageView = findViewById(R.id.settings_image_background);
        photoImageView = findViewById(R.id.settings_image_small);

        doNotTrackSwitch = findViewById(R.id.settings_switch_do_not_track);
        doNotTrackSwitch.setEnabled(false);
        doNotTrackSwitch.setOnCheckedChangeListener((view, isChecked) -> {
            /* Return to avoid setChecked() inside the callback below triggering the listener again,
             * entering a loop */
            if (!view.isEnabled()) return;

            viewModel.onTrackingChange(isChecked);
        });

        optOutDataCollectionSwitch = findViewById(R.id.settings_switch_opt_out_data_collection);
        optOutDataCollectionSwitch.setEnabled(false);
        optOutDataCollectionSwitch.setOnCheckedChangeListener((view, isChecked) -> {
            /* Return to avoid setChecked() inside the callback below triggering the listener again,
             * entering a loop */
            if (!view.isEnabled()) return;

            viewModel.onOptOutDataCollectionChange(isChecked);
        });

        findViewById(R.id.settings_button_about).setOnClickListener(view ->
                startActivity(new Intent(this, AboutActivity.class)));

        changePhotoButton = findViewById(R.id.settings_button_change_photo);
        changePhotoButton.setOnClickListener(view -> {
            view.setEnabled(false);
            showImagePicker();
        });

        setupViewModelObservers();
        fetchData();
    }

    private void setupViewModelObservers() {
        // Checked deliberately observed before enabled, to avoid changing switch state
        // whilst being enabled, causing callback to change view model and enter loop.

        viewModel.doNotTrackSwitchChecked.observe(this, checked ->
            doNotTrackSwitch.setChecked(checked != null && checked)
        );

        viewModel.doNotTrackSwitchEnabled.observe(this, enabled ->
            doNotTrackSwitch.setEnabled(enabled != null && enabled)
        );

        viewModel.optOutDataCollectionSwitchChecked.observe(this, checked ->
                optOutDataCollectionSwitch.setChecked(checked != null && checked)
        );

        viewModel.optOutDataCollectionSwitchEnabled.observe(this, enabled ->
                optOutDataCollectionSwitch.setEnabled(enabled != null && enabled)
        );
    }

    private void fetchData() {
        viewModel.getUserProfilePhotoURL().observe(this, url -> {
            if (!TextUtils.isEmpty(url))
                GlideApp.with(this)
                        .load(url)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(photoImageView);
        });

        viewModel.getUserPhoto().observe(this, photo -> {
            if (photo != null)
                GlideApp.with(SettingsActivity.this)
                        .load(photo.thumbnailUrl)
                        .transforms(new BlurTransformation(SettingsActivity.this), new TintTransformation(0.1))
                        .priority(Priority.HIGH)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(backgroundImageView);
        });
    }

    private void showImagePicker() {
        CropImage.activity()
                .setAspectRatio(1, 1) // Force a square aspect ratio
                .setBackgroundColor(Color.BLACK)
                .setActivityTitle(getString(R.string.image_crop_title))

                // Minimum size (pixels)
                .setMinCropResultSize(150, 150)

                // Maximum size (pixels)
                .setMaxCropResultSize(1280, 1280)

                //Dimensions (dp)
                .setMinCropWindowSize(128, 128)

                .setOutputCompressQuality(60)

                .start(this);
    }

    private void updateUIWithImage(@Nullable Uri imageUri) {
        if (imageUri == null) return;

        GlideApp.with(SettingsActivity.this)
                .load(imageUri)
                .priority(Priority.HIGH)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(photoImageView);

        GlideApp.with(SettingsActivity.this)
                .load(imageUri)
                .transforms(new BlurTransformation(SettingsActivity.this), new TintTransformation(0.1))
                .placeholder(R.color.profileImageBackground)
                .priority(Priority.HIGH)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(backgroundImageView);

        viewModel.updateUserPhoto(imageUri).observe(this, success -> {
            if (success != null)
                changePhotoButton.setEnabled(true);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent returnedIntent) {
        super.onActivityResult(requestCode, resultCode, returnedIntent);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(returnedIntent);

            if (resultCode == RESULT_OK) {
                Uri imageUri = result.getUri();
                updateUIWithImage(imageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                error.printStackTrace();
            }
        }
    }
}