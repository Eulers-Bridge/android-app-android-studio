package com.eulersbridge.isegoria;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class PhotosFragment extends Fragment {
	private TableLayout photosTableLayout;

	private boolean insertedFirstRow = false;
    private PhotosFragment photosFragment;
    private android.support.v4.widget.SwipeRefreshLayout swipeContainerPhotos;
    private Network network;
	
	public PhotosFragment() {
		insertedFirstRow = false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.photos_fragment, container, false);
		getActivity().setTitle("Isegoria");
        photosFragment = this;

		photosTableLayout = rootView.findViewById(R.id.photosTableLayout);

        swipeContainerPhotos = rootView.findViewById(R.id.swipeContainerPhotos);
        swipeContainerPhotos.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeContainerPhotos.setRefreshing(true);
                photosFragment.clearTable();
                network.getPhotoAlbums(photosFragment);
                ( new android.os.Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        insertedFirstRow = false;
                        swipeContainerPhotos.setRefreshing(false);
                    }
                }, 7000);
            }
        });
        
        MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();
        network.getPhotoAlbums(this);

		return rootView;
	}

    private void clearTable() {
        photosTableLayout.removeAllViews();
    }
	
	public void addPhotoAlbum(final int albumId, final String label, final String caption, String photoAlbumThumb) {
		addTableRow(albumId, label, caption, photoAlbumThumb);
	}

	private void addTableRow(final int albumId, String label, String caption, String bitmap) {
		try {
			TableRow tr = new TableRow(getActivity());

            int paddingMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    (float) 6.666666667, getResources().getDisplayMetrics());
            int imageSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    (float) 66.66666667, getResources().getDisplayMetrics());

			if(!insertedFirstRow) {
				insertedFirstRow = true;
				tr.setPadding(paddingMargin, paddingMargin, 0, paddingMargin);
			}
			else {
				tr.setPadding(paddingMargin, 0, 0, paddingMargin);
			}
			TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
			tr.setLayoutParams(rowParams);
			
			ImageView view = new ImageView(getActivity());
			view.setLayoutParams(new TableRow.LayoutParams(imageSize, imageSize));
			view.setScaleType(ScaleType.CENTER_CROP);
            view.setBackgroundColor(Color.GRAY);
	        LinearLayout linearLayout = new LinearLayout(getActivity());
			linearLayout.setOrientation(LinearLayout.VERTICAL);
			linearLayout.setGravity(Gravity.CENTER_VERTICAL);
			linearLayout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
			linearLayout.setPadding(paddingMargin, 0, 0, 0);

            network.getPictureVolley(bitmap, view);
	        
	        final TextView textViewArticle = new TextView(getActivity());
	        textViewArticle.setTextColor(Color.parseColor("#000000"));
	        textViewArticle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18.0f);
	        textViewArticle.setText(label);
	        textViewArticle.setGravity(Gravity.START);

	        textViewArticle.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View view) {
                        FragmentManager fragmentManager2 = getActivity().getSupportFragmentManager();
			    		FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
			    		PhotoAlbumFragment fragment2 = new PhotoAlbumFragment();
			    		Bundle args = new Bundle();
			    		args.putString("Album", String.valueOf(albumId));
			    		fragment2.setArguments(args);
			    		fragmentTransaction2.addToBackStack(null);
			    		fragmentTransaction2.add(R.id.photosFrameLayout, fragment2);
			    		fragmentTransaction2.commit();
	            }
	       });
	        
	       view.setOnClickListener(new View.OnClickListener() {        
	            @Override
	            public void onClick(View view) {
                        FragmentManager fragmentManager2 = getActivity().getSupportFragmentManager();
			    		FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
			    		PhotoAlbumFragment fragment2 = new PhotoAlbumFragment();
			    		Bundle args = new Bundle();
			    		args.putString("Album", String.valueOf(albumId));
			    		fragment2.setArguments(args);
			    		fragmentTransaction2.addToBackStack(null);
			    		fragmentTransaction2.add(R.id.photosFrameLayout, fragment2);
			    		fragmentTransaction2.commit();
	            }
	         });
	        
	        TextView textViewArticleTime = new TextView(getActivity());
	        textViewArticleTime.setTextColor(Color.parseColor("#000000"));
	        textViewArticleTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.0f);
	        textViewArticleTime.setText(caption);
	        textViewArticleTime.setPadding(0, 0, 0, 0);
	        textViewArticleTime.setGravity(Gravity.START);
	        
	        linearLayout.addView(textViewArticle);
	        linearLayout.addView(textViewArticleTime);
	        
	        tr.addView(view);
	        tr.addView(linearLayout);	
	        photosTableLayout.addView(tr);
		} catch(Exception ignored) {
			
		}
	}
}
