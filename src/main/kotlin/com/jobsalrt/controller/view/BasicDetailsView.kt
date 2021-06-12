package com.jobsalrt.controller.view

import com.jobsalrt.domain.FormType
import com.jobsalrt.domain.Post
import org.bson.types.ObjectId
import java.time.LocalDate
import java.time.LocalDateTime

data class BasicDetailsView(
    val id: ObjectId? = null,
    val name: String,
    val formType: FormType? = FormType.ONLINE,
    val advtNo: String? = null,
    val lastDate: LocalDate? = null,
    val totalVacancies: Long? = null,
    val location: String? = null,
    val company: String? = null,
    val qualification: String? = null,
    val postLogo: String = "",
    val url: String,
    val createdAt: LocalDateTime,
    val postUpdateDate: LocalDateTime,
    val isUpdateAvailable: Boolean,
    val totalViews: Long
) {
    companion object {
        fun from(post: Post): BasicDetailsView {
            val basicDetails = post.basicDetails
            return BasicDetailsView(
                id = post.id,
                name = basicDetails.name,
                formType = basicDetails.formType ?: FormType.ONLINE,
                advtNo = basicDetails.advtNo,
                lastDate = basicDetails.lastDate,
                totalVacancies = basicDetails.totalVacancies,
                location = basicDetails.location,
                company = basicDetails.company,
                qualification = basicDetails.qualification,
                postLogo = basicDetails.postLogo,
                url = basicDetails.url,
                createdAt = post.createdAt,
                postUpdateDate = post.postUpdateDate,
                isUpdateAvailable = post.isUpdateAvailable,
                totalViews = post.totalViews
            )
        }
    }
}
