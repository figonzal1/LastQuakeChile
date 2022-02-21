package cl.figonzal.lastquakechile.fakes

/*
class FakeReportDAO : ReportDAO() {

    private val list: MutableList<ReportWithQuakeCityEntity> = mutableListOf()

    override fun insertReport(report: ReportEntity): Long {

        val reportWithQuakeCityEntity = QuakeCityEntity(

        )
        list.add(report)
    }

    override fun insertAll(topCities: List<QuakeCityEntity>) {
    }

    override fun getReport(): List<ReportWithQuakeCityEntity> {
        return list
    }

    fun testInsert(reportWithQuakeCitiesEntity: ReportWithQuakeCityEntity) {

        val reportID = insertReport(reportWithQuakeCitiesEntity.report)

        reportWithQuakeCitiesEntity.topCities.forEach {
            it.idReport = reportID
        }

        insertAll(reportWithQuakeCitiesEntity.topCities)
    }

}
*/