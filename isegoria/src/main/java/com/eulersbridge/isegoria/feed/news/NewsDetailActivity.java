package com.eulersbridge.isegoria.feed.news;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.network.api.models.NewsArticle;
import com.eulersbridge.isegoria.util.Constants;
import com.eulersbridge.isegoria.util.Strings;
import com.eulersbridge.isegoria.util.transformation.BlurTransformation;
import com.eulersbridge.isegoria.util.transformation.TintTransformation;

import org.parceler.Parcels;

public class NewsDetailActivity extends AppCompatActivity {

    private ImageView articleImageView;
    private ImageView authorImageView;
    private TextView likesTextView;
    private ImageView starView;

    private boolean userLikedArticle = false;

    private NewsDetailViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.news_detail_activity);

        viewModel = ViewModelProviders.of(this).get(NewsDetailViewModel.class);
        setupModelObservers();

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        findViewById(R.id.back_button).setOnClickListener(view -> onBackPressed());

        articleImageView = findViewById(R.id.article_image);
        authorImageView = findViewById(R.id.article_image_author);
        likesTextView = findViewById(R.id.article_likes);
        starView = findViewById(R.id.article_star);

        NewsArticle article = Parcels.unwrap(getIntent().getParcelableExtra(Constants.ACTIVITY_EXTRA_NEWS_ARTICLE));
        viewModel.newsArticle.setValue(article);
    }

    private void setupModelObservers() {
        viewModel.newsArticle.observe(this, this::populateUIWithArticle);

        viewModel.articleLikes.observe(this, likes -> {
            if (likes != null) {
                runOnUiThread(() -> likesTextView.setText(String.valueOf(likes.size())));
            }
        });

        viewModel.articleLikedByUser.observe(this, liked -> {
            if (liked != null && liked) {
                runOnUiThread(() -> {
                    userLikedArticle = true;
                    starView.setImageResource(R.drawable.star);
                    starView.setEnabled(true);
                });
            }
        });
    }

    private void populateUIWithArticle(NewsArticle article) {
        GlideApp.with(this)
                .load(article.getPhotoUrl())
                .priority(Priority.HIGH)
                .transforms(new BlurTransformation(NewsDetailActivity.this), new TintTransformation())
                .placeholder(R.color.lightGrey)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(articleImageView);

        runOnUiThread(() -> {
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

            TextView titleTextView = findViewById(R.id.article_title);
            titleTextView.setText(article.title);

            likesTextView.setText(String.valueOf(article.likeCount));

            TextView contentTextView = findViewById(R.id.article_content);
            contentTextView.setText(article.content);

            TextView authorNameTextView = findViewById(R.id.article_author);
            authorNameTextView.setText(article.creator.getFullName());

            TextView dateTextView = findViewById(R.id.article_date);
            dateTextView.setText(Strings.fromTimestamp(this, article.dateTimestamp));

            final ImageView flagView = findViewById(R.id.article_flag);
            flagView.setOnClickListener(view -> flagView.setImageResource(R.drawable.flag));

            if (article.hasInappropriateContent)
                flagView.setImageResource(R.drawable.flagdefault);

            final ImageView starView = findViewById(R.id.article_star);
            starView.setOnClickListener(view -> {
                userLikedArticle = !userLikedArticle;

                view.setEnabled(false);

                if (userLikedArticle) {

                    viewModel.likeArticle().observe(this, success -> {
                        view.setEnabled(true);

                        if (success != null && success) {
                            starView.setColorFilter(ContextCompat.getColor(NewsDetailActivity.this, R.color.star_active));

                            int newLikes = Integer.parseInt(String.valueOf(likesTextView.getText())) + 1;
                            likesTextView.setText(String.valueOf(newLikes));
                        }
                    });

                } else {
                    viewModel.unlikeArticle().observe(this, success -> {
                        view.setEnabled(true);

                        if (success != null && success) {
                            starView.setColorFilter(null);

                            int newLikes = Integer.parseInt(String.valueOf(likesTextView.getText())) - 1;
                            likesTextView.setText(String.valueOf(newLikes));
                        }
                    });
                }
            });

            //ImageView headShotImageView = (ImageView) rootView.findViewById(R.id.newsArticleHeadView);
            //network.getFirstPhotoImage(email, headShotImageView);

            //authorImageView.setVisibility(ViewGroup.VISIBLE);
        });
    }
}
