package com.jobsalrt.controller.view

data class RecentlyVisitedRequest(val type: PostsType, val urls: List<String>, val count: Int = 36)

enum class PostsType {
    TRENDING_JOBS,
    RECOMMENDED_JOBS,
    RECENTLY_VIEWED
}
