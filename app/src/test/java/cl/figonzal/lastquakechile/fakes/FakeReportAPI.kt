package cl.figonzal.lastquakechile.fakes

import cl.figonzal.lastquakechile.reports_feature.data.remote.ReportAPI
import cl.figonzal.lastquakechile.reports_feature.data.remote.ReportResult
import cl.figonzal.lastquakechile.reports_feature.data.remote.dto.QuakeCityDTO
import cl.figonzal.lastquakechile.reports_feature.data.remote.dto.ReportDTO
import retrofit2.Response

class FakeReportAPI : ReportAPI {
    override suspend fun listReports(): Response<ReportResult> {

        return Response.success(
            ReportResult(
                listOf(
                    ReportDTO(
                        mes_reporte = "2022-01",
                        n_sismos = 634,
                        n_sensibles = 15,
                        prom_magnitud = 3.12,
                        prom_profundidad = 90.08,
                        max_magnitud = 5.7,
                        min_profundidad = 2.00,
                        top_ciudades = listOf(
                            QuakeCityDTO("socaire", 65),
                            QuakeCityDTO("ollagua", 49),
                            QuakeCityDTO("mina collahuasi", 47),
                            QuakeCityDTO("caldera", 37)
                        )
                    )
                )
            )
        )
    }

}