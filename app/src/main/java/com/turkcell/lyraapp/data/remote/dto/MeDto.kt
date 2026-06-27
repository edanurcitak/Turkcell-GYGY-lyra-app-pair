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

// --- Oynatma (playback) uçları — free akış: sunucu "sıradaki ne çalınmalı?"yı belirler ---

/** `POST /me/playback/next` gövdesi: sıradaki çalınacak şarkının id'si. */
@Serializable
data class PlaybackNextBody(
    val songId: String,
)

/** `POST /me/playback/next` yanıt zarfı (şarkı ya da önce reklam). */
@Serializable
data class PlaybackNextResponseDto(
    val data: PlaybackNextDataDto = PlaybackNextDataDto(),
)

/**
 * `playback/next` yükü. API `oneOf` (PlaybackSong | PlaybackAd) döndürür; [type] ayraçtır
 * ("song" | "ad"). Polimorfik çözüm yerine düz, varsayılan-değerli tek DTO tutulur (mevcut DTO
 * deseni): her iki durumda da [song]+[stream] gelir; reklamlıda ek olarak [ad]+[adStream]+
 * [impressionId]. Akış yükü için katalogdaki [StreamUrlDto] yeniden kullanılır (aynı şema).
 */
@Serializable
data class PlaybackNextDataDto(
    val type: String = "song",
    val song: SongDto? = null,
    val stream: StreamUrlDto? = null,
    val ad: AdDto? = null,
    val adStream: StreamUrlDto? = null,
    val impressionId: String? = null,
)

/** API `Ad` şeması (bkz. `docs/api/openapi.json`). */
@Serializable
data class AdDto(
    val id: String = "",
    val title: String = "",
    val advertiser: String = "",
    val durationMs: Long = 0,
    val mimeType: String = "",
)

/** `POST /me/playback/ad-complete` gövdesi: tamamlanan reklamın izlenim (impression) kimliği. */
@Serializable
data class AdCompleteBody(
    val impressionId: String,
)

/** `POST /me/playback/ad-complete` yanıt zarfı: `{ data: { completed: true } }`. */
@Serializable
data class AdCompleteResponseDto(
    val data: AdCompleteDataDto = AdCompleteDataDto(),
)

@Serializable
data class AdCompleteDataDto(
    val completed: Boolean = false,
)
