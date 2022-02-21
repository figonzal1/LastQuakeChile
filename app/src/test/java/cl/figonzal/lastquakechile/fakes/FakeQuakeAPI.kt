package cl.figonzal.lastquakechile.fakes

import cl.figonzal.lastquakechile.quake_feature.data.remote.QuakeAPI
import cl.figonzal.lastquakechile.quake_feature.data.remote.QuakeResult
import cl.figonzal.lastquakechile.quake_feature.data.remote.dto.QuakeDTO
import retrofit2.Response

class FakeQuakeAPI : QuakeAPI {
    override suspend fun listQuakes(limite: Int): Response<QuakeResult> {
        return Response.success(
            QuakeResult(
                listOf(
                    QuakeDTO(
                        fecha_utc = "2022-02-20 18:13:35",
                        ciudad = "Petorca",
                        referencia = "37 km al S de Petorca",
                        magnitud = 2.5,
                        escala = "Ml",
                        sensible = "0",
                        latitud = -32.580,
                        longitud = -70.889,
                        profundidad = 78.49,
                        imagen_url = "141597",
                        estado = "verificado"
                    )
                ).takeLast(limite)
            )
        )
    }

}