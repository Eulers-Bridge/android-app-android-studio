package com.eulersbridge.isegoria;

import android.app.ActionBar;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

/**
 * Created by Anthony on 30/03/20paddingMargin3.
 */
public class ContactProfileFragment extends SherlockFragment {
    private View rootView;

    private float dpWidth;
    private float dpHeight;

    private int profileId;

    private ViewPager mPager;
    private Network network;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.contact_profile_fragment, container, false);
        ((SherlockFragmentActivity) getActivity()).getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        getActivity().getActionBar().removeAllTabs();

        Bundle bundle = this.getArguments();
        profileId = (int) bundle.getInt("ProfileId");

        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        dpHeight = displayMetrics.heightPixels / displayMetrics.density;

        int imageHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 100.00, getResources().getDisplayMetrics());

        ImageView photoImageView = (ImageView) rootView.findViewById(R.id.profilePic);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(imageHeight, imageHeight);
        photoImageView.setLayoutParams(layoutParams);

        MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();

        TextView name = (TextView) rootView.findViewById(R.id.profileName);
        network.getUserFullName(profileId, name, "");

        LinearLayout backgroundLinearLayout = (LinearLayout) rootView.findViewById(R.id.topBackgroundNews);
        network.getUserDP(profileId, photoImageView, backgroundLinearLayout);

        network.getTasks(this);

        final TextView showProgressButton = (TextView) rootView.findViewById(R.id.showProgressButton);
        showProgressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mPager.setCurrentItem(1);
            }
        });

        CircularSeekBar circularSeekBar1 = (CircularSeekBar) rootView.findViewById(R.id.circularSeekBar1);
        CircularSeekBar circularSeekBar2 = (CircularSeekBar) rootView.findViewById(R.id.circularSeekBar2);
        CircularSeekBar circularSeekBar3 = (CircularSeekBar) rootView.findViewById(R.id.circularSeekBar3);
        CircularSeekBar circularSeekBar4 = (CircularSeekBar) rootView.findViewById(R.id.circularSeekBar4);

        circularSeekBar1.setCircleProgressColor(Color.parseColor("#2C9F47"));
        circularSeekBar2.setCircleProgressColor(Color.parseColor("#FFB400"));
        circularSeekBar3.setCircleProgressColor(Color.parseColor("#B61B1B"));

        circularSeekBar1.setProgress(30);
        circularSeekBar2.setProgress(30);
        circularSeekBar3.setProgress(30);
        circularSeekBar4.setProgress(30);

        return rootView;
    }

    public void setViewPager(ViewPager mPager) {
        this.mPager = mPager;
    }

    public void addTask(long taskId, String action, long xpValue) {
        LinearLayout tasksLinearLayout = (LinearLayout) rootView.findViewById(R.id.tasksLayout);

        int paddingMargin1 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 43.33333333, getResources().getDisplayMetrics());
        int paddingMargin2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 3.333333333, getResources().getDisplayMetrics());
        int paddingMargin3 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 10, getResources().getDisplayMetrics());

        RelativeLayout taskLayout = new RelativeLayout(getActivity());
        taskLayout.setGravity(Gravity.LEFT);
        taskLayout.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, paddingMargin1));

        LinearLayout leftLayout = new LinearLayout(getActivity());
        LinearLayout rightLayout = new LinearLayout(getActivity());

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        rightLayout.setLayoutParams(lp);

        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(paddingMargin1, paddingMargin1);
        ImageView iconImage = new ImageView(getActivity());
        iconImage.setScaleType(ImageView.ScaleType.FIT_XY);
        iconImage.setLayoutParams(layoutParams);
        iconImage.setPadding(paddingMargin2, 0, 0, 0);
        //iconImage.setBackgroundColor(Color.BLACK);

        network.getFirstPhoto((int) taskId, (int) taskId, iconImage);

        TextView taskLabel = new TextView(getActivity());
        taskLabel.setGravity(Gravity.CENTER_VERTICAL);
        taskLabel.setPadding(paddingMargin3, paddingMargin3, 0, 0);

        TextView xpLabel = new TextView(getActivity());
        xpLabel.setGravity(Gravity.RIGHT);
        xpLabel.setPadding(0, paddingMargin3, paddingMargin3, 0);
        xpLabel.setText(String.valueOf(xpValue) + " XP");

        taskLabel.setText(action);

        leftLayout.addView(iconImage);
        leftLayout.addView(taskLabel);
        rightLayout.addView(xpLabel);

        taskLayout.addView(leftLayout);
        taskLayout.addView(rightLayout);

        View divider = new View(getActivity());
        divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(Color.parseColor("#838a8a8a"));

        tasksLinearLayout.addView(divider);
        tasksLinearLayout.addView(taskLayout);

        divider = new View(getActivity());
        divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(Color.parseColor("#838a8a8a"));
        tasksLinearLayout.addView(divider);
    }

    public static Bitmap fastBlur(Bitmap sentBitmap, int radius) {
        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }
}
