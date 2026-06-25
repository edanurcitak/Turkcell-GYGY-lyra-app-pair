package com.turkcell.lyraapp.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * `me` (profil) uçlarının DTO'ları (bkz. `docs/api/openapi.json` → `me`).
 *
 * Mevcut DTO deseni korunur: `@Serializable` zarf (`...ResponseDto { data }`), yanıt alanları
 * varsayılan değerli. Kullanıcı yükü için var olan [UserDto] yeniden kullanılır
 * (DTO → domain dönüşümü [toDomain] üzerinden, bkz. `AuthDto.kt`).
 */

/** `POST /me/update-informations` gövdesi: kayıt/profil tamamlama adımı. */
@Serializable
data class UpdateInformationsBody(
    val firstName: String,
    val lastName: String,
    /** Takvim tarihi `YYYY-MM-DD` biçiminde (geçerli, gelecekte olmayan bir tarih). */
    val birthDate: String,
)

/** `POST /me/update-informations` yanıt zarfı: güncellenmiş kullanıcı (`profileCompleted: true`). */
@Serializable
data class UserResponseDto(
    val data: UserDto = UserDto(),
)

/**
 * `POST /me/plays` gövdesi: gerçek bir çalmayı kaydeder.
 *
 * "Son Çalınanlar" ve öneri uçlarını besleyen tek sinyaldir; parça başına bir kez (Range isteği
 * başına değil) gönderilir.
 */
@Serializable
data class RecordPlayBody(
    val songId: String,
)

/** `POST /me/plays` yanıt zarfı: `{ data: { recorded: true } }`. */
@Serializable
data class RecordPlayResponseDto(
    val data: RecordPlayDataDto = RecordPlayDataDto(),
)

@Serializable
data class RecordPlayDataDto(
    val recorded: Boolean = false,
)
