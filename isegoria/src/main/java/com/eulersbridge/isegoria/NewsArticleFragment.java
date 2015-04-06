package com.eulersbridge.isegoria;


import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.InputStream;

public class NewsArticleFragment extends Fragment {
	private View rootView;
    private View newsArticleDivider;
    private ImageView newsArticleAuthorImage;
	private float dpWidth;
	private float dpHeight;
	private Isegoria isegoria;
	private int articleId;

    private boolean setLiked = false;
    private Network network;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {   
		rootView = inflater.inflate(R.layout.news_article_fragment, container, false);
		this.isegoria = (Isegoria) getActivity().getApplication();
		Bundle bundle = this.getArguments();
		
		isegoria.getNetwork().getNewsArticle(this, bundle.getInt("ArticleId"));
		articleId = bundle.getInt("ArticleId");

        MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();
        network.getNewsArticleLiked(this);

        newsArticleDivider = (View) rootView.findViewById(R.id.newsArticleDivider);
        newsArticleAuthorImage = (ImageView) rootView.findViewById(R.id.newsArticleAuthorImage);

		return rootView;
	}

    public boolean isSetLiked() {
        return setLiked;
    }

    public void setSetLiked(boolean setLiked) {
        this.setLiked = setLiked;
    }

    public int getArticleId() {
		return articleId;
	}

	public void setArticleId(int articleId) {
		this.articleId = articleId;
	}

	public void populateContent(final String title, final String content, final String likes, final long date, final Bitmap picture, final String email) {
		try {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
					dpWidth = displayMetrics.widthPixels / displayMetrics.density;
			        dpHeight = displayMetrics.heightPixels / displayMetrics.density;  
					
					LinearLayout backgroundLinearLayout = (LinearLayout) rootView.findViewById(R.id.topBackgroundNews);
					backgroundLinearLayout.getLayoutParams().height = (int) (displayMetrics.heightPixels / 2.7);
					Drawable d = new BitmapDrawable(getActivity().getResources(), picture);
					d.setColorFilter(Color.argb(125, 35, 35, 35), Mode.DARKEN);
					backgroundLinearLayout.setBackgroundDrawable(d);
					
					TextView newsTitle = (TextView) rootView.findViewById(R.id.newsArticleTitle);
					newsTitle.setText(title);
					
					TextView newsArticleLikes = (TextView) rootView.findViewById(R.id.newsArticleLikes);
					newsArticleLikes.setText(likes);
					
					TextView newsText = (TextView) rootView.findViewById(R.id.textNews);
					newsText.setText(content);
					
					TextView newsArticleDate = (TextView) rootView.findViewById(R.id.newsArticleDate);
					newsArticleDate.setText(TimeConverter.convertTimestampToString(date));				
					
					final ImageView flagView = (ImageView) rootView.findViewById(R.id.flagView);
					flagView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View view) {
							flagView.setImageResource(R.drawable.flag);
						}
					});
	
					final ImageView starView = (ImageView) rootView.findViewById(R.id.starView);
					flagView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View view) {
							starView.setImageResource(R.drawable.star);
						}
					});

                    //ImageView headShotImageView = (ImageView) rootView.findViewById(R.id.newsArticleHeadView);
                    //network.getFirstPhotoImage(email, headShotImageView);

                    newsArticleDivider.setVisibility(ViewGroup.VISIBLE);
                    newsArticleAuthorImage.setVisibility(ViewGroup.VISIBLE);
				}
			});
		} catch(Exception e) {
			
		}
	}
	
	public void populateUserContent(final String name, final Bitmap picture) {
		try {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					TextView newsArticleName = (TextView) rootView.findViewById(R.id.photoTitle);
					newsArticleName.setText(name);
				}
			});
		} catch(Exception e) {
			
		}
	}

	public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
	
	        final int halfHeight = height / 2;
	        final int halfWidth = width / 2;
	
	        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
	        // height and width larger than the requested height and width.
	        while ((halfHeight / inSampleSize) > reqHeight
	                && (halfWidth / inSampleSize) > reqWidth) {
	            inSampleSize *= 2;
	        }
	    }
	
	    return inSampleSize;
	}
	
	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
	        int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeResource(res, resId, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeResource(res, resId, options);
	}
	
	public Bitmap fastBlur(Bitmap sentBitmap, int radius) {
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
	
	public static Bitmap decodeSampledBitmapFromBitmap(InputStream is,
	        int reqWidth, int reqHeight) {
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeStream(is);
	}
}