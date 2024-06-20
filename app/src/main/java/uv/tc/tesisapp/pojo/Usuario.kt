package uv.tc.tesisapp.pojo

import java.io.Serializable

class Usuario (
    var idUsuario: String,
    var nombre: String,
    var nombreUsuario: String,
    var edad: String,
    var genero: String,
    var codigo: String,
    var idUsuarioAdmin: String,
    val alarmasAsociadas: List<String> 

): Serializable

{
    // Constructor sin argumentos necesario para Firebase
    constructor() : this("","", "", "", "", "", "",emptyList())
}