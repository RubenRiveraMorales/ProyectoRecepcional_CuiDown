package uv.tc.tesisapp

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import uv.tc.tesisapp.databinding.ActivityMainUsuarioBinding
import uv.tc.tesisapp.pojo.Usuario


class MainActivity_Usuario : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var fragmentManager: FragmentManager
    lateinit var binding: ActivityMainUsuarioBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var usuario: Usuario? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainUsuarioBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        usuario = intent.getSerializableExtra("usuario") as? Usuario

        setSupportActionBar(binding.toolbar)
        val toggle = ActionBarDrawerToggle(this, binding.drawerLayoutUsuario, binding.toolbar, R.string.nav_open, R.string.nav_close)
        binding.drawerLayoutUsuario.addDrawerListener(toggle)
        toggle.syncState()
        binding.bottomNavigationUsuario.background = null

        fragmentManager = supportFragmentManager
        openFragment(Home_UsuarioFragment.newInstance(usuario))


        binding.bottomNavigationUsuario.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_homeUser -> openFragment(Home_UsuarioFragment.newInstance(usuario))
                R.id.bottom_actividad -> openFragment(ActividadesFragment())
                R.id.bottom_recompensasUser -> openFragment(Recompensas_UsuarioFragment.newInstance(usuario))
            }
            true
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.bottom_homeUser -> openFragment(Home_UsuarioFragment.newInstance(usuario))
            R.id.bottom_actividad -> openFragment(ActividadesFragment())
            R.id.bottom_recompensasUser -> openFragment(Recompensas_UsuarioFragment())
        }
        binding.drawerLayoutUsuario.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (binding.drawerLayoutUsuario.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayoutUsuario.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun openFragment(fragment: Fragment) {
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmet_container_usuario, fragment)
        fragmentTransaction.commit()
    }
}