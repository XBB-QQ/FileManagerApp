package com.filemanager.app.domain.usecase

import net.lingala.zip4j.ZipFile
import java.io.File
import javax.inject.Inject

/**
 * Use case for extracting a ZIP archive.
 */
class ExtractZipUseCase @Inject constructor() {
    operator fun invoke(archivePath: String, destinationDir: String): Result<Unit> {
        return try {
            val zipFile = ZipFile(archivePath)
            zipFile.extractAll(destinationDir)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
