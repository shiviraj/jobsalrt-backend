package com.jobsalrt.repository

import com.jobsalrt.domain.Contact
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ContactRepository : ReactiveCrudRepository<Contact, String>
