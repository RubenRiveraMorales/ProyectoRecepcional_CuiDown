package uv.tc.tesisapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import uv.tc.tesisapp.R
import uv.tc.tesisapp.pojo.Alarma
import java.util.Locale

class AlarmAdapter(private var alarmas: List<Alarma>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int, alarma: Alarma)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_list_alarmas, parent, false)
        return AlarmViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarma = alarmas[position]
        holder.bind(alarma)
        holder.itemView.setOnClickListener {
            listener.onItemClick(position, alarma)
        }
    }

    override fun getItemCount(): Int {
        return alarmas.size
    }

    fun updateData(newAlarmas: List<Alarma>) {
        alarmas = newAlarmas
        notifyDataSetChanged()
    }





    inner class AlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvHora: TextView = itemView.findViewById(R.id.tv_hora)
        private val tvNombreActividad: TextView = itemView.findViewById(R.id.tv_nombreAlarma)
        private val tvEstatus: TextView = itemView.findViewById(R.id.tv_estatus)
        private val ivIconoActividad: ImageView = itemView.findViewById(R.id.ivIconoActividad)

        fun bind(alarma: Alarma) {
            tvHora.text = "Hora: ${alarma.hora}"
            tvNombreActividad.text = "Nombre de la actividad: ${alarma.actividad}"
            tvEstatus.text = "Estatus: ${alarma.estatus}"
            val iconoResId = obtenerIcono(alarma.actividad)
            ivIconoActividad.setImageResource(iconoResId)

        }

        private fun obtenerIcono(nombreActividad: String): Int {
            return when (nombreActividad.toLowerCase(Locale.getDefault())) {
                "cepillardientes" -> R.drawable.dientes
                "peinarse" -> R.drawable.estilo_pelo
                "comer" -> R.drawable.comer_icon
                "bañarse" -> R.drawable.ducha
                "arreglarse"-> R.drawable.vestirse
                "lavarmanos" ->R.drawable.lavar_manos
                else -> R.drawable.baseline_pending_actions // Icono predeterminado si no coincide con ninguna actividad conocida
            }

        }
    }
}