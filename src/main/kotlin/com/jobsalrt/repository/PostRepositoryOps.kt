package com.jobsalrt.repository

import com.jobsalrt.controller.view.FilterRequest
import com.jobsalrt.domain.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.aggregation.Aggregation
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
        val fields = listOf("basicDetails", "createdAt", "postUpdateDate", "totalViews")
        val criteria = createCriteriaWithFilter(filter)

        if (filter.type != null) {
            val aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.unwind("states"),
                Aggregation.match(criteria),
                Aggregation.group(*fields.toTypedArray()).push("states").`as`("states"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "states.createdAt")),
                Aggregation.skip(((page - 1) * limit).toLong()),
                Aggregation.limit(limit.toLong())
            )
            return mongoOperations.aggregate(aggregation, POST_COLLECTION, PostView::class.java).map { it.id }
        }
        val query = Query(criteria).with(Sort.by(Sort.Direction.DESC, "createdAt"))
        return mongoOperations.find(query, Post::class.java, POST_COLLECTION)
    }

    fun findPostCount(filter: FilterRequest): Mono<Pair<Long, Double>> {
        val query = Query(createCriteriaWithFilter(filter))
        return mongoOperations.count(query, Post::class.java, POST_COLLECTION)
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

    private fun createCriteriaWithFilter(filter: FilterRequest): Criteria {
        val filters = filter.filters
        return Criteria.where("").andOperator(
            criteriaForFilter("location", filters.location),
            criteriaForFilter("qualification", filters.qualification),
            criteriaForFilter("company", filters.company),
            criteriaForVacancies(filters.vacancies),
            criteriaForAgeLimit(filters.ageLimit),
            Criteria.where(findKey("status")).`is`(Status.VERIFIED),
            createCriteriaForSearch(filter.search),
            createCriteriaForType(filter.type)
        )
    }

    private fun createCriteriaForType(type: Type?): Criteria {
        val criteria = Criteria.where("")
        if (type != null) {
            criteria.orOperator(Criteria.where(findKey("type")).`in`(type))
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
                    Criteria.where(findKey(key)).regex(".*${it}.*", "i")
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

    private fun findKey(key: String): String {
        val keyMapping = mapOf(
            "formType" to "basicDetails.formType",
            "location" to "basicDetails.location",
            "qualification" to "basicDetails.qualification",
            "company" to "basicDetails.company",
            "type" to "states.type"
        )
        return keyMapping[key] ?: key
    }
}

data class PostView(val id: Post, val states: List<State>)
