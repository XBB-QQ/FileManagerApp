package com.filemanager.app.domain.repository

import com.filemanager.app.domain.model.FileItem
import com.filemanager.app.domain.model.FileOperation
import com.filemanager.app.domain.model.OperationResult
import com.filemanager.app.domain.model.StorageInfo
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for file system operations.
 */
interface FileRepository {
    /** List files in a directory */
    fun listFiles(path: String, showHidden: Boolean = false): Flow<Result<List<FileItem>>>

    /** Copy files to destination */
    suspend fun copyFiles(sources: List<String>, destination: String): Flow<Result<Unit>>

    /** Move files to destination */
    suspend fun moveFiles(sources: List<String>, destination: String): Flow<Result<Unit>>

    /** Delete files/directories */
    suspend fun deleteFiles(paths: List<String>): Flow<Result<Unit>>

    /** Rename a file/directory */
    suspend fun renameFile(oldPath: String, newName: String): Flow<Result<Unit>>

    /** Create a new directory */
    suspend fun createDirectory(path: String): Flow<Result<Unit>>

    /** Search files by query string */
    fun searchFiles(query: String, scope: String? = null): Flow<Result<List<FileItem>>>

    /** Get storage information */
    fun getStorageInfo(): Flow<Result<List<StorageInfo>>>

    /** Get file type detection */
    fun detectFileType(path: String): com.filemanager.app.domain.model.FileType
}
