package com.jobsalrt.webClient

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.time.Duration

@Service
class WebClientWrapper {
    private val webClient = WebClient.builder().build()
    private val defaultRequestTimeout = Duration.ofMinutes(3)

    fun <T> get(
        baseUrl: String,
        path: String,
        returnType: Class<T>,
        queryParams: MultiValueMap<String, String> = LinkedMultiValueMap(),
        uriVariables: Map<String, Any> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        requestTimeout: Duration? = null,
    ): Mono<T> {
        val url = createUrlForRequest(baseUrl, path, uriVariables, queryParams)

        return webClient.get()
            .uri(url)
            .headers { h ->
                headers.map {
                    h.set(it.key, it.value)
                }
            }
            .retrieve()
            .onStatus(HttpStatus::is3xxRedirection) {
                Mono.error(RedirectionError("Redirection Error", it))
            }
            .bodyToMono(returnType)
            .timeout(requestTimeout ?: defaultRequestTimeout)
    }

    fun <T> post(
        baseUrl: String,
        path: String,
        body: Any,
        returnType: Class<T>,
        queryParams: MultiValueMap<String, String> = LinkedMultiValueMap(),
        uriVariables: Map<String, Any> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        requestTimeout: Duration? = null,
        skipLoggingRequestBody: Boolean = false,
        skipLoggingResponseBody: Boolean = false
    ): Mono<T> {

        val url = createUrlForRequest(baseUrl, path, uriVariables, queryParams)

        return webClient
            .post().uri(url)
            .headers { h ->
                headers.map {
                    h.set(it.key, it.value)
                }
            }.bodyValue(body)
            .retrieve()
            .bodyToMono(returnType)
            .timeout(requestTimeout ?: defaultRequestTimeout)
    }

    private fun createUrlForRequest(
        baseUrl: String,
        path: String,
        uriVariables: Map<String, Any>,
        queryParams: MultiValueMap<String, String>
    ) = baseUrl + UriComponentsBuilder
        .fromPath(path)
        .uriVariables(uriVariables)
        .queryParams(queryParams)
        .build()
        .toUriString()
}


data class RedirectionError(override val message: String, val clientResponse: ClientResponse) : Exception(message)
