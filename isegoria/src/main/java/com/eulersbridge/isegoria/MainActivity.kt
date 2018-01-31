package com.eulersbridge.isegoria


import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.content.Intent
import android.content.pm.ShortcutManager
import android.os.Build
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.eulersbridge.isegoria.auth.AuthActivity
import com.eulersbridge.isegoria.auth.login.EmailVerificationFragment
import com.eulersbridge.isegoria.election.ElectionMasterFragment
import com.eulersbridge.isegoria.feed.FeedFragment
import com.eulersbridge.isegoria.friends.FriendsFragment
import com.eulersbridge.isegoria.poll.PollsFragment
import com.eulersbridge.isegoria.profile.ProfileViewPagerFragment
import com.eulersbridge.isegoria.util.ui.TitledFragment
import com.eulersbridge.isegoria.vote.VoteViewPagerFragment
import com.google.firebase.FirebaseApp
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.main_partial_appbar.*
import java.util.*

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener,
    FragmentManager.OnBackStackChangedListener {

    private var tabFragmentsStack: ArrayDeque<Fragment> = ArrayDeque(4)
    private val currentFragment: Fragment?
        get() = if (tabFragmentsStack.size == 0) null else tabFragmentsStack.peekFirst()

    private val navigationView: BottomNavigationViewEx
        get() = navigation

    interface TabbedFragment {
        fun setupTabLayout(tabLayout: TabLayout)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null)
            FirebaseApp.initializeApp(this)

        setContentView(R.layout.activity_main)

        setupNavigation()

        val application = applicationContext as IsegoriaApp
        setupApplicationObservers(application)

        application.login().observe(this, Observer { success ->
            if (success == true)
                onLoginSuccess()
        })
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

    private fun setupApplicationObservers(application: IsegoriaApp) {
        application.loggedInUser.observe(this, Observer { user ->
            val userLoggedOut = user == null

            if (userLoggedOut) {
                finish()

            } else {
                onLoginSuccess()
            }
        })

        application.loginVisible.observe(this, Observer {
            if (it == true)
                startActivity(Intent(this, AuthActivity::class.java))
        })

        application.userVerificationVisible.observe(this, Observer {
            if (it == true)
                presentRootContent(EmailVerificationFragment())
        })

        application.friendsVisible.observe(this, Observer {
            if (it == true)
                showFriends()
        })
    }

    private fun onLoginSuccess() = navigationView.apply {
        if (!isEnabled) {
            isEnabled = true

            if (!handleAppShortcutIntent())
                selectedItemId = R.id.navigation_feed
        }
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
                        val app = application as IsegoriaApp
                        app.friendsVisible.value = true
                        handledShortcut = true
                    }
                }

                if (handledShortcut) {
                    val shortcutManager: ShortcutManager = getSystemService(ShortcutManager::class.java)
                    shortcutManager.reportShortcutUsed(it)
                }
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
            launcherAction.addCategory(Intent.CATEGORY_HOME)
            launcherAction.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(launcherAction)
        }
    }

    override fun onBackStackChanged() {
        val canGoBack = supportFragmentManager.backStackEntryCount > 0

        supportActionBar?.let {
            /* When a fragment is added to the stack, show the back button in the app bar,
               as the user can navigate back to a previous fragment. */
            it.setDisplayHomeAsUpEnabled(canGoBack)
            it.setDisplayShowHomeEnabled(canGoBack)
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

    private fun showFriends() = presentContent(FriendsFragment())

    override fun onCreateOptionsMenu(menu: Menu) = false

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        val fragment: Fragment = when (item.itemId) {
            R.id.navigation_feed -> FeedFragment()
            R.id.navigation_poll -> PollsFragment()
            R.id.navigation_vote -> VoteViewPagerFragment()
            R.id.navigation_profile -> ProfileViewPagerFragment()
            R.id.navigation_election -> ElectionMasterFragment()
            else -> return true
        }

        if (currentFragment == null || fragment::class != currentFragment!!::class) {

            val app = application as IsegoriaApp

            if (app.friendsVisible.value == true) {
                supportFragmentManager.popBackStackImmediate(
                    null,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
                tabFragmentsStack.clear()
                app.friendsVisible.value = false
            }

            presentRootContent(fragment)
        }

        return true
    }

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
}