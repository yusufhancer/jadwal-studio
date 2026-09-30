package com.jadwalstudio.app.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Validate size before creating a preview; do not trust provider metadata alone. */
suspend fun validateAttachmentInput(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
    val name = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
        if (it.moveToFirst()) it.getString(0) else null
    } ?: throw IllegalArgumentException("Nama file tidak dapat dibaca.")
    require(name.substringAfterLast('.', "").lowercase() in listOf("pdf", "jpg", "jpeg", "png", "docx")) {
        "Pilih file PDF, JPG, PNG, atau DOCX."
    }
    context.contentResolver.openInputStream(uri)?.use { input ->
        val buffer = ByteArray(8192)
        var total = 0
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            total += read
            require(total <= 10 * 1024 * 1024) { "Lampiran maksimal 10 MB." }
        }
        require(total > 0) { "File kosong." }
    } ?: throw IllegalArgumentException("File tidak dapat dibaca.")
    // Document providers may not offer persistent grants; upload still works
    // while the current grant is alive and failure asks the user to reselect.
    runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
    name
}
