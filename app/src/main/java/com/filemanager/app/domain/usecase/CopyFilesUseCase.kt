package com.filemanager.app.domain.usecase

import com.filemanager.app.domain.repository.FileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for copying files.
 */
class CopyFilesUseCase @Inject constructor(
    private val fileRepository: FileRepository
) {
    operator suspend fun invoke(sources: List<String>, destination: String): Flow<Result<Unit>> {
        return fileRepository.copyFiles(sources, destination)
    }
}
