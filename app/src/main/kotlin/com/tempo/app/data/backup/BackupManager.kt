package com.tempo.app.data.backup

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.tempo.app.data.local.TempoDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Backs up the Room database file to a user-chosen SAF tree (a local folder or, since Google Drive
 * registers itself as a DocumentsProvider, a Drive folder) — no Firebase/Supabase project or
 * OAuth client needed, since the user picks the destination through Android's own file picker.
 */
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: TempoDatabase,
) {
    suspend fun backupTo(treeUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            database.query("PRAGMA wal_checkpoint(FULL)", null).close()

            val treeDoc = DocumentFile.fromTreeUri(context, treeUri)
                ?: error("Can't access the selected folder")
            val fileName = "tempo_backup_${LocalDateTime.now().format(FILE_TIMESTAMP_FORMAT)}.db"

            treeDoc.findFile(fileName)?.delete()
            val newDoc = treeDoc.createFile("application/octet-stream", fileName)
                ?: error("Can't create the backup file")

            context.contentResolver.openOutputStream(newDoc.uri)?.use { out ->
                context.getDatabasePath(TempoDatabase.DATABASE_NAME).inputStream().use { it.copyTo(out) }
            } ?: error("Can't open the backup file for writing")

            fileName
        }
    }

    /**
     * Overwrites the live database with [fileUri]'s contents. The Room instance is closed first
     * (and its stale WAL/SHM sidecar files removed) so the next access reopens cleanly against the
     * restored file — callers must restart the app afterward, since every other already-injected
     * Room/DAO reference in the process still points at the now-closed connection.
     */
    suspend fun restoreFrom(fileUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            database.close()

            val dbFile = context.getDatabasePath(TempoDatabase.DATABASE_NAME)
            context.contentResolver.openInputStream(fileUri)?.use { input ->
                dbFile.outputStream().use { output -> input.copyTo(output) }
            } ?: error("Can't read the selected backup file")

            File(dbFile.path + "-wal").delete()
            File(dbFile.path + "-shm").delete()
            Unit
        }
    }

    companion object {
        private val FILE_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmm")
    }
}
