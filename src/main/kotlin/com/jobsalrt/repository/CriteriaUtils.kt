package com.jobsalrt.repository

import com.jobsalrt.controller.view.FilterRequest
import com.jobsalrt.domain.Status
import com.jobsalrt.domain.Type
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class CriteriaUtils(
    val keyUtils: KeyUtils
) {
    fun createCriteriaWithFilter(filter: FilterRequest): Criteria {
        val filters = filter.filters
        return Criteria.where("").andOperator(
            criteriaForFilter("location", filters.location),
            criteriaForFilter("qualification", filters.qualification),
            criteriaForFilter("company", filters.company),
            criteriaForVacancies(filters.vacancies),
            criteriaForAgeLimit(filters.ageLimit),
            Criteria.where(keyUtils.find("status")).`is`(Status.VERIFIED),
            createCriteriaForSearch(filter.search),
            createCriteriaForType(filter.type)
        )
    }

    private fun createCriteriaForType(type: Type?): Criteria {
        val criteria = Criteria.where("")
        if (type != null) {
            criteria.orOperator(Criteria.where(keyUtils.find("type")).`in`(type))
        }
        return criteria
    }

    private fun criteriaForVacancies(vacancies: List<Int>): Criteria {
        val criteria = Criteria.where("")
        if (vacancies.isNotEmpty()) {
            val minVacancyCriteria = if (vacancies.first() != 0) {
                Criteria.where("basicDetails.vacancies").gte(vacancies.first())
            } else Criteria.where("")

            val maxVacancyCriteria = if (vacancies[1] != 5500) {
                Criteria.where("basicDetails.vacancies").lte(vacancies[1])
            } else Criteria.where("")

            criteria.andOperator(minVacancyCriteria, maxVacancyCriteria)
        }
        return criteria
    }

    private fun criteriaForAgeLimit(ages: List<Long>): Criteria {
        val criteria = Criteria.where("")
        if (ages.isNotEmpty()) {
            val today = LocalDateTime.now()
            criteria.orOperator(
                *ages.map {
                    Criteria.where("").andOperator(
                        Criteria.where("basicDetails.minAgeLimit").lte(today.minusYears(it)),
                        Criteria.where("basicDetails.maxAgeLimit").gte(today.minusYears(it))
                    )
                }.toTypedArray()
            )
        }
        return criteria
    }

    private fun criteriaForFilter(key: String, list: List<String>): Criteria {
        val criteria = Criteria.where("")
        if (list.isNotEmpty()) {
            criteria.orOperator(
                *list.map {
                    Criteria.where(keyUtils.find(key)).regex(".*${it}.*", "i")
                }.toTypedArray()
            )
        }
        return criteria
    }

    private fun createCriteriaForSearch(searchText: String): Criteria {
        val criteria = Criteria.where("")
        if (searchText.isNotEmpty()) {
            criteria.orOperator(
                Criteria.where("basicDetails.url").regex(".*${searchText}.*", "i"),
                Criteria.where("basicDetails.name").regex(".*${searchText}.*", "i"),
                Criteria.where("basicDetails.location").regex(".*${searchText}.*", "i"),
                Criteria.where("basicDetails.company").regex(".*${searchText}.*", "i"),
                Criteria.where("basicDetails.qualification").regex(".*${searchText}.*", "i"),
            )
        }
        return criteria
    }
}
