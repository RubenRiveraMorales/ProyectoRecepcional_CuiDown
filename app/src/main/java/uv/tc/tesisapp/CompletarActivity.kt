package uv.tc.tesisapp

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import uv.tc.tesisapp.pojo.Usuario
import uv.tc.tesisapp.pojo.recompensa
import java.io.File

class CompletarActivity : AppCompatActivity() {
    private lateinit var player: SimpleExoPlayer
    private lateinit var nombreAct: TextView
    private lateinit var usuario:Usuario


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_completar)
        usuario = intent.getSerializableExtra("usuario") as Usuario
        nombreAct= findViewById(R.id.tv_nombre_actividad)
        val nombreActividad = intent.getStringExtra("actividad")
        val storageRef = FirebaseStorage.getInstance().reference
        val videoRef = storageRef.child("$nombreActividad.mp4")
        nombreAct.text= nombreActividad.toString()
        val localFile = File.createTempFile("videos", "mp4")

        videoRef.getFile(localFile).addOnSuccessListener {
            initializeExoPlayer(localFile.absolutePath)
        }.addOnFailureListener {
            Toast.makeText(this, "Error al cargar el video, por favor inténtelo más tarde", Toast.LENGTH_LONG).show()
        }

        findViewById<Button>(R.id.Finalizar).setOnClickListener {
            Log.d("Boton finalizar clickeado","clic")
            val usuarioId = obtenerIdUsuarioActual()
            guardarActividadCompletada(usuarioId)
            Toast.makeText(this,"Actividad completada, puntos recibidos",Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun initializeExoPlayer(videoPath: String) {
        player = SimpleExoPlayer.Builder(this).build()
        val mediaItem = MediaItem.fromUri(videoPath)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
        val playerView = findViewById<PlayerView>(R.id.playerView)
        playerView.player = player
    }

    private fun obtenerIdUsuarioActual(): String {
        val usuarioId = usuario.idUsuario
        return usuarioId ?: ""
    }

    private fun guardarActividadCompletada(idUsuario: String) {
        val db = FirebaseFirestore.getInstance()
        val recompensasRef = db.collection("recompensa").document(idUsuario)
        recompensasRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Si el documento existe, realizar la transacción para actualizar las actividades y puntos
                db.runTransaction { transaction ->
                    val snapshot = transaction.get(recompensasRef)

                    val actividadesAnteriores = snapshot.getLong("actividades") ?: 0
                    val puntosAnteriores = snapshot.getLong("puntos") ?: 0
                    val nuevasActividades = actividadesAnteriores + 1
                    val nuevosPuntos = puntosAnteriores + 1

                    transaction.update(recompensasRef, "actividades", nuevasActividades)
                    transaction.update(recompensasRef, "puntos", nuevosPuntos)
                    Log.d(TAG, "Recompensa actualizada para usuario $idUsuario: Actividades: $nuevasActividades, Puntos: $nuevosPuntos")
                    null
                }.addOnSuccessListener {
                    Log.d(TAG, "Transacción completada con éxito.")
                }.addOnFailureListener { e ->
                    Log.w(TAG, "Error al actualizar la recompensa", e)
                }
            } else {
                // Si el documento no existe, crear uno nuevo con las actividades y puntos iniciales
                val nuevaRecompensa = hashMapOf(
                    "actividades" to 1,
                    "puntos" to 1
                )
                recompensasRef.set(nuevaRecompensa)
                    .addOnSuccessListener {
                        Log.d(TAG, "Nuevo documento de recompensa creado para usuario $idUsuario")
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error al crear el nuevo documento de recompensa", e)
                    }
            }
        }.addOnFailureListener { e ->
            Log.w(TAG, "Error al obtener el documento de recompensa", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}
