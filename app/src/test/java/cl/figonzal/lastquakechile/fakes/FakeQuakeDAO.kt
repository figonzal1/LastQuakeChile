package cl.figonzal.lastquakechile.fakes

import cl.figonzal.lastquakechile.quake_feature.data.local.QuakeDAO
import cl.figonzal.lastquakechile.quake_feature.data.local.entity.QuakeEntity

class FakeQuakeDAO : QuakeDAO {

    private val lista: MutableList<QuakeEntity> = mutableListOf(
        QuakeEntity(
            id = 1,
            quakeCode = 141641,
            utcDate = "2022-02-21 04:34:37",
            city = "Tongoy",
            reference = "24 km al SO de Tongoy",
            magnitude = 2.5,
            scale = "Ml",
            isSensitive = false,
            latitude = -30.447,
            longitude = -71.616,
            depth = 33.30,
            isVerified = true
        ),
        QuakeEntity(
            id = 2,
            quakeCode = 141568,
            utcDate = "2022-02-20 17:12:05",
            city = "Huasco",
            reference = "43 km al NO de Huasco",
            magnitude = 2.9,
            scale = "Ml",
            isSensitive = false,
            latitude = -28.244,
            longitude = -71.578,
            depth = 30.00,
            isVerified = true
        )

    )

    override fun insertQuake(quake: QuakeEntity) {
        lista.add(quake)
    }

    override fun getQuakes(): List<QuakeEntity> {
        return lista
    }

    override fun deleteAll() {
        lista.clear()
    }

}