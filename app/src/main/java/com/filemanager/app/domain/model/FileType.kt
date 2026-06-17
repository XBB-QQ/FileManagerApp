package com.filemanager.app.domain.model

/**
 * Enum representing file/folder types for the file manager.
 */
enum class FileType {
    DIRECTORY,
    IMAGE,
    VIDEO,
    AUDIO,
    DOCUMENT,
    ARCHIVE,
    APK,
    TEXT,
    XML,
    CODE,
    FONT,
    HIDDEN,       // dotfiles
    UNKNOWN,
    APACHE_DOC,
}

/**
 * Returns the MIME category of this file type.
 */
fun FileType.getMimeTypeCategory(): String? = when (this) {
    FileType.IMAGE -> "image"
    FileType.VIDEO -> "video"
    FileType.AUDIO -> "audio"
    FileType.DOCUMENT, FileType.TEXT, FileType.XML, FileType.CODE, FileType.APACHE_DOC -> "application"
    FileType.ARCHIVE -> "application"
    FileType.APK -> "application/vnd.android.package-archive"
    FileType.DIRECTORY -> null
    FileType.FONT -> "font"
    FileType.HIDDEN, FileType.UNKNOWN -> null
}
