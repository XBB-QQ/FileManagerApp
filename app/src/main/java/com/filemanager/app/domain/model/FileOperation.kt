package com.filemanager.app.domain.model

/**
 * Represents a batch file operation (cut, copy, delete, move, rename, compress).
 */
sealed class FileOperation {
    data class Cut(val sources: List<String>, val destination: String) : FileOperation()
    data class Copy(val sources: List<String>, val destination: String) : FileOperation()
    data class Delete(val paths: List<String>) : FileOperation()
    data class Move(val sources: List<String>, val destination: String) : FileOperation()
    data class Rename(val oldPath: String, val newPath: String) : FileOperation()
    data class CreateDirectory(val path: String) : FileOperation()
    data class Compress(val sources: List<String>, val archivePath: String) : FileOperation()
    data class Extract(val archivePath: String, val destination: String) : FileOperation()
}

/**
 * Result of a file operation.
 */
sealed class OperationResult {
    data class Success(val operation: FileOperation) : OperationResult()
    data class Failure(val operation: FileOperation, val error: String) : OperationResult()
}
