package com.jobsalrt.service.builder

import com.jobsalrt.domain.*
import com.jobsalrt.domain.Status.VERIFIED
import org.bson.types.ObjectId
import java.time.LocalDate
import java.time.LocalDateTime

data class PostBuilder(
    val id: ObjectId? = null,
    val basicDetails: BasicDetails = BasicDetailsBuilder().build(),
    val states: List<State> = listOf(),
    val dates: Details? = null,
    val feeDetails: Details? = null,
    val vacancyDetails: Details? = null,
    val ageLimit: Details? = null,
    val selectionProcess: List<String>? = listOf(),
    val howToApply: List<String>? = listOf(),
    val importantLinks: Details? = null,
    val others: Map<String, Details>? = mapOf(),
    val source: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val status: Status = VERIFIED,
    val isUpdateAvailable: Boolean = false,
    val otherSource: String? = null,
    val failures: List<String> = listOf(),
    val totalViews: Long = 0,
    val postUpdateDate: LocalDateTime = LocalDateTime.now()
) {
    fun build(): Post {
        return Post(
            id = id,
            basicDetails = basicDetails,
            states = states,
            dates = dates,
            feeDetails = feeDetails,
            vacancyDetails = vacancyDetails,
            ageLimit = ageLimit,
            selectionProcess = selectionProcess,
            howToApply = howToApply,
            importantLinks = importantLinks,
            others = others,
            source = source,
            createdAt = createdAt,
            status = status,
            isUpdateAvailable = isUpdateAvailable,
            otherSource = otherSource,
            failures = failures,
            totalViews = totalViews,
            postUpdateDate = postUpdateDate
        )
    }
}

data class BasicDetailsBuilder(
    val name: String = "",
    val formType: FormType? = null,
    val advtNo: String? = null,
    val lastDate: LocalDate? = null,
    val vacancies: Long? = null,
    val location: String? = null,
    val company: String? = null,
    val qualification: String? = null,
    val minAgeLimit: LocalDate? = null,
    val maxAgeLimit: LocalDate? = null,
    val postLogo: String = "",
    val url: String = ""
) {
    fun build(): BasicDetails {
        return BasicDetails(
            name = name,
            formType = formType,
            advtNo = advtNo,
            lastDate = lastDate,
            vacancies = vacancies,
            location = location,
            company = company,
            qualification = qualification,
            minAgeLimit = minAgeLimit,
            maxAgeLimit = maxAgeLimit,
            postLogo = postLogo,
            url = url
        )
    }
}

data class DetailsBuilder(
    val header: List<String> = listOf(),
    val body: List<List<String>> = listOf()
) {
    fun build(): Details {
        return Details(
            header = header,
            body = body
        )
    }
}
