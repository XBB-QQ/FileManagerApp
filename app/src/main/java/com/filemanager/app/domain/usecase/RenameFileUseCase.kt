package com.filemanager.app.domain.usecase

import com.filemanager.app.domain.repository.FileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for renaming a file.
 */
class RenameFileUseCase @Inject constructor(
    private val fileRepository: FileRepository
) {
    operator suspend fun invoke(oldPath: String, newName: String): Flow<Result<Unit>> {
        return fileRepository.renameFile(oldPath, newName)
    }
}
