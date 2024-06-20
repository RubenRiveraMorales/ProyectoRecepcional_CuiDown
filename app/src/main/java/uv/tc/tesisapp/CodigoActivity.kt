package uv.tc.tesisapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import uv.tc.tesisapp.databinding.ActivityCodigoBinding
import uv.tc.tesisapp.pojo.Usuario

 class CodigoActivity : AppCompatActivity() {
    lateinit var binding: ActivityCodigoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCodigoBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val btnVincular = findViewById<Button>(R.id.btnVincular)
        btnVincular.setOnClickListener {
            vincularCuenta()
        }
    }

    private fun vincularCuenta() {
        val codigoIngresado = findViewById<EditText>(R.id.edIngresarCodigo).text.toString()
        val db = FirebaseFirestore.getInstance()
        val usuariosCollection = db.collection("usuario")

        usuariosCollection
            .whereEqualTo("codigo", codigoIngresado)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val usuario = result.documents[0].toObject(Usuario::class.java)
                    abrirActividadPrincipal(usuario)
                } else {
                    mostrarMensajeError("Código no válido. Inténtalo de nuevo.")
                }
            }
            .addOnFailureListener { exception ->
                mostrarMensajeError("Error al verificar el código. Inténtalo de nuevo.")
            }
    }

     private fun abrirActividadPrincipal(usuario: Usuario?) {
         val intent = Intent(this, MainActivity_Usuario::class.java)
         intent.putExtra("usuario", usuario)
         startActivity(intent)
         finish()

         Toast.makeText(this, "¡Bienvenid@, ${usuario?.nombre}!", Toast.LENGTH_LONG).show()
     }
    private fun validarCamposRegistro(): Boolean {
        var camposValidos = true
        binding.edIngresarCodigo.text.toString()


        try {
            if ( binding.edIngresarCodigo.text.isEmpty()) {
                camposValidos = false
                binding.edIngresarCodigo.error = "Este campo es obligatorio"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            error(true)
        }

        return camposValidos
    }

    private fun mostrarMensajeError(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }
}