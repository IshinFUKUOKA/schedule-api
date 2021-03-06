package dev.yoghurt1131.calendarapi.application.controller

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import dev.yoghurt1131.calendarapi.GoogleOAuth2
import dev.yoghurt1131.calendarapi.service.CalendarService
import dev.yoghurt1131.calendarapi.service.Schedule
import dev.yoghurt1131.personallib.auth.HumbleAuth
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class CalendarController(
        private val googleCredential: GoogleCredential,
        private val googleClientSecrets: GoogleClientSecrets,
        private val googleAuthorizationCodeFlow: GoogleAuthorizationCodeFlow,
        private val calendarService: CalendarService) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping("/schedule/daily")
    @HumbleAuth
    @GoogleOAuth2
    fun todaySchedule(): Schedule {
        val schedule = calendarService.getSchedule(1L)
        return schedule
    }

    @GetMapping("/schedule/weekly")
    @HumbleAuth
    @GoogleOAuth2
    fun weeklySchedule(): Schedule {
        val schedule = calendarService.getSchedule(7L)
        return schedule
    }

    /**
     * receive callback request from google oauth server
     */
    @GetMapping("/callback")
    fun callback(code: String, scope: String): String {
        // codeをもとにアクセストークンを取得
        val request = googleAuthorizationCodeFlow.newTokenRequest(code)
                .setRedirectUri(googleClientSecrets.details.redirectUris.first())
        val response = request.execute()
        googleCredential.setAccessToken(response.accessToken)
        return "This is endpoint for google oauth2 callback."

    }
}