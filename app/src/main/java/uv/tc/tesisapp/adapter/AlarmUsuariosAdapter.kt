package uv.tc.tesisapp.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import uv.tc.tesisapp.MainActivity
import uv.tc.tesisapp.ProviderType
import uv.tc.tesisapp.R
import uv.tc.tesisapp.VideoActivity
import uv.tc.tesisapp.pojo.Alarma
import java.util.Locale

class AlarmUsuariosAdapter(private val listener: OnAlarmClickListener) : RecyclerView.Adapter<AlarmUsuariosAdapter.AlarmaViewHolder>() {
    private var alarmasList: List<Alarma> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_alarmas, parent, false)
        return AlarmaViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlarmaViewHolder, position: Int) {
        val alarma = alarmasList[position]
        holder.bind(alarma)
    }

    override fun getItemCount(): Int {
        return alarmasList.size
    }

    fun actualizarAlarmas(nuevaLista: List<Alarma>) {
        alarmasList = nuevaLista
        notifyDataSetChanged()
    }

    inner class AlarmaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvHora: TextView = itemView.findViewById(R.id.tv_hora)
        private val tvNombreActividad: TextView = itemView.findViewById(R.id.tv_nombreAlarma)
        private val tvEstatus: TextView = itemView.findViewById(R.id.tv_estatus)
        private val ivIconoActividad: ImageView= itemView.findViewById(R.id.ivIconoActividad)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val alarma = alarmasList[position]
                    listener.onAlarmClick(alarma)
                }
            }
        }

        fun bind(alarma: Alarma) {
            tvHora.text = "Hora: ${alarma.hora}"
            tvNombreActividad.text = "Nombre de la actividad: ${alarma.actividad}"
            tvEstatus.text = "Estatus: ${alarma.estatus}"
            val iconoResId = obtenerIcono(alarma.actividad)
            ivIconoActividad.setImageResource(iconoResId)
        }
    }


    private fun obtenerIcono(nombreActividad: String): Int {
        return when (nombreActividad.toLowerCase(Locale.getDefault())) {
            "cepillardientes" -> R.drawable.dientes
            "peinarse" -> R.drawable.estilo_pelo
            "comer" -> R.drawable.comer_icon
            "bañarse" -> R.drawable.ducha
            "arreglarse"-> R.drawable.vestirse
            "lavarmanos" ->R.drawable.lavar_manos
            else -> R.drawable.baseline_pending_actions
        }

    }

    interface OnAlarmClickListener {
        fun onAlarmClick(alarma: Alarma)
    }
}

