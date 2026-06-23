# ── Gson ────────────────────────────────────────────────────────────────────
# Gson читает поля DTO рефлексией — имена полей нельзя обфусцировать,
# иначе парсинг ответа sport-tv.by молча сломается в release-сборке.
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**

-keep class com.google.gson.** { *; }

# Сохраняем DTO внешнего фида с исходными именами полей
-keep class com.svoysport.tv.data.remote.sporttv.** { *; }

# Доменные модели (на случай сериализации/рефлексии)
-keep class com.svoysport.tv.domain.model.** { *; }

# ── Media3 / ExoPlayer ────────────────────────────────────────────────────────
-dontwarn androidx.media3.**
-keep class androidx.media3.** { *; }
