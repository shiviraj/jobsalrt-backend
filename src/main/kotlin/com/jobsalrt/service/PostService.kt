package com.jobsalrt.service

import com.jobsalrt.controller.view.FilterRequest
import com.jobsalrt.domain.Post
import com.jobsalrt.repository.PostRepositoryOps
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class PostService(
    val postRepositoryOps: PostRepositoryOps,
) {

    fun getAllPosts(page: Int, filterRequest: FilterRequest): Mono<List<Post>> {
        return postRepositoryOps.findPosts(filterRequest, page).collectList()
    }

    fun getPostsPageCount(filterRequest: FilterRequest): Mono<Pair<Long, Double>> {
        return postRepositoryOps.findPostCount(filterRequest)
    }

    fun getPostByUrl(url: String): Mono<Post> {
        return postRepositoryOps.findByBasicDetailsUrl(url)
    }

    fun getLocations(location: String): Mono<List<String>> {
        return postRepositoryOps.getOptions("location", location)
            .map {
                it.location?.split(",")?.map { location -> location.trim() } ?: emptyList()
            }
            .collectList()
            .map { list ->
                list.flatten().distinct().filter { result ->
                    result.contains(Regex(".*$location.*", RegexOption.IGNORE_CASE))
                }
            }
    }

    fun getQualification(qualification: String): Mono<List<String>> {
        return postRepositoryOps.getOptions("qualification", qualification)
            .map {
                it.qualification?.split(",")?.map { value -> value.trim() } ?: emptyList()
            }
            .collectList()
            .map { list ->
                list.flatten().distinct().filter { result ->
                    result.contains(Regex(".*$qualification.*", RegexOption.IGNORE_CASE))
                }
            }
    }

    fun getCompanies(company: String): Mono<List<String>> {
        return postRepositoryOps.getOptions("company", company)
            .map {
                it.company?.split(",")?.map { value -> value.trim() } ?: emptyList()
            }
            .collectList()
            .map { list ->
                list.flatten().distinct().filter { result ->
                    result.contains(Regex(".*$company.*", RegexOption.IGNORE_CASE))
                }
            }
    }

    fun getSearchOptions(search: String): Mono<List<String>> {
        return postRepositoryOps.getSearchOptions(search)
    }
}
