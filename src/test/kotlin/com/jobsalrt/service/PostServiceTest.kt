package com.jobsalrt.service

import com.jobsalrt.controller.view.PostsType
import com.jobsalrt.repository.PostRepositoryOps
import com.jobsalrt.service.builder.BasicDetailsBuilder
import com.jobsalrt.service.builder.FilterRequestBuilder
import com.jobsalrt.service.builder.PostBuilder
import com.jobsalrt.service.builder.RecentlyVisitedRequestBuilder
import com.jobsalrt.service.testUtils.assertNextWith
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class PostServiceTest {
    private val postRepositoryOps = mockk<PostRepositoryOps>()
    private val postService = PostService(postRepositoryOps)

    @Test
    fun `should get all the posts`() {
        val post = PostBuilder().build()
        every { postRepositoryOps.findPosts(any(), any()) } returns Flux.just(post)

        val filterRequest = FilterRequestBuilder().build()
        val allPosts = postService.getAllPosts(1, filterRequest)

        assertNextWith(allPosts) {
            it shouldHaveSize 1
            it shouldBe listOf(post)

            verify(exactly = 1) {
                postRepositoryOps.findPosts(filterRequest, 1)
            }
        }
    }

    @Test
    fun `should get posts page count`() {
        every { postRepositoryOps.findPostCount(any()) } returns Mono.just(Pair(1, 1.0))

        val filterRequest = FilterRequestBuilder().build()
        val postsCount = postService.getPostsPageCount(filterRequest)

        assertNextWith(postsCount) {
            it.first shouldBe 1
            it.second shouldBe 1.0
            verify(exactly = 1) {
                postRepositoryOps.findPostCount(filterRequest)
            }
        }
    }

    @Test
    fun `should get post by post url`() {
        val post = PostBuilder(basicDetails = BasicDetailsBuilder(url = "url").build()).build()
        every { postRepositoryOps.findByBasicDetailsUrl(any()) } returns Mono.just(post)

        val actualPost = postService.getPostByUrl("url")

        assertNextWith(actualPost) {
            it shouldBe post
            verify(exactly = 1) {
                postRepositoryOps.findByBasicDetailsUrl("url")
            }
        }
    }

    @Test
    fun `should update post view`() {
        val post = PostBuilder(basicDetails = BasicDetailsBuilder(url = "url").build()).build()
        every { postRepositoryOps.updateViews(any()) } returns Mono.just(post)

        val actualPost = postService.updateViews("url")

        assertNextWith(actualPost) {
            it shouldBe true
            verify(exactly = 1) {
                postRepositoryOps.updateViews("url")
            }
        }
    }

    @Test
    fun `should get all the locations`() {
        val basicDetails = BasicDetailsBuilder(location = "Uttar Pradesh, Maharastra").build()
        every { postRepositoryOps.getOptions(any(), any()) } returns Flux.just(basicDetails)

        val locations = postService.getLocations("uttar")

        assertNextWith(locations) {
            it shouldHaveSize 1
            it shouldBe listOf("Uttar Pradesh")
            verify(exactly = 1) {
                postRepositoryOps.getOptions("location", "uttar")
            }
        }
    }

    @Test
    fun `should return empty list to get all the locations for null locations`() {
        val basicDetails = BasicDetailsBuilder().build()
        every { postRepositoryOps.getOptions(any(), any()) } returns Flux.just(basicDetails)

        val locations = postService.getLocations("uttar")

        assertNextWith(locations) {
            it shouldHaveSize 0
            verify(exactly = 1) {
                postRepositoryOps.getOptions("location", "uttar")
            }
        }
    }


    @Test
    fun `should get all the qualifications`() {
        val basicDetails = BasicDetailsBuilder(qualification = "BCA, B.Sc, Diploma").build()
        every { postRepositoryOps.getOptions(any(), any()) } returns Flux.just(basicDetails)

        val qualifications = postService.getQualification("b")

        assertNextWith(qualifications) {
            it shouldHaveSize 2
            it shouldBe listOf("BCA", "B.Sc")
            verify(exactly = 1) {
                postRepositoryOps.getOptions("qualification", "b")
            }
        }
    }


    @Test
    fun `should return empty list to get all the qualifications for null qualifications`() {
        val basicDetails = BasicDetailsBuilder().build()
        every { postRepositoryOps.getOptions(any(), any()) } returns Flux.just(basicDetails)

        val qualifications = postService.getQualification("b")

        assertNextWith(qualifications) {
            it shouldHaveSize 0
            verify(exactly = 1) {
                postRepositoryOps.getOptions("qualification", "b")
            }
        }
    }


    @Test
    fun `should get all the companies`() {
        val basicDetails = BasicDetailsBuilder(company = "UPSC").build()
        every { postRepositoryOps.getOptions(any(), any()) } returns Flux.just(basicDetails)

        val companies = postService.getCompanies("u")

        assertNextWith(companies) {
            it shouldHaveSize 1
            it shouldBe listOf("UPSC")
            verify(exactly = 1) {
                postRepositoryOps.getOptions("company", "u")
            }
        }
    }

    @Test
    fun `should get all search options`() {
        every { postRepositoryOps.getSearchOptions(any()) } returns Mono.just(listOf("UPSC"))

        val searchOptions = postService.getSearchOptions("u")

        assertNextWith(searchOptions) {
            it shouldHaveSize 1
            it shouldBe listOf("UPSC")
            verify(exactly = 1) {
                postRepositoryOps.getSearchOptions("u")
            }
        }
    }

    @Test
    fun `should get all posts with recently visited`() {
        val recentlyVisitedRequest = RecentlyVisitedRequestBuilder(PostsType.RECOMMENDED_JOBS, listOf("url")).build()
        val post = PostBuilder(basicDetails = BasicDetailsBuilder(url = "url").build()).build()
        every { postRepositoryOps.getTrendingJobs() } returns Flux.just(post)
        every { postRepositoryOps.newJobs() } returns Flux.just(post)


        val searchOptions = postService.getAllPostsWithRecentlyVisited(recentlyVisitedRequest)

        assertNextWith(searchOptions) {
            it shouldHaveSize 1
            it shouldBe listOf(post)
            verify(exactly = 1) {
                postRepositoryOps.getTrendingJobs()
                postRepositoryOps.newJobs()
            }
        }
    }

    @Test
    fun `should get all posts with recently viewed`() {
        val recentlyVisitedRequest = RecentlyVisitedRequestBuilder(PostsType.RECENTLY_VIEWED, listOf("url")).build()
        val post = PostBuilder(basicDetails = BasicDetailsBuilder(url = "url").build()).build()
        every { postRepositoryOps.findByBasicDetailsUrl("url") } returns Mono.just(post)


        val searchOptions = postService.getAllPostsWithRecentlyVisited(recentlyVisitedRequest)

        assertNextWith(searchOptions) {
            it shouldHaveSize 1
            it shouldBe listOf(post)
            verify(exactly = 1) {
                postRepositoryOps.findByBasicDetailsUrl("url")
            }
        }
    }


    @Test
    fun `should get all posts with trending jobs`() {
        val recentlyVisitedRequest = RecentlyVisitedRequestBuilder(PostsType.TRENDING_JOBS, listOf("url")).build()
        val post = PostBuilder(basicDetails = BasicDetailsBuilder(url = "url").build()).build()
        every { postRepositoryOps.getTrendingJobs() } returns Flux.just(post)


        val searchOptions = postService.getAllPostsWithRecentlyVisited(recentlyVisitedRequest)

        assertNextWith(searchOptions) {
            it shouldHaveSize 1
            it shouldBe listOf(post)
            verify(exactly = 1) {
                postRepositoryOps.getTrendingJobs()
            }
        }
    }


}
