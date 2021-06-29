package com.jobsalrt.domain

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

const val CONTACT_COLLECTION = "contacts"

@TypeAlias("Contact")
@Document(CONTACT_COLLECTION)
data class Contact(
    @Id
    val id: ObjectId? = null,
    val name: String,
    val email: String? = null,
    val subject: String,
    val message: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val isResolved: Boolean = false,
    val isFeedback: Boolean = false
)
