package com.jobsalrt.repository

import com.jobsalrt.controller.view.FilterRequest
import com.jobsalrt.controller.view.Filters
import com.jobsalrt.domain.*
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import kotlin.math.ceil

@Service
class PostRepositoryOps(
    val mongoOperations: ReactiveMongoOperations,
    val keyUtil: KeyUtils,
    val criteriaUtils: CriteriaUtils
) {
    private val limit = 48
    fun findPosts(filter: FilterRequest, page: Int): Flux<Post> {
        val fields = listOf("basicDetails", "createdAt", "postUpdateDate", "totalViews")
        val criteria = criteriaUtils.createCriteriaWithFilter(filter)

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
        query.fields().include(*fields.toTypedArray())
        return mongoOperations.find(query, Post::class.java, POST_COLLECTION)
    }

    fun findPostCount(filter: FilterRequest): Mono<Pair<Long, Double>> {
        val query = Query(criteriaUtils.createCriteriaWithFilter(filter))
        return mongoOperations.count(query, Post::class.java, POST_COLLECTION)
            .map { Pair(it, ceil(it.toDouble() / limit)) }
    }

    fun findByBasicDetailsUrl(url: String): Mono<Post> {
        val query = Query(Criteria.where("basicDetails.url").`is`(url))
        return mongoOperations.findOne(query, Post::class.java, POST_COLLECTION)
            .flatMap {
                it.totalViews += 1
                mongoOperations.save(it)
            }
    }

    fun getOptions(key: String, value: String): Flux<BasicDetails> {
        val criteria = Criteria.where("basicDetails.$key").regex(".*${value}.*", "i")
            .andOperator(Criteria.where(keyUtil.find("status")).`is`(Status.VERIFIED))
        val query = Query(criteria).limit(limit)
        query.fields().include("basicDetails.$key")
        return mongoOperations.find(query, Post::class.java, POST_COLLECTION).map { it.basicDetails }
    }

    fun getSearchOptions(search: String): Mono<List<String>> {
        val filterRequest = FilterRequest(search = search, filters = Filters())
        return findPosts(filterRequest, 1)
            .map {
                it.createSearchOptionsList(search)
            }
            .collectList()
            .map { list ->
                val options = list.flatten().distinct().sortedBy { it.length }
                if (options.size > 10) options.subList(0, 10) else options
            }
    }
}

data class PostView(val id: Post, val states: List<State>)
