export const meta = {
  name: 'fix-file-manager',
  description: '全面修复 FileManagerApp 使其成为真正的文件管理器',
  phases: [
    { title: '修复 MainViewModel', detail: '解决并发请求和文件加载问题' },
    { title: '修复 HomeScreen', detail: '移除假数据绑定真实存储信息' },
    { title: '编译验证', detail: '确保编译通过并能安装' },
  ],
}

// Phase 1: Fix MainViewModel
phase('修复 MainViewModel')
const mainViewModelResult = await agent(`
修复 MainViewModel.kt 文件。

核心问题: loadFiles() 每次调用都 launch 新协程 collect Flow，没有取消机制。
当用户快速切换目录时，会产生多个并发请求，导致文件扫描阻塞。

修复方案:
1. 添加 private val _currentLoadPath = MutableStateFlow<String?>(null)
2. 在 init 中添加一个 launch { _currentLoadPath.collect { path -> ... } } 持续收集路径变化
3. loadFiles(path) 只设置 _currentLoadPath.value = path
4. 收集器中执行 fileRepository.listFiles(path, showHidden).first() 取第一个结果即可
5. 注意: 使用 .first() 而不是 .collect() 因为 listFiles 只 emit 一次

具体修改:
- 添加字段: private val _currentLoadPath = MutableStateFlow<String?>(null)
- 在 init 块中添加一个收集器
- 修改 loadFiles 为: fun loadFiles(path: String) { _currentLoadPath.value = path }
- 修改 goUp 和 navigateToPath 也直接用 _currentLoadPath.value = path

请读取文件 D:/claudeCode/FileManagerApp/app/src/main/java/com/filemanager/app/presentation/main/MainViewModel.kt，修改后写回。只修改 MainViewModel.kt 这一个文件。
`, {
  label: '修复 MainViewModel',
  phase: '修复 MainViewModel',
  agentType: 'general-purpose',
})

// Phase 2: Fix HomeScreen
phase('修复 HomeScreen')
const homeScreenResult = await agent(`
修复 HomeScreen.kt 文件。

核心问题: 当 storages 为空时显示硬编码假数据（"已用 60 GB / 总计 128 GB"）。

修复方案:
1. 移除 else 分支中的假数据
2. 当 storages 为空时显示 "正在加载存储信息..." 或使用 ProgressBar
3. 确保 StorageCard 正确显示真实数据

请读取文件 D:/claudeCode/FileManagerApp/app/src/main/java/com/filemanager/app/presentation/home/HomeScreen.kt，修改后写回。只修改 HomeScreen.kt 这一个文件。
`, {
  label: '修复 HomeScreen',
  phase: '修复 HomeScreen',
  agentType: 'general-purpose',
})

// Phase 3: Build and verify
phase('编译验证')
const buildResult = await agent(`
cd D:/claudeCode/FileManagerApp
./gradlew clean assembleDebug 2>&1 | tail -20

如果编译成功，输出 BUILD SUCCESSFUL
如果编译失败，输出错误信息和失败原因
`, {
  label: '编译验证',
  phase: '编译验证',
  agentType: 'general-purpose',
})

log('All phases complete')
