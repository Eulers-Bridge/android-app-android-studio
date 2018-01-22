package com.eulersbridge.isegoria;


import android.content.Intent;
import android.content.pm.ShortcutManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.eulersbridge.isegoria.auth.AuthActivity;
import com.eulersbridge.isegoria.auth.login.EmailVerificationFragment;
import com.eulersbridge.isegoria.election.ElectionMasterFragment;
import com.eulersbridge.isegoria.feed.FeedFragment;
import com.eulersbridge.isegoria.friends.FriendsFragment;
import com.eulersbridge.isegoria.network.api.models.User;
import com.eulersbridge.isegoria.personality.PersonalityQuestionsActivity;
import com.eulersbridge.isegoria.poll.PollsFragment;
import com.eulersbridge.isegoria.profile.ProfileViewPagerFragment;
import com.eulersbridge.isegoria.util.Constants;
import com.eulersbridge.isegoria.util.ui.TitledFragment;
import com.eulersbridge.isegoria.vote.VoteViewPagerFragment;
import com.google.firebase.FirebaseApp;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.securepreferences.SecurePreferences;

import java.util.ArrayDeque;
import java.util.Deque;

public class MainActivity extends AppCompatActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener, FragmentManager.OnBackStackChangedListener {

    public interface TabbedFragment {
        void setupTabLayout(TabLayout tabLayout);
    }

	private Deque<Fragment> tabFragments;
    private TabLayout tabLayout;
    private CoordinatorLayout coordinatorLayout;
    private BottomNavigationViewEx navigationView;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null)
		    FirebaseApp.initializeApp(this);

		setContentView(R.layout.activity_main);

        IsegoriaApp application = (IsegoriaApp) getApplicationContext();
		application.setMainActivity(this);

		setupToolbarAndNavigation();

		coordinatorLayout = findViewById(R.id.coordinator_layout);

		getSupportFragmentManager().addOnBackStackChangedListener(this);

        application.loggedInUser.observe(this, user -> {
            final boolean userLoggedOut = user == null;

            if (userLoggedOut) {
                finish();

            } else {
                onLoginSuccess(user);
            }
        });

        attemptUserLogin(application);
	}

	private void attemptUserLogin(IsegoriaApp app) {
        if (app.loggedInUser.getValue() == null) {
            String userEmail = new SecurePreferences(this)
                    .getString(Constants.USER_EMAIL_KEY, null);
            String userPassword = new SecurePreferences(this)
                    .getString(Constants.USER_PASSWORD_KEY, null);

            final boolean haveStoredCredentials = userEmail != null && userPassword != null;

            if (haveStoredCredentials) {
                app.login(userEmail, userPassword).observe(this, success -> {
                    if (success == null || !success) {
                        showLogin();
                    }
                });

                // Add 3 empty tabs to flesh out the empty/not loaded feed fragment screen
                tabLayout.addTab(tabLayout.newTab());
                tabLayout.addTab(tabLayout.newTab());
                tabLayout.addTab(tabLayout.newTab());
                tabLayout.setVisibility(View.VISIBLE);

            } else {
                showLogin();
            }
        }
    }

    public CoordinatorLayout getCoordinatorLayout() {
        return coordinatorLayout;
    }

    private boolean handleAppShortcutIntent() {
	    boolean handledShortcut = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            Intent intent = getIntent();
            if (intent != null) {

                String action = getIntent().getAction();
                if (action != null) {
                    switch (action) {
                        case Constants.SHORTCUT_ACTION_ELECTION:
                            showElection();
                            handledShortcut = true;
                            break;

                        case Constants.SHORTCUT_ACTION_FRIENDS:
                            showFriends();
                            handledShortcut = true;
                            break;
                    }

                    if (handledShortcut) {
                        ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
                        if (shortcutManager != null)
                            shortcutManager.reportShortcutUsed(action);
                    }
                }
            }
        }

        return handledShortcut;
    }

	private void setupToolbarAndNavigation() {
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		navigationView = findViewById(R.id.navigation);
		if (navigationView != null) {
		    navigationView.setOnNavigationItemSelectedListener(this);

            navigationView.enableShiftingMode(false);
            navigationView.setTextVisibility(false);
		}

		tabLayout = findViewById(R.id.tab_layout);

		tabFragments = new ArrayDeque<>(4);
	}

	public void setToolbarShowsTitle(boolean visible) {
        if (getSupportActionBar() != null)
	        getSupportActionBar().setDisplayShowTitleEnabled(visible);
	}

	private void setToolbarTitle(String title) {
	    if (getSupportActionBar() != null)
	        getSupportActionBar().setTitle(title);
	}

    @Override
    public void onBackPressed() {
        if (tabFragments.size() > 1) {
            // Only pop if more than 1 fragment on stack (i.e. always leave a root fragment)

            tabFragments.pop();
            getSupportFragmentManager().popBackStack();

        } else if (tabFragments.size() == 1) {
            // Return to launcher
            Intent launcherAction = new Intent(Intent.ACTION_MAIN);
            launcherAction.addCategory(Intent.CATEGORY_HOME);
            launcherAction.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(launcherAction);
        }
    }

    @Override
    public void onBackStackChanged() {
        boolean canGoBack = (getSupportFragmentManager().getBackStackEntryCount() > 0);

        if (getSupportActionBar() != null) {
            /* When a fragment is added to the stack, show the back button in the app bar,
               as the user can navigate back to a previous fragment. */
            getSupportActionBar().setDisplayHomeAsUpEnabled(canGoBack);
            getSupportActionBar().setDisplayShowHomeEnabled(canGoBack);
        }

        updateAppBarState();
    }

    @Override
	public boolean onSupportNavigateUp() {
	    if (tabFragments.size() > 0)
            tabFragments.pop();

		getSupportFragmentManager().popBackStack();

		return true;
	}

    private @Nullable Fragment getCurrentFragment() {
	    return tabFragments.size() == 0? null : tabFragments.peekFirst();
    }

	private void showElection() {
        final @IdRes int id = R.id.navigation_election;

        if (navigationView.getSelectedItemId() != id)
            navigationView.setSelectedItemId(id);
    }

    public void showFriends() {
        presentContent(new FriendsFragment());
    }

	public void showLogin() {
        startActivity(new Intent(this, AuthActivity.class));
	}

	public void showVerification() {
	    presentRootContent(new EmailVerificationFragment());
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		return false;
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        Fragment fragment;

        switch(item.getItemId()) {
            case R.id.navigation_feed:
                fragment = new FeedFragment();
                break;

            case R.id.navigation_poll:
                fragment = new PollsFragment();
                break;

            case R.id.navigation_vote:
                fragment = new VoteViewPagerFragment();
                break;

            case R.id.navigation_profile:
                fragment = new ProfileViewPagerFragment();
                break;

            case R.id.navigation_election:
                fragment = new ElectionMasterFragment();
                break;

            default:
                return true;
        }

        Fragment currentFragment = getCurrentFragment();

        if (currentFragment == null || !fragment.getClass().equals(currentFragment.getClass()))
            presentRootContent(fragment);

		return true;
	}

	private void onLoginSuccess(User loggedInUser) {
        if (!handleAppShortcutIntent())
            navigationView.setSelectedItemId(R.id.navigation_feed);

        if (!loggedInUser.hasPersonality)
            startActivity(new Intent(this, PersonalityQuestionsActivity.class));
    }

	public void presentContent(@NonNull Fragment fragment) {
        tabFragments.push(fragment);

        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager
                .beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();

        fragmentManager.executePendingTransactions();
    }

	private void presentRootContent(@NonNull Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack();

        tabFragments.clear();
        tabFragments.push(fragment);

        fragmentManager
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commitNow(); // commit() + executePendingTransactions()

        updateAppBarState();
	}

	private void updateAppBarState() {
        final Fragment currentFragment = getCurrentFragment();

        if (currentFragment != null) {
            runOnUiThread(() -> {
                if (currentFragment instanceof TitledFragment) {
                    String fragmentTitle = ((TitledFragment) currentFragment).getTitle(MainActivity.this);

                    if (!TextUtils.isEmpty(fragmentTitle))
                        setToolbarTitle(fragmentTitle);
                }

                if (currentFragment instanceof TabbedFragment) {
                    tabLayout.clearOnTabSelectedListeners();
                    ((TabbedFragment) currentFragment).setupTabLayout(tabLayout);
                }
            });
        }
    }
}