package com.jobsalrt.repository

import org.springframework.stereotype.Component

@Component
class KeyUtils {
    fun find(key: String): String {
        val keyMapping = mapOf(
            "formType" to "basicDetails.formType",
            "location" to "basicDetails.location",
            "qualification" to "basicDetails.qualification",
            "company" to "basicDetails.company",
            "type" to "states.type"
        )
        return keyMapping[key] ?: key
    }
}
