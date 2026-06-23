package com.turkcell.lyraapp.data.remote.dto

import com.turkcell.lyraapp.data.auth.AuthSession
import com.turkcell.lyraapp.data.auth.AuthTokens
import com.turkcell.lyraapp.data.auth.User
import kotlinx.serialization.Serializable

/**
 * Auth (parolasız OTP) uçlarının DTO'ları (bkz. `docs/api/openapi.json` → `auth`).
 *
 * Akış: `POST /auth/otp/request` (kod "gönderilir") → `POST /auth/otp/verify` (token alınır).
 * `refresh` token'ı rotate eder; `logout` refresh token'ı iptal eder.
 *
 * Mevcut DTO deseni korunur: `@Serializable` zarf (`...ResponseDto { data }`), tüm alanlar
 * varsayılan değerli (yanıtın eksik/değişken olmasına karşı), DTO → domain `toDomain()` ile.
 */

// --- İstek gövdeleri ---

/** `POST /auth/otp/request` gövdesi. */
@Serializable
data class OtpRequestBody(
    val phone: String,
)

/** `POST /auth/otp/verify` gövdesi. */
@Serializable
data class OtpVerifyBody(
    val phone: String,
    val code: String,
)

/** `POST /auth/refresh` gövdesi. */
@Serializable
data class RefreshBody(
    val refreshToken: String,
)

/** `POST /auth/logout` gövdesi. */
@Serializable
data class LogoutBody(
    val refreshToken: String,
)

// --- Yanıt zarfları ---

/** `POST /auth/otp/request` yanıt zarfı. */
@Serializable
data class OtpRequestResponseDto(
    val data: OtpRequestDataDto = OtpRequestDataDto(),
)

/**
 * OTP isteği yükü.
 *
 * [firstTime]: telefon yeni ya da kaydı tamamlanmamışsa `true` (doğrulama sonrası profil
 * tamamlama adımına yönlendirme bu alana göre yapılır).
 */
@Serializable
data class OtpRequestDataDto(
    val sent: Boolean = false,
    val firstTime: Boolean = false,
)

/** `POST /auth/otp/verify` yanıt zarfı (oturum). */
@Serializable
data class AuthSessionResponseDto(
    val data: AuthSessionDto,
)

/** API `AuthSession` şeması: token çifti + kullanıcı + `firstTime`. */
@Serializable
data class AuthSessionDto(
    val accessToken: String = "",
    val refreshToken: String = "",
    val tokenType: String = "Bearer",
    val expiresIn: Long = 0,
    val user: UserDto? = null,
    val firstTime: Boolean = false,
)

/** `POST /auth/refresh` yanıt zarfı (yeni token çifti). */
@Serializable
data class AuthTokensResponseDto(
    val data: AuthTokensDto,
)

/** API `AuthTokens` şeması. */
@Serializable
data class AuthTokensDto(
    val accessToken: String = "",
    val refreshToken: String = "",
    val tokenType: String = "Bearer",
    val expiresIn: Long = 0,
)

/** `POST /auth/logout` yanıt zarfı. */
@Serializable
data class LogoutResponseDto(
    val data: RevokedDto = RevokedDto(),
)

/** Logout sonucu. */
@Serializable
data class RevokedDto(
    val revoked: Boolean = false,
)

/** API `User` şeması (bkz. `docs/api/openapi.json`). */
@Serializable
data class UserDto(
    val id: String = "",
    val phone: String = "",
    val displayName: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val birthDate: String? = null,
    val createdAt: String? = null,
    val profileCompleted: Boolean = false,
)

// --- DTO → domain dönüşümleri ---

/** DTO → domain [User]. */
fun UserDto.toDomain(): User = User(
    id = id,
    phone = phone,
    displayName = displayName,
    firstName = firstName,
    lastName = lastName,
    birthDate = birthDate,
    profileCompleted = profileCompleted,
)

/** DTO → domain [AuthTokens]. */
fun AuthTokensDto.toDomain(): AuthTokens = AuthTokens(
    accessToken = accessToken,
    refreshToken = refreshToken,
    expiresIn = expiresIn,
)

/** DTO → domain [AuthSession] (token alanları kendi seviyesinden okunur). */
fun AuthSessionDto.toDomain(): AuthSession = AuthSession(
    tokens = AuthTokens(
        accessToken = accessToken,
        refreshToken = refreshToken,
        expiresIn = expiresIn,
    ),
    user = user?.toDomain(),
    firstTime = firstTime,
)
