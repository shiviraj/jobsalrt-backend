package com.jobsalrt.controller.view

import com.jobsalrt.domain.Type

data class FilterRequest(
    val filters: Filters,
    val search: String = "",
    val type: Type? = null
)

data class Filters(
    val location: List<String> = emptyList(),
    val company: List<String> = emptyList(),
    val qualification: List<String> = emptyList(),
    val ageLimit: List<Long> = emptyList(),
    val vacancies: List<Int> = emptyList(),
    val formType: List<String> = emptyList()
)

