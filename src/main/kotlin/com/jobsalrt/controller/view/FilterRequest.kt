package com.jobsalrt.controller.view

import com.jobsalrt.domain.Type

data class FilterRequest(
    val filters: Map<String, List<Any>>,
    val search: String = "",
    val type: Type? = null
) {
    init {
        filters.filterNot { it.value.isNullOrEmpty() }
    }
}

