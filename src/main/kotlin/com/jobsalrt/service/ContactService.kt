package com.jobsalrt.service

import com.jobsalrt.domain.Contact
import com.jobsalrt.repository.ContactRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class ContactService(
    val contactRepository: ContactRepository
) {
    fun save(contact: Contact): Mono<Contact> {
        return contactRepository.save(contact)
    }

}
