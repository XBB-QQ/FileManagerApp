package com.filemanager.app.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.storage.StorageManager
import com.filemanager.app.domain.model.FileItem
import com.filemanager.app.domain.model.FileType
import com.filemanager.app.domain.model.StorageInfo
import com.filemanager.app.domain.repository.FileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import java.io.File as JavaFile

/**
 * Repository implementation for file system operations using SAF + legacy File API.
 */
class FileRepositoryImpl @Inject constructor(
    private val context: Context
) : FileRepository {

    private val contentResolver: ContentResolver
        get() = context.contentResolver

    override fun listFiles(path: String, showHidden: Boolean): Flow<Result<List<FileItem>>> =
        channelFlow {
            try {
                val files = withContext(Dispatchers.IO) {
                    getFileItems(path, showHidden)
                }
                send(Result.success(files))
            } catch (e: Exception) {
                send(Result.failure(e))
            }
        }

    override suspend fun copyFiles(sources: List<String>, destination: String): Flow<Result<Unit>> =
        channelFlow {
            try {
                withContext(Dispatchers.IO) {
                    sources.forEach { sourcePath ->
                        // Skip SAF/content URIs for file operations — not supported yet
                        if (sourcePath.startsWith("content://") || sourcePath.startsWith("file://")) {
                            return@forEach
                        }
                        val srcFile = JavaFile(sourcePath)
                        val destFile = JavaFile(destination, srcFile.name)
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

    override suspend fun moveFiles(sources: List<String>, destination: String): Flow<Result<Unit>> =
        channelFlow {
            try {
                withContext(Dispatchers.IO) {
                    sources.forEach { sourcePath ->
                        if (sourcePath.startsWith("content://") || sourcePath.startsWith("file://")) {
                            return@forEach
                        }
                        val srcFile = JavaFile(sourcePath)
                        val destFile = JavaFile(destination, srcFile.name)
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
                    // Skip SAF/content URIs for file operations — not supported yet
                    if (path.startsWith("content://") || path.startsWith("file://")) {
                        return@forEach
                    }
                    val file = JavaFile(path)
                    if (file.isDirectory) {
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
    private fun deleteFileWithMediaStore(file: JavaFile) {
        // Step 1: Try to delete via MediaStore if it's a media file in a managed directory
        val uri = resolveMediaStoreUri(file)
        if (uri != null) {
            try {
                contentResolver.delete(uri, null, null)
            } catch (e: Exception) {
                // MediaStore delete failed - fall through to File.delete()
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
    private fun resolveMediaStoreUri(file: JavaFile): Uri? {
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

    override suspend fun renameFile(oldPath: String, newName: String): Flow<Result<Unit>> =
        channelFlow {
            try {
                withContext(Dispatchers.IO) {
                    // Skip SAF/content URIs for file operations
                    if (oldPath.startsWith("content://") || oldPath.startsWith("file://")) {
                        throw IllegalStateException("File operation not supported for SAF URIs: $oldPath")
                    }
                    val oldFile = JavaFile(oldPath)
                    val parentDir = oldFile.parentFile ?: JavaFile("/")
                    val newFile = JavaFile(parentDir, newName)
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
                val dir = JavaFile(path)
                if (!dir.exists()) dir.mkdirs()
            }
            send(Result.success(Unit))
        } catch (e: Exception) {
            send(Result.failure(e))
        }
    }

    override fun searchFiles(query: String, scope: String?): Flow<Result<List<FileItem>>> =
        channelFlow {
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
        val file = JavaFile(path)
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

    //    /**
//     * Get file items in a directory.
//     *
//     * Strategy (always use ContentResolver — File.listFiles() broken on Android 10+):
//     * 1. For /storage/emulated/0/* paths: query MediaStore + DocumentsContract
//     * 2. For other paths: try DocumentsContract primary tree
//     * 3. Fallback: java.io.File.listFiles()
//     */
    private fun getFileItems(path: String, showHidden: Boolean): List<FileItem> {
        val dir = JavaFile(path)
        if (!dir.exists() || !dir.isDirectory) return emptyList()

        // Use ContentResolver + MediaStore.Files to enumerate files.
        // This works on Android 10+ regardless of scoped storage restrictions,
        // because READ_EXTERNAL_STORAGE grants access to MediaStore.
        return listViaMediaStore(path, showHidden)
    }

    /**
     * List files in a directory using MediaStore.Files.EXTERNAL_CONTENT_URI.
     * Queries the unified media store which contains all files (images, videos, audio, documents).
     * Filters results by the target directory path.
     */
    private fun listViaMediaStore(path: String, showHidden: Boolean): List<FileItem> {
        val items = mutableListOf<FileItem>()
        val pathLower = path.lowercase()
        val pathEndsWithSlash = path.endsWith('/')

        // Query MediaStore.Files for ALL file types
        val projection = arrayOf(
            android.provider.MediaStore.MediaColumns._ID,
            android.provider.MediaStore.MediaColumns.DISPLAY_NAME,
            android.provider.MediaStore.MediaColumns.DATA,
            android.provider.MediaStore.MediaColumns.SIZE,
            android.provider.MediaStore.MediaColumns.DATE_MODIFIED,
            android.provider.MediaStore.MediaColumns.MIME_TYPE
        )

        contentResolver.query(
            android.provider.MediaStore.Files.getContentUri("external"),
            projection,
            null, null, null
        )?.use { cursor ->
            val nameIdx = cursor.getColumnIndex(android.provider.MediaStore.MediaColumns.DISPLAY_NAME)
            val dataIdx = cursor.getColumnIndex(android.provider.MediaStore.MediaColumns.DATA)
            val sizeIdx = cursor.getColumnIndex(android.provider.MediaStore.MediaColumns.SIZE)
            val dateIdx = cursor.getColumnIndex(android.provider.MediaStore.MediaColumns.DATE_MODIFIED)
            val mimeIdx = cursor.getColumnIndex(android.provider.MediaStore.MediaColumns.MIME_TYPE)

            val parentPath = if (pathEndsWithSlash) pathLower else "$pathLower/"

            while (cursor.moveToNext()) {
                val name = if (nameIdx >= 0) cursor.getString(nameIdx) else continue
                if (name.startsWith(".") && !showHidden) continue

                val data = if (dataIdx >= 0) cursor.getString(dataIdx) else null
                val size = if (sizeIdx >= 0) cursor.getLong(sizeIdx) else 0L
                val dateMod = if (dateIdx >= 0) cursor.getLong(dateIdx) else 0L
                val mimeType = if (mimeIdx >= 0) cursor.getString(mimeIdx) else null

                // Determine if directory by MIME type or extension
                val isDir = mimeType == null || mimeType == "vnd.android.document/directory" ||
                        mimeType.startsWith("resource_dir") ||
                        name.endsWith(".dir")

                // Determine if this file belongs to the target directory
                if (data != null) {
                    val dataLower = data.lowercase()
                    val fileParent = if (dataLower.endsWith('/')) dataLower else dataLower.substringBeforeLast('/')
                    if (fileParent != parentPath) continue
                } else {
                    continue
                }

                // Build type
                val fileType = if (isDir) {
                    FileType.DIRECTORY
                } else {
                    detectFileTypeFromMimeType(mimeType ?: "", name)
                }

                items.add(FileItem(
                    path = data,
                    name = name,
                    type = fileType,
                    size = if (isDir) 0L else size,
                    lastModified = dateSecToMillis(dateMod),
                    isHidden = name.startsWith("."),
                    mimeType = if (isDir) null else mimeType
                ))
            }
        }

        return items.sortedWith(compareBy(
            { it.type != FileType.DIRECTORY },
            { it.name.lowercase() }
        ))
    }

    private fun dateSecToMillis(dateSec: Long): Long {
        return if (dateSec > 0) dateSec * 1000L else 0L
    }

    private fun detectFileTypeFromMimeType(mimeType: String, name: String): FileType {
        return when {
            mimeType.startsWith("image/") -> FileType.IMAGE
            mimeType.startsWith("video/") -> FileType.VIDEO
            mimeType.startsWith("audio/") -> FileType.AUDIO
            mimeType.contains("pdf") || mimeType.contains("document") -> FileType.DOCUMENT
            mimeType.contains("zip") || mimeType.contains("compress") -> FileType.ARCHIVE
            mimeType.startsWith("text/") -> FileType.TEXT
            mimeType.contains("archive") || mimeType.contains("x-rar") || mimeType.contains("x-7z-compressed") -> FileType.ARCHIVE
            name.endsWith(".apk") -> FileType.APK
            else -> FileType.UNKNOWN
        }
    }

    /**
     * Search files recursively by query.
     */
    private fun searchFilesInternal(query: String, scope: String?): List<FileItem> {
        val rootDir = JavaFile(scope ?: "/storage/emulated/0")
        val results = mutableListOf<FileItem>()
        _searchRecursive(rootDir, query.lowercase(), results)
        return results
    }

    private fun _searchRecursive(dir: JavaFile, query: String, results: MutableList<FileItem>) {
        if (!dir.isDirectory) return
        if (results.size >= 200) return  // Limit search results

        dir.listFiles()?.forEach { file ->
            if (file.name.lowercase().contains(query)) {
                results.add(
                    FileItem(
                        path = file.path,
                        name = file.name,
                        type = detectFileType(file.path),
                        size = if (file.isDirectory) 0L else file.length(),
                        lastModified = file.lastModified(),
                        isHidden = file.isHidden,
                        mimeType = contentResolver.getType(Uri.fromFile(file))
                    )
                )
            }
            if (file.isDirectory && results.size < 200) {
                _searchRecursive(file, query, results)
            }
        }
    }

    /**
     * Get storage information using Android StorageManager API.
     * Discovers all mounted storage volumes dynamically (internal, SD cards, OTG).
     */
    private fun getStorageInfoInternal(): List<StorageInfo> {
        val storages = mutableListOf<StorageInfo>()

        @Suppress("DEPRECATION")
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager

        // Get volumes via reflection (StorageManager.volumes is API 30+)
        val volumes = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val volumesField = StorageManager::class.java.getDeclaredField("volumes")
                volumesField.isAccessible = true
                @Suppress("UNCHECKED_CAST")
                volumesField.get(storageManager) as List<*>
            } else {
                @Suppress("DEPRECATION")
                listOf(storageManager.primaryStorageVolume)
            }
        } catch (e: Exception) {
            emptyList()
        }

        val internalPath = "/storage/emulated/0"

        for (volume in volumes) {
            // Cast volume to a java.lang.Object so we can access reflection members safely
            val vol = volume as Object

            // Get volume type (TYPE_PRIMARY = 0, TYPE_PUBLIC = 1)
            val volumeType = try {
                vol.javaClass.getDeclaredField("type").getInt(vol)
            } catch (_: Exception) {
                -1
            }

            val isPrimary = volumeType == 0  // StorageManager.TYPE_PRIMARY

            // Get mount point path via getMountPoint()
            val mountPoint = try {
                vol.javaClass.getDeclaredMethod("getMountPoint").invoke(vol) as? JavaFile
            } catch (_: Exception) {
                null
            }

            val path = mountPoint?.absolutePath
            if (path.isNullOrEmpty()) continue

            // Get volume label via getVolumeLabel()
            val label = try {
                vol.javaClass.getDeclaredMethod("getVolumeLabel", Context::class.java)
                    .invoke(vol, context) as? String ?: ""
            } catch (_: Exception) {
                ""
            }

            val humanName = if (label.isNotBlank()) {
                label
            } else if (isPrimary) {
                "内部存储"
            } else {
                "存储 (${JavaFile(path).name})"
            }

            val dir = JavaFile(path)
            if (!dir.exists() || !dir.isDirectory) {
                continue
            }

            storages.add(
                StorageInfo(
                    name = humanName,
                    path = path,
                    totalBytes = dir.totalSpace,
                    usableBytes = dir.freeSpace,
                    isRemovable = !isPrimary,
                    isMounted = true
                )
            )
        }

        // Fallback: if no volumes found, try the standard internal path
        if (storages.isEmpty()) {
            val internalDir = JavaFile(internalPath)
            if (internalDir.exists() && internalDir.isDirectory && internalDir.canRead()) {
                storages.add(
                    StorageInfo(
                        name = "内部存储",
                        path = internalPath,
                        totalBytes = internalDir.totalSpace,
                        usableBytes = internalDir.freeSpace,
                        isRemovable = false,
                        isMounted = true
                    )
                )
            }
        }

        // Fallback: also try common alternative paths for SD cards
        if (storages.size <= 1) {
            val altPaths =
                listOf("/storage/sdcard0", "/mnt/sdcard", "/storage/extSdCard", "/mnt/media_rw")
            for (altPath in altPaths) {
                val altDir = JavaFile(altPath)
                if (altDir.exists() && altDir.isDirectory && altDir.canRead()) {
                    if (!storages.any { it.path == altDir.path }) {
                        storages.add(
                            StorageInfo(
                                name = "SD卡 (${altDir.name})",
                                path = altPath,
                                totalBytes = altDir.totalSpace,
                                usableBytes = altDir.freeSpace,
                                isRemovable = true,
                                isMounted = true
                            )
                        )
                    }
                }
            }
        }

        return storages
    }

    companion object {
        private val IMAGE_EXTENSIONS =
            setOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "tiff", "heic")
        private val VIDEO_EXTENSIONS =
            setOf("mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "m4v", "3gp")
        private val AUDIO_EXTENSIONS =
            setOf("mp3", "wav", "flac", "aac", "ogg", "wma", "m4a", "opus")
        private val DOCUMENT_EXTENSIONS =
            setOf("doc", "docx", "xls", "xlsx", "ppt", "pptx", "odt", "ods", "odp", "rtf")
        private val ARCHIVE_EXTENSIONS = setOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz")
        private val TEXT_EXTENSIONS = setOf("txt", "md", "log", "cfg", "ini", "conf")
        private val CODE_EXTENSIONS = setOf(
            "java",
            "kt",
            "xml",
            "json",
            "yaml",
            "yml",
            "toml",
            "css",
            "html",
            "js",
            "ts",
            "py",
            "c",
            "cpp",
            "h",
            "rs",
            "go",
            "swift"
        )
        private val XML_EXTENSIONS = setOf("xml", "axml")
        private val FONT_EXTENSIONS = setOf("ttf", "otf", "woff", "woff2")
    }
}