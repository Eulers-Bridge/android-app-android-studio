package com.eulersbridge.isegoria.feed;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.common.BlurTransformation;
import com.eulersbridge.isegoria.common.Constant;
import com.eulersbridge.isegoria.common.TintTransformation;
import com.eulersbridge.isegoria.common.Utils;
import com.eulersbridge.isegoria.models.LikeInfo;
import com.eulersbridge.isegoria.models.NewsArticle;
import com.eulersbridge.isegoria.network.IgnoredCallback;
import com.eulersbridge.isegoria.network.SimpleCallback;

import org.parceler.Parcels;

import java.util.List;

import retrofit2.Response;

public class NewsDetailActivity extends AppCompatActivity {

    private Isegoria isegoria;

    private TextView likesTextView;
    private ImageView starView;

    private boolean userLikedArticle = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.news_detail_activity);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        findViewById(R.id.back_button).setOnClickListener(view -> onBackPressed());

        ImageView articleImageView = findViewById(R.id.article_image);
        ImageView authorImageView = findViewById(R.id.article_image_author);
        likesTextView = findViewById(R.id.article_likes);
        starView = findViewById(R.id.article_star);

        Bundle extras = getIntent().getExtras();

        NewsArticle article = Parcels.unwrap(extras.getParcelable(Constant.ACTIVITY_EXTRA_NEWS_ARTICLE));
        populateTextContent(article);

        isegoria = (Isegoria)getApplication();

        isegoria.getAPI().getNewsArticleLikes(article.id).enqueue(new SimpleCallback<List<LikeInfo>>() {
            @Override
            protected void handleResponse(Response<List<LikeInfo>> response) {
                List<LikeInfo> likes = response.body();

                if (likes != null) populateLikes(likes);
            }
        });

        GlideApp.with(this)
                .load(article.getPhotoUrl())
                .priority(Priority.HIGH)
                .transforms(new BlurTransformation(NewsDetailActivity.this), new TintTransformation())
                .placeholder(R.color.lightGrey)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(articleImageView);

        String creatorPhotoURL = article.creator.profilePhotoURL;

        if (TextUtils.isEmpty(creatorPhotoURL)) {
            authorImageView.setVisibility(View.GONE);

        } else {
            GlideApp.with(this)
                    .load(creatorPhotoURL)
                    .priority(Priority.LOW)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(authorImageView);
        }
    }

    private void populateLikes(@NonNull List<LikeInfo> likes) {
        likesTextView.setText(String.valueOf(likes.size()));

        for (LikeInfo likeInfo : likes) {
            if (likeInfo.email.equals(isegoria.getLoggedInUser().email)) {
                userLikedArticle = true;
                starView.setImageResource(R.drawable.star);
                starView.setEnabled(true);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @UiThread
    private void populateTextContent(final NewsArticle article) {

        TextView titleTextView = findViewById(R.id.article_title);
        titleTextView.setText(article.title);

        likesTextView.setText(String.valueOf(article.likeCount));

        TextView contentTextView = findViewById(R.id.article_content);
        contentTextView.setText(article.content);

        TextView authorNameTextView = findViewById(R.id.article_author);
        authorNameTextView.setText(article.creator.getFullName());

        TextView dateTextView = findViewById(R.id.article_date);
        dateTextView.setText(Utils.convertTimestampToString(this, article.dateTimestamp));

        final ImageView flagView = findViewById(R.id.article_flag);
        flagView.setOnClickListener(view -> flagView.setImageResource(R.drawable.flag));

        if (article.hasInappropriateContent) flagView.setImageResource(R.drawable.flagdefault);

        final ImageView starView = findViewById(R.id.article_star);
        starView.setOnClickListener(view -> {
            userLikedArticle = !userLikedArticle;

            IgnoredCallback callback = new IgnoredCallback<>();

            if (userLikedArticle) {
                isegoria.getAPI().likeArticle(article.id, isegoria.getLoggedInUser().email).enqueue(callback);

                starView.setColorFilter(ContextCompat.getColor(this, R.color.star_active));

                int newLikes = Integer.parseInt(String.valueOf(likesTextView.getText())) + 1;
                likesTextView.setText(String.valueOf(newLikes));

            } else {
                isegoria.getAPI().unlikeArticle(article.id, isegoria.getLoggedInUser().email).enqueue(callback);

                starView.setColorFilter(null);

                int newLikes = Integer.parseInt(String.valueOf(likesTextView.getText())) - 1;
                likesTextView.setText(String.valueOf(newLikes));
            }
        });

        //ImageView headShotImageView = (ImageView) rootView.findViewById(R.id.newsArticleHeadView);
        //network.getFirstPhotoImage(email, headShotImageView);

        //authorImageView.setVisibility(ViewGroup.VISIBLE);
    }
}
