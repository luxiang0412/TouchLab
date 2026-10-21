# TouchLab 测试报告

- 测试日期：2026-10-21（Phase 0–7）+ 2026-10-21 真机补充测试
- 代码 commit：见 `git log --oneline`（报告最后一份提交）
- OS / JDK / Gradle / AGP / Compose BOM：
  - Windows 11 10.0.26200 / Zulu JDK 17.0.3 / Gradle 9.4.1 / AGP 9.2.1 /
    Compose BOM 2026.09.00（以上为实测可用组合）
- minSdk / targetSdk / compileSdk：26 / 36 / 37
- 测试设备：
  - `touchlab_api34` 模拟器：Google APIs x86_64，Android 14 (API 34)，
    1080×2400 @ 420dpi，软件渲染（AEHD 加速）。
  - **真机（无线调试连接）**：Xiaomi 22021211RC（POCO F5 / munch），
    Android 14 (API 34)，1080×2400 @ 440dpi，120Hz 屏幕，12GB RAM。
- 真机 UI 自动化说明（MIUI）：
  - 每次重装 APK（UID 变化）后，需在「应用管理 → TouchLab → 权限管理 →
    后台弹出界面」重新设为允许，否则 MIUI 会拦截测试进程拉起 App（日志出现
    `MIUILOG- Permission Denied Activity`）。
  - 玩具用例使用 Compose 手动帧时钟（`autoAdvance=false`），真机上需把开发者
    选项中的三个动画缩放临时设为 0，否则 Espresso 等待合屏会超时。
  - 满足上述条件后，`am instrument` 直跑 10/10 通过，`connectedDebugAndroidTest`
    同理。

## 构建和静态检查

| 检查项 | 命令 | 结果 | 证据路径 |
|---|---|---|---|
| Debug 构建 | `gradlew :app:assembleDebug` | PASS | `app/build/outputs/apk/debug/app-debug.apk` |
| JVM 单元测试 | `gradlew :app:testDebugUnitTest` | PASS (31/31) | `app/build/test-results/testDebugUnitTest/*.xml` |
| Lint | `gradlew :app:lintDebug` | PASS（1 个 error 已修复后为 0） | `app/build/reports/lint-results-debug.html` |
| UI 自动化（模拟器） | `gradlew :app:connectedDebugAndroidTest` | PASS (9/9) | `app/build/outputs/androidTest-results/connected/debug/*.xml` |
| UI 自动化（真机） | `am instrument`（Xiaomi POCO F5, API 34） | PASS (10/10) | `docs/real-device-instrumentation.log` |
| Release 构建 | `gradlew :app:assembleRelease` | PASS（unsigned APK） | `app/build/outputs/apk/release/app-release-unsigned.apk` |

Lint 剩余告警均为预期项：targetSdk 36 < compileSdk 37（符合 Play target API
要求）、AGP/Gradle/Compose 插件存在更新版本（按 Guide §2.2 锁定）、
`mipmap-anydpi-v26` 目录因 adaptive-icon 需要 v26 限定符。均为 warning，非 error。

## 自动化测试

| 类别 | 用例数 | 通过 | 失败 | 未运行与原因 |
|---|---|---|---|---|
| JVM 物理单测（Jelly） | 8 | 8 | 0 | – |
| JVM 物理单测（Magnet Dust） | 11 | 11 | 0 | – |
| JVM 物理单测（Marbles） | 12 | 12 | 0 | – |
| Compose UI 测试 | 10 | 10 | 0 | – |

UI 测试覆盖：首页 3 卡片存在与文案；Jelly/Dust/Marbles 进入与返回；Reset；
Marbles Gravity 开关；Pause/Resume；Settings 三项开关（sound/haptic/reduce motion）；
Canvas down/move/up 手势注入（Jelly 与 Marbles）；系统返回键；语言切换（中文/英文往返）。
断言全部基于 testTag 与资源字符串，中英文设备均通过。

说明：物理玩具每帧都在动画，Compose 测试使用 `mainClock.autoAdvance = false`
手动推进帧时钟以避免无限等待（标准做法，Guide §11 推荐不使用 sleep）。

## 手工验收

| 场景 | 操作步骤 | 预期 | 实际 | 结果 |
|---|---|---|---|---|
| Jelly 按压/拖动/释放 | adb motionevent down→move→up，连续 8 次 | 局部凹陷、跟随、回弹、稳定 | 有变形（像素比对 L1 差异 992），无越界/NaN | PASS |
| Jelly Reset/Pause | 点击按钮 | 复位/冻结 | Reset 复位，Pause 显示 Resume 冻结 | PASS |
| Dust 长按聚集 | motionevent DOWN 保持 30 秒 | 粒子向指尖聚集形成光环，不穿透单像素 | 聚集可见（截图），粒子数不变 | PASS |
| Dust Reset/复现 | 多次 Reset | 相同 seed 复现初始分布 | 单测覆盖，手动无异常 | PASS |
| Marbles 弹射/碰撞 | 6 次 swipe flick、连续碰撞 | 惯性飞出不爆能量、反弹 | 弹球出射可见，无穿透/加速 | PASS |
| Marbles Gravity | 开关切换 | 开=下落，关=漂浮 | 文案切换正确，行为符合 | PASS |
| 音效开关 | 打开/关闭后 touch | 开=播放，关=静音 | logcat 日志证实生效 | PASS |
| 设置持久化 | 改开关→force-stop→重启 | 保持 | 截图证实（sound off、reduce on） | PASS |
| 后台往返 | HOME/回前台 ×10 | 暂停动画、恢复无大跳帧、无 Crash | 无 Crash/ANR | PASS |
| 旋转 | Jelly 页面旋转至横屏再回 | 不崩溃、重置到安全态 | 无 Crash | PASS |
| 窄屏+大字号 | 360dp 宽度，font_scale 1.4 | 文案可读、可滚动、无截断 | uiautomator bounds 证实无截断 | PASS |
| 触觉（真机） | 拖拽 Jelly | 按下/释放各一次轻震动 | 代码路径执行（真机上触发了 performHapticFeedback）；物理体感以用户确认为准 | 见文末 |
| 语言切换（真机） | 设置→语言→English | 界面变为英文 | 首页/设置面板切换为英文（截图），再切回跟随系统显示中文 | PASS |
| 中文本地化（真机） | 跟随系统（手机为中文） | 首页/设置面板显示简体中文 | 首页「挑一个来玩玩吧」、设置「音效/语言/跟随系统」均正确显示 | PASS |

## 性能与稳定性

- 构建类型：debug。真机性能为硬件实测；模拟器数据仅作参考。
- 测量方式：`dumpsys gfxinfo`。
- **真机（Xiaomi POCO F5，120Hz 屏，debug 构建）** Jelly 快速拖拽约 3 秒：
  - Total frames rendered: 988；Janky frames: 29（2.94%，modern 判定）；
  - 50th percentile 14ms / 90th percentile 17ms；GPU p50 3ms / p90 3ms。
- 模拟器（软件渲染）参考：197 帧 / 36% janky / 50th 53ms（不代表硬件）。
- 无法测量的项目：
  - Macrobenchmark 模块（未创建，属 v1.1 backlog）；
  - 15 分钟连续游戏（本次脚本交互约 5 分钟无 Crash/ANR）。
- 稳定性（真机）：三玩具脚本交互（Jelly 拖拽 6 轮+Reset/Pause、Dust 20 秒长按、
  Marbles 弹射 6 次+重力开关+Reset），后台往返 8 次与旋转测试后
  `logcat` 无 FATAL/ANR。

## 截图清单（docs/screenshots/）

- `home.png` — 首页全屏
- `phase2-jelly-rest-final.png` / `phase2-jelly-press-final.png` — Jelly 静止/按压
- `phase3-dust-idle-final.png` / `phase3-dust-gather-final.png` — Dust 漂浮/聚集
- `phase4-marbles-motion-final.png` / `phase4-marbles-floating-final.png` — Marbles 运动/零重力漂浮
- `settings-final.png` — Settings 面板
- `home-narrow-font.png` — 360dp 宽 + 1.4 倍字号首页
- `phase5-settings-toggled.png` / `phase5-settings-persisted.png` — 设置更改与持久化证据
- 真机截图（`real-*`）：`real-home.png`、`real-jelly-rest/press.png`、
  `real-dust-idle/gather.png`、`real-marbles-motion/floating.png`、`real-settings.png`

## 缺陷与风险

- 阻塞项：无。
- 非阻塞项：
  - 触觉的真实体感与音效听感需用户在真机上主观确认（逻辑与设备端行为已
    验证：设置持久化、开关生效、Haptics/SoundPool 调用执行）。
  - API 37 与 minSdk 26 设备仍未实测（镜像已就绪，可后续建 AVD/真机补测）。
  - Macrobenchmark 模块未创建（v1.1 backlog）。
  - `app-release-unsigned.apk` 未签名，上架 Play 前需正确签名并核验包名
    `dev.touchlab.app` 归属与唯一性。

## 最终判定

PARTIAL → 待真机主观项确认后升级 DONE

依据：全部自动化测试（31 单测；9 UI 在模拟器与真机各跑一遍均通过）、构建 /
lint / release 通过；真机（Xiaomi POCO F5）完成三玩具交互、设置持久化、
音效开关、后台/旋转/稳定性与 120Hz 帧统计（p50 14ms）验证。剩余待确认项为：
真机触觉体感与音效听感的用户主观确认、API 37 / minSdk 26 设备覆盖、
Macrobenchmark（backlog）。这些均不阻塞 MVP 交付判断。