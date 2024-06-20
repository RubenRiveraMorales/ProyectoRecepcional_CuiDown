package uv.tc.tesisapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import uv.tc.tesisapp.R
import uv.tc.tesisapp.pojo.Usuario

class UsuarioAdapter(private var listaUsuarios: List<Usuario>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int, usuario: Usuario)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_list_usuario, parent, false)
        return UsuarioViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val usuario = listaUsuarios[position]
        holder.bind(usuario)
    }

    override fun getItemCount(): Int {
        return listaUsuarios.size
    }

    inner class UsuarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Definir vistas del elemento de la lista según el diseño de item_list_usuarios.xml
        private val tvNombre: TextView = itemView.findViewById(R.id.tv_nombreUsuario)
        private val tvEdad: TextView = itemView.findViewById(R.id.tv_edad)
        private val tvCodigo: TextView = itemView.findViewById(R.id.tv_codigo)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition, listaUsuarios[adapterPosition])
            }
        }

        fun bind(usuario: Usuario) {
            // Actualizar vistas con la información del usuario
            tvNombre.text = "Nombre: ${usuario.nombre}"
            tvEdad.text = "Edad: ${usuario.edad}"
            tvCodigo.text = "Código de vinculación: ${usuario.codigo}"
        }
    }
}