package com.filemanager.app.domain.usecase

import com.filemanager.app.domain.repository.FileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for browsing files in a directory.
 */
class BrowseFilesUseCase @Inject constructor(
    private val fileRepository: FileRepository
) {
    operator fun invoke(path: String, showHidden: Boolean = false): Flow<List<com.filemanager.app.domain.model.FileItem>> {
        return fileRepository.listFiles(path, showHidden)
            .map { result ->
                result.getOrElse { emptyList() }
            }
            .catch { emit(emptyList()) }
    }
}
