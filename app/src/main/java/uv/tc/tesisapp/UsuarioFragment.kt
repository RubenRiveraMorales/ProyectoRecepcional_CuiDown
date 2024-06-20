package uv.tc.tesisapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import uv.tc.tesisapp.adapter.UsuarioAdapter
import uv.tc.tesisapp.pojo.Usuario


class UsuarioFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var usuarioAdapter: UsuarioAdapter
    private val db = FirebaseFirestore.getInstance()
    private val usuariosCollection = db.collection("usuario")
    private lateinit var tvSinUsuarios: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_usuario, container, false)

        recyclerView = rootView.findViewById(R.id.recycler_usuarios)
        tvSinUsuarios = rootView.findViewById(R.id.tv_sin_usuario)

        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        obtenerUsuariosDesdeFirestore(tvSinUsuarios)

        // Agregar OnClickListener al botón de agregar usuario
        val btnAgregarUsuario: ImageView = rootView.findViewById(R.id.btnAgregarUsuario)
        btnAgregarUsuario.setOnClickListener {
            abrirFormularioRegistroUsuario()
        }

        return rootView
    }

    private fun obtenerUsuariosDesdeFirestore(tvSinUsuarios: TextView) {
        val idUsuarioAdmin = FirebaseAuth.getInstance().currentUser?.uid

        // Filtra la lista de usuarios por el ID del usuario administrador
        usuariosCollection.whereEqualTo("idUsuarioAdmin", idUsuarioAdmin).get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val listaUsuarios = result.toObjects(Usuario::class.java)
                    mostrarUsuariosEnRecyclerView(listaUsuarios, tvSinUsuarios)
                } else {
                    mostrarMensajeSinUsuarios(tvSinUsuarios)
                }
            }
            .addOnFailureListener { exception ->
                // Manejar errores de Firestore aquí
            }
    }

    private fun mostrarUsuariosEnRecyclerView(listaUsuarios: List<Usuario>, tvSinUsuarios: TextView) {
        // Proporcionar la implementación de OnItemClickListener al crear la instancia del adaptador
        usuarioAdapter = UsuarioAdapter(listaUsuarios, object : UsuarioAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, usuario: Usuario) {
                // Manejar el clic en el elemento de la lista según sea necesario
                // Puedes abrir un diálogo, realizar una acción, etc.
                Toast.makeText(requireContext(), "Usuario seleccionado: ${usuario.nombre}", Toast.LENGTH_SHORT).show()
            }
        })

        recyclerView.adapter = usuarioAdapter

        if (listaUsuarios.isNotEmpty()) {
            tvSinUsuarios.visibility = View.GONE
        } else {
            tvSinUsuarios.visibility = View.VISIBLE
        }
    }

    private fun mostrarMensajeSinUsuarios(tvSinUsuarios: TextView) {
        tvSinUsuarios.visibility = View.VISIBLE
    }

    private fun abrirFormularioRegistroUsuario() {
        // Aquí abre el formulario de registro de usuario (puedes usar un Intent para iniciar RegistroUsuarioActivity)
        val intent = Intent(activity, RegistroUsuarioActivity::class.java)
        startActivity(intent)
    }

    fun actualizarDatos() {
        Log.d("UsuarioFragment", "Actualizando datos...")
        obtenerUsuariosDesdeFirestore(tvSinUsuarios)
    }
}