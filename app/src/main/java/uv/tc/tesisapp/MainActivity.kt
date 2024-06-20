package uv.tc.tesisapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import uv.tc.tesisapp.databinding.ActivityMainBinding

enum class ProviderType{
    BASIC
}

class MainActivity : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener {
    lateinit var fragmetManager: FragmentManager
    lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val bundle = intent.extras
        val idUsuarioAdmin = intent.getStringExtra("idUsuarioAdmin")
        val homeFragment = HomeFragment().apply {
            arguments = Bundle().apply {
                putString("idUsuarioAdmin", idUsuarioAdmin)
            }
        }
        val email= bundle?.getString("email")
        val provider = bundle?.getString("provider")
        setSupportActionBar(binding.toolbar)
        val toggle= ActionBarDrawerToggle(this,binding.drawerLayout,binding.toolbar,R.string.nav_open,R.string.nav_close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navigationDrawer.setNavigationItemSelectedListener(this)
        binding.bottomNavigation.background=null
        binding.bottomNavigation.setOnItemSelectedListener {
                item ->
            when(item.itemId){
                R.id.bottom_home-> openFragment(HomeFragment())
                R.id.bottom_estadisticas-> openFragment(EstadisticasFragment())
                R.id.bottom_usuarios-> openFragment(UsuarioFragment())
            }
            true
        }

        fragmetManager=supportFragmentManager
        openFragment(HomeFragment())

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.bottom_home -> openFragment(HomeFragment())
            R.id.bottom_estadisticas -> openFragment(EstadisticasFragment())
            R.id.bottom_usuarios -> openFragment(UsuarioFragment())
        }
        return true
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)){
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }else{
            super.onBackPressed()
        }

    }

    private fun openFragment(fragment: Fragment){
        val fragmentTransation : FragmentTransaction = fragmetManager.beginTransaction()
        fragmentTransation.replace(R.id.fragmet_container,fragment)
        fragmentTransation.commit()
    }


}