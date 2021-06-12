package com.jobsalrt.repository

import com.jobsalrt.controller.view.FilterRequest
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
import kotlin.math.ceil

@Service
class PostRepositoryOps(
    @Autowired val mongoOperations: ReactiveMongoOperations
) {
    private val limit = 48
    fun findPosts(filter: FilterRequest, page: Int): Flux<Post> {
        val query = createQueryWithFilter(filter)
            .skip(((page - 1) * limit).toLong())
            .limit(limit)

        val fields = listOf(
            "basicDetails",
            "createdAt",
            "postUpdateDate",
            "totalViews",
        )
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

    private fun createQueryWithFilter(filter: FilterRequest): Query {
        val query = Query()
        filter.filters.forEach {
            query.addCriteria(Criteria.where(findKey(it.key)).`in`(it.value))
        }
        query.addCriteria(Criteria.where(findKey("status")).`is`(Status.VERIFIED))
        if (filter.type != null)
            query.addCriteria(Criteria.where(findKey("states.type")).`in`(filter.type))
        if (filter.search.isNotEmpty()) {
            query.addCriteria(
                Criteria.where("").orOperator(
                    Criteria.where("basicDetails.url").regex(".*${filter.search}.*", "i"),
                    Criteria.where("basicDetails.name").regex(".*${filter.search}.*", "i"),
                    Criteria.where("basicDetails.location").regex(".*${filter.search}.*", "i"),
                    Criteria.where("basicDetails.company").regex(".*${filter.search}.*", "i"),
                    Criteria.where("basicDetails.qualification").regex(".*${filter.search}.*", "i"),
                )
            )
        }
        return query
    }

    private fun findKey(key: String): String {
        val keyMapping = mapOf(
            "formType" to "basicDetails.formType",
            "location" to "basicDetails.location",
            "qualification" to "basicDetails.qualification",
        )
        return keyMapping[key] ?: key
    }

    fun getOptions(key: String, value: String): Flux<BasicDetails> {
        val criteria = Criteria.where("basicDetails.$key").regex(".*${value}.*", "i")
        val query = Query(criteria).limit(limit)
        query.fields().include("basicDetails.$key", "source")
        return mongoOperations.find(query, Post::class.java, POST_COLLECTION).map { it.basicDetails }
    }
}
