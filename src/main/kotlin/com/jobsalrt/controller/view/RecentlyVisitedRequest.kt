package com.jobsalrt.controller.view

data class RecentlyVisitedRequest(val type: PostsType, var urls: List<String>) {
    init {
        if (urls.size > 48) urls = urls.subList(0, 48)
    }
}

enum class PostsType {
    TRENDING_JOBS,
    RECOMMENDED_JOBS,
    RECENTLY_VIEWED
}
