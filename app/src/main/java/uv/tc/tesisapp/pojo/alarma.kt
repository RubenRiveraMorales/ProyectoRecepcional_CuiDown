package uv.tc.tesisapp.pojo

import java.io.Serializable

data class Alarma(
    var id: String = "",
    var actividad: String,
    var diasRepeticion: List<String>,
    var hora: String,
    var estatus: String,
    var videoUrl: String,
    var nota: String,
    var usuario: String
):Serializable


{
    constructor() : this("","", emptyList(), "", "", "", "", "")
}