package cl.figonzal.lastquakechile.reports_feature.ui

import cl.figonzal.lastquakechile.reports_feature.domain.model.Report

data class ReportState(
    val reports: List<Report> = emptyList(),
    val isLoading: Boolean = true
)