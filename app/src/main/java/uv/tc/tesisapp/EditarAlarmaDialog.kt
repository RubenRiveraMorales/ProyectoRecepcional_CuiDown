package uv.tc.tesisapp

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FieldValue
import uv.tc.tesisapp.pojo.Alarma
import com.google.firebase.firestore.FirebaseFirestore
import uv.tc.tesisapp.Interfaces.OnAlarmAddedListener
import uv.tc.tesisapp.Interfaces.OnAlarmDeletedListener
import uv.tc.tesisapp.adapter.AlarmAdapter
import uv.tc.tesisapp.service.AlarmReceiver
import java.util.Calendar

class EditarAlarmaDialog(private var recyclerView: RecyclerView, private var alarmAdapter: AlarmAdapter, private val usuarioSeleccionado: String,) : DialogFragment() {

    private lateinit var edHora: EditText
    private var listaAlarmas: MutableList<Alarma> = mutableListOf()
    var isEditing: Boolean = false
    private lateinit var firestore: FirebaseFirestore
    private lateinit var btnGuardar: Button
    private lateinit var ivEliminar: ImageView
    private lateinit var spActividad: Spinner
    private lateinit var swActivar: Switch
    private lateinit var cbLunes: CheckBox
    private lateinit var cbMartes: CheckBox
    private lateinit var cbMiercoles: CheckBox
    private lateinit var cbJueves: CheckBox
    private lateinit var cbViernes: CheckBox
    private lateinit var cbSabado: CheckBox
    private lateinit var cbDomingo: CheckBox


    private var onAlarmAddedListener: OnAlarmAddedListener? = null
    private var onAlarmDeletedListener: OnAlarmDeletedListener? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_editar_alarma_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        edHora = view.findViewById(R.id.edHora)
        swActivar = view.findViewById(R.id.swActivar)
        spActividad = view.findViewById(R.id.spActividad)
        ivEliminar= view.findViewById(R.id.ivEliminar)
        cbLunes = view.findViewById(R.id.cbLunes)
        cbMartes = view.findViewById(R.id.cbMartes)
        cbMiercoles = view.findViewById(R.id.cbMiercoles)
        cbJueves = view.findViewById(R.id.cbJueves)
        cbViernes = view.findViewById(R.id.cbViernes)
        cbSabado = view.findViewById(R.id.cbSabado)
        cbDomingo = view.findViewById(R.id.cbDomingo)
        val args = arguments
        if (args != null) {
            val alarma = args.getSerializable("alarma") as Alarma

            edHora.setText(alarma.hora)
            spActividad.setSelection(obtenerPosicionItem(spActividad, alarma.actividad))
            swActivar.isChecked = alarma.estatus == "Encendida"
            cbLunes.isChecked = alarma.diasRepeticion.contains("lunes")
            cbMartes.isChecked = alarma.diasRepeticion.contains("martes")
            cbMiercoles.isChecked = alarma.diasRepeticion.contains("miercoles")
            cbJueves.isChecked = alarma.diasRepeticion.contains("jueves")
            cbViernes.isChecked = alarma.diasRepeticion.contains("viernes")
            cbSabado.isChecked = alarma.diasRepeticion.contains("sabado")
            cbDomingo.isChecked = alarma.diasRepeticion.contains("domingo")
            if (isEditing) {
                ivEliminar.visibility = View.VISIBLE
            } else {
                ivEliminar.visibility = View.GONE
            }

        }

        btnGuardar = view.findViewById(R.id.btnGuardar)
        btnGuardar.setOnClickListener { guardarAlarmaEnFirebase() }
        ivEliminar.setOnClickListener { eliminarAlarma() }

        firestore = FirebaseFirestore.getInstance()
        onAlarmDeletedListener = (activity as? OnAlarmDeletedListener)
            ?: (parentFragment as? OnAlarmDeletedListener)


        edHora.setOnClickListener { showTimePickerDialog() }
    }

    private fun showTimePickerDialog() {
        val timePicker = TimePickerFragment { onTimeSelected(it) }
        timePicker.show(childFragmentManager, "timePicker")
    }
    private fun obtenerPosicionItem(spinner: Spinner, item: String): Int {
        val adapter = spinner.adapter
        for (i in 0 until adapter.count) {
            if (adapter.getItem(i).toString() == item) {
                return i
            }
        }
        return 0
    }

    private fun onTimeSelected(time: String) {
        val formattedTime = formatTime(time)
        edHora.setText(formattedTime)
    }

    private fun formatTime(time: String): String {
        val parts = time.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()
        return String.format("%02d:%02d", hour, minute)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun guardarAlarmaEnFirebase() {
        val hora = edHora.text.toString()
        val actividad = spActividad.selectedItem.toString()
        val dias = obtenerDiasSeleccionados()
        val activar = if (swActivar.isChecked) "Encendida" else "Apagada"

        if (hora.isBlank() || actividad.isBlank() || dias.isEmpty()) {
            Toast.makeText(context, "Completa todos los campos antes de guardar la alarma", Toast.LENGTH_SHORT).show()
            return
        }

        val videoUrl = "Ejemplo.com"
        val nota = "Recordatorio"
        val usuario = usuarioSeleccionado

        // Obtén la alarma de los argumentos
        val args = arguments
        val alarma = args?.getSerializable("alarma") as? Alarma

        val db = FirebaseFirestore.getInstance()

        if (alarma != null && alarma.id.isNotEmpty()) {
            val nuevaAlarma = Alarma(alarma.id, actividad, dias, hora, activar, videoUrl, nota, usuario)
            val index = listaAlarmas.indexOfFirst { it.id == alarma.id }

            db.collection("alarma").document(alarma.id)
                .set(nuevaAlarma)
                .addOnSuccessListener {
                    dismiss()
                    if (index != -1) {
                        listaAlarmas.removeAt(index)
                        listaAlarmas.add(index, nuevaAlarma)
                    } else {
                        listaAlarmas.add(nuevaAlarma)
                    }
                    configurarAlarma(nuevaAlarma, hora, obtenerDiasSeleccionados(), activar)
                    alarmAdapter.notifyDataSetChanged()
                    alarmAdapter.updateData(listaAlarmas)
                    onAlarmAddedListener?.onAlarmAdded()
                    notifyAlarmAdded()
                }
                .addOnFailureListener {
                    Log.d("Error al actualizar la alarma", "Error")
                }
        } else {
            // Agregar nueva alarma
            val nuevaAlarma = Alarma("", actividad, dias, hora, activar, videoUrl, nota, usuario)

            db.collection("alarma")
                .add(nuevaAlarma)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val documentReference = task.result
                        val alarmaConId = nuevaAlarma.copy(id = documentReference.id)

                        db.collection("alarma").document(documentReference.id)
                            .set(alarmaConId)
                            .addOnSuccessListener {
                                dismiss()
                                listaAlarmas.add(alarmaConId)
                                alarmAdapter.updateData(listaAlarmas)
                                configurarAlarma(alarmaConId, hora, obtenerDiasSeleccionados(), activar)
                                updateAlarmasAsociadas(usuario, documentReference.id)
                                alarmAdapter.notifyDataSetChanged()
                                onAlarmAddedListener?.onAlarmAdded()
                                notifyAlarmAdded()
                            }
                            .addOnFailureListener {
                                Log.d("Error al actualizar el ID", "Error")
                            }
                    } else {
                        Log.d("Error al guardar los datos", "Error")
                    }
                }
                .addOnFailureListener {
                    Log.d("Error al guardar los datos", "Error")
                }
        }
    }


    private fun configurarAlarma(alarma: Alarma, hora: String, dias: List<String>, activar: String) {
        Log.d("Notificaciones", "Configurando alarma: $alarma, Hora: $hora, Días: $dias, Activar: $activar")

        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        for (dia in dias) {
            val intent = Intent(context, AlarmReceiver::class.java)
            intent.putExtra(AlarmReceiver.ALARMA_EXTRA, alarma)

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                obtenerCodigoDia(dia),
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
            calendar.set(Calendar.DAY_OF_WEEK, obtenerCodigoDia(dia))

            try {
                if (activar == "Encendida") {
                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        AlarmManager.INTERVAL_DAY * 7,
                        pendingIntent
                    )
                } else {
                    // Si está apagada, cancelamos la alarma correspondiente
                    alarmManager.cancel(pendingIntent)
                }
            } catch (e: SecurityException) {
                Log.e("Notificaciones", "Error al configurar alarma repetitiva: $e")
            }
        }
    }

    private fun eliminarAlarma() {
        val args = arguments
        val alarma = args?.getSerializable("alarma") as? Alarma

        if (alarma != null && alarma.id.isNotEmpty()) {
            val db = FirebaseFirestore.getInstance()
            db.collection("alarma").document(alarma.id)
                .delete()
                .addOnSuccessListener {
                    dismiss()
                    listaAlarmas.remove(alarma)
                    alarmAdapter.updateData(listaAlarmas)
                    onAlarmDeletedListener?.onAlarmDeleted()
                    notifyAlarmDelete()
                }
                .addOnFailureListener {
                    Log.d("Error al eliminar la alarma", "Error")
                }
        } else {
            // No hay alarma para eliminar
            dismiss()
        }
    }


    private fun obtenerDiasSeleccionados(): List<String> {
        val diasSeleccionados = mutableListOf<String>()
        if (cbLunes.isChecked) diasSeleccionados.add("lunes")
        if (cbMartes.isChecked) diasSeleccionados.add("martes")
        if (cbMiercoles.isChecked) diasSeleccionados.add("miercoles")
        if (cbJueves.isChecked) diasSeleccionados.add("jueves")
        if (cbViernes.isChecked) diasSeleccionados.add("viernes")
        if (cbSabado.isChecked) diasSeleccionados.add("sabado")
        if (cbDomingo.isChecked) diasSeleccionados.add("domingo")

        return diasSeleccionados
    }

    fun setOnAlarmAddedListener(listener: OnAlarmAddedListener) {
        onAlarmAddedListener = listener

    }

    fun setOnAlarmDeleteListener(listener: OnAlarmDeletedListener){
        onAlarmDeletedListener= listener
    }


    private fun notifyAlarmAdded() {
        onAlarmAddedListener?.onAlarmAdded()
    }

    private fun notifyAlarmDelete(){
        onAlarmDeletedListener?.onAlarmDeleted()
    }

    private fun obtenerCodigoDia(dia: String): Int {
        val dias = mapOf(
            "domingo" to Calendar.SUNDAY,
            "lunes" to Calendar.MONDAY,
            "martes" to Calendar.TUESDAY,
            "miercoles" to Calendar.WEDNESDAY,
            "jueves" to Calendar.THURSDAY,
            "viernes" to Calendar.FRIDAY,
            "sabado" to Calendar.SATURDAY
        )
        return dias[dia] ?: Calendar.SUNDAY
    }

    private fun updateAlarmasAsociadas(idUsuario: String, alarmaId: String) {
        val usuarioDoc = FirebaseFirestore.getInstance().collection("usuario").document(idUsuario)
        usuarioDoc.update("alarmasAsociadas", FieldValue.arrayUnion(alarmaId))
            .addOnSuccessListener {
                Log.d("Actualización exitosa", "ID de alarma agregado a las alarmas asociadas del usuario.")
            }
            .addOnFailureListener {
                Log.e("Error al actualizar", "Error al agregar ID de alarma a las alarmas asociadas del usuario.")
            }
    }


}