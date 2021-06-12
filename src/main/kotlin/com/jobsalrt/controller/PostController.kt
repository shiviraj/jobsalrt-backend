package com.jobsalrt.controller

import com.jobsalrt.domain.Post
import com.jobsalrt.service.PostService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/post")
class PostController(
    @Autowired val postService: PostService,
) {

    @GetMapping("/{url}")
    fun getPostByUrl(@PathVariable url: String): Mono<Post> {
        return postService.getPostByUrl(url)
    }
}

