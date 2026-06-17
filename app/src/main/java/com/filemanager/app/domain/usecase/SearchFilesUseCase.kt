package com.filemanager.app.domain.usecase

import com.filemanager.app.domain.model.FileItem
import com.filemanager.app.domain.repository.FileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for searching files.
 */
class SearchFilesUseCase @Inject constructor(
    private val fileRepository: FileRepository
) {
    operator fun invoke(query: String, scope: String? = null): Flow<List<FileItem>> {
        return fileRepository.searchFiles(query, scope)
            .map { result ->
                result.getOrElse { emptyList() }
            }
            .catch { emit(emptyList()) }
    }
}
