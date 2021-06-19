package com.jobsalrt.controller

import com.jobsalrt.controller.view.BasicDetailsView
import com.jobsalrt.controller.view.FilterRequest
import com.jobsalrt.controller.view.PageCountView
import com.jobsalrt.service.PostService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono


@RestController
@RequestMapping("/posts")
class PostsController(
    @Autowired val postService: PostService
) {

    @PostMapping("/page/{page}")
    fun getAllPosts(@PathVariable page: Int, @RequestBody filterRequest: FilterRequest): Mono<List<BasicDetailsView>> {
        return postService.getAllPosts(page, filterRequest)
            .map { posts ->
                posts.map {
                    BasicDetailsView.from(it)
                }
            }
    }

    @PostMapping("/page-count")
    fun getPostsPageCount(@RequestBody filterRequest: FilterRequest): Mono<PageCountView> {
        return postService.getPostsPageCount(filterRequest)
            .map {
                PageCountView(page = it.second.toLong(), totalPost = it.first)
            }
    }

    @GetMapping("/options/location/{location}")
    fun getLocations(@PathVariable location: String): Mono<List<String>> {
        return postService.getLocations(location)
    }

    @GetMapping("/options/qualification/{qualification}")
    fun getQualifications(@PathVariable qualification: String): Mono<List<String>> {
        return postService.getQualification(qualification)
    }

    @GetMapping("/options/company/{company}")
    fun getCompanies(@PathVariable company: String): Mono<List<String>> {
        return postService.getCompanies(company)
    }

    @GetMapping("/search-options/{search}")
    fun getSearchOptions(@PathVariable search: String): Mono<List<String>> {
        return postService.getSearchOptions(search)
    }
}

