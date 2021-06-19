package com.jobsalrt.domain

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate
import java.time.LocalDateTime

const val POST_COLLECTION = "posts"

@TypeAlias("Post")
@Document(POST_COLLECTION)
data class Post(
    @Id
    val id: ObjectId? = null,
    val basicDetails: BasicDetails = BasicDetails(name = "Anonymous", postLogo = ""),
    val states: List<State> = emptyList(),
    val dates: Details? = null,
    val feeDetails: Details? = null,
    val vacancyDetails: Details? = null,
    val ageLimit: Details? = null,
    val selectionProcess: List<String>? = null,
    val howToApply: List<String>? = null,
    val importantLinks: Details? = null,
    val others: Map<String, Details>? = null,
    @Indexed(unique = true)
    val source: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val status: Status = Status.NOT_VERIFIED,
    val isUpdateAvailable: Boolean = false,
    val otherSource: String? = null,
    val failures: List<String> = emptyList(),
    var totalViews: Long = 0,
    val postUpdateDate: LocalDateTime = LocalDateTime.now()
)

data class BasicDetails(
    val name: String = "Anonymous",
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
)

data class State(
    val type: Type,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class Details(
    val header: List<String> = emptyList(),
    val body: List<List<String>>
)

enum class FormType {
    ONLINE,
    OFFLINE;
}

enum class Type {
    LATEST_JOB,
    ADMIT_CARD,
    RESULT,
    SYLLABUS,
    ANSWER_KEY,
    ADMISSION,
}

enum class Status {
    NOT_VERIFIED,
    DISABLED,
    VERIFIED
}
