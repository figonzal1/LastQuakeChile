package cl.figonzal.lastquakechile.reports_feature.ui

import cl.figonzal.lastquakechile.core.domain.DomainError
import cl.figonzal.lastquakechile.reports_feature.domain.model.Report

data class ReportState(
    val isLoading: Boolean = false,
    val domainError: DomainError? = null,
    val reports: List<Report> = emptyList(),
    val isLastPage: Boolean = false
)
