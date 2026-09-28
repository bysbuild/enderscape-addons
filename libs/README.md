# 本地编译依赖

构建 Expansion 时，将以下模组 JAR 复制到此目录并重命名：

| 文件名 | 依赖 |
| --- | --- |
| `enderscape.jar` | Enderscape NeoForge 1.0.9，Minecraft 1.21.1 |
| `jei.jar` | JEI NeoForge，Minecraft 1.21.1（当前实例使用 19.57.0.449） |
| `colorfulhearts.jar` | Colorful Hearts NeoForge，Minecraft 1.21.1，包含 `terrails.colorfulhearts.api.neoforge.event` API |

这些依赖不包含在源码导出包中，`.gitignore` 已排除它们。
Alex 兼容通过字符串或注册表检查加载，不需要 Alex JAR 参与 Java 编译。
Trim Bridge 的 Java 源码仅使用 Minecraft、NeoForge 和 Mixin API，不需要上述三个 JAR 参与编译。

这里列的是编译依赖。运行时仍以各模块 `META-INF/neoforge.mods.toml` 的依赖声明为准。
