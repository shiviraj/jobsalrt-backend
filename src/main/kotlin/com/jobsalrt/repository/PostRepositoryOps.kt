package com.jobsalrt.repository

import com.jobsalrt.controller.view.FilterRequest
import com.jobsalrt.controller.view.Filters
import com.jobsalrt.domain.BasicDetails
import com.jobsalrt.domain.POST_COLLECTION
import com.jobsalrt.domain.Post
import com.jobsalrt.domain.Status
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import kotlin.math.ceil

@Service
class PostRepositoryOps(@Autowired val mongoOperations: ReactiveMongoOperations) {
    private val limit = 48
    fun findPosts(filter: FilterRequest, page: Int): Flux<Post> {
        val query = createQueryWithFilter(filter)
            .skip(((page - 1) * limit).toLong())
            .limit(limit)

        val fields = listOf("basicDetails", "createdAt", "postUpdateDate", "totalViews")
        query.fields().include(*fields.toTypedArray())
        return mongoOperations.find(query, Post::class.java, POST_COLLECTION)
    }

    fun findPostCount(filter: FilterRequest): Mono<Pair<Long, Double>> {
        return mongoOperations.count(createQueryWithFilter(filter), Post::class.java, POST_COLLECTION)
            .map { Pair(it, ceil(it.toDouble() / limit)) }
    }

    fun findByBasicDetailsUrl(url: String): Mono<Post> {
        val query = Query(Criteria.where("basicDetails.url").`is`(url))
        return mongoOperations.findOne(query, Post::class.java, POST_COLLECTION)
    }

    fun getOptions(key: String, value: String): Flux<BasicDetails> {
        val criteria = Criteria.where("basicDetails.$key").regex(".*${value}.*", "i")
            .andOperator(Criteria.where(findKey("status")).`is`(Status.VERIFIED))
        val query = Query(criteria).limit(limit)
        query.fields().include("basicDetails.$key")
        return mongoOperations.find(query, Post::class.java, POST_COLLECTION).map { it.basicDetails }
    }

    private fun createQueryWithFilter(filter: FilterRequest): Query {
        val query = createQueryForFilters(filter.filters)
        query.addCriteria(Criteria.where(findKey("status")).`is`(Status.VERIFIED))
        if (filter.type != null)
            query.addCriteria(Criteria.where(findKey("states.type")).`in`(filter.type))
        if (filter.search.isNotEmpty()) query.addCriteria(createCriteriaForSearch(filter.search))
        return query
    }

    private fun createQueryForFilters(filters: Filters): Query {
        val criteria = Criteria.where("").andOperator(
            criteriaForFilter("location", filters.location),
            criteriaForFilter("qualification", filters.qualification),
            criteriaForFilter("company", filters.company),
            criteriaForVacancies(filters.vacancies),
            criteriaForAgeLimit(filters.ageLimit)
        )
        return Query(criteria)
    }

    private fun criteriaForVacancies(vacancies: List<Int>): Criteria {
        val criteria = Criteria.where("")
        if (vacancies.isNotEmpty()) {
            val minVacancyCriteria = if (vacancies.first() != 0) {
                Criteria.where("basicDetails.vacancies").gte(vacancies.first())
            } else Criteria.where("")

            val maxVacancyCritera = if (vacancies[1] != 5500) {
                Criteria.where("basicDetails.vacancies").lte(vacancies[1])
            } else Criteria.where("")

            criteria.andOperator(minVacancyCriteria, maxVacancyCritera)
        }
        return criteria
    }

    private fun criteriaForAgeLimit(ageLimit: List<Long>): Criteria {
        val criteria = Criteria.where("")
        if (ageLimit.isNotEmpty()) {
            val today = LocalDateTime.now()
            criteria.andOperator(
                Criteria.where("basicDetails.minAgeLimit").lte(today.minusYears(ageLimit.first())),
                Criteria.where("basicDetails.maxAgeLimit").gte(today.minusYears(ageLimit.first()))
            )
        }
        return criteria
    }

    private fun criteriaForFilter(key: String, list: List<String>): Criteria {
        val criteria = Criteria.where("")
        if (list.isNotEmpty()) {
            criteria.orOperator(
                *list.map {
                    Criteria.where(findKey(key)).regex(".*${it}.*", "i")
                }.toTypedArray()
            )
        }
        return criteria
    }

    private fun createCriteriaForSearch(searchText: String): Criteria {
        return Criteria.where("").orOperator(
            Criteria.where("basicDetails.url").regex(".*${searchText}.*", "i"),
            Criteria.where("basicDetails.name").regex(".*${searchText}.*", "i"),
            Criteria.where("basicDetails.location").regex(".*${searchText}.*", "i"),
            Criteria.where("basicDetails.company").regex(".*${searchText}.*", "i"),
            Criteria.where("basicDetails.qualification").regex(".*${searchText}.*", "i"),
        )
    }

    private fun findKey(key: String): String {
        val keyMapping = mapOf(
            "formType" to "basicDetails.formType",
            "location" to "basicDetails.location",
            "qualification" to "basicDetails.qualification",
            "company" to "basicDetails.company",
        )
        return keyMapping[key] ?: key
    }
}
