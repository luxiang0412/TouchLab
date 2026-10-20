# TouchLab 测试报告

- 测试日期：2026-10-21（本地完成全部 Phase 0–7 验证）
- 代码 commit：见 `git log --oneline`（报告最后一份提交）
- OS / JDK / Gradle / AGP / Compose BOM：
  - Windows 11 10.0.26200 / Zulu JDK 17.0.3 / Gradle 9.4.1 / AGP 9.2.1 /
    Compose BOM 2026.09.00（以上为实测可用组合）
- minSdk / targetSdk / compileSdk：26 / 36 / 37
- 测试设备：
  - `touchlab_api34` 模拟器：Google APIs x86_64，Android 14 (API 34)，
    1080×2400 @ 420dpi，软件渲染（AEHD 加速）。这是当前唯一的测试环境，
    **没有真机**。

## 构建和静态检查

| 检查项 | 命令 | 结果 | 证据路径 |
|---|---|---|---|
| Debug 构建 | `gradlew :app:assembleDebug` | PASS | `app/build/outputs/apk/debug/app-debug.apk` |
| JVM 单元测试 | `gradlew :app:testDebugUnitTest` | PASS (31/31) | `app/build/test-results/testDebugUnitTest/*.xml` |
| Lint | `gradlew :app:lintDebug` | PASS（1 个 error 已修复后为 0） | `app/build/reports/lint-results-debug.html` |
| UI 自动化 | `gradlew :app:connectedDebugAndroidTest` | PASS (9/9) | `app/build/outputs/androidTest-results/connected/debug/*.xml` |
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
| Compose UI 测试 | 9 | 9 | 0 | – |

UI 测试覆盖：首页 3 卡片存在与文案；Jelly/Dust/Marbles 进入与返回；Reset；
Marbles Gravity 开关与文案切换；Pause/Resume 文案切换；Settings 三项开关
（sound/haptic/reduce motion）往返切换；Canvas down/move/up 手势注入
（Jelly 与 Marbles）；系统返回键回首页。

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
| 触觉 | 拖拽 Jelly | 按下/释放各一次轻震动 | 代码路径执行；**模拟器无振动硬件，未真机验证** | PARTIAL |

## 性能与稳定性

- 构建类型：debug（模拟器无法代表硬件）；未在真机/Release 上测。
- 测量方式：`dumpsys gfxinfo`（软件渲染模拟器），自研 Jelly 交互约 3 秒。
- 观测值（仅作环境参考，不代表真机）：
  - Total frames rendered: 197；Janky frames: 36%（90th GPU 有脉冲尖峰，
    与模拟器软件渲染有关）。
- 无法测量的项目：
  - 60 FPS 真机指标（无真机）；
  - Macrobenchmark 模块（未创建，属 v1.1 backlog）；
  - 15 分钟连续游戏稳定性（本次实际交互约 4–5 分钟无 Crash/ANR）。
- 稳定性：monkey+脚本式交互（8 次 Jelly 拖拽循环、Dust 30s 长按、6 次
  Marbles 弹射、多次 Reset/开关）后 `logcat` 无 FATAL/ANR。

## 截图清单（docs/screenshots/）

- `home.png` — 首页全屏
- `phase2-jelly-rest-final.png` / `phase2-jelly-press-final.png` — Jelly 静止/按压
- `phase3-dust-idle-final.png` / `phase3-dust-gather-final.png` — Dust 漂浮/聚集
- `phase4-marbles-motion-final.png` / `phase4-marbles-floating-final.png` — Marbles 运动/零重力漂浮
- `settings-final.png` — Settings 面板
- `home-narrow-font.png` — 360dp 宽 + 1.4 倍字号首页
- `phase5-settings-toggled.png` / `phase5-settings-persisted.png` — 设置更改与持久化证据

## 缺陷与风险

- 阻塞项：无。
- 非阻塞项：
  - 模拟器无振动/音频硬件：音效、触觉仅代码级与 log 验证，需真机复测。
  - 模拟器为软件渲染，性能数据不可当硬件结论。
  - `app-release-unsigned.apk` 未签名，上架 Play 前需正确签名并核验包名
    `dev.touchlab.app` 归属与唯一性。
  - 首次冷启动在模拟器约 3–4 秒（软件渲染），真机需复测。

## 最终判定

PARTIAL

依据：全部自动化测试（31 单测 + 9 UI）与构建/lint/release 均通过，三个玩具
真实可玩；但**没有真机**，音效/触觉硬件输出与 60 FPS 性能目标无法真实验收，
因此不能宣称 DONE。除该设备覆盖缺口外，MVP 功能验收全部满足。