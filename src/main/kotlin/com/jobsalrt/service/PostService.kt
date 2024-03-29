package com.jobsalrt.service

import com.jobsalrt.controller.view.FilterRequest
import com.jobsalrt.controller.view.PostsType
import com.jobsalrt.controller.view.RecentlyVisitedRequest
import com.jobsalrt.domain.Post
import com.jobsalrt.repository.PostRepositoryOps
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
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

    fun updateViews(url: String): Mono<Boolean> {
        return postRepositoryOps.updateViews(url).map { true }
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
                list.flatten().distinct()
                    .filter { result ->
                        result.contains(Regex(".*$company.*", RegexOption.IGNORE_CASE))
                    }
                    .map {
                        it.replace(Regex("\\(.*\\)", RegexOption.IGNORE_CASE), "").trim()
                    }
            }
    }

    fun getSearchOptions(search: String): Mono<List<String>> {
        return postRepositoryOps.getSearchOptions(search)
    }

    fun getAllPostsWithRecentlyVisited(recentlyVisitedRequest: RecentlyVisitedRequest): Mono<List<Post>> {
        val recentlyViewed = getPostsByUrls(recentlyVisitedRequest.urls)
        if (recentlyVisitedRequest.type == PostsType.RECENTLY_VIEWED) return recentlyViewed

        val trendingJobs = postRepositoryOps.getTrendingJobs().collectList()
        if (recentlyVisitedRequest.type == PostsType.TRENDING_JOBS) return trendingJobs

        return Mono.zip(postRepositoryOps.newJobs().collectList(), trendingJobs)
            .map {
                it.t1.addAll(it.t2)
                val posts = it.t1.distinct().shuffled()
                if (posts.size > 48) posts.subList(0, 48) else posts
            }
    }

    private fun getPostsByUrls(urls: List<String>): Mono<List<Post>> {
        return Flux.fromIterable(urls)
            .flatMap {
                getPostByUrl(it)
            }.collectList()
    }
}
