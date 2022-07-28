package cl.figonzal.lastquakechile.quake_feature.data.remote


class QuakeRemoteDataSource(private val quakeAPI: QuakeAPI) {
    suspend fun getQuakes(limit: Int) = quakeAPI.listQuakes(limit)
}
