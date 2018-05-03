package com.eulersbridge.isegoria


import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ShortcutManager
import android.os.Build
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.annotation.StringRes
import android.support.annotation.UiThread
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.systemService
import com.eulersbridge.isegoria.auth.verification.EmailVerificationFragment
import com.eulersbridge.isegoria.data.LoginState
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.election.ElectionMasterFragment
import com.eulersbridge.isegoria.feed.FeedFragment
import com.eulersbridge.isegoria.friends.FriendsFragment
import com.eulersbridge.isegoria.personality.PersonalityActivity
import com.eulersbridge.isegoria.poll.PollsFragment
import com.eulersbridge.isegoria.profile.ProfileViewPagerFragment
import com.eulersbridge.isegoria.util.ui.TitledFragment
import com.eulersbridge.isegoria.vote.VoteViewPagerFragment
import com.google.firebase.FirebaseApp
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.main_partial_appbar.*
import java.util.*
import javax.inject.Inject


class MainActivity : DaggerAppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener,
    FragmentManager.OnBackStackChangedListener {

    @Inject
    lateinit var appRouter: AppRouter

    @Inject
    lateinit var repository: Repository

    private var tabFragmentsStack: ArrayDeque<Fragment> = ArrayDeque(4)

    private val currentFragment: Fragment?
        get() = tabFragmentsStack.peekFirst() ?: null

    private val navigationView: BottomNavigationViewEx
        get() = navigation

    private var loginActionsComplete = false
    private val compositeDisposable = CompositeDisposable()

    interface TabbedFragment {
        fun setupTabLayout(tabLayout: TabLayout)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null)
            FirebaseApp.initializeApp(this)

        setContentView(R.layout.activity_main)
        setupNavigation()
        compositeDisposable.size()
    }

    override fun onPause() {
        compositeDisposable.clear()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        createObservers()
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    private fun setupNavigation() {
        setSupportActionBar(toolbar)

        navigationView.apply {
            isEnabled = false
            onNavigationItemSelectedListener = this@MainActivity
            enableShiftingMode(false)
            setTextVisibility(false)
        }

        supportFragmentManager.addOnBackStackChangedListener(this)
    }

    private fun createObservers() {
        repository.getLoginState().subscribe {
            when (it) {
                is LoginState.LoggedIn -> {
                    onLoginSuccess()

                    if (!it.user.hasPersonality) {
                        startActivity(Intent(this, PersonalityActivity::class.java))
                    }
                }
                is LoginState.LoggedOut -> {
                    startActivity(Intent(this, SplashActivity::class.java))
                    finish()
                }
            }
        }.addTo(compositeDisposable)

        appRouter.getUserVerificationScreenVisible()
                .filter { it }
                .subscribe {
                    runOnUiThread { presentRootContent(EmailVerificationFragment()) }
                }
                .addTo(compositeDisposable)

        appRouter.getFriendsScreenVisible()
                .filter { it }
                .subscribe {
                    runOnUiThread { showFriends() }
                }
                .addTo(compositeDisposable)
    }

    private fun onLoginSuccess() {
        if (loginActionsComplete)
            return

        navigationView.apply {
            if (!isEnabled) {
                isEnabled = true

                if (!handleAppShortcutIntent())
                    selectedItemId = R.id.navigation_feed
            }
        }

        loginActionsComplete = true
    }

    @SuppressLint("NewApi")
    private fun handleAppShortcutIntent(): Boolean {
        var handledShortcut = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            intent?.action?.let {
                when (it) {
                    SHORTCUT_ACTION_ELECTION -> {
                        showElection()
                        handledShortcut = true
                    }

                    SHORTCUT_ACTION_FRIENDS -> {
                        appRouter.setFriendsScreenVisible(true)
                        handledShortcut = true
                    }
                }

                if (handledShortcut)
                    systemService<ShortcutManager>().reportShortcutUsed(it)
            }
        }

        return handledShortcut
    }

    fun setToolbarShowsTitle(visible: Boolean)
            = supportActionBar?.setDisplayShowTitleEnabled(visible)

    private fun setToolbarTitle(title: String) {
        supportActionBar?.title = title
    }

    override fun onBackPressed() {
        if (tabFragmentsStack.size > 1) {
            // Only pop if more than 1 fragment on stack (i.e. always leave a root fragment)

            tabFragmentsStack.pop()
            supportFragmentManager.popBackStack()

        } else if (tabFragmentsStack.size == 1) {
            // Return to launcher
            val launcherAction = Intent(Intent.ACTION_MAIN)
            launcherAction.apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            startActivity(launcherAction)
        }
    }

    override fun onBackStackChanged() {
        val canGoBack = supportFragmentManager.backStackEntryCount > 0

        supportActionBar?.apply {
            /* When a fragment is added to the stack, show the back button in the app bar,
               as the user can navigate back to a previous fragment. */
            setDisplayHomeAsUpEnabled(canGoBack)
            setDisplayShowHomeEnabled(canGoBack)
        }

        updateAppBarState()
    }

    override fun onSupportNavigateUp(): Boolean {
        if (tabFragmentsStack.size > 0)
            tabFragmentsStack.pop()

        supportFragmentManager.popBackStack()

        return true
    }

    private fun showElection() {
        @IdRes val id = R.id.navigation_election

        if (navigationView.selectedItemId != id)
            navigationView.selectedItemId = id
    }

    @UiThread
    private fun showFriends() = presentContent(FriendsFragment())

    override fun onCreateOptionsMenu(menu: Menu) = false

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (!navigationView.isEnabled)
            return false

        val fragment: Fragment = when (item.itemId) {
            R.id.navigation_feed -> FeedFragment()
            R.id.navigation_poll -> PollsFragment()
            R.id.navigation_vote -> VoteViewPagerFragment()
            R.id.navigation_profile -> ProfileViewPagerFragment()
            R.id.navigation_election -> ElectionMasterFragment()
            else -> return false
        }

        fragment
            .takeIf { currentFragment == null || it::class != currentFragment!!::class }
            ?.let {
                if (currentFragment is FriendsFragment) {
                    supportFragmentManager.popBackStackImmediate(
                        null,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )

                    tabFragmentsStack.clear()
                    appRouter.setFriendsScreenVisible(false)
                }

                presentRootContent(fragment)
            }

        return true
    }

    @UiThread
    fun presentContent(fragment: Fragment) {
        tabFragmentsStack.push(fragment)

        supportFragmentManager.apply {
            beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit()

            executePendingTransactions()
        }
    }

    @UiThread
    private fun presentRootContent(fragment: Fragment) {
        tabFragmentsStack.clear()
        tabFragmentsStack.push(fragment)

        supportFragmentManager.apply {
            popBackStack()

            beginTransaction()
                .replace(R.id.container, fragment)
                .commitNow() // commit() + executePendingTransactions()
        }

        updateAppBarState()
    }

    private fun updateAppBarState() {
        currentFragment?.let {
            runOnUiThread {
                if (it is TitledFragment) {
                    val fragmentTitle =
                        (it as TitledFragment).getTitle(this@MainActivity)

                    if (!fragmentTitle.isNullOrBlank())
                        setToolbarTitle(fragmentTitle!!)
                }

                if (it is TabbedFragment) {
                    tabLayout.clearOnTabSelectedListeners()
                    (it as TabbedFragment).setupTabLayout(tabLayout)
                }
            }
        }
    }

    internal fun showSnackbar(@StringRes stringRes: Int, length: Int = Snackbar.LENGTH_LONG ) {
        Snackbar.make(coordinator_layout, stringRes, length).show()
    }
}