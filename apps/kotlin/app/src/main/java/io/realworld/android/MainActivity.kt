package io.realworld.android

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import io.realworld.android.databinding.ActivityMainBinding
import io.realworld.api.ConduitClient
import io.realworld.api.models.entities.User

class MainActivity : AppCompatActivity() {

    companion object {
        const val PREFS_FILE_AUTH = "prefs_auth"
        const val PREFS_KEY_TOKEN = "token"
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var authViewModel: AuthViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private var optionsMenu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            sharedPreferences = getSharedPreferences(PREFS_FILE_AUTH, Context.MODE_PRIVATE)
            authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "onCreate threw exception", e)
            throw RuntimeException("Activity startup failed", e)
        }
    }
    
    override fun onStart() {
        super.onStart()
        try {
            setSupportActionBar(binding.appBarMain.toolbar)

            val drawerLayout: DrawerLayout = binding.drawerLayout
            val navView: NavigationView = binding.navView
            val navController = findNavController(R.id.nav_host_fragment_content_main)
            
            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_feed,
                    R.id.nav_my_feed,
                    R.id.nav_auth
                ), drawerLayout
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            navView.setupWithNavController(navController)

            sharedPreferences.getString(PREFS_KEY_TOKEN, null)?.let { t ->
                authViewModel.getCurrentUser(t)
            }

            authViewModel.user.observe(this) {
                updateMenu(it)
                it?.token?.let { t ->
                    sharedPreferences.edit {
                        putString(PREFS_KEY_TOKEN, t)
                    }
                } ?: run {
                    sharedPreferences.edit {
                        remove(PREFS_KEY_TOKEN)
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "onStart threw exception", e)
            throw RuntimeException("onStart failed", e)
        }
    }
    
    override fun onResume() {
        super.onResume()
    }

    private fun updateMenu(user: User?) {
        when (user) {
            is User -> {
                binding.navView.menu.clear()
                binding.navView.inflateMenu(R.menu.menu_main_user)
            }
            else -> {
                binding.navView.menu.clear()
                binding.navView.inflateMenu(R.menu.menu_main_guest)
            }
        }
        optionsMenu?.findItem(R.id.action_search)?.isVisible = user != null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_logout -> {
                authViewModel.logout()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        optionsMenu = menu

        menu.findItem(R.id.action_search)?.isVisible = false

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? SearchView
        searchView?.queryHint = getString(R.string.action_search)
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val q = query?.trim() ?: return false
                if (q.isBlank()) return false
                searchItem.collapseActionView()
                val navController = findNavController(R.id.nav_host_fragment_content_main)
                if (ConduitClient.authToken == null) {
                    navController.navigate(R.id.nav_auth)
                } else {
                    navController.navigate(
                        R.id.action_global_search,
                        bundleOf("query" to q)
                    )
                }
                return true
            }

            override fun onQueryTextChange(newText: String?) = false
        })

        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}