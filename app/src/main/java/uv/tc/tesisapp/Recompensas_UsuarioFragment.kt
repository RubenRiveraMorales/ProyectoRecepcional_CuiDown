package uv.tc.tesisapp

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import uv.tc.tesisapp.pojo.Usuario
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Recompensas_UsuarioFragment : Fragment() {

    private lateinit var usuarioId: String
    private lateinit var actividadesRealizadas: EditText
    private lateinit var puntosCantidad: TextView
    private lateinit var tvUsuario: TextView
    private lateinit var tvProgreso: TextView
    private lateinit var descargar: ImageView
    private lateinit var compartir: ImageView
    private lateinit var ivPerfil:ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var usuario: Usuario

    companion object {
        fun newInstance(usuario: Usuario?): Recompensas_UsuarioFragment {
            val fragment = Recompensas_UsuarioFragment()
            val bundle = Bundle()
            bundle.putSerializable("usuario", usuario)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recompensas_usuario, container, false)
        usuario = arguments?.getSerializable("usuario") as Usuario
        actividadesRealizadas = view.findViewById(R.id.edActividades)
        puntosCantidad = view.findViewById(R.id.tvCantidadPuntos)
        descargar = view.findViewById(R.id.descargar_reward)
        progressBar = view.findViewById(R.id.progreso)
        tvUsuario= view.findViewById(R.id.tvNombre)
        tvProgreso= view.findViewById(R.id.tvProgreso)
        compartir= view.findViewById(R.id.ivCompartir)
        ivPerfil= view.findViewById(R.id.ivPerfil)
        usuarioId = obtenerIdUsuarioActual()
        Log.d(TAG, "ID del usuario actual: $usuarioId")
        obtenerYMostrarRecompensas()
        tvUsuario.text = usuario.nombre


        ivPerfil.setOnClickListener {
            val opciones = arrayOf("avatar_1", "avatar_2","avatar_3", "avatar_4","avatar_5","avatar_6") // Nombre de las imágenes de perfil
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Elige una imagen de perfil")
            builder.setItems(opciones) { dialog, which ->
                val imagenSeleccionada = opciones[which]
                // Establece la imagen seleccionada en el ImageView
                val resourceId = resources.getIdentifier(imagenSeleccionada, "drawable", requireContext().packageName)
                ivPerfil.setImageResource(resourceId)
                Toast.makeText(requireContext(), "Imagen de perfil seleccionada: $imagenSeleccionada", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            builder.show()
        }

        descargar.setOnClickListener {
            val puntos = puntosCantidad.text.toString().toInt()
            val carpeta = when {
                puntos >= 60 -> "nivel_45"
                puntos >= 48 -> "nivel_30"
                puntos >= 36 -> "nivel_15"
                else -> ""
            }
            if (carpeta.isNotEmpty()) {
                descargarImagen(carpeta, puntos, descargar, requireContext())
                Toast.makeText(requireContext(),"Se ha descargado su recompensa",Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), "No tienes suficientes puntos para descargar una recompensa.", Toast.LENGTH_SHORT).show()
            }
        }
        compartir.setOnClickListener {
            compartirImagen()
        }



        return view
    }

    override fun onResume() {
        super.onResume()
        obtenerYMostrarRecompensas()
    }

    private fun obtenerIdUsuarioActual(): String {
        val usuarioId = usuario.idUsuario
        return usuarioId ?: ""
    }

    private fun obtenerYMostrarRecompensas() {
        val db = FirebaseFirestore.getInstance()
        val recompensasRef = db.collection("recompensa").document(usuarioId)
        recompensasRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val actividades = document.getLong("actividades") ?: 0
                    val puntos = document.getLong("puntos") ?: 0
                    actividadesRealizadas.text = Editable.Factory.getInstance().newEditable(actividades.toString())
                    puntosCantidad.text = puntos.toString()
                    val progreso = (puntos.toDouble() / 48.0 * 100).toInt()
                    progressBar.progress = progreso
                    val porcentajeActividades = (puntos.toDouble() / 48.0 * 100).toInt()
                    tvProgreso.text = "$porcentajeActividades%"
                } else {
                    val nuevoDocumento = hashMapOf(
                        "actividades" to 0,
                        "puntos" to 0
                    )
                    db.collection("recompensa").document(usuarioId)
                        .set(nuevoDocumento)
                        .addOnSuccessListener {
                            Log.d(TAG, "Documento de recompensa creado para el usuario con ID: $usuarioId")
                            obtenerYMostrarRecompensas()
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error al crear el documento de recompensa para el usuario con ID: $usuarioId", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error al obtener las recompensas del usuario con ID: $usuarioId", e)
            }
    }


    private fun descargarImagen(carpeta: String, puntos: Int, imageView: ImageView, context: Context) {
        val nombreImagen = when {
            puntos >= 60 -> "cat_45.png"
            puntos >= 48 -> "bowser_30.png"
            puntos >= 36 -> "unicorn_15.png"
            else -> ""
        }
        if (nombreImagen.isNotEmpty()) {
            val storageRef = FirebaseStorage.getInstance().reference.child("recompensas/$carpeta/$nombreImagen")

            val localFile = File.createTempFile("imagen_recompensa", "png")

            storageRef.getFile(localFile).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                imageView.setImageBitmap(bitmap)
                // Guardar la imagen en el almacenamiento compartido
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "imagen_recompensa.png")
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Error al descargar la imagen de recompensa.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "No se encontró una imagen disponible para tu nivel de puntos.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveImageToStorage(imageFile: File): String {
        val directory = File(requireContext().filesDir, "recompensas")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        // copia el archivo a la carpeta de almacenamiento interno
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "imagen_$timeStamp.png"
        val destinationFile = File(directory, fileName)
        try {
            val inputStream = FileInputStream(imageFile)
            val outputStream = FileOutputStream(destinationFile)
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }
            outputStream.flush()
            outputStream.close()
            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return destinationFile.absolutePath
    }

    private fun compartirImagen() {
        val bitmap = (descargar.drawable as? BitmapDrawable)?.bitmap // Obtener el bitmap de la imagen descargada
        if (bitmap != null) {
            val file = saveBitmapToFile(bitmap) // Guardar el bitmap en un archivo
            if (file != null) {
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.provider",
                    file
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM, uri)
                }
                startActivity(Intent.createChooser(intent, "Compartir imagen"))
            } else {
                Toast.makeText(requireContext(), "Error al guardar la imagen", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "No se encontró una imagen para compartir", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap): File? {
        val fileName = "imagen_compartir.png"
        val directory = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File(directory, fileName)
        return try {
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                file
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

}