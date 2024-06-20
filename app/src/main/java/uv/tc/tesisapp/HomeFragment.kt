package uv.tc.tesisapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import uv.tc.tesisapp.Interfaces.OnAlarmAddedListener
import uv.tc.tesisapp.Interfaces.OnAlarmDeletedListener
import uv.tc.tesisapp.adapter.AlarmAdapter
import uv.tc.tesisapp.pojo.Alarma
import uv.tc.tesisapp.pojo.Usuario

class HomeFragment : Fragment(), OnAlarmAddedListener, OnAlarmDeletedListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var alarmAdapter: AlarmAdapter
    private val db = FirebaseFirestore.getInstance()
    private val alarmasCollection = db.collection("alarma")
    private val isEditing: Boolean = false
    private lateinit var spinnerUsuarios: Spinner
    private val usuariosCollection = db.collection("usuario")
    private var listaAlarmas: MutableList<Alarma> = mutableListOf()
    private lateinit var tvSinAlarmas: TextView
    private var addingAlarm: Boolean = false
    private lateinit var listaUsuarios: List<Usuario>
   //private lateinit var searchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)
        recyclerView = rootView.findViewById(R.id.recycler_alarmas)
        tvSinAlarmas = rootView.findViewById(R.id.tv_sin_alarmas)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        spinnerUsuarios = rootView.findViewById(R.id.spUsuario)
        //searchView = rootView.findViewById(R.id.svBuscar)
        obtenerNombresUsuariosDesdeFirestore(spinnerUsuarios)


        val btnAgregar: ImageView = rootView.findViewById(R.id.btnAgregar)


        btnAgregar.setOnClickListener {
            addingAlarm = true
            val usuarioSeleccionado = obtenerUsuarioSeleccionado(spinnerUsuarios)
            val idUsuario = usuarioSeleccionado.idUsuario
            val editarAlarmaDialog = EditarAlarmaDialog(recyclerView, alarmAdapter, idUsuario)
            editarAlarmaDialog.setOnAlarmAddedListener(this)
            editarAlarmaDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog)

            // Establecer isEditing a false ya que estás agregando una nueva alarma
            editarAlarmaDialog.isEditing = false

            val fragmentManager = requireActivity().supportFragmentManager

            fragmentManager.beginTransaction().apply {
                editarAlarmaDialog.show(this, "editarAlarmaDialog")
                addToBackStack(null)
                updateAlarmsForUser(usuarioSeleccionado)
            }

            Log.d("HomeFragment", "Botón agregar clickeado")
        }



        return rootView
    }

    override fun onAlarmAdded() {
        val usuarioSeleccionado = obtenerUsuarioSeleccionado(spinnerUsuarios)
        updateAlarmsForUser(usuarioSeleccionado)
    }

    override fun onAlarmDeleted() {
        val usuarioSeleccionado = obtenerUsuarioSeleccionado(spinnerUsuarios)
        updateAlarmsForUser(usuarioSeleccionado)
        Log.d("HomeFragment", "Alarma eliminada. Actualizando alarmas para el usuario.")
    }

    private fun mostrarAlarmasEnRecyclerView(listaAlarmas: List<Alarma>, tvSinAlarmas: TextView) {
        if (!::alarmAdapter.isInitialized) {
            alarmAdapter = AlarmAdapter(listaAlarmas, object : AlarmAdapter.OnItemClickListener {
                override fun onItemClick(position: Int, alarma: Alarma) {
                    val usuarioSeleccionado = obtenerUsuarioSeleccionado(spinnerUsuarios)
                    val idUsuario = usuarioSeleccionado.idUsuario
                    val editarAlarmaDialog = EditarAlarmaDialog(recyclerView, alarmAdapter, idUsuario,)
                    editarAlarmaDialog.setOnAlarmAddedListener(this@HomeFragment)
                    editarAlarmaDialog.setOnAlarmDeleteListener(this@HomeFragment)
                    editarAlarmaDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog)
                    val args = Bundle()
                    args.putSerializable("alarma", alarma)
                    editarAlarmaDialog.arguments = args

                    val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
                    fragmentManager.beginTransaction().apply {
                        editarAlarmaDialog.show(this, "editarAlarmaDialog")
                        addToBackStack(null)
                    }
                }
            })
            recyclerView.adapter = alarmAdapter
        } else {
            alarmAdapter.updateData(listaAlarmas)
        }

        if (listaAlarmas.isNotEmpty()) {
            tvSinAlarmas.visibility = View.GONE
        } else {
            tvSinAlarmas.visibility = View.VISIBLE
        }
    }

    private fun obtenerNombresUsuariosDesdeFirestore(spinner: Spinner) {
        val idUsuarioAdmin = FirebaseAuth.getInstance().currentUser?.uid
        val administradorId = idUsuarioAdmin
        Log.d("ObtenerUsuario", "$idUsuarioAdmin")
        usuariosCollection.whereEqualTo("idUsuarioAdmin", administradorId)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    listaUsuarios = result.toObjects(Usuario::class.java)
                    listaUsuarios = listaUsuarios.sortedBy { it.nombre }

                    val nombresUsuarios = listaUsuarios.map { it.nombre }
                    mostrarNombresEnSpinner(nombresUsuarios, spinner)

                    spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            val usuarioSeleccionado = listaUsuarios.getOrNull(position)
                            Log.d("ObtenerUsuario", "$usuarioSeleccionado")
                            if (usuarioSeleccionado != null) {
                                obtenerAlarmasUsuarioDesdeFirestore(usuarioSeleccionado.alarmasAsociadas)
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                            // Manejar el caso cuando no se selecciona nada
                        }
                    }
                } else {
                    // Manejar el caso cuando no hay usuarios
                }
            }
            .addOnFailureListener { exception ->
                // Manejar errores de Firestore aquí
            }
    }



    private fun updateAlarmsForUser(usuario: Usuario) {
        Log.d("HomeFragment", "Obteniendo alarmas para el usuario: ${usuario.nombre}")
        obtenerAlarmasUsuarioDesdeFirestore(usuario.alarmasAsociadas)
    }


    private fun obtenerAlarmasUsuarioDesdeFirestore(alarmasIds: List<String>) {
        Log.d("HomeFragment", "Obteniendo alarmas desde Firestore para los IDs: $alarmasIds")
        if (alarmasIds.isNotEmpty()) {
            alarmasCollection.whereIn(FieldPath.documentId(), alarmasIds)
                .get()
                .addOnSuccessListener { result ->
                    if (!result.isEmpty) {
                        val listaAlarmas = result.toObjects(Alarma::class.java)
                        listaAlarmas.clear()
                        listaAlarmas.addAll(result.toObjects(Alarma::class.java))
                        mostrarAlarmasEnRecyclerView(listaAlarmas, tvSinAlarmas)
                    } else {
                        mostrarMensajeSinAlarmas(tvSinAlarmas)
                    }
                }
                .addOnFailureListener { exception ->
                    // Manejar errores de Firestore aquí
                }
        } else {
            listaAlarmas.clear()
            mostrarMensajeSinAlarmas(tvSinAlarmas)
        }
    }

    private fun obtenerUsuarioSeleccionado(spinner: Spinner): Usuario {
        val nombreUsuarioSeleccionado = spinner.selectedItem as? String ?: throw IllegalStateException("No se ha seleccionado ningún usuario")
        return listaUsuarios.find { it.nombre == nombreUsuarioSeleccionado } ?: throw IllegalStateException("No se ha encontrado el usuario correspondiente")
    }




    private fun mostrarNombresEnSpinner(nombresUsuarios: List<String>, spinner: Spinner) {
        val nombresOrdenados = nombresUsuarios.sorted()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombresOrdenados)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }


    private fun mostrarMensajeSinAlarmas(tvSinAlarmas: TextView) {
        tvSinAlarmas.visibility = View.VISIBLE
    }

    private fun obtenerNuevaAlarmaRecienAgregada(callback: (Alarma?) -> Unit) {
        val usuarioSeleccionado = obtenerUsuarioSeleccionado(spinnerUsuarios)
        val alarmasIds = usuarioSeleccionado.alarmasAsociadas

        if (alarmasIds.isNotEmpty()) {
            alarmasCollection.whereIn(FieldPath.documentId(), alarmasIds)
                .get()
                .addOnSuccessListener { result ->
                    if (!result.isEmpty) {
                        val listaAlarmas = result.toObjects(Alarma::class.java)
                        val nuevaAlarma = listaAlarmas.lastOrNull()
                        callback(nuevaAlarma)
                    } else {
                        callback(null)
                    }
                }
                .addOnFailureListener { exception ->
                    callback(null)
                }
        } else {
            callback(null)
        }
    }

}

