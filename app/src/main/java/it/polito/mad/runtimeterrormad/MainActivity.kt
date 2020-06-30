package it.polito.mad.runtimeterrormad

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import it.polito.mad.runtimeterrormad.data.Profile
import it.polito.mad.runtimeterrormad.ui.Constants
import it.polito.mad.runtimeterrormad.viewmodels.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*

class MainActivity : AppCompatActivity() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val authVM: AuthViewModel by viewModels { AuthViewModelFactory(firebaseAuth) }
    private lateinit var profileVM: ProfileViewModel
    private lateinit var itemListVM: ItemListViewModel
    private lateinit var onSaleListVM: OnSaleListViewModel
    private lateinit var boughtItemListVM: BoughtItemsListViewModel
    private lateinit var profileObserver: Observer<Profile>
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme_NoActionBar)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.itemListFragment,
                R.id.showProfileFragment,
                R.id.onSaleListFragment,
                R.id.itemsOfInterestListFragment,
                R.id.boughtItemsListFragment
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        onSaleListVM = ViewModelProvider(this).get(OnSaleListViewModel::class.java)

        authVM.getUserID().observe(this, Observer {
            if (it == Constants.EMPTY_UID)
                setSignedOutUI()
            else
                setSignedInUI(it)
        })

        setNavControllerListener()
        initFirebaseMessaging()
    }

    private fun initFirebaseMessaging() {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FIREBASE", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }
            })
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        hideKeyboard()
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun setNavControllerListener() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.onSaleListFragment -> {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    mainActivityFAB.hide()
                }

                R.id.showProfileFragment -> {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    mainActivityFAB.hide()
                }
                R.id.editProfileFragment -> {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    mainActivityFAB.hide()
                }
                R.id.itemDetailsFragment -> {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    mainActivityFAB.hide()
                }
                R.id.itemEditFragment -> {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    mainActivityFAB.hide()
                }
                R.id.itemListFragment -> {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    mainActivityFAB.setImageResource(R.drawable.ic_add_item)
                    mainActivityFAB.setOnClickListener {
                        navController.navigate(
                            R.id.action_itemListFragment_to_itemEditFragment,
                            bundleOf(Constants.BUNDLE_ITEM_ID to "")
                        )
                    }
                    mainActivityFAB.show()
                }
                R.id.showOtherProfileFragment -> {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    mainActivityFAB.hide()
                }
                R.id.itemsOfInterestListFragment -> {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    mainActivityFAB.hide()
                }
                R.id.boughtItemsListFragment -> {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    mainActivityFAB.hide()
                }
                R.id.mapFragment -> {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    mainActivityFAB.hide()
                }
            }
        }
    }


    private fun setSignedOutUI() {
        if (this::profileVM.isInitialized && this::profileObserver.isInitialized) {
            profileVM.getProfile().removeObserver(profileObserver)
            navView.getHeaderView(0).findViewById<TextView>(R.id.fullnameNavHeaderTV).text =
                getString(R.string.fullname)
            navView.getHeaderView(0).findViewById<TextView>(R.id.emailNavHeaderTV).text =
                getString(R.string.email)
            navView.getHeaderView(0).findViewById<ImageView>(R.id.imageView)
                .setImageResource(R.drawable.ic_profile_image)
        }
        val navMenu = navView.menu
        navMenu.findItem(R.id.itemListFragment).isVisible = false
        navMenu.findItem(R.id.showProfileFragment).isVisible = true
        navMenu.findItem(R.id.onSaleListFragment).isVisible = true
        navMenu.findItem(R.id.itemsOfInterestListFragment).isVisible = false
        navMenu.findItem(R.id.boughtItemsListFragment).isVisible = false
    }

    private fun setSignedInUI(userID: String) {
        profileVM = ViewModelProvider(
            this,
            ProfileViewModelFactory(userID)
        ).get(ProfileViewModel::class.java)

        itemListVM = ViewModelProvider(
            this,
            ItemListViewModelFactory(userID)
        ).get(ItemListViewModel::class.java)

        boughtItemListVM = ViewModelProvider(
            this,
            BoughtItemsListViewModelFactory(userID)
        ).get(BoughtItemsListViewModel::class.java)

        profileObserver = Observer {
            if (it.fullname.isNotEmpty()) {
                navView.getHeaderView(0).findViewById<TextView>(R.id.fullnameNavHeaderTV).text =
                    it.fullname
                navView.getHeaderView(0).findViewById<TextView>(R.id.emailNavHeaderTV).text =
                    it.email
            }
            if (it.image.isNotEmpty()) {
                val imageView = navView.getHeaderView(0).findViewById<ImageView>(R.id.imageView)
                Glide.with(this)
                    .load(it.image)
                    .into(imageView)
            }
        }
        profileVM.getProfile().observe(this, profileObserver)
        val navMenu = navView.menu
        navMenu.findItem(R.id.itemListFragment).isVisible = true
        navMenu.findItem(R.id.showProfileFragment).isVisible = true
        navMenu.findItem(R.id.onSaleListFragment).isVisible = true
        navMenu.findItem(R.id.itemsOfInterestListFragment).isVisible = true
        navMenu.findItem(R.id.boughtItemsListFragment).isVisible = true
    }


    private fun hideKeyboard() {
        currentFocus?.let { view ->
            getSystemService(Context.INPUT_METHOD_SERVICE)?.let {
                it as InputMethodManager
                it.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
    }
}
