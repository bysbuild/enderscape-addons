# Enderscape Addons

Minecraft **1.21.1** / NeoForge **21.1.252** / Java **21**。

面向 Minecraft 1.21.1 NeoForge 的 Enderscape 扩展与专用纹饰桥接源码合集。
本仓库整理扩展玩法、指定模组联动和 Trim Bridge 的实现，供阅读、维护与二次开发。

源码从两个已编译 JAR 导出。
Java 代码由 **Vineflower 1.11.2** 反编译，资源直接从 JAR 提取。
这不是原作者的原始源码仓库；原始注释、构建脚本及部分局部变量名无法恢复。

## 模块

| 目录 | 内容 |
| --- | --- |
| `enderscape-expansion` | 扩展内容，内部版本 `1.0.11-selected-compat` |
| `enderscape-trim-bridge` | 专用纹饰桥接，发布版本 `1.0.0` |

两个模块均采用 `src/main/java` 和 `src/main/resources` 目录结构。
资源包括纹理、音效、模型、语言、配方、数据包、Mixin 配置及 NeoForge 模组声明。

Expansion 保留 Alex 系列、JEI、Colorful Hearts、Cataclysm、Legendary Monsters、
Kaleidoscope End 联动；BetterEnd、The Beyond、Trek 兼容已在输入 JAR 中移除。

Trim Bridge 使用兼容 ID `trimmed`、兼容版本 `3.0.0` 满足 Enderscape 的依赖声明，
提供专用纹饰功能；它并非原版 Trimmed 的通用 API 实现，不应与原版 Trimmed 同装。

## 功能概览

### Enderscape Expansion

- 回移植 Enderscape 新版内容：末地地形与结构、End Haven、物品与配方、虚空机制、存储及磁岩系统。
- 保留末地城单船规则，以及实体动画、视觉和音效相关内容。
- 保留 Alex 系列的末地城 Mimicube、Spectre 牵引修复和奶酪矿石联动。
- 保留 JEI 配方展示、Colorful Hearts 虚空生命值显示，以及 Cataclysm、Legendary Monsters、Kaleidoscope End 的结构或生成适配。

### Enderscape Trim Bridge

- 为 Enderscape 1.0.9 提供专用纹饰资源和物品模型适配。
- 支持原版盔甲、漂移护腿（Drift Leggings）及 Enderscape 纹饰材料。
- 通过兼容模组 ID 满足 Enderscape 的 Trimmed 依赖，不提供完整的第三方 Trimmed API。

## 版本与依赖

| 项目 | 要求 |
| --- | --- |
| Minecraft | 1.21.1 |
| Java | 21 |
| NeoForge | 至少 21.1.249；构建配置采用 21.1.252 |
| Enderscape | 两个模块共同使用时为 1.0.9 |
| Lithostitched | Expansion 要求至少 1.8.0 |

JEI、Colorful Hearts 等兼容模组按需安装；编译依赖见 `libs/README.md`。
仓库不包含游戏本体、第三方依赖 JAR、个人配置或存档。

## 构建配置与验证范围

Gradle 配置是此次重新整理的开发起点，使用 ModDevGradle 2.0.147；并非从 JAR 还原的原始构建配置。
配置依据：[ModDevGradle 官方文档](https://github.com/neoforged/ModDevGradle)。

1. 安装 JDK 21。
2. 按 `libs/README.md` 准备 Expansion 的本地编译依赖。
3. 在仓库根目录执行：

```powershell
.\gradlew.bat :enderscape-expansion:build :enderscape-trim-bridge:build
```

macOS / Linux 可执行 `sh gradlew :enderscape-expansion:build :enderscape-trim-bridge:build`。
首次构建需要联网下载 Gradle、NeoForge 及其开发依赖。

本次验证源码覆盖、Java 语法、资源完整性和输入文件校验值；**未完成完整编译及游戏内测试**。
反编译代码仍可能需要修复泛型推断、访问权限或 API 类型等问题，不能将本包视为已验证可重建的发行源码。
详细导出统计见 `EXPORT-REPORT.md`。

## 来源与声明

原 JAR 的模组声明、署名和许可证均原样保留。见 `NOTICE.md`。
特别是 Expansion 附带的 Enderscape 许可明确区分 MIT 代码和保留所有权利的美术资源，
本源码导出没有将所有资源改为 MIT。
