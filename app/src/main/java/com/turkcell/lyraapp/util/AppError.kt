package com.turkcell.lyraapp.util

import retrofit2.HttpException
import java.io.IOException

/**
 * Uygulama genelinde tek tip hata modeli.
 *
 * Katman farkı gözetmeksizin ([data]/network kaynaklı) ham hatalar bu [Throwable]'a indirgenir;
 * kullanıcıya gösterilecek metin [toUserMessage] ile [ErrorContext]'e göre çözülür. Böylece
 * ViewModel'ler ham `HttpException`/`IOException` yerine bu sabit sözleşmeyle çalışır.
 */
sealed class AppError(message: String? = null, cause: Throwable? = null) : Throwable(message, cause) {

    /** İnternet/bağlantı kaynaklı hata (ör. [IOException]). */
    data object Network : AppError()

    /** Sunucudan dönen HTTP hata kodu (bkz. [HttpException.code]). */
    data class Api(val code: Int, override val message: String? = null) : AppError(message)

    /** Sınıflandırılamayan diğer hatalar. */
    data class Unknown(override val message: String? = null) : AppError(message)
}

/**
 * Retrofit/OkHttp katmanından yükselen ham [Throwable]'ı [AppError]'a çevirir.
 *
 * - [HttpException] → [AppError.Api] (HTTP kodu korunur)
 * - [IOException]   → [AppError.Network]
 * - zaten [AppError] ise → aynen döner
 * - diğer           → [AppError.Unknown]
 */
fun Throwable.toAppError(): AppError = when (this) {
    is AppError -> this
    is HttpException -> AppError.Api(code = code(), message = message())
    is IOException -> AppError.Network
    else -> AppError.Unknown(message)
}
