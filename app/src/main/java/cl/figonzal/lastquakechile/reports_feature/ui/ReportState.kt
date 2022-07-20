package cl.figonzal.lastquakechile.reports_feature.ui

import cl.figonzal.lastquakechile.core.data.remote.ApiError
import cl.figonzal.lastquakechile.reports_feature.domain.model.Report

data class ReportState(
    val isLoading: Boolean = false,
    val apiError: ApiError? = null,
    val reports: List<Report> = listOf()
)