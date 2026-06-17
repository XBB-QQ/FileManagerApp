package com.filemanager.app.domain.usecase

import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import java.io.File
import javax.inject.Inject

/**
 * Use case for creating a ZIP archive.
 */
class CreateZipUseCase @Inject constructor() {
    operator fun invoke(sourcePaths: List<String>, archivePath: String): Result<Unit> {
        return try {
            val zipFile = ZipFile(archivePath)
            val params = ZipParameters().apply {
                isEncryptFiles = false
            }

            val fileList = sourcePaths.map { File(it) }.filter { it.exists() }.toMutableList()
            zipFile.addFiles(fileList, params)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
