package com.filemanager.app.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.filemanager.app.data.mapper.toFileItem
import com.filemanager.app.domain.model.FileItem
import com.filemanager.app.domain.model.FileType
import com.filemanager.app.domain.model.StorageInfo
import com.filemanager.app.domain.repository.FileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * Repository implementation for file system operations using SAF + legacy File API.
 */
class FileRepositoryImpl @Inject constructor(
    private val context: Context
) : FileRepository {

    private val contentResolver: ContentResolver
        get() = context.contentResolver

    override fun listFiles(path: String, showHidden: Boolean): Flow<Result<List<FileItem>>> = channelFlow {
        try {
            val files = withContext(Dispatchers.IO) {
                getFileItems(path, showHidden)
            }
            send(Result.success(files))
        } catch (e: Exception) {
            send(Result.failure(e))
        }
    }

    override suspend fun copyFiles(sources: List<String>, destination: String): Flow<Result<Unit>> = channelFlow {
        try {
            withContext(Dispatchers.IO) {
                sources.forEach { sourcePath ->
                    val srcFile = File(sourcePath)
                    val destFile = File(destination, srcFile.name)
                    if (srcFile.isDirectory) {
                        srcFile.copyRecursively(destFile)
                    } else {
                        srcFile.copyTo(destFile, overwrite = true)
                    }
                }
            }
            send(Result.success(Unit))
        } catch (e: Exception) {
            send(Result.failure(e))
        }
    }

    override suspend fun moveFiles(sources: List<String>, destination: String): Flow<Result<Unit>> = channelFlow {
        try {
            withContext(Dispatchers.IO) {
                sources.forEach { sourcePath ->
                    val srcFile = File(sourcePath)
                    val destFile = File(destination, srcFile.name)
                    srcFile.renameTo(destFile)
                }
            }
            send(Result.success(Unit))
        } catch (e: Exception) {
            send(Result.failure(e))
        }
    }

    override suspend fun deleteFiles(paths: List<String>): Flow<Result<Unit>> = channelFlow {
        try {
            withContext(Dispatchers.IO) {
                paths.forEach { path ->
                    val file = File(path)
                    if (file.isDirectory) {
                        // For directories, delete children first then MediaStore entries
                        file.listFiles()?.forEach { child ->
                            deleteFileWithMediaStore(child)
                        }
                        file.delete()
                    } else {
                        deleteFileWithMediaStore(file)
                    }
                }
            }
            send(Result.success(Unit))
        } catch (e: Exception) {
            send(Result.failure(e))
        }
    }

    /**
     * Delete a single file, also removing its MediaStore database record if it's a media file.
     * This prevents "ghost files" from appearing in gallery/files app after deletion.
     *
     * Strategy:
     * - Android 10+ (API 29+): Use MediaStore API for media files (images/video/audio)
     * - Non-media files: Just use File.delete()
     * - Directories: handled by caller (recursive)
     */
    private fun deleteFileWithMediaStore(file: File) {
        // Step 1: Try to delete via MediaStore if it's a media file in a managed directory
        val uri = resolveMediaStoreUri(file)
        if (uri != null) {
            try {
                contentResolver.delete(uri, null, null)
            } catch (e: Exception) {
                // MediaStore delete failed — fall through to File.delete()
            }
        }

        // Step 2: Always delete the actual file from disk
        if (file.isDirectory) {
            file.deleteRecursively()
        } else {
            file.delete()
        }
    }

    /**
     * Resolve a file path to its corresponding MediaStore URI.
     * Returns null if the file is not a media file or not in a MediaStore-managed location.
     */
    private fun resolveMediaStoreUri(file: File): android.net.Uri? {
        val path = file.absolutePath.lowercase()

        // Determine which MediaStore collection to query
        val collection = when {
            path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".png") ||
            path.endsWith(".gif") || path.endsWith(".bmp") || path.endsWith(".webp") ||
            path.endsWith(".heic") || path.endsWith(".heif") -> {
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
            path.endsWith(".mp4") || path.endsWith(".mkv") || path.endsWith(".avi") ||
            path.endsWith(".mov") || path.endsWith(".wmv") || path.endsWith(".flv") ||
            path.endsWith(".webm") || path.endsWith(".m4v") -> {
                android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }
            path.endsWith(".mp3") || path.endsWith(".wav") || path.endsWith(".flac") ||
            path.endsWith(".aac") || path.endsWith(".ogg") || path.endsWith(".wma") ||
            path.endsWith(".m4a") -> {
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
            else -> return null
        }

        // Query MediaStore for the file by path
        val selection = "${android.provider.MediaStore.MediaColumns.DATA} = ?"
        val selectionArgs = arrayOf(file.absolutePath)

        contentResolver.query(
            collection,
            arrayOf(android.provider.MediaStore.MediaColumns._ID),
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndex(android.provider.MediaStore.MediaColumns._ID)
            if (idIndex >= 0 && cursor.moveToFirst()) {
                val id = cursor.getLong(idIndex)
                return when {
                    collection == android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI ->
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon()
                            .appendPath(id.toString()).build()
                    collection == android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI ->
                        android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI.buildUpon()
                            .appendPath(id.toString()).build()
                    collection == android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI ->
                        android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.buildUpon()
                            .appendPath(id.toString()).build()
                    else -> null
                }
            }
        }

        return null
    }

    override suspend fun renameFile(oldPath: String, newName: String): Flow<Result<Unit>> = channelFlow {
        try {
            withContext(Dispatchers.IO) {
                val oldFile = File(oldPath)
                val parentDir = oldFile.parentFile ?: File("/")
                val newFile = File(parentDir, newName)
                oldFile.renameTo(newFile)
            }
            send(Result.success(Unit))
        } catch (e: Exception) {
            send(Result.failure(e))
        }
    }

    override suspend fun createDirectory(path: String): Flow<Result<Unit>> = channelFlow {
        try {
            withContext(Dispatchers.IO) {
                val dir = File(path)
                if (!dir.exists()) dir.mkdirs()
            }
            send(Result.success(Unit))
        } catch (e: Exception) {
            send(Result.failure(e))
        }
    }

    override fun searchFiles(query: String, scope: String?): Flow<Result<List<FileItem>>> = channelFlow {
        try {
            val files = withContext(Dispatchers.IO) {
                searchFilesInternal(query, scope)
            }
            send(Result.success(files))
        } catch (e: Exception) {
            send(Result.failure(e))
        }
    }

    override fun getStorageInfo(): Flow<Result<List<StorageInfo>>> = channelFlow {
        try {
            val storages = withContext(Dispatchers.IO) {
                getStorageInfoInternal()
            }
            send(Result.success(storages))
        } catch (e: Exception) {
            send(Result.failure(e))
        }
    }

    override fun detectFileType(path: String): FileType {
        val file = File(path)
        if (file.isDirectory) return FileType.DIRECTORY
        if (file.isHidden) return FileType.HIDDEN

        val ext = file.extension.lowercase()
        val mimeType = contentResolver.getType(Uri.fromFile(file)) ?: ""

        return when {
            ext in IMAGE_EXTENSIONS || mimeType.startsWith("image/") -> FileType.IMAGE
            ext in VIDEO_EXTENSIONS || mimeType.startsWith("video/") -> FileType.VIDEO
            ext in AUDIO_EXTENSIONS || mimeType.startsWith("audio/") -> FileType.AUDIO
            ext in DOCUMENT_EXTENSIONS || mimeType.contains("pdf") || mimeType.contains("document") -> FileType.DOCUMENT
            ext in ARCHIVE_EXTENSIONS || mimeType.contains("zip") || mimeType.contains("compress") -> FileType.ARCHIVE
            ext == "apk" -> FileType.APK
            ext in TEXT_EXTENSIONS || mimeType.startsWith("text/") -> FileType.TEXT
            ext in CODE_EXTENSIONS || mimeType.startsWith("text/x-") -> FileType.CODE
            ext in XML_EXTENSIONS -> FileType.XML
            ext in FONT_EXTENSIONS -> FileType.FONT
            else -> FileType.UNKNOWN
        }
    }

    /**
     * Get file items in a directory.
     */
    private fun getFileItems(path: String, showHidden: Boolean): List<FileItem> {
        val dir = File(path)
        if (!dir.exists() || !dir.isDirectory || !dir.canRead()) {
            // Try parent directory as fallback
            val parent = dir.parentFile
            if (parent != null && parent.exists() && parent.isDirectory && parent.canRead()) {
                return emptyList()
            }
            return emptyList()
        }

        val files = dir.listFiles { file ->
            val hidden = file.isHidden
            showHidden || !hidden
        } ?: emptyArray()

        return files.mapNotNull { file ->
            try {
                val type = detectFileType(file.path)
                FileItem(
                    path = file.path,
                    name = file.name,
                    type = type,
                    size = if (file.isDirectory) 0L else file.length(),
                    lastModified = file.lastModified(),
                    isHidden = file.isHidden,
                    mimeType = contentResolver.getType(Uri.fromFile(file)),
                    childCount = if (file.isDirectory) file.listFiles()?.size ?: 0 else null
                )
            } catch (_: Exception) {
                null
            }
        }.sortedWith(compareBy(
            { it.type != FileType.DIRECTORY },  // directories first
            { it.name.lowercase() }
        ))
    }

    /**
     * Search files recursively by query.
     */
    private fun searchFilesInternal(query: String, scope: String?): List<FileItem> {
        val rootDir = File(scope ?: "/storage/emulated/0")
        val results = mutableListOf<FileItem>()
        _searchRecursive(rootDir, query.lowercase(), results)
        return results
    }

    private fun _searchRecursive(dir: File, query: String, results: MutableList<FileItem>) {
        if (!dir.isDirectory) return
        if (results.size >= 200) return  // Limit search results

        dir.listFiles()?.forEach { file ->
            if (file.name.lowercase().contains(query)) {
                results.add(FileItem(
                    path = file.path,
                    name = file.name,
                    type = detectFileType(file.path),
                    size = if (file.isDirectory) 0L else file.length(),
                    lastModified = file.lastModified(),
                    isHidden = file.isHidden,
                    mimeType = contentResolver.getType(Uri.fromFile(file))
                ))
            }
            if (file.isDirectory && results.size < 200) {
                _searchRecursive(file, query, results)
            }
        }
    }

    /**
     * Get storage information — scans all mounted storage volumes.
     */
    private fun getStorageInfoInternal(): List<StorageInfo> {
        val storages = mutableListOf<StorageInfo>()

        // Standard internal storage
        val internalDir = File("/storage/emulated/0")
        if (internalDir.exists() && internalDir.isDirectory && internalDir.canRead()) {
            storages.add(StorageInfo(
                name = "内部存储",
                path = "/storage/emulated/0",
                totalBytes = internalDir.totalSpace,
                usableBytes = internalDir.freeSpace,
                isRemovable = false,
                isMounted = true
            ))
        }

        // Secondary storage (SD cards, OTG, etc.)
        if (internalDir.exists() && internalDir.isDirectory) {
            val parent = internalDir.parentFile
            if (parent != null && parent.isDirectory) {
                parent.listFiles { f -> f.isDirectory && f.canRead() }
                    ?.filter { it.path != internalDir.path }
                    ?.forEach { sdCard ->
                        storages.add(StorageInfo(
                            name = "SD卡 (${sdCard.name})",
                            path = sdCard.path,
                            totalBytes = sdCard.totalSpace,
                            usableBytes = sdCard.freeSpace,
                            isRemovable = true,
                            isMounted = true
                        ))
                    }
            }
        }

        // Also try common alternative paths
        val altPaths = listOf("/storage/sdcard0", "/mnt/sdcard", "/storage/extSdCard", "/mnt/media_rw")
        altPaths.forEach { altPath ->
            val altDir = File(altPath)
            if (altDir.exists() && altDir.isDirectory && altDir.canRead()) {
                // Check if not already added
                if (!storages.any { it.path == altDir.path }) {
                    storages.add(StorageInfo(
                        name = "存储 (${altDir.name})",
                        path = altDir.path,
                        totalBytes = altDir.totalSpace,
                        usableBytes = altDir.freeSpace,
                        isRemovable = false,
                        isMounted = true
                    ))
                }
            }
        }

        // Fallback: root filesystem
        if (storages.isEmpty()) {
            val root = File("/")
            if (root.exists() && root.isDirectory && root.canRead()) {
                storages.add(StorageInfo(
                    name = "设备存储",
                    path = "/",
                    totalBytes = root.totalSpace,
                    usableBytes = root.freeSpace,
                    isRemovable = false,
                    isMounted = true
                ))
            }
        }

        return storages
    }

    companion object {
        private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "tiff", "heic")
        private val VIDEO_EXTENSIONS = setOf("mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "m4v", "3gp")
        private val AUDIO_EXTENSIONS = setOf("mp3", "wav", "flac", "aac", "ogg", "wma", "m4a", "opus")
        private val DOCUMENT_EXTENSIONS = setOf("doc", "docx", "xls", "xlsx", "ppt", "pptx", "odt", "ods", "odp", "rtf")
        private val ARCHIVE_EXTENSIONS = setOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz")
        private val TEXT_EXTENSIONS = setOf("txt", "md", "log", "cfg", "ini", "conf")
        private val CODE_EXTENSIONS = setOf("java", "kt", "xml", "json", "yaml", "yml", "toml", "css", "html", "js", "ts", "py", "c", "cpp", "h", "rs", "go", "swift")
        private val XML_EXTENSIONS = setOf("xml", "axml")
        private val FONT_EXTENSIONS = setOf("ttf", "otf", "woff", "woff2")
    }
}
