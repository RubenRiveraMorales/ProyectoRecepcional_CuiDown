package uv.tc.tesisapp.pojo

import java.io.Serializable

class recompensa (
    var idUsuario : String,
    var actividades : Int,
    var puntos: Int

): Serializable

{
    constructor(): this("",0,0)
}