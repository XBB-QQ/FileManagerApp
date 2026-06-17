package com.filemanager.app.presentation.component

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/**
 * Context menu items for file operations.
 */
data class ContextMenuItem(val label: String) {
    companion object {
        val Open = ContextMenuItem("打开")
        val Cut = ContextMenuItem("剪切")
        val Copy = ContextMenuItem("复制")
        val Paste = ContextMenuItem("粘贴")
        val Rename = ContextMenuItem("重命名")
        val Delete = ContextMenuItem("删除")
        val Share = ContextMenuItem("分享")
        val Properties = ContextMenuItem("属性")
        val AddFavorite = ContextMenuItem("添加到收藏")
        val Compress = ContextMenuItem("压缩")
        val Extract = ContextMenuItem("解压")
        val Edit = ContextMenuItem("编辑")
    }
}

/**
 * Returns context menu items for a given file type.
 */
fun getFileContextMenuItems(
    isMultiSelect: Boolean = false,
    hasClipboard: Boolean = false,
    isDirectory: Boolean = false,
    isArchive: Boolean = false,
    isAPK: Boolean = false
): List<ContextMenuItem> {
    val items = mutableListOf<ContextMenuItem>()
    val cm = ContextMenuItem

    if (isMultiSelect) {
        items.add(ContextMenuItem("全选"))
    }

    if (isMultiSelect || isDirectory) {
        items.add(cm.Cut)
        items.add(cm.Copy)
        if (hasClipboard) {
            items.add(cm.Paste)
        }
    }

    if (!isDirectory) {
        items.add(cm.Open)
        items.add(cm.Share)
        items.add(cm.AddFavorite)
        if (isArchive) {
            items.add(cm.Extract)
            items.add(cm.Compress)
        }
    } else {
        items.add(cm.AddFavorite)
        items.add(cm.Compress)
    }

    items.add(cm.Rename)
    items.add(cm.Delete)
    items.add(cm.Properties)

    return items
}

/**
 * Shows a context menu for a file item.
 */
@Composable
fun ContextMenu(
    items: List<ContextMenuItem>,
    onItemSelected: (ContextMenuItem) -> Unit,
    visible: Boolean
) {
    DropdownMenu(
        expanded = visible,
        onDismissRequest = { /* dismissed */ }
    ) {
        items.forEach { item ->
            DropdownMenuItem(
                text = { Text(item.label) },
                onClick = {
                    onItemSelected(item)
                }
            )
        }
    }
}
