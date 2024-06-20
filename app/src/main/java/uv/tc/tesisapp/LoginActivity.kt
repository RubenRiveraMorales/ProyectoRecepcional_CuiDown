package uv.tc.tesisapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import uv.tc.tesisapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        iniciarSesion()
    }

    private fun iniciarSesion() {
        binding.tvRegistro.setOnClickListener {
            val registroIntent = Intent(this, SeleccionarPerfilActivity::class.java)
            startActivity(registroIntent)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.edNombreUsuario.text.toString()
            val password = binding.edPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                mostrarAlerta("Por favor, complete todos los campos.")
            } else {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val idUsuarioAdmin = FirebaseAuth.getInstance().currentUser?.uid

                            // Pasar el ID del usuario administrador al método irPantallaPrincipal
                            irPantallaPrincipal(task.result?.user?.email ?: "", ProviderType.BASIC, idUsuarioAdmin)
                        } else {
                            mostrarAlerta("Correo o contraseña incorrecta, por favor inténtelo de nuevo.")
                        }
                    }
            }
        }
    }

    private fun mostrarAlerta(message: String) {
        val builder = AlertDialog.Builder(this@LoginActivity)
        builder.setTitle("ERROR")
        builder.setMessage(message)
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun irPantallaPrincipal(email: String, provider: ProviderType, idUsuarioAdmin: String?) {
        val homeIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("email", email)
            putExtra("provider", provider.name)
            putExtra("idUsuarioAdmin", idUsuarioAdmin)
        }
        startActivity(homeIntent)
    }
}