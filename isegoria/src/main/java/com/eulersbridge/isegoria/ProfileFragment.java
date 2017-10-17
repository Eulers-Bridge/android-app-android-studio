package com.eulersbridge.isegoria;

import android.app.ActionBar;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

public class ProfileFragment extends Fragment {
	private View rootView;
	
	private float dpWidth;
	private float dpHeight;

    private TextView friendsNumTextView;
    private TextView groupNumTextView;
    private TextView rewardsNumTextView;

    private CircularSeekBar circularSeekBar1;
    private CircularSeekBar circularSeekBar2;
    private CircularSeekBar circularSeekBar3;
    private CircularSeekBar circularSeekBar4;

    private ViewPager mPager;
    private Network network;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {   
		rootView = inflater.inflate(R.layout.profile_fragment, container, false);

		DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
		dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        dpHeight = displayMetrics.heightPixels / displayMetrics.density;

        int imageHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 100.00, getResources().getDisplayMetrics());

        ImageView photoImageView = rootView.findViewById(R.id.profilePic);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(imageHeight, imageHeight);
        photoImageView.setLayoutParams(layoutParams);

        final MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();

        friendsNumTextView = rootView.findViewById(R.id.friendsNum);
        groupNumTextView = rootView.findViewById(R.id.groupNum);
        rewardsNumTextView = rootView.findViewById(R.id.rewardsNum);

        TextView name = rootView.findViewById(R.id.profileName);
        network.getUserFullName((int) network.userId, name, "");

        LinearLayout backgroundLinearLayout = rootView.findViewById(R.id.topBackgroundNews);
        network.getUserDP(photoImageView, backgroundLinearLayout);

        network.getTasks(this);
        network.getDashboardStats(this);

        /*final TextView showProgressButton = (TextView) rootView.findViewById(R.id.showProfile);
        showProgressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPager.setCurrentItem(1);
            }
        });*/

        final TextView personalityTestButton = rootView.findViewById(R.id.personalityTestButton);
        personalityTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager2 = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
                PersonalityQuestionsFragment fragment2 = new PersonalityQuestionsFragment();
                Bundle args = new Bundle();
                fragment2.setArguments(args);
                fragmentTransaction2.addToBackStack(null);
                fragmentTransaction2.add(R.id.content_frame, fragment2);
                fragmentTransaction2.commit();
            }
        });

        circularSeekBar1 = rootView.findViewById(R.id.circularSeekBar1);
        circularSeekBar2 = rootView.findViewById(R.id.circularSeekBar2);
        circularSeekBar3 = rootView.findViewById(R.id.circularSeekBar3);
        circularSeekBar4 = rootView.findViewById(R.id.circularSeekBar4);

        circularSeekBar1.setCircleProgressColor(Color.parseColor("#2C9F47"));
        circularSeekBar2.setCircleProgressColor(Color.parseColor("#FFB400"));
        circularSeekBar3.setCircleProgressColor(Color.parseColor("#B61B1B"));

		return rootView;
	}

    public void updateStats(int numOfContacts, int numOfCompBadges, int numOfCompTasks,
                            int totalBadges, int totalTasks, int experience) {
        friendsNumTextView.setText(String.valueOf(numOfContacts));
        groupNumTextView.setText(String.valueOf("0"));
        rewardsNumTextView.setText(String.valueOf("0"));
        circularSeekBar1.setTopLine(String.valueOf(experience));
        circularSeekBar1.setBottomLine("/ ∞");
        circularSeekBar2.setTopLine(String.valueOf(numOfCompBadges));
        circularSeekBar2.setBottomLine("/" + String.valueOf(totalBadges));
        circularSeekBar3.setTopLine(String.valueOf(numOfCompTasks));
        circularSeekBar3.setBottomLine("PER DAY");
        circularSeekBar4.setTopLine(String.valueOf("0"));
        circularSeekBar4.setBottomLine("ATTENDED");

        circularSeekBar1.setMax(2000);
        circularSeekBar1.setProgress(experience);
        circularSeekBar2.setMax(totalBadges);
        circularSeekBar2.setProgress(numOfCompBadges);
        circularSeekBar3.setMax(totalTasks);
        circularSeekBar3.setProgress(numOfCompTasks);
        circularSeekBar4.setProgress(30);

        Thread t1 = new Thread(circularSeekBar1);
        t1.start();
        Thread t2 = new Thread(circularSeekBar2);
        t2.start();
        Thread t3 = new Thread(circularSeekBar3);
        t3.start();
        Thread t4 = new Thread(circularSeekBar4);
        t4.start();
    }

    public void setViewPager(ViewPager mPager) {
        this.mPager = mPager;
    }

    public void addTask(long taskId, String action, long xpValue) {
        LinearLayout tasksLinearLayout = rootView.findViewById(R.id.tasksLayout);

        int paddingMargin1 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 43.33333333, getResources().getDisplayMetrics());
        int paddingMargin2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 3.333333333, getResources().getDisplayMetrics());
        int paddingMargin3 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 10, getResources().getDisplayMetrics());

        RelativeLayout taskLayout = new RelativeLayout(getActivity());
        taskLayout.setGravity(Gravity.START);
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
        xpLabel.setGravity(Gravity.END);
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