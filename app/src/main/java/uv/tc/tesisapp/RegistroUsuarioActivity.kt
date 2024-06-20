package uv.tc.tesisapp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import uv.tc.tesisapp.databinding.ActivityRegistroUsuarioBinding
import kotlin.random.Random

class RegistroUsuarioActivity : AppCompatActivity() {
    lateinit var binding: ActivityRegistroUsuarioBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroUsuarioBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        binding.btnRegistarUsuario.setOnClickListener {
            // ... (otros códigos)

            val generoSeleccionado = binding.spSexo.selectedItem.toString()
            val idUsuarioAdmin = FirebaseAuth.getInstance().currentUser?.uid
            val codigoGenerado = generarCodigoAleatorio(8)

            if (validarCamposRegistro()) {
                val db = FirebaseFirestore.getInstance()

                // Generar un ID único para el usuario
                val usuarioId = db.collection("usuario").document().id
                val usuarioDocument = db.collection("usuario").document(usuarioId)

                val datosUsuario = mapOf(
                    "idUsuario" to usuarioId,
                    "nombre" to binding.edNombreCompleto.text.toString(),
                    "nombreUsuario" to binding.edUserName.text.toString(),
                    "edad" to binding.edEdad.text.toString(),
                    "genero" to generoSeleccionado,
                    "codigo" to codigoGenerado,
                    "idUsuarioAdmin" to idUsuarioAdmin // Agrega el ID del usuario administrador
                )

                // Almacenar datos en Firestore
                usuarioDocument.set(datosUsuario)
                    .addOnSuccessListener {
                        mostrarAlertaRegistroExitoso(codigoGenerado)
                        Toast.makeText(this@RegistroUsuarioActivity, "Registro Exitoso", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener { e ->
                        mostrarAlerta("Error al guardar los datos")
                    }
            } else {
                mostrarAlerta("Por favor, complete todos los campos.")
            }
        }

    }

    private fun validarCamposRegistro(): Boolean {
        var camposValidos = true
        binding.edNombreCompleto.text.toString()
        binding.edEdad.text.toString()
        binding.edUserName.text.toString()

        try {
            if ( binding.edNombreCompleto.text.isEmpty()) {
                camposValidos = false
                binding.edNombreCompleto.error = "Este campo es obligatorio"
            }

            if ( binding.edEdad.text.isEmpty()) {
                camposValidos = false
                binding.edEdad.error= "Este campo es obligatorio"
            }
            if ( binding.edUserName.text.isEmpty()) {
                camposValidos = false
                binding.edUserName.error = "Este campo es obligatorio"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            error(true)
        }

        return camposValidos
    }


    private fun mostrarAlerta(mensaje: String) {
        val builder = AlertDialog.Builder(this@RegistroUsuarioActivity)
        builder.setTitle("ERROR")
        builder.setMessage(mensaje)
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun mostrarAlertaRegistroExitoso(codigoGenerado: String) {
        val builder = AlertDialog.Builder(this@RegistroUsuarioActivity)
        builder.setTitle("Registro Exitoso")
        builder.setMessage("Su registro ha sido exitoso. A continuación, se muestra su código:\n\n$codigoGenerado")

        builder.setPositiveButton("Copiar Código") { _, _ ->
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Código Generado", codigoGenerado)
            clipboard.setPrimaryClip(clip)

            val usuarioFragment = supportFragmentManager.findFragmentById(R.id.usuarioFragment) as? UsuarioFragment
            usuarioFragment?.actualizarDatos()

            // Cerrar la actividad después de realizar la transacción del fragmento
            finish()

            Toast.makeText(this@RegistroUsuarioActivity, "Código copiado al portapapeles", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("Aceptar") { _, _ ->
            // Llamar al método de actualización en el fragmento
            val usuarioFragment = supportFragmentManager.findFragmentById(R.id.usuarioFragment) as? UsuarioFragment
            usuarioFragment?.actualizarDatos()

            // Cerrar la actividad después de realizar la transacción del fragmento
            finish()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }


    private fun setupRecyclerViewOrReplaceFragment() {
        val usuarioFragment = UsuarioFragment()

        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()

        transaction.replace(R.id.usuarioFragment, usuarioFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun generarCodigoAleatorio(longitud: Int): String {
        val alfanumerico = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val codigo = StringBuilder()

        repeat(longitud) {
            val randomIndex = Random.nextInt(alfanumerico.length)
            codigo.append(alfanumerico[randomIndex])
        }

        return codigo.toString()
    }

}