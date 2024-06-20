package uv.tc.tesisapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import uv.tc.tesisapp.databinding.ActivitySeleccionarPerfilBinding

class SeleccionarPerfilActivity : AppCompatActivity() {
    lateinit var binding: ActivitySeleccionarPerfilBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySeleccionarPerfilBinding.inflate(layoutInflater)
        val view =binding.root
        setContentView(view)

        binding.ivTutor.setOnClickListener {
            val loginIntent = Intent(this, RegistroTutorActivity::class.java)
            startActivity(loginIntent)
        }

        binding.ivUsuario.setOnClickListener {
            val intent = Intent(this,CodigoActivity::class.java)
            startActivity(intent)
        }


    }
}