package uv.tc.tesisapp

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import uv.tc.tesisapp.databinding.ActivityVideoBinding
import uv.tc.tesisapp.pojo.recompensa
import uv.tc.tesisapp.service.AlarmReceiver
import java.io.File

class VideoActivity : AppCompatActivity() {
    private lateinit var player: SimpleExoPlayer
    private lateinit var nombreAct: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_completar)

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
        val usuarioId = FirebaseAuth.getInstance().currentUser?.uid
        return usuarioId ?: ""
    }

    private fun guardarActividadCompletada(idUsuario: String) {
        val db = FirebaseFirestore.getInstance()
        val recompensasRef = db.collection("recompensa").document(idUsuario)
        db.runTransaction { transaction ->
            // Obtener el documento de recompensas del usuario actual
            val snapshot = transaction.get(recompensasRef)
            val recompensa: recompensa

            if (snapshot.exists()) {
                val actividadesAnteriores = snapshot.getLong("actividades") ?: 0
                val puntosAnteriores = snapshot.getLong("puntos") ?: 0
                val nuevasActividades = actividadesAnteriores + 1
                val nuevosPuntos = puntosAnteriores + 1
                transaction.update(recompensasRef, "actividades", nuevasActividades)
                transaction.update(recompensasRef, "puntos", nuevosPuntos)

                recompensa = recompensa(idUsuario, nuevasActividades.toInt(), nuevosPuntos.toInt())
            } else {
                transaction.set(recompensasRef, recompensa(idUsuario, 1, 5))
                recompensa = recompensa(idUsuario, 1, 5)
            }
            Log.d(ContentValues.TAG, "Recompensa guardada para usuario $idUsuario: Actividades: ${recompensa.actividades}, Puntos: ${recompensa.puntos}")
            null
        }.addOnSuccessListener {
            Log.d(ContentValues.TAG, "Transacción completada con éxito.")
        }.addOnFailureListener { e ->
            Log.w(ContentValues.TAG, "Error al actualizar la recompensa", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}
