package com.jadwalstudio.app.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jadwalstudio.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

fun JSONObject.items(key: String): List<JSONObject> = optJSONArray(key)?.let { array ->
    (0 until array.length()).map { array.getJSONObject(it) }
} ?: emptyList()

fun JSONObject.strings(key: String): List<String> = optJSONArray(key)?.let { array ->
    (0 until array.length()).map { array.getString(it) }
} ?: emptyList()

fun json(vararg values: Pair<String, Any?>) = JSONObject().apply {
    values.forEach { (key, value) -> put(key, when (value) {
        null -> JSONObject.NULL
        is List<*> -> JSONArray(value)
        else -> value
    }) }
}

class ApiException(message: String, val code: Int = 0) : Exception(message)

/** Server is the source of truth. Encrypted cache is read-only when disconnected. */
class StudioRepository(private val context: Context) {
    private val sessionLock = Mutex()
    private val refreshLock = Mutex()
    private val preferences = context.getSharedPreferences("studio_session", Context.MODE_PRIVATE)
    private val cache = AtomicFile(File(context.noBackupFilesDir, "studio-cache.enc"))
    private val vault = Vault()
    var baseUrl: String = preferences.getString("server", BuildConfig.API_BASE_URL)!!.trimEnd('/')
        private set
    private var token: String = runCatching {
        preferences.getString("session", null)?.let { vault.decrypt(it) } ?: ""
    }.getOrDefault("")
    var signedIn by mutableStateOf(token.isNotBlank())
        private set
    var state by mutableStateOf(if (signedIn) runCatching {
        JSONObject(vault.decrypt(cache.readFully().toString(Charsets.UTF_8)))
    }.getOrDefault(JSONObject()) else JSONObject())
        private set
    var offline by mutableStateOf(false)
        private set

    fun setServer(value: String) {
        val parsed = runCatching { URL(value.trim()) }.getOrNull()
        require(parsed != null && parsed.host.isNotBlank() && parsed.userInfo == null && parsed.query == null && parsed.ref == null &&
            (parsed.protocol == "https" || (BuildConfig.DEBUG && parsed.protocol == "http"))) {
            "Masukkan alamat HTTPS yang valid (HTTP hanya untuk build debug lokal)."
        }
        require(!signedIn) { "Keluar dari akun sebelum mengganti server." }
        baseUrl = value.trim().trimEnd('/')
        preferences.edit().putString("server", baseUrl).apply()
        cache.delete()
    }

    suspend fun register(name: String, email: String, password: String, confirmation: String) {
        request("POST", "/auth/register", json("name" to name, "email" to email, "password" to password, "confirmPassword" to confirmation))
    }

    suspend fun updatePhoto(uri: android.net.Uri) = withContext(Dispatchers.IO) {
        val options = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { android.graphics.BitmapFactory.decodeStream(it, null, options) }
        require(options.outWidth > 0 && options.outHeight > 0) { "Foto tidak dapat dibaca. Pilih JPG atau PNG." }
        options.inJustDecodeBounds = false
        options.inSampleSize = com.jadwalstudio.app.utils.photoSampleSize(options.outWidth, options.outHeight)
        val bitmap = context.contentResolver.openInputStream(uri)?.use { android.graphics.BitmapFactory.decodeStream(it, null, options) }
            ?: throw IllegalArgumentException("Foto tidak dapat dibaca.")
        val bytes = java.io.ByteArrayOutputStream().use { output ->
            try {
                check(bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, output)) { "Foto gagal diproses. Pilih foto lain." }
                output.toByteArray()
            }
            finally { bitmap.recycle() }
        }
        require(bytes.size <= 512000) { "Foto terlalu besar. Pilih foto lain." }
        request("PUT", "/profile/photo", json("photo" to Base64.encodeToString(bytes, Base64.NO_WRAP)))
    }

    suspend fun login(email: String, password: String, remember: Boolean) {
        val result = request("POST", "/auth/login", json("email" to email, "password" to password))
        sessionLock.withLock {
            cache.delete()
            token = result.getString("token")
            state = json("user" to result.getJSONObject("user"))
            signedIn = true
            preferences.edit().apply {
                if (remember) putString("session", vault.encrypt(token)) else remove("session")
            }.apply()
        }
    }

    suspend fun logout() {
        // Server revocation must succeed before reporting a successful logout.
        request("POST", "/auth/logout")
        clearSession()
    }

    suspend fun clearSession() = sessionLock.withLock {
        token = ""
        signedIn = false
        state = JSONObject()
        preferences.edit().remove("session").apply()
        cache.delete()
        withContext(Dispatchers.IO) { File(context.cacheDir, "attachments").deleteRecursively() }
        Unit
    }

    suspend fun refresh() = refreshLock.withLock refresh@{
        val requestedToken = token
        try {
            val result = request("GET", "/state")
            sessionLock.withLock {
                if (token != requestedToken || !signedIn) return@refresh
                withContext(Dispatchers.IO) {
                    val bytes = vault.encrypt(result.toString()).toByteArray()
                    val output = cache.startWrite()
                    try { output.write(bytes); cache.finishWrite(output) }
                    catch (error: Exception) { cache.failWrite(output); throw error }
                }
                state = result
                offline = false
            }
        } catch (error: Exception) {
            if (error is kotlinx.coroutines.CancellationException) throw error
            if (token != requestedToken) return@refresh
            offline = true
            if (error is ApiException && error.code == 401) clearSession()
            throw error
        }
    }

    suspend fun request(method: String, path: String, body: JSONObject? = null): JSONObject = withContext(Dispatchers.IO) {
        val connection = URL(baseUrl + path).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.connectTimeout = 10000
            connection.readTimeout = 30000
            connection.instanceFollowRedirects = false
            connection.setRequestProperty("Accept", "application/json")
            if (token.isNotEmpty()) connection.setRequestProperty("Authorization", "Bearer $token")
            if (body != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                val bytes = body.toString().toByteArray()
                connection.setFixedLengthStreamingMode(bytes.size)
                connection.outputStream.use { it.write(bytes) }
            }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val result = stream?.bufferedReader()?.use { JSONObject(it.readText()) } ?: JSONObject()
            if (status !in 200..299) throw ApiException(result.optString("error", "Permintaan gagal ($status)."), status)
            result
        } catch (error: ApiException) { throw error }
        catch (error: Exception) { throw ApiException("Tidak dapat menghubungi server. Periksa koneksi dan alamat server, lalu coba lagi.") }
        finally { connection.disconnect() }
    }

    suspend fun attachment(uri: String, suppliedName: String?): JSONObject = withContext(Dispatchers.IO) {
        val parsed = android.net.Uri.parse(uri)
        var name = suppliedName ?: ""
        context.contentResolver.query(parsed, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
            if (it.moveToFirst()) name = it.getString(0)
        }
        require(name.substringAfterLast('.', "").lowercase() in listOf("pdf", "jpg", "jpeg", "png", "docx")) {
            "Lampiran harus PDF, JPG, PNG, atau DOCX."
        }
        val bytes = context.contentResolver.openInputStream(parsed)?.use { input ->
            val output = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            var count = input.read(buffer)
            while (count != -1) {
                require(output.size() + count <= 10 * 1024 * 1024) { "Lampiran maksimal 10 MB." }
                output.write(buffer, 0, count)
                count = input.read(buffer)
            }
            output.toByteArray()
        } ?: throw IllegalArgumentException("Lampiran tidak dapat dibaca. Pilih ulang file.")
        require(bytes.isNotEmpty()) { "Lampiran kosong." }
        json("name" to name, "content" to Base64.encodeToString(bytes, Base64.NO_WRAP))
    }

    suspend fun downloadAttachment(id: String): Pair<File, String> {
        val result = request("GET", "/attachments/$id")
        return withContext(Dispatchers.IO) {
            val directory = File(context.cacheDir, "attachments").apply { mkdirs() }
            val extension = result.getString("name").substringAfterLast('.').lowercase()
            require(extension in listOf("pdf", "jpg", "jpeg", "png", "docx"))
            val target = File(directory, "$id.$extension")
            target.writeBytes(Base64.decode(result.getString("content"), Base64.DEFAULT))
            target to result.getString("mime")
        }
    }
}

private class Vault {
    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey("jadwalstudio.session.v1", null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder("jadwalstudio.session.v1", KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
        }.generateKey()
    }
    fun encrypt(value: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        return Base64.encodeToString(cipher.iv + cipher.doFinal(value.toByteArray()), Base64.NO_WRAP)
    }
    fun decrypt(value: String): String {
        val bytes = Base64.decode(value, Base64.DEFAULT)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
        return cipher.doFinal(bytes.copyOfRange(12, bytes.size)).toString(Charsets.UTF_8)
    }
}
