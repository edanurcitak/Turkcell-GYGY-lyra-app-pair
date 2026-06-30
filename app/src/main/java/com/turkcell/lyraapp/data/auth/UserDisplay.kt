package com.turkcell.lyraapp.data.auth

/**
 * Kullanıcı kimliğinden gösterim değeri (ad / baş harf) türeten saf yardımcılar.
 *
 * Tek kaynak: hem Profil ekranı (ad + baş harf) hem Ana sayfa başlığındaki avatar (baş harf) bu
 * mantığı paylaşır. Kaynak API'dir (§2.2 — istemci uydurmaz, yalnızca eldeki alanları biçimlendirir);
 * boş-durum etiketi (fallback) çağıran ekrana bırakılır, burada üretilmez (her ekranın kendi
 * nötr varsayılanı vardır).
 */

/**
 * Gösterilecek adı çözer: önce register'da girilen "ad soyad", yoksa API'nin [User.displayName]'i,
 * o da yoksa telefon. Hiçbiri yoksa `null` döner (çağıran kendi nötr varsayılanını uygular).
 */
fun User?.resolveDisplayName(): String? {
    val firstLast = listOfNotNull(this?.firstName, this?.lastName)
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .joinToString(" ")
    if (firstLast.isNotEmpty()) return firstLast

    val display = this?.displayName?.trim().orEmpty()
    if (display.isNotEmpty()) return display

    val phone = this?.phone?.trim().orEmpty()
    if (phone.isNotEmpty()) return phone

    return null
}

/**
 * Addan baş harfleri (en fazla iki) türetir; harf ile başlamayan parçalar (ör. telefon) elenir.
 * Türetilemezse `null` döner (çağıran kendi nötr varsayılanını uygular).
 */
fun resolveInitials(name: String?): String? =
    name?.split(' ')
        ?.filter { it.isNotBlank() && it.first().isLetter() }
        ?.take(2)
        ?.map { it.first().uppercaseChar() }
        ?.joinToString("")
        ?.ifBlank { null }
