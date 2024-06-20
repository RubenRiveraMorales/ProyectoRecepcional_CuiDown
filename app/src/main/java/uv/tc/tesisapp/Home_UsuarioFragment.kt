package uv.tc.tesisapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import uv.tc.tesisapp.adapter.AlarmAdapter
import uv.tc.tesisapp.adapter.AlarmUsuariosAdapter
import uv.tc.tesisapp.pojo.Alarma
import uv.tc.tesisapp.pojo.Usuario
import uv.tc.tesisapp.service.AlarmReceiver
import java.util.Calendar


class Home_UsuarioFragment : Fragment(), AlarmUsuariosAdapter.OnAlarmClickListener {

    private lateinit var usuario: Usuario
    private lateinit var adapter: AlarmUsuariosAdapter

    companion object {
        fun newInstance(usuario: Usuario?): Home_UsuarioFragment {
            val fragment = Home_UsuarioFragment()
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
        val view = inflater.inflate(R.layout.fragment_home__usuario, container, false)
        usuario = arguments?.getSerializable("usuario") as Usuario
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_alarmas_usuario)
        adapter = AlarmUsuariosAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        obtenerAlarmasAsociadas()

        return view
    }

    private fun obtenerAlarmasAsociadas() {
        val alarmasIds = usuario.alarmasAsociadas
        val db = FirebaseFirestore.getInstance()
        val alarmasCollection = db.collection("alarma")

        alarmasCollection
            .whereIn("id", alarmasIds)
            .get()
            .addOnSuccessListener { result ->
                val alarmas = result.toObjects(Alarma::class.java)
                adapter.actualizarAlarmas(alarmas)

                // Configurar notificaciones para cada alarma
                for (alarma in alarmas) {
                    configurarNotificacion(alarma)
                }
            }
            .addOnFailureListener { exception ->
            }
    }

    private fun configurarNotificacion(alarma: Alarma) {
        Log.d("Notificaciones", "Configurando notificación para alarma: $alarma")
        val hora = alarma.hora
        val dias = alarma.diasRepeticion  // Obtener los días seleccionados del documento
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        for (dia in dias) {
            val intent = Intent(context, AlarmReceiver::class.java)
            intent.putExtra(AlarmReceiver.ALARMA_EXTRA, alarma)

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                dia.hashCode(),  // Utilizar algún identificador único para cada día
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val horaSplit = hora.split(":")
            val horas = horaSplit[0].toInt()
            val minutos = horaSplit[1].toInt()

            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, horas)
            calendar.set(Calendar.MINUTE, minutos)
            calendar.set(Calendar.SECOND, 0)

            try {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY * 7,
                    pendingIntent
                )
            } catch (e: SecurityException) {
                Log.e("Notificaciones", "Error al configurar alarma repetitiva: $e")
            }
        }
    }

    override fun onAlarmClick(alarma: Alarma) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("¿Estás seguro de que deseas iniciar esta actividad?")
            .setPositiveButton("Sí") { dialog, id ->
                iniciarActividadCompletar(alarma)
            }
            .setNegativeButton("No") { dialog, id ->
            }
        val dialog = builder.create()
        dialog.show()
    }

    private fun iniciarActividadCompletar(alarma: Alarma) {
        val intent = Intent(requireContext(), CompletarActivity::class.java)
        intent.putExtra("actividad", alarma.actividad)
        intent.putExtra("usuario",usuario)
        startActivity(intent)
    }



}

