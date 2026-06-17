package com.filemanager.app.domain.usecase

import com.filemanager.app.domain.repository.FileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for deleting files.
 */
class DeleteFilesUseCase @Inject constructor(
    private val fileRepository: FileRepository
) {
    operator suspend fun invoke(paths: List<String>): Flow<Result<Unit>> {
        return fileRepository.deleteFiles(paths)
    }
}
