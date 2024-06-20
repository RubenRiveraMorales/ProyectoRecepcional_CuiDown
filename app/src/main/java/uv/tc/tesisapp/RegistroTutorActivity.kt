package uv.tc.tesisapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import uv.tc.tesisapp.databinding.ActivityRegistroTutorBinding

class RegistroTutorActivity : AppCompatActivity() {
    lateinit var binding: ActivityRegistroTutorBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroTutorBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        auth = FirebaseAuth.getInstance()


        val rolAdapter = ArrayAdapter.createFromResource(this, R.array.roles, android.R.layout.simple_spinner_item)
        val generoAdapter = ArrayAdapter.createFromResource(this, R.array.generos, android.R.layout.simple_spinner_item)


        rolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        generoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)




    binding.btnRegistrar.setOnClickListener {

       val email = binding.edCorreo.text.toString().trim()
       val password = binding.edPass.text.toString().trim()

       if (validarCamposRegistro()) {
           auth.createUserWithEmailAndPassword(email, password)
               .addOnCompleteListener(this) { task: Task<AuthResult> ->
                   if (task.isSuccessful) {
                       // Registro en Authentication exitoso
                       val db = FirebaseFirestore.getInstance()
                       val uidUsuario = auth.currentUser?.uid
                       val usuarioDocument = db.collection("administrador").document(uidUsuario ?: "")

                       val datosUsuario = mapOf(
                           "nombre" to binding.edNombre.text.toString(),
                           "nombreUsuario" to binding.edNombre2.text.toString(),
                           "edad" to binding.edEdad.text.toString(),
                           "correo" to email,
                           "ciudad" to binding.edCiudad.text.toString(),
                           "codigoPostal" to binding.edCodigoPostal.text.toString(),
                           "direccion" to binding.edDireccion.text.toString(),
                           "password" to password,
                           "rol" to binding.spRol.selectedItem.toString(),
                           "genero" to binding.spSexo.selectedItem.toString()
                       )

                       // Almacenar datos en Firestore
                       usuarioDocument.set(datosUsuario)
                           .addOnSuccessListener {
                               Toast.makeText(this@RegistroTutorActivity, "Registro Exitoso", Toast.LENGTH_LONG).show()
                               val intent=Intent(this,LoginActivity::class.java)
                               startActivity(intent)
                           }
                           .addOnFailureListener { e ->
                               mostrarAlerta("Error al guardar los datos en Firestore")
                           }
                   } else {
                       mostrarAlerta("Error al crear la cuenta: ${task.exception?.message}")
                   }
               }
       } else {
           mostrarAlerta("Por favor, complete todos los campos.")
       }
    }
    }


    private fun validarCamposRegistro(): Boolean {
        var camposValidos = true
        binding.edNombre.text.toString()
        binding.edEdad.text.toString()
        binding.edNombre2.text.toString()
        binding.edPass.text.toString()
        binding.edDireccion.toString()
        binding.edCodigoPostal.toString()
        binding.edCiudad.toString()
        binding.edCorreo.toString()
        try {
           if ( binding.edNombre.text.isEmpty()) {
               camposValidos = false
               binding.edNombre.error = "Este campo es obligatorio"
           }

           if ( binding.edNombre2.text.isEmpty()) {
               camposValidos = false
               binding.edNombre2.error= "Este campo es obligatorio"
           }
           if ( binding.edCorreo.text.isEmpty()) {
               camposValidos = false
               binding.edCorreo.error = "Este campo es obligatorio"
           }
           if ( binding.edEdad.text.isEmpty()) {
               camposValidos = false
               binding.edDireccion.error = "Este campo es obligatorio"
           }
           if (binding.edCiudad.text.isEmpty()) {
               camposValidos = false
               binding.edCiudad.error = "Este campo es obligatorio"
           }
           if (binding.edCodigoPostal.text.isEmpty()) {
               camposValidos = false
               binding.edCodigoPostal.error = "Este campo es obligatorio"
           }
           if ( binding.edPass.text.isEmpty()) {
               camposValidos = false
               binding.edPass.error = "Este campo es obligatorio"
           }
        } catch (e: Exception) {
           e.printStackTrace()
           error(true)
        }

        return camposValidos
        }


        private fun mostrarAlerta(mensaje: String) {
        val builder = AlertDialog.Builder(this@RegistroTutorActivity)
        builder.setTitle("ERROR")
        builder.setMessage(mensaje)
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
        }



}
