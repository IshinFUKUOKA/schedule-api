package dev.yoghurt1131.calendarapi

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets.Details
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import dev.yoghurt1131.calendarapi.application.interceptor.GoogleOAuth2Interceptor
import dev.yoghurt1131.calendarapi.service.GoogleOAuth2Service
import dev.yoghurt1131.personallib.auth.HeaderAuthInterceptor
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.util.*


@Configuration
class CalendarConfig(private val properties: CalendarProperties) {

    private val SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY)
    private val JSON_FACTORY = JacksonFactory.getDefaultInstance()
    val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()

    val logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun details(): Details{
        var details = Details()
        details.clientId = properties.clientId
        details.authUri = properties.authUri
        details.tokenUri = properties.tokenUri
        details.clientSecret = properties.clientSecret
        details.redirectUris = properties.redirectUris
        return details
    }

    @Bean
    fun googleClientSecrets(details: Details): GoogleClientSecrets {
        var googleClientSecrets = GoogleClientSecrets()
        googleClientSecrets.installed = details
        return googleClientSecrets
    }


    @Bean
    fun googleCredential(details: Details): GoogleCredential {
        logger.info("initialize bean of Credential")
        return GoogleCredential()
    }

    @Bean
    fun googleAuthorizationCodeFlow(googleClientSecrets: GoogleClientSecrets): GoogleAuthorizationCodeFlow {
        return GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, googleClientSecrets, SCOPES)
                .build()
    }


    @Bean
    fun calender(googleCredential: GoogleCredential): Calendar {
        logger.info("initialize bean of Calendar")
        // Build a new authorized API client service.
        val service = Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, googleCredential).build()
        return service
    }

}

@Configuration
class CalendarWebMvcConfigure(
        private val googleClientSecrets: GoogleClientSecrets,
        private val googleCredential: GoogleCredential,
        private val headerCheckProperties: HeaderCheckProperties,
        private val googleOauth2Service : GoogleOAuth2Service
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        val content = System.getenv("HEADER_CONTENT")
        registry.addInterceptor(HeaderAuthInterceptor(headerCheckProperties.content))
        registry.addInterceptor(GoogleOAuth2Interceptor(googleClientSecrets, googleCredential, googleOauth2Service))
                .addPathPatterns("/schedule/**")
    }
}

