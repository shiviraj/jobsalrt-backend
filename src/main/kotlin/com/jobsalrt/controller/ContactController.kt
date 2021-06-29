package com.jobsalrt.controller

import com.jobsalrt.domain.Contact
import com.jobsalrt.service.ContactService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class ContactController(
    val contactService: ContactService
) {
    @PostMapping("/contact")
    fun saveContact(@RequestBody contact: Contact): Mono<Contact> {
        return contactService.save(contact)
    }
}
