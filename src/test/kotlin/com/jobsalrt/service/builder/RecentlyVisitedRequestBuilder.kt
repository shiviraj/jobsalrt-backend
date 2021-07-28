package com.jobsalrt.service.builder

import com.jobsalrt.controller.view.PostsType
import com.jobsalrt.controller.view.PostsType.RECOMMENDED_JOBS
import com.jobsalrt.controller.view.RecentlyVisitedRequest

data class RecentlyVisitedRequestBuilder(
    val type: PostsType = RECOMMENDED_JOBS,
    val urls: List<String> = listOf()
) {
    fun build(): RecentlyVisitedRequest {
        return RecentlyVisitedRequest(
            type = type,
            urls = urls
        )
    }
}
