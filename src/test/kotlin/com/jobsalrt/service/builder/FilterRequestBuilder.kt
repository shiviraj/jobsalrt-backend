package com.jobsalrt.service.builder

import com.jobsalrt.controller.view.FilterRequest
import com.jobsalrt.controller.view.Filters
import com.jobsalrt.domain.Type

data class FilterRequestBuilder(
    val filters: Filters = FiltersBuilder().build(),
    val search: String = "",
    val type: Type? = null
) {
    fun build(): FilterRequest {
        return FilterRequest(
            filters = filters,
            search = search,
            type = type
        )
    }
}

data class FiltersBuilder(
    val location: List<String> = listOf(),
    val company: List<String> = listOf(),
    val qualification: List<String> = listOf(),
    val ageLimit: List<Long> = listOf(),
    val vacancies: List<Int> = listOf(),
    val formType: List<String> = listOf()
) {
    fun build(): Filters {
        return Filters(
            location = location,
            company = company,
            qualification = qualification,
            ageLimit = ageLimit,
            vacancies = vacancies,
            formType = formType
        )
    }
}
