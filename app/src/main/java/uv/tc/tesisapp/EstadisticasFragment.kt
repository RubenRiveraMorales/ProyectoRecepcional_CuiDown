package uv.tc.tesisapp

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import uv.tc.tesisapp.pojo.Usuario
import uv.tc.tesisapp.pojo.recompensa


class EstadisticasFragment : Fragment() {
    private lateinit var spinnerUsuarios: Spinner

    private lateinit var tvActividades: TextView
    private lateinit var tvCompletadas: TextView
    private lateinit var tvPuntos: TextView
    private  lateinit var barChart: BarChart
    private val db = FirebaseFirestore.getInstance()
    private val usuariosCollection = db.collection("usuario")
    private val recompensaCollection = db.collection("recompensa")
    private lateinit var listaUsuarios: List<Usuario>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_estadisticas, container, false)
        spinnerUsuarios = view.findViewById<Spinner>(R.id.spUsuariosEstadisticas)
        barChart= view.findViewById(R.id.barChart)
        tvActividades= view.findViewById(R.id.tvActividades)
        tvCompletadas=view.findViewById(R.id.tvCompletadas)
        tvPuntos= view.findViewById(R.id.tvPuntos)
        obtenerNombresUsuarios(spinnerUsuarios)
        spinnerUsuarios.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val usuarioSeleccionado = listaUsuarios.getOrNull(position)
                usuarioSeleccionado?.let {
                    // Obtener las estadísticas del usuario seleccionado
                    obtenerEstadisticasUsuario(usuarioSeleccionado.idUsuario)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        return view
    }

    private fun obtenerEstadisticasUsuario(idUsuario: String) {
        recompensaCollection.whereEqualTo("idUsuario", idUsuario)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val estadisticasFragmet = result.documents.first().toObject(recompensa::class.java)
                    estadisticasFragmet?.let {
                        val actividadesTotales = it.actividades
                        val actividadesCompletadas = it.actividades

                        // Crear el conjunto de datos para el gráfico de barras
                        val entries = ArrayList<BarEntry>()
                        entries.add(BarEntry(0f, actividadesTotales.toFloat())) // Datos para actividades totales
                        entries.add(BarEntry(1f, actividadesCompletadas.toFloat())) // Datos para actividades completadas

                        val dataSet = BarDataSet(entries, "Actividades")
                        dataSet.colors = listOf(Color.BLUE, Color.GREEN) // Colores para actividades totales y completadas

                        val data = BarData(dataSet)
                        barChart.data = data
                        barChart.description.isEnabled = false
                        barChart.legend.isEnabled = false
                        barChart.invalidate() // Actualiza el gráfico

                        // Actualiza los textviews con los datos
                        tvActividades.text = actividadesTotales.toString()
                        tvCompletadas.text = actividadesCompletadas.toString()
                        tvPuntos.text = it.puntos.toString()
                    }
                } else {
                    // Manejar caso de resultados vacíos
                }
            }
            .addOnFailureListener { exception ->
                // Manejar errores de consulta
            }
    }


    private fun obtenerNombresUsuarios(spinner: Spinner) {
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
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            val usuarioSeleccionado = listaUsuarios.getOrNull(position)
                            Log.d("ObtenerUsuario", "$usuarioSeleccionado")
                            if (usuarioSeleccionado != null) {
                                obtenerEstadisticasUsuario(usuarioSeleccionado.idUsuario)
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {

                        }
                    }
                } else {

                }
            }
            .addOnFailureListener { exception ->

            }
    }


    private fun obtenerUsuarioSeleccionado(spinner: Spinner): Usuario {
        val nombreUsuarioSeleccionado = spinner.selectedItem as? String
            ?: throw IllegalStateException("No se ha seleccionado ningún usuario")
        return listaUsuarios.find { it.nombre == nombreUsuarioSeleccionado }
            ?: throw IllegalStateException("No se ha encontrado el usuario correspondiente")
    }

    private fun mostrarNombresEnSpinner(nombresUsuarios: List<String>, spinner: Spinner) {
        val nombresOrdenados = nombresUsuarios.sorted()
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombresOrdenados)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

}