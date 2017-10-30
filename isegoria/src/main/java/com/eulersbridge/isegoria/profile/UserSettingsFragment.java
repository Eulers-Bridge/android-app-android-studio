package com.eulersbridge.isegoria.profile;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.Network;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.User;
import com.eulersbridge.isegoria.utilities.Utils;

import java.io.File;

public class UserSettingsFragment extends Fragment {
    private static final int PICK_IMAGE = 1;
    private final static int REQ_CODE_PICK_IMAGE = 1;

    private ImageView photoImageView;
    private LinearLayout backgroundLinearLayout;

    private MainActivity mainActivity;
    private Network network;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.user_settings_fragment, container, false);

        // Ensure options menu from another fragment is not carried over
        getActivity().invalidateOptionsMenu();

        mainActivity = (MainActivity) getActivity();

        mainActivity.setToolbarTitle(getString(R.string.section_title_settings));

        network = mainActivity.getIsegoriaApplication().getNetwork();
        network.getUserDPId();

        photoImageView = rootView.findViewById(R.id.profilePicSettings);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(150, 150);
        photoImageView.setLayoutParams(layoutParams);

        final Switch doNotTrackSwitch = rootView.findViewById(R.id.doNotTrackSwitch);
        doNotTrackSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            network.setTrackingOff(isChecked);
            network.updateUserDetails();
        });
        final Switch optOutDataCollectionSwitch = rootView.findViewById(R.id.optOutDataCollectionSwitch);
        optOutDataCollectionSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            network.setOptedOutOfDataCollection(isChecked);
            network.updateUserDetails();
        });

        User loggedInUser = ((Isegoria)getActivity().getApplication()).getLoggedInUser();

        doNotTrackSwitch.setChecked(loggedInUser.isOptedOutOfDataCollection());
        optOutDataCollectionSwitch.setChecked(loggedInUser.isTrackingOff());

        backgroundLinearLayout = rootView.findViewById(R.id.topBackgroundSettings);
        network.getUserDP(photoImageView, backgroundLinearLayout);

        final TextView aboutThisAppButton = rootView.findViewById(R.id.aboutThisAppButton);
        aboutThisAppButton.setOnClickListener(view -> {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

            fragmentManager
                    .beginTransaction()
                    .addToBackStack(null)
                    .add(R.id.container, new AboutScreenFragment())
                    .commit();
        });

        final TextView changePhotoButton = rootView.findViewById(R.id.changePhotoButton);
        changePhotoButton.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
        });

       // network.s3Auth();

		return rootView;
	}

    public void setTabLayout(TabLayout tabLayout) {
        tabLayout.setVisibility(View.GONE);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                    Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case REQ_CODE_PICK_IMAGE:
                if(resultCode == Activity.RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getActivity().getContentResolver().query(
                            selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String filePath = cursor.getString(columnIndex);
                    cursor.close();

                    File file = new File(filePath);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getPath(), options);
                    photoImageView.setImageBitmap(bitmap);

                    Drawable d = new BitmapDrawable(mainActivity.getResources(),
                            Utils.fastBlur(bitmap, 25));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        backgroundLinearLayout.setBackground(d);
                    } else {
                        backgroundLinearLayout.setBackgroundDrawable(d);
                    }

                    network.s3Upload(file);
                }
        }
    }
}