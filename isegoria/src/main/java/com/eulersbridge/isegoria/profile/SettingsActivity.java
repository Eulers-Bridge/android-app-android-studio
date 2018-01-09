package com.eulersbridge.isegoria.profile;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.common.BlurTransformation;
import com.eulersbridge.isegoria.common.TintTransformation;
import com.eulersbridge.isegoria.models.Photo;
import com.eulersbridge.isegoria.models.User;
import com.eulersbridge.isegoria.models.UserSettings;
import com.eulersbridge.isegoria.network.IgnoredCallback;
import com.eulersbridge.isegoria.network.NetworkService;
import com.eulersbridge.isegoria.network.PhotosResponse;
import com.eulersbridge.isegoria.network.SimpleCallback;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;

import retrofit2.Response;

public class SettingsActivity extends AppCompatActivity {

    private ImageView backgroundImageView;
    private ImageView photoImageView;

    private NetworkService network;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_activity);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        findViewById(R.id.settings_button_back).setOnClickListener(view -> onBackPressed());

        Isegoria isegoria = (Isegoria)getApplication();
        network = isegoria.getNetworkService();

        backgroundImageView = findViewById(R.id.settings_image_background);
        photoImageView = findViewById(R.id.settings_image_small);

        User loggedInUser = isegoria.getLoggedInUser();

        final Switch doNotTrackSwitch = findViewById(R.id.settings_switch_do_not_track);
        doNotTrackSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isegoria.setTrackingOff(isChecked);

            UserSettings userSettings = new UserSettings(isChecked, loggedInUser.isOptedOutOfDataCollection);

            isegoria.getAPI().updateUserDetails(loggedInUser.email, userSettings).enqueue(new IgnoredCallback<>());
        });

        final Switch optOutDataCollectionSwitch = findViewById(R.id.settings_switch_opt_out_data_collection);
        optOutDataCollectionSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isegoria.setOptedOutOfDataCollection(isChecked);

            UserSettings userSettings = new UserSettings(loggedInUser.trackingOff, isChecked);

            isegoria.getAPI().updateUserDetails(loggedInUser.email, userSettings).enqueue(new IgnoredCallback<>());
        });

        doNotTrackSwitch.setChecked(loggedInUser.isOptedOutOfDataCollection);
        optOutDataCollectionSwitch.setChecked(loggedInUser.trackingOff);

        GlideApp.with(this)
                .load(loggedInUser.profilePhotoURL)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(photoImageView);

        isegoria.getAPI().getPhotos(loggedInUser.email).enqueue(new SimpleCallback<PhotosResponse>() {
            @Override
            protected void handleResponse(Response<PhotosResponse> response) {
                PhotosResponse body = response.body();
                if (body != null && body.totalPhotos > 0) {
                    Photo photo = body.photos.get(0);

                    GlideApp.with(SettingsActivity.this)
                            .load(photo.thumbnailUrl)
                            .transforms(new BlurTransformation(SettingsActivity.this), new TintTransformation(0.1))
                            .priority(Priority.HIGH)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(backgroundImageView);
                }
            }
        });

        findViewById(R.id.settings_button_about).setOnClickListener(view ->
                startActivity(new Intent(this, AboutActivity.class)));

        findViewById(R.id.settings_button_change_photo).setOnClickListener(view -> showImagePicker());
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

        uploadImage(new File(imageUri.getPath()));
    }

    private void uploadImage(File imageFile) {
        network.s3Upload(imageFile);
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
