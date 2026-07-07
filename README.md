<div align="center">

# 🎵 LyraApp

**Online / Offline Müzik Çalar Uygulaması**

Kotlin Jetpack Compose ile yazılmış, arka planda çalma, çevrimdışı dinleme ve premium üyelik akışları içeren modern bir Android müzik çalar  uygulaması.

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](#)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)](#)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.09-4285F4?logo=jetpackcompose&logoColor=white)](#)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24-orange)](#)
[![Architecture](https://img.shields.io/badge/Architecture-MVI-blueviolet)](#)

</div>

---

## 📖  Uygulama Hakkında

**LyraApp**,  kullanıcıların çevrimiçi müzik akışı (streaming) yapabildiği, özel çalma listeleri oluşturup yönetebildiği, favori şarkılarını beğenerek kişisel bir kütüphane oluşturabildiği, şarkıları çevrimdışı dinlemek üzere indirebildiği ve premium üyelik satın alabildiği gelişmiş bir müzik çalar uygulamasıdır.

Uygulama; güvenli kimlik doğrulama, dinamik keşfet akışı, gelişmiş arama, tam ekran ve mini oynatıcı modları ile arka planda kesintisiz çalma gibi eksiksiz bir müzik deneyimi sunar. Projenin backend mimarisi, ayrı bir ekip tarafından geliştirilen bağımsız bir RESTful API üzerinden sağlanmaktadır.

## ✨ Uygulama Özellikleri

- 🔐 **Kimlik Doğrulama** — Giriş, kayıt ve OTP doğrulama akışı
- 🏠 **Keşfet / Ana Sayfa** — Kişiselleştirilmiş içerik akışı (feed)
- 🔍 **Arama** — Şarkı ve içerik arama
- 📚 **Kütüphane & Çalma Listeleri** — Çalma listesi oluşturma, detay görüntüleme ve yönetme
- ❤️ **Favoriler** — Şarkıları dinamik olarak favorilere ekleme/çıkarma
- ▶️ **Oynatıcı** — Tam ekran oynatıcı + kalıcı mini player
- 🎧 **Arka Planda Çalma** — Media3 (ExoPlayer) tabanlı `MediaSessionService` ile bildirim üzerinden kontrol
- 📶 **Online / Offline** — Ağ durumunu algılama; çevrimdışıyken yalnızca indirilenleri gösterme
- ⬇️ **İndirme** — Şarkıları çevrimdışı dinleme için indirme
- 💎 **Premium & Ödeme** — Üyelik ve ödeme akışı
- 👤 **Profil** — Kullanıcı profili ve çıkış (logout)





## 📱 Uygulama İçi Ekran Görüntüleri

### Giriş Akışı
| 1. Telefon Girişi | 2. OTP Doğrulama | 3. Kayıt Ol |
|:---:|:---:|:---:|
| <img src="https://github.com/user-attachments/assets/0e2a7ebc-b5d0-4561-9d4b-6f35e38ea210" width="240px" alt="Telefon Giriş Ekranı" /> | <img src="https://github.com/user-attachments/assets/80c00719-f5a5-4df5-8540-b32161efffea" width="240px" alt="OTP Ekranı" /> | <img src="https://github.com/user-attachments/assets/ee4c75e8-3e3f-4b79-8e64-7ed0ad1cd5d1" width="240px" alt="Kayıt Ol" /> |

### Ana Sayfalar
| Ana Sayfa Ekranı | Arama Ekranı | Kütüphane Ekranı |
|:---:|:---:|:---:|
| <img src="https://github.com/user-attachments/assets/c1966fa4-dc10-4052-a3ac-32e887f2d084" width="240px" alt="Ana Sayfa" /> | <img src="https://github.com/user-attachments/assets/75e4bbb0-4c3b-465b-82c6-dd3110adc38d" width="240px" alt="Arama Ekranı" /> /> |<img src="https://github.com/user-attachments/assets/d9512490-5a8b-4f05-857f-d7cdba1be015" width="240px" alt="Arama" />|

| Profil Ekranı 1 | Profil Ekranı 2 |
|:---:|:---:|
| <img src="https://github.com/user-attachments/assets/7bd37913-005b-43e4-8a04-ca45e440b34b" width="240px" alt="Profil Ekranı 1" /> | <img src="https://github.com/user-attachments/assets/55a93604-3e92-4903-8dff-d8dfc2b9847c" width="240px" alt="Profil Ekranı 2" /> |

### Premium & Ödeme
| Premium Seçim Ekranı | Ödeme Bilgileri Ekranı | Ödeme Onayı Ekranı | Ödeme Başarılı Ekranı |
|:---:|:---:|:---:|:---:|
| <img src="https://github.com/user-attachments/assets/a737896b-4bd2-4d0c-8b9a-da67c67062b0" width="300px" alt="Premium" /> | <img src="https://github.com/user-attachments/assets/44bad581-1ec0-4422-8931-f2a0f2d63600" width="300px" alt="Ödeme Ekranı 1" /> | <img src="https://github.com/user-attachments/assets/019bc0a5-d44a-4c13-b255-6d7505870216" width="300px" alt="Ödeme Ekranı 2" /> | <img src="https://github.com/user-attachments/assets/f2c47d4a-0db6-4a7c-8392-8848bde6bc1f" width="300px" alt="Ödeme Başarılı Ekranı" /> |

### Müzik Çalma
| Şimdi Çalıyor | Mini Oynatıcı | Arka Planda Çalma |
|:---:|:---:|:---:|
| <img src="https://github.com/user-attachments/assets/fb7f8771-03ec-45ca-bea6-146cacb6d4e3" width="240px" alt="Şimdi Çalıyor" /> | <img src="https://github.com/user-attachments/assets/e1459000-cd75-4775-898a-676f02505ab7" width="240px" alt="Mini Player" /> | <img src="https://github.com/user-attachments/assets/1c64cb3b-e77f-49e2-9c6b-5d6c5c93f007" width="240px" alt="Arka Planda Çalma Ekranı" /> |

### Çalma Listeleri
| Çalma Listesi | Favoriler | Yeni Liste Ekle | Çalma Listesi Silme |
|:---:|:---:|:---:|:---:|
| <img src="https://github.com/user-attachments/assets/7ebe2902-6a2c-4531-8b97-ae787ddc3842" width="285px" alt="Çalma Listesi Ekranı" /> | <img src="https://github.com/user-attachments/assets/d406ad62-e8f0-48f4-9fee-add2b858c898" width="285px" alt="Favoriler Listesi Ekranı" /> | <img src="https://github.com/user-attachments/assets/3f5f15f1-4540-4345-a75a-a3480cebd01c" width="300px" alt="Yeni Liste" /> | <img src="https://github.com/user-attachments/assets/0544ef6b-9059-4846-a313-9765ac570b9c" width="285px" alt="Çalma Listesi Silme Ekranı" /> |
### Çevrimdışı Mod
| Çevrimdışı Uyarı | İndirilen Müzik | Çevrimdışı Çalma |
|:---:|:---:|:---:|
| <img src="https://github.com/user-attachments/assets/2ca1946c-b73c-4ffe-b617-88a23ec25b6c" width="240px" alt="Çevrimdışı" /> | <img src="https://github.com/user-attachments/assets/1ffde7bd-3f5e-47b9-84e7-2cc3324bc699" width="240px" alt="İndirilen" /> | <img src="https://github.com/user-attachments/assets/dd7656bf-52d5-4bd4-b6fd-0c1250720ffc" width="240px" alt="Çevrimdışı Çalma" /> |


## 🛠️ Teknoloji Yığını

| Katman | Teknoloji |
|---|---|
| **Dil** | Kotlin 2.0.21 |
| **UI** | Jetpack Compose · Material 3 |
| **Mimari** | MVI (Model–View–Intent) |
| **State** | Kotlin `StateFlow` + `collectAsStateWithLifecycle` |
| **DI** | Hilt (Dagger)  |
| **Navigasyon** | Compose Navigation |
| **Ağ** | Retrofit · OkHttp · Kotlinx Serialization |
| **Medya** | AndroidX Media3 (ExoPlayer · UI · Session) |
| **Build** | Gradle (Kotlin DSL) · Version Catalog |

## 🏛️ Mimari

Uygulama **MVI (Model–View–Intent)** desenini takip eder ve **tek yönlü veri akışı** (unidirectional data flow) ile öngörülebilir, test edilebilir bir UI sağlar. Akış her zaman aynı yönde ilerler:

```
Intent ──▶ ViewModel (reducer) ──▶ UiState ──▶ Screen
  ▲                                              │
  └──────────────  kullanıcı aksiyonu  ──────────┘
```

Her özellik `ui/<feature>/` paketi altında üç dosya ile yapılandırılır:

```
ui/<feature>/
├── <Feature>Contract.kt    # UiState (state) + Intent (kullanıcı aksiyonları)
├── <Feature>ViewModel.kt   # @HiltViewModel, tek giriş noktası: onIntent()
└── <Feature>Screen.kt      # Stateful sarmalayıcı + Stateless composable çifti
```

- **State** → Tek bir `data class <Feature>UiState`; tüm alanları varsayılan değerli, yalnızca saf UI durumunu tutar (loading, data, `errorMessage` vb.).
- **Intent** → `sealed interface`; parametreli aksiyonlar `data class`, parametresizler `data object`.
- **ViewModel** → `MutableStateFlow` içeride tutulur, dışarıya salt-okunur `StateFlow` olarak açılır; reducer mantığı `onIntent()` içindeki `when` bloğunda çalışır. UI'a `copy()` ile üretilen yeni bir immutable state döner.
- **Screen** → Stateful sarmalayıcı `collectAsStateWithLifecycle()` ile state'i toplar ve intent'leri ViewModel'e iletir; stateless composable yalnızca state alıp UI çizer (preview & test dostu).


## 🛡️ Merkezî Hata Yönetimi (util/)

Hiçbir ViewModel hata mesajını elle (hard-coded string) üretmez. Ham `Throwable`'lar tek tip bir modele indirgenir ve kullanıcı metni **tek bir yerden** — `util/ErrorMessages.kt` — çözülür. Böylece mesajlar tutarlı, tek dilde ve tek noktadan yönetilebilir olur.

Yapı iki dosyadan oluşur:

| Dosya | Sorumluluk |
|-------|-----------|
| `util/AppError.kt` | Uygulama genelinde tek tip hata modeli (`sealed class AppError`) + ham `Throwable`'ı buna çeviren `toAppError()` |
| `util/ErrorMessages.kt` | `AppError` → kullanıcıya gösterilebilir Türkçe metin çevirisi (`toUserMessage()`) + ekran bağlamı (`ErrorContext`) |

**1. Ham hata → tek tip model.** Retrofit/OkHttp katmanından yükselen her hata `toAppError()` ile normalize edilir:

```kotlin
sealed class AppError : Throwable() {
    data object Network : AppError()                     // IOException → bağlantı yok
    data class  Api(val code: Int, ...) : AppError()     // HttpException → HTTP kodu korunur
    data class  Unknown(...) : AppError()                // sınıflandırılamayan
}
```

**2. Model → kullanıcı metni (bağlama duyarlı).** Aynı HTTP kodu (ör. `400`, `403`, `404`, `409`) ekrana göre farklı anlam taşıdığından, çeviri bir `ErrorContext` alır. `resolveApiMessage()` içinde sıra önemlidir (ilk eşleşen kazanır): önce sunucu (5xx) hataları, sonra bağlama özgü net eşlemeler, en sonda koda göre genel yedekler.

```kotlin
// ViewModel içinde tek satırlık kullanım:
} catch (e: Exception) {
    _uiState.update {
        it.copy(
            isLoading = false,
            errorMessage = e.toAppError().toUserMessage(ErrorContext.PLAYER),
        )
    }
}
```

Örneğin `403` kodu bağlama göre farklı metne çözülür:
- `ErrorContext.PLAYER` → *"Bu içerik için premium üyelik gerekli"*
- `ErrorContext.LIBRARY` → *"Bu çalma listesi sana ait değil"*
- Bağlam belirtilmezse → genel yedek: *"Bu işlem için yetkin yok"*

Mesaj tablosunun kaynağı `docs/api/openapi.json` uç yanıtlarıdır; yeni bir uç eklendiğinde yalnızca `ErrorMessages.kt` güncellenir, ViewModel'lere dokunulmaz.

## 📚 Detaylı Dokümantasyon

Mimari kararlar için [`docs/`](docs/) klasörüne bakın:
- [`docs/decisions.md`](docs/decisions.md) — Teknik-mimari kararlar ve geçmişi
- [`docs/architecture/`](docs/architecture/) — MVI kontratları, ViewModel kuralları ve genel bakış
- [`docs/design/`](docs/design/) — Renk sistemi ve tasarım notları
- [`docs/api/openapi.json`](docs/api/) — Backend API sözleşmesi

## 📁 Proje Yapısı

```
app/src/main/java/com/turkcell/lyraapp/
├── data/           # Veri katmanı
│   ├── auth/           # Kimlik doğrulama
│   ├── connectivity/   # Ağ durumu (online/offline)
│   ├── download/       # İndirme yönetimi
│   ├── favorites/      # Favoriler
│   ├── feed/           # Keşfet akışı
│   ├── membership/     # Üyelik / premium
│   ├── playback/       # Oynatma
│   ├── playlist/       # Çalma listeleri
│   └── remote/         # API servisleri (Auth, Me, Streaming) + DTO'lar
├── di/             # Hilt modülleri (Network, Playback, Download, ...)
├── ui/             # Özellik ekranları (MVI)
│   ├── login/ · register/ · otp/
│   ├── home/ · feed/ · search/
│   ├── library/ · playlistdetail/ · createplaylist/
│   ├── player/ · miniplayer/
│   ├── premium/ · payment/ · profile/
│   ├── navigation/ · theme/ · icons/
├── util/           # Ortak yardımcılar (hata yönetimi vb.)
├── LyraApplication.kt
└── MainActivity.kt
```

## 🚀 Kurulum

### Gereksinimler

- **Android Studio** (Ladybug veya üzeri önerilir)
- **JDK 11**
- **Android SDK** — compileSdk 36, minSdk 24, targetSdk 35

### Adımlar

1. Depoyu klonlayın:
   ```bash
   git clone <repo-url>
   cd Turkcell-GYGY-lyra-app-pair
   ```

2. `local.properties` dosyasında Android SDK yolunuzun tanımlı olduğundan emin olun:
   ```properties
   sdk.dir=/path/to/Android/Sdk
   ```

3. Projeyi Android Studio'da açın ve Gradle senkronizasyonunun tamamlanmasını bekleyin.

4. Uygulamayı çalıştırın:
   ```bash
   ./gradlew installDebug
   ```
   veya Android Studio üzerinden **Run** ▶️.



## 📝 Geliştirme Kuralları

Bu projede çalışan herkesin (insan/AI) uyması gereken kurallar [`AGENTS.MD`](AGENTS.MD) dosyasında tanımlıdır.


## 👥 Geliştiriciler

* **Erdem Akatay** — [GitHub](https://github.com/erdemakatay)
* **Edanur Çıtak** — [GitHub](https://github.com/edanurcitak)
* **Mustafa Derinöz** — [GitHub](https://github.com/mustafaderinoz)

