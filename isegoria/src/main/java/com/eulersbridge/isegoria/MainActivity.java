package com.eulersbridge.isegoria;


import android.content.Intent;
import android.content.pm.ShortcutManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.eulersbridge.isegoria.auth.EmailVerificationFragment;
import com.eulersbridge.isegoria.election.ElectionMasterFragment;
import com.eulersbridge.isegoria.feed.FeedFragment;
import com.eulersbridge.isegoria.friends.FriendsFragment;
import com.eulersbridge.isegoria.personality.PersonalityQuestionsActivity;
import com.eulersbridge.isegoria.poll.PollsFragment;
import com.eulersbridge.isegoria.profile.ProfileViewPagerFragment;
import com.eulersbridge.isegoria.util.Constants;
import com.eulersbridge.isegoria.util.ui.TitledFragment;
import com.eulersbridge.isegoria.vote.VoteViewPagerFragment;
import com.google.firebase.FirebaseApp;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayDeque;
import java.util.Deque;

public class MainActivity extends AppCompatActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener, FragmentManager.OnBackStackChangedListener {

    public interface TabbedFragment {
        void setupTabLayout(TabLayout tabLayout);
    }

	private Deque<Fragment> tabFragmentsStack;
    private TabLayout tabLayout;
    private BottomNavigationViewEx navigationView;

    private boolean loginActionsComplete;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null)
		    FirebaseApp.initializeApp(this);

		setContentView(R.layout.activity_main);

		setupNavigation();

        IsegoriaApp application = (IsegoriaApp) getApplicationContext();
        setupApplicationObservers(application);
	}

    @Override
    protected void onDestroy() {
	    try {
            GlideApp.with(this).onDestroy();
        } catch (Exception e) {
	        // Ignored
        }

        super.onDestroy();
    }

    private void setupNavigation() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navigationView = findViewById(R.id.navigation);
        if (navigationView != null) {
            navigationView.setEnabled(false);
            navigationView.setOnNavigationItemSelectedListener(this);
            navigationView.enableShiftingMode(false);
            navigationView.setTextVisibility(false);
        }

        tabLayout = findViewById(R.id.tab_layout);

        tabFragmentsStack = new ArrayDeque<>(4);

        getSupportFragmentManager().addOnBackStackChangedListener(this);
    }

	private void setupApplicationObservers(IsegoriaApp application) {
        application.loggedInUser.observe(this, user -> {
            final boolean userLoggedOut = user == null;

            if (userLoggedOut) {
                finish();

            } else {
                onLoginSuccess();
            }
        });

        application.userVerificationVisible.observe(this, verificationVisible -> {
            if (verificationVisible != null && verificationVisible)
                presentRootContent(new EmailVerificationFragment());
        });

        application.friendsVisible.observe(this, friendsVisible -> {
            if (friendsVisible != null && friendsVisible)
                showFriends();
        });
    }

    private void onLoginSuccess() {
	    if (loginActionsComplete) return;

	    navigationView.setEnabled(true);

        if (!handleAppShortcutIntent())
            navigationView.setSelectedItemId(R.id.navigation_feed);

        IsegoriaApp app = (IsegoriaApp) getApplication();

        loginActionsComplete = true;

        if (app.loggedInUser.getValue() != null && !app.loggedInUser.getValue().hasPersonality)
            startActivity(new Intent(this, PersonalityQuestionsActivity.class));
    }

    private boolean handleAppShortcutIntent() {
	    boolean handledShortcut = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            Intent intent = getIntent();
            if (intent != null) {

                String action = intent.getAction();
                if (action != null) {
                    switch (action) {
                        case Constants.SHORTCUT_ACTION_ELECTION:
                            showElection();
                            handledShortcut = true;
                            break;

                        case Constants.SHORTCUT_ACTION_FRIENDS:
                            IsegoriaApp app = (IsegoriaApp) getApplication();
                            app.friendsVisible.setValue(true);
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
        if (tabFragmentsStack.size() > 1) {
            // Only pop if more than 1 fragment on stack (i.e. always leave a root fragment)

            tabFragmentsStack.pop();
            getSupportFragmentManager().popBackStack();

        } else if (tabFragmentsStack.size() == 1) {
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
	    if (tabFragmentsStack.size() > 0)
            tabFragmentsStack.pop();

		getSupportFragmentManager().popBackStack();

		return true;
	}

    private @Nullable Fragment getCurrentFragment() {
	    return tabFragmentsStack.size() == 0? null : tabFragmentsStack.peekFirst();
    }

	private void showElection() {
        final @IdRes int id = R.id.navigation_election;

        if (navigationView.getSelectedItemId() != id)
            navigationView.setSelectedItemId(id);
    }

    private void showFriends() {
        presentContent(new FriendsFragment());
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
	    if (!navigationView.isEnabled())
	        return false;

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

        if (currentFragment == null || !fragment.getClass().equals(currentFragment.getClass())) {

            IsegoriaApp app = (IsegoriaApp) getApplication();
            //noinspection ConstantConditions
            if (app.friendsVisible.getValue()) {
                getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                tabFragmentsStack.clear();

                app.friendsVisible.setValue(false);
            }

            presentRootContent(fragment);
        }

		return true;
	}

	public void presentContent(@NonNull Fragment fragment) {
        tabFragmentsStack.push(fragment);

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

        tabFragmentsStack.clear();
        tabFragmentsStack.push(fragment);

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