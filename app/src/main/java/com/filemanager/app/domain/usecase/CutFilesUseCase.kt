package com.filemanager.app.domain.usecase

import com.filemanager.app.domain.repository.FileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for cutting (moving) files.
 */
class CutFilesUseCase @Inject constructor(
    private val fileRepository: FileRepository
) {
    operator suspend fun invoke(sources: List<String>, destination: String): Flow<Result<Unit>> {
        return fileRepository.moveFiles(sources, destination)
    }
}
