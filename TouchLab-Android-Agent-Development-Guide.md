# TouchLab — Android App 全流程开发执行书

> **文档用途**：直接交给本地 AI Coding Agent（例如 Pi），指导它从空目录完成 Android App 的需求实现、UI/UX、构建、运行、调试、自动化测试、性能验证和最终交付。
>
> **文档版本**：v1.0 · 2026-10-20 · 语言：中文 · 项目代号：TouchLab
>
> **工作原则**：这是**执行规范**，不是创意讨论。按阶段逐项实现，每个阶段必须提交可检查的证据。禁止以“理论上可用”“编译应该通过”代替实际验证。

---

## 0. 给本地 Agent 的最高优先级指令（先读这一节）

你是本项目的唯一执行开发 Agent，负责产品、Android 代码、交互体验、测试、调试、验收材料。用户希望获得一个**真正能玩、体验精致、能在 Android 手机上安装、具备未来上架 Google Play 基础**的原生 App。

### 0.1 不可违反的执行规则

1. **先检查环境，再写代码；先跑通最小工程，再做功能；先保证一个实验完整可用，再开发下一个。**
2. 不要一次生成所有代码然后宣称完成。必须按照第 9 节阶段顺序执行，每阶段运行对应验证命令，并保留输出。
3. 默认自主决定小问题，不因命名、配色、目录、图标等可合理决策事项中断。只有确实无法恢复的环境阻塞、涉及账号/费用/敏感权限/删除用户数据时才要求用户介入。
4. **不要假设工具存在**：先用命令检查 `java`、`adb`、`gradle` / `gradlew`、Android SDK、模拟器。缺依赖则尝试安全安装，并如实记录；不能安装就标记阻塞，不得伪造运行结果。
5. 每完成一个阶段都更新 `docs/PROGRESS.md`，写清已完成项、执行命令、通过/失败结果、剩余问题以及下一步。建议小步提交 Git commit。
6. 任何修改影响现有代码后，至少重新执行 `testDebugUnitTest` 和 `assembleDebug`；交付前执行全部第 11 节必需检查。
7. 严禁通过删除测试、跳过检查、吞掉异常、硬编码“成功”状态来让验收通过。遇到失败先定位根因、修复、重跑。
8. 不允许无证据声称“已在真机测试”“60 FPS 稳定”“符合上架政策”。没有设备、截图、测量或控制台输出，就明确写**未验证**。
9. 项目优先完整、稳定、漂亮的 **MVP**，避免过度架构、第三方物理引擎、广告、云后端与账户系统。
10. **所有用户可见功能必须可操作**。禁止占位按钮、假设置、静态截图伪装交互、不能返回的页面。
11. 修改/安装仅限项目及开发工具；不得删除其他项目或修改系统全局安全设置。不得在日志、Git、APK 内写入凭据。
12. 在每个阶段遇到超出范围的“好主意”时，先记入 `docs/BACKLOG.md`，不擅自扩大 MVP。

### 0.2 完成的定义（Definition of Done）

只有下列各项全部满足，才能将项目标记为 `DONE`：

- [ ] Android 原生 Kotlin + Jetpack Compose 项目，可以从干净工作区构建。
- [ ] 首页展示 **Jelly / Magnet Dust / Marbles** 3 个独立互动实验；任何一个均可进入、操作、重置和返回。
- [ ] 三个实验**真实运行**，手势会影响状态，释放后产生可见运动反馈，并且不会出现数值爆炸、卡死、黑屏或无限增长的粒子。
- [ ] 设计风格一致；深色主题、浅色层次、字体、空间、按钮和动画符合第 6 节；小屏及大字号布局可用。
- [ ] 声音开关、触觉反馈开关、减弱动态效果开关真实生效，并跨应用重启保存。
- [ ] 离线可用；不需要用户登录；不申请不必要权限。
- [ ] `assembleDebug`、`testDebugUnitTest`、`lintDebug` 通过。
- [ ] 有设备时 `connectedDebugAndroidTest` 通过，并在模拟器或真机执行真实交互冒烟测试、截图及 Logcat 检查。
- [ ] 有可复查的性能测试记录；如未具备性能测试设备，必须记为**尚未完成性能验收**，不能宣称达到 FPS 指标。
- [ ] `README.md`、`docs/PROGRESS.md`、`docs/TEST-REPORT.md`、`docs/KNOWN-ISSUES.md`、APK 路径、版本清单齐备。

---

## 1. 产品定位与范围

### 1.1 一句话定位

**TouchLab：一个装在口袋里的触觉互动实验室。** 用户用手指碰触软糖、吸引粒子、弹射小球，得到即时、柔软、顺滑的视觉/声音/触觉反馈。

### 1.2 体验目标

- **3 秒理解**：打开首页能看出这是可玩的互动玩具，而不是设置工具或普通信息 App。
- **1 次触摸有反馈**：触下、拖动和松手分别有明确反应。
- **轻松放松**：界面克制，没有弹窗轰炸、积分、任务、广告、强制登录。
- **反复可玩**：物体运动不是固定视频；每次交互结果稍有不同，但行为可预测。
- **先把触感做好**：手指跟随、惯性、碰撞、回弹和动效，比功能数量更重要。

### 1.3 MVP 必做与不做

| 优先级 | 内容 | 判定 |
|---|---|---|
| P0 | 原生首页、3 个完整实验、真实手势、重置/返回、统一设计、离线运行 | 必做 |
| P0 | 数据安全、生命周期暂停、基本无障碍、Crash-free 冒烟、基础自动化测试 | 必做 |
| P0 | 可切换并持久化音效/触觉反馈/减少动效 | 必做 |
| P1 | 精致微动效、首次进入的轻提示、原创 App 图标、基础深色视觉体系 | 必做，但不得阻塞核心玩法 |
| P2 | 录屏分享、动态主题、更多玩具、成就、桌面小组件、倾斜传感器、平板专属布局 | v1.1 以后 |
| 明确不做 | 登录、账号、云同步、网络请求、付费、推送、广告、聊天 AI、复杂流体模拟、3D 引擎 | MVP 禁止 |

### 1.4 发布范围

- 第一个目标：**手机 Android**；横屏、平板精细适配可以后续增强，但不得在窗口尺寸变化时崩溃。
- App 显示名：`TouchLab`；副标题 `Tiny physics toys`。
- 临时包名：`dev.touchlab.app`。此包名仅用于本地工程，**提交 Play 前必须确认唯一性和归属**。
- `minSdk = 26`（Android 8.0）；`targetSdk = 36`（2026-10 Google Play 新应用要求至少 36）；`compileSdk = 37`（配合所选 Compose 版本）。
- 版本起点：`versionName = "0.1.0"`、`versionCode = 1`。

---

## 2. 技术选型与约束

### 2.1 固定架构

- 语言：**Kotlin**。
- UI：**Jetpack Compose + Material 3**；主实验画布使用 Compose `Canvas`、`pointerInput`、`drawWithCache`（仅在确有必要时使用）。
- 构建：Gradle Kotlin DSL、Gradle Wrapper、Android Gradle Plugin（AGP）。
- 结构：**单 Activity、单 `:app` Module、按 feature 分包**，不要引入多模块、Clean Architecture 六层包装或依赖注入框架。
- 页面切换：MVP 仅首页和实验页，可采用 `rememberSaveable` + `sealed`/枚举定义当前页面；必须支持系统返回键并验证状态恢复。**不用为两级页面引入复杂导航框架**。
- 物理计算：Kotlin 纯函数/类（与 Compose 分离），固定时间步长数值积分；不引入 Box2D/Unity/LibGDX。
- 设置存储：Jetpack DataStore Preferences（3 个布尔值即可）。
- 音频：本地原创短声音资源（可由 Python 标准库生成 WAV），`SoundPool` 播放；音频不可用时只禁用音效，不得影响画面。
- 触觉：Compose `LocalHapticFeedback` 或对应官方兼容 API。设备不支持时静默降级，不能因震动缺失崩溃。**不要做每帧震动**。
- 测试：JVM 单元测试、Compose instrumentation UI test；按条件补充 Macrobenchmark。
- 目标环境：不需要服务器、不需要 API 密钥、不需要 Internet 权限。

### 2.2 版本策略：先验证兼容再锁版本

以 2026-10-20 已公开的稳定组合为**初始候选**：

| 组件 | 候选版本/范围 | 注意 |
|---|---|---|
| JDK | 17 | 与 AGP 9.2 系列兼容 |
| AGP | `9.2.1` | 若仓库不可获得，用官方已核实的同系列稳定版本并记录原因 |
| Gradle Wrapper | `9.4.1` | 与 AGP 9.2 系列对应 |
| Kotlin | AGP 9.x 内置 Kotlin | AGP 9.x 默认内置 Kotlin；**不要额外应用 `org.jetbrains.kotlin.android` 插件** |
| Compose Compiler plugin | `org.jetbrains.kotlin.plugin.compose` | 选择与最终生效 Kotlin 版本兼容/对应的版本，验证构建；不要把 Compose BOM 当编译器版本 |
| Compose BOM | `2026.09.00` | 当前官方文档展示的稳定 BOM；优先锁定该版本 |
| `compileSdk` | 37 | Compose 1.12+ 对应 37 |
| `targetSdk` | 36 | 编译 SDK 和目标 SDK **不是一回事** |
| Material 3 / Compose UI | 使用 BOM 管理 | 不单独为 BOM 组件胡乱指定版本 |

**很重要：** 这些是执行起点，不保证本地 SDK / 网络镜像已经提供所有依赖。先创建一个空 Compose Activity，验证插件解析和 `assembleDebug` 后，将**实际可运行的版本组合**写到 `docs/ENVIRONMENT.md`。如果出现版本冲突，按官方文档查兼容关系，优先修改一个变量并记录结果；禁止反复猜不同版本，禁止使用 `+` 动态版本、alpha/canary（除非稳定工具链无法工作且有充分证据）。

官方资料：

- Compose / BOM / Compiler: https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler
- AGP 9.2 兼容信息: https://developer.android.com/build/releases/agp-9-2-0-release-notes
- AGP 内置 Kotlin: https://developer.android.com/build/migrate-to-built-in-kotlin
- Play target API: https://support.google.com/googleplay/android-developer/answer/11926878

### 2.3 拟定目录（以实际可编译项目为准）

```text
TouchLab/
├── README.md
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
├── gradle/libs.versions.toml
├── gradlew / gradlew.bat
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/dev/touchlab/app/
│       │   │   ├── MainActivity.kt
│       │   │   ├── ui/TouchLabApp.kt
│       │   │   ├── ui/theme/{Color,Theme,Type}.kt
│       │   │   ├── ui/components/{LabCard,LabTopBar,PlayControls}.kt
│       │   │   ├── feature/home/HomeScreen.kt
│       │   │   ├── feature/jelly/{JellyScreen,JellyPhysics}.kt
│       │   │   ├── feature/dust/{DustScreen,DustPhysics}.kt
│       │   │   ├── feature/marbles/{MarblesScreen,MarblesPhysics}.kt
│       │   │   ├── settings/{SettingsRepository,SettingsSheet}.kt
│       │   │   └── feedback/{SoundEngine,Haptics}.kt
│       │   └── res/{drawable,mipmap-*,values,raw}/
│       ├── test/java/dev/touchlab/app/physics/
│       └── androidTest/java/dev/touchlab/app/
├── tools/generate_sounds.py
└── docs/
    ├── ENVIRONMENT.md
    ├── PROGRESS.md
    ├── TEST-REPORT.md
    ├── KNOWN-ISSUES.md
    ├── BACKLOG.md
    └── screenshots/
```

文件结构可随实际代码演进调整，但要保持：**绘制与物理分离、UI 与持久化分离、可测试、依赖简洁**。

---

## 3. 功能规格（按这个做，不要自行改玩法）

### 3.1 Home：实验室首页

打开后展示：

1. 顶部品牌字标 `TouchLab`；下面一行小字 `A tiny playground for your fingers.`。
2. 主标题 `Pick something to play with.`。
3. 三张纵向大卡片，按顺序：
   - **Jelly** · `Squish it. Stretch it. Let it bounce.` · 薄荷绿软胶形状。
   - **Magnet Dust** · `Gather a little universe.` · 紫色粒子旋涡。
   - **Marbles** · `Flick. Bounce. Repeat.` · 珊瑚色小球与轨迹。
4. 每张卡片右上有编号 `01 / 02 / 03`，右下是明确的进入箭头；点击卡片任意主要区域进入实验。
5. 页底小型 `Settings` 入口和版本信息（仅在不造成拥挤时展示）。

约束：

- 主卡片点击区至少 48dp 高，实际卡片建议高度 **156–188dp**。
- 首页卡片插画使用代码绘制的矢量元素，不使用外部版权图片。
- 首页插画默认静态或轻量动画，**不能同时持续运行三个物理引擎**。
- 卡片不同，但视觉体系统一；不能变成普通的“3 个灰色 ListItem”。

### 3.2 Play：实验页通用框架

- 顶部：返回箭头、实验名称、小号 `01/03`。顶部控件必须在安全区域。
- 中间：占屏幕主要空间的可交互画布，尽量保留大片负空间；沉浸式但不遮挡系统手势区。
- 底部：简短操作提示，如 `Touch and drag`；显著的 **Reset** 按钮，以及更多设置入口。
- 提供**暂停（Pause/Resume）**操作。暂停时画面静止、不再更新模拟，并能恢复。
- 首次进入某实验显示一句轻提示（非阻塞、可自动消失或首触消失），下次是否显示可自行简化。
- 返回首页时停止该实验的计算/音频；再次进入可以重新开始实验；不要求保存玩具在离开时的物理状态。
- 屏幕配置变化时不崩溃；可以选择合理地重置模拟并在 `README` 说明。

### 3.3 实验一：Jelly（软糖）

**视觉**：深色背景中央一个大号薄荷绿、柔和渐变或双层高光的软胶团。建议默认直径 `170–220dp`（随画布尺寸自适应），下方少量阴影，背景有柔和圆形点缀。

**互动**：

1. 按压软糖：按压点附近产生清楚但不过度的局部凹陷；边缘有回弹感。
2. 拖动：软糖跟随手指移动，跟随可有轻微滞后，不能跳到错误坐标。
3. 松开：产生短暂的弹性摆动，逐步回到稳定形状；软糖不会无限漂移出屏幕。
4. 在边缘拖动可产生拉伸的错觉；实现可以简化，但不能只做按钮点击缩放。
5. Reset：软糖居中、速度清零、形状恢复。

**推荐实现（既可玩又可控）**：

- 用 `N=20` 个等角采样点表示封闭轮廓；点状态包括径向位移 `d[i]` 和速度 `v[i]`。
- 每帧按弹簧阻尼更新 `v += (-k*d - damping*v + touchForce) * dt`，`d += v*dt`。
- 手指压力作用于附近轮廓点，权重可用角度距离高斯衰减；只对局部轮廓产生有限位移，位移需要 clamp。
- 圆心独立维护位移/速度，拖动时向手指目标位置弹簧跟随；松开后将圆心限制在画布安全范围。可以让圆心停留在松手位置，不必强制回中心。
- 将采样点串成平滑封闭 `Path`（二次贝塞尔/中点插值），使用 `Canvas` 绘制；注意首尾闭合及异常坐标。
- **触觉**：触下或一次明显弹性回弹时轻触发；拖动过程中不可每帧震动。
- **音效**：按下/释放时短促、轻柔，不允许尖锐连续鸣叫。

**验收**：点击、连续拖动 10 秒、多次快速放手、触摸画布边缘、Reset 20 次均无异常；放手后 3 秒内运动趋于稳定，形状无自交爆炸。

### 3.4 实验二：Magnet Dust（磁力粒子）

**视觉**：深蓝/紫色空间中的约 `120` 个亮点，大小、亮度有细微差别，画面带轻微弧线/轨迹感（轨迹不是必需）。

**互动**：

1. 未触摸时粒子缓慢漂浮，屏幕不是静态图片。
2. 手指按住时，附近粒子被吸引，逐步向指尖聚集；拖动时粒子跟随形成尾迹。
3. 松手后粒子继续运动、阻尼减速、逐渐散开；不能全部永久粘在一个像素上。
4. Reset：按固定随机种子（例如 `42`）重建初始粒子分布，方便截图和测试。

**推荐实现**：

- 粒子状态：`x, y, vx, vy, radius, phase`，使用屏幕像素坐标，所有粒子数量有上限。
- 每帧：计算到触点的矢量，加入**有上限的引力**和少量切向扰动，再加阻尼；距离下限 `epsilon` 防止除零；速度 clamp，边界使用反弹或 wrap（选定一种并保持一致）。
- 引力可用 `force = normalized(direction) * min(Fmax, strength / (distance^2 + softening))`；不要求真实天体力学。
- 限制绘图工作量；不要为每个粒子创建 Compose Composable，**单 Canvas 一次绘制**。
- 避免无限分配新对象、无限 trail 列表或无限协程。

**验收**：长按 30 秒、快速拖动、反复按下松开、Reset 20 次，粒子始终有边界/可见，坐标与速度不出现 `NaN` / `Infinity`，粒子数量恒定。

### 3.5 实验三：Marbles（弹珠）

**视觉**：6 颗（允许 5–8 颗）不同大小的彩色小球，在一块有轻量柔和边界的“玻璃托盘”中运动。不要做真实 3D。

**互动**：

1. 手指点中某球并拖动，球跟随手指；其余球仍按物理规则运动。
2. 松手时按最近一小段移动距离/时间计算抛掷速度，球按惯性飞出。
3. 球撞边界会反弹；球与球相撞会交换部分动量，不能无限加速。
4. `Gravity` 开关：开启时球受向下重力，关闭时以零重力漂浮；与全局 Settings 不同，此开关只属于 Marbles。
5. Reset：恢复 6 个预设球的位置和静止速度。

**推荐实现**：

- 基于圆形的 2D 运动，使用半隐式 Euler；重力、摩擦/阻尼、墙壁碰撞恢复系数（初始建议 `0.82`）及球球碰撞冲量。
- 保证同半径两球完全重叠时仍有稳定法向量（固定向量或确定性微扰），避免 `0/0`。
- 对位置分离和速度冲量分别处理，**不要在碰撞回调中重复相加无限能量**。
- 只允许同时拖动一颗球（MVP），其他指针忽略；必须正确处理 `ACTION_CANCEL` / 手势取消。
- 速度、安全加速度、dt、位置全部 clamp；画布尺寸发生变化时重新约束边界。

**验收**：快速弹射 50 次、连续撞墙、多个球重叠、切换重力 20 次、Reset 20 次均无崩溃或无限运动能量增长。

---

## 4. 模拟器与帧循环规范（重要）

1. **固定物理步长**：推荐 `1/120 秒` 的模拟步长；渲染依据屏幕帧信号推进；每次帧间真实时间 `dt` 最多取 `50ms`，每帧最多执行 4–6 次物理步，避免退后台恢复造成“螺旋死亡”。如用更简单的每帧积分，也必须 clamp `dt` 并说明精度折衷。
2. **生命周期**：使用 `Lifecycle` 的 STARTED/RESUMED 可见性门控；进入后台、切到别的实验、弹出较长遮挡层时暂停模拟；恢复后重新设置上一帧时间戳，不能补算离开期间的几分钟。
3. **线程规则**：物理数据读写必须一致。MVP 可以在主线程以受控的帧时长更新轻量粒子；若有明显卡顿，再单独分析是否需要分离计算。禁止多个协程同时修改同一可变列表。
4. **帧驱动**：推荐 `withFrameNanos` / Compose 动画时钟；不要 `while(true) { delay(1) }` 自旋；对 `LaunchedEffect` 正确取消。
5. **坐标系**：物理画布内部用 **px**；UI 布局用 **dp**。所有触点和模拟坐标必须处于同一个 Canvas 本地坐标系；统一 `Density` 转换，不可混用绝对屏幕位置和局部触点。
6. **变更尺寸**：画布 `size.width/height <= 0` 时跳过更新；尺寸变化后将物体限制到新边界，或按比例重置，禁止产生负半径。
7. **数值保护**：所有除法有最小 epsilon，限制速度与位移，有限值检查，固定粒子数；没有 NaN/Inf。
8. **降级**：降低动态效果时减小回弹、粒子漂移和背景动画；仍保留触摸可感知的即时反馈，不能直接把 App 变成毫无互动的图片。
9. **性能优化顺序**：先用工具定位，再避免 Compose 逐粒子重组、每帧图片解码、每帧列表分配、频繁日志、过量高斯模糊；不要未经测量就引入复杂渲染引擎。

---

## 5. 交互状态与可访问性

### 5.1 必须处理的状态

| 状态 | 必须表现 |
|---|---|
| 首次打开 | 立刻看到 3 张能玩的卡片，不强制引导流程 |
| 点击卡片 | 给触摸反馈，进入对应实验 |
| 玩具按下 | 立即有视觉反馈，音效/触觉按设置可选 |
| 手指移动 | 画面连续变化；不会突然跳位或捕获错误触点 |
| 松手/取消 | 物理继续正确运行；捕获状态释放 |
| 点击 Reset | 立即重置且可以再次交互 |
| 点击 Pause | 画面冻结，控件显示 Resume；再次点击继续 |
| 系统返回手势/返回键 | 从实验返回首页；首页再按遵循 Android 默认退出行为 |
| 旋转/窗口变化 | 不崩溃、不越界；可重置模拟 |
| 进入后台/锁屏 | 暂停动画和音效；回来正常恢复 |
| 无声音/无触觉设备 | 不崩溃；关闭效果或无声降级 |
| 大字号、窄屏 | 控件不重叠，文案能阅读，底部按钮可见 |

### 5.2 无障碍要求

- 所有常规按钮至少 **48dp 可点击区域**，提供 `contentDescription`，图标按钮不能只有形状没说明。
- 每张首页卡片有明确可访问名称和可点击语义（例如 `Open Jelly`）。
- 画布至少有可访问描述：`Interactive jelly toy. Drag to move it.`；可使用 `Reset` 替代部分复杂手势操作的恢复功能。
- 不仅靠颜色区分状态；Pause/Resume 必须显示文字或可读语义。
- 遵从系统字体缩放、系统栏 Insets；避免把重要按钮置于返回手势热区。
- 音效默认低音量，提供用户开关；震动不应密集或惊扰。

---

## 6. UI/UX 设计说明（具有可执行性）

### 6.1 设计关键词

**Tactile / Playful / Calm / Premium / Minimal**（有触感、好玩、安静、高级、极简）。不要做赛博朋克控制台、数据监控仪表盘、密密麻麻的圆角卡片，不要使用默认 Demo 的随意蓝紫渐变。

### 6.2 基础 Design Tokens

| Token | 值 | 用途 |
|---|---|---|
| `Background` | `#10121B` | App 主背景 |
| `Surface` | `#1C2030` | 卡片和面板 |
| `SurfaceRaised` | `#282D40` | 次级层次 |
| `TextPrimary` | `#F8F7F4` | 标题和正文 |
| `TextSecondary` | `#A5AABB` | 说明文字 |
| `JellyMint` | `#A9EFC7` | Jelly 主色 |
| `DustLavender` | `#B9A7FF` | Magnet Dust 主色 |
| `MarbleCoral` | `#FFAB95` | Marbles 主色 |
| `AccentWarm` | `#F9DFAB` | 少量强调 |
| 卡片圆角 | `28dp` | Home 大卡片 |
| 按钮圆角 | `20dp` / 胶囊形 | Reset、Pause |
| 页面外边距 | `20dp` | 手机常规布局 |
| 常规控件间距 | `8/12/16/24dp` | 避免任意尺寸 |

- 深色是 MVP 的主设计风格；浅色主题暂不做，可适配系统导航栏图标明暗。
- 优先系统字体（兼容、无额外版权风险）；标题粗细显著，正文不过小。建议首页标题约 32–36sp，卡片标题约 24sp，正文 14–16sp；需允许字号缩放后自然换行。
- 高光/阴影只用于强调物体“有弹性/漂浮”。不要所有卡片叠 5 层 Glow 导致渲染性能差。
- 选择一个明确的产品图标造型：由 3 个圆与一条弧构成的抽象软糖/触点，使用原生 Vector/Adaptive Icon，**不要盗用第三方图标**。

### 6.3 布局与动效

**Home**：品牌 + 主标题 + 3 张大卡片，底部 Settings。卡片可有轻微弹性点击缩放（约 `0.98`，回到 `1.0`）。卡片插图不能抢文案可读性。

**实验页**：画布优先，顶部和底部控制区保持稳定。背景允许有淡淡径向光晕，但不能因绘制大面积模糊明显卡顿。

**动效规范**：

- 标准 UI 过渡参考 `180–260ms`；大型弹簧运动由物理模型控制，不用固定循环补间伪装。
- 点击有反馈，页面变化清晰；无需花哨的三维过场。
- 返回时应有逻辑清楚的层级关系。
- 如果系统动画关闭或者用户开启减少动态效果，则减小动画幅度与持续时间。

### 6.4 UI 验收截图目标

至少采集以下**真实运行截图**（不能用设计稿代替）：

1. 首页全屏（默认手机尺寸）。
2. Jelly 按压/变形状态。
3. Magnet Dust 长按吸引状态。
4. Marbles 运动中状态。
5. Settings 面板。
6. 360dp 左右窄屏、较大字体时的首页。

每张截图人工或视觉检查：文字有无截断、系统栏遮挡、卡片间距是否一致、控件能否点击、色彩对比是否足够、是否存在明显的廉价 Demo 感。

---

## 7. 设置与数据

| 设置项 | 默认值 | 效果 | 持久化 |
|---|---|---|---|
| `Sound effects` | 开 | 触摸/碰撞播放轻音效 | DataStore |
| `Haptic feedback` | 开 | 支持设备按触点/撞击反馈 | DataStore |
| `Reduce motion` | 关（若系统有降低动效偏好则尊重它） | 减少装饰性循环/大幅弹簧 | DataStore |

- 操作设置后立刻生效；杀进程重启保持一致。
- 不存用户私密信息，不收集传感器/位置数据，不需要联网。
- 音效资源由本项目生成（4–6 个极短 WAV），源代码放 `tools/generate_sounds.py`；输出文件提交到 `app/src/main/res/raw/`，README 标注原创程序合成。音频播放需限流，短时间碰撞集中过多时最多间隔约 `80–120ms` 播放一次，避免爆音。
- Settings 入口既可在首页也可在实验页打开；关闭面板后恢复正确页面状态。

---

## 8. 环境检查与可运行命令

### 8.1 每个环境先执行

先确认运行平台（Windows PowerShell 或 Linux/macOS shell），执行最适合的命令。记录到 `docs/ENVIRONMENT.md`：

```bash
java -version
adb version
adb devices -l
```

Linux/macOS 另查：

```bash
printf 'ANDROID_HOME=%s\nANDROID_SDK_ROOT=%s\n' "$ANDROID_HOME" "$ANDROID_SDK_ROOT"
command -v sdkmanager || true
command -v emulator || true
```

Windows PowerShell 另查：

```powershell
$env:ANDROID_HOME
$env:ANDROID_SDK_ROOT
Get-Command java, adb, sdkmanager, emulator -ErrorAction SilentlyContinue
```

如 SDK 尚未安装，可在用户有权限的目录使用 Android Studio SDK Manager / 官方命令行工具安装：**Android Platform API 37、Build Tools、Platform Tools、Emulator 与至少一个系统镜像**。不可臆造 SDK 安装路径；`local.properties` 中可配置 `sdk.dir`，但不要把机器专属绝对路径提交 Git。

### 8.2 初始化项目

1. 如果目录已有项目，先查看 Git 状态、目录与现有配置；**不得覆盖用户文件**。
2. 空项目使用 Android Studio 的 **Empty Activity / Compose** 模板或官方可复现的 Gradle 初始化方式。
3. `settings.gradle.kts` 必须正确声明 `google()`、`mavenCentral()`、`gradlePluginPortal()`（各用于相应仓库），并包含 `:app`。
4. 使用 Gradle Wrapper；最终必须保留 `gradlew`、`gradlew.bat` 和 wrapper JAR/properties（来自可信构建生成）。
5. 先构建最简单的 Compose 文本界面，再引入 DataStore、音效和实验逻辑。
6. Gradle 9.x/AGP 9.x 的 Kotlin 插件配置不能照抄几年前的教程；如遇 `kotlin-android` 重复插件或版本冲突先参考第 2.2 节官方资料。

### 8.3 通用构建/测试命令

Linux/macOS：

```bash
./gradlew --version
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
./gradlew :app:connectedDebugAndroidTest
```

Windows PowerShell：

```powershell
.\gradlew.bat --version
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest
```

**说明**：`connectedDebugAndroidTest` 必须有在线设备/已启动的模拟器；没有设备时不可标记通过。若测试目标名因插件版本有变化，先运行 `./gradlew :app:tasks --all` 查询，不猜任务名。

### 8.4 APK 安装和截图

```bash
adb devices -l
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell monkey -p dev.touchlab.app -c android.intent.category.LAUNCHER 1
adb shell screencap -p /sdcard/touchlab-home.png
adb pull /sdcard/touchlab-home.png docs/screenshots/home.png
```

- 有多个设备时使用 `adb -s <serial>` 指定，避免误安装到其他设备。
- 启动之前核对最终 `applicationId`，不能假定一直是 `dev.touchlab.app`。
- 检查崩溃可用：

```bash
adb logcat -c
# 完成一次真实交互后再执行
adb logcat -d -s AndroidRuntime:E ActivityManager:E
```

`logcat -c` 只对已选测试设备执行；如果是他人的设备且不适合清日志，则先记录时间并按应用 PID 过滤，避免清除别人的日志。

---

## 9. 强制开发阶段与阶段门禁

> 每个阶段严格执行「实现 → 构建/测试 → 检查结果 → 更新报告 → Git 提交（若仓库允许）」；失败不跳阶段。

### Phase 0 — 勘察环境，初始化仓库

**任务**：

- 检测 OS、JDK、SDK、`adb`、模拟器与 Git。
- 检查是否存在既有仓库或未提交修改。
- 创建目录和文档：`README.md`、`docs/ENVIRONMENT.md`、`docs/PROGRESS.md`、`docs/BACKLOG.md`。
- 创建最小 Compose 应用，按第 2.2 节选版本、实现构建成功。

**验收命令**：`assembleDebug` 返回 `BUILD SUCCESSFUL`，记录 SDK/AGP/Gradle/Kotlin/BOM 版本；如有设备至少安装启动一次。

**完成条件**：最小首页渲染正常；Phase 0 对应日志在 `docs/PROGRESS.md`。

### Phase 1 — 设计系统与首页

**任务**：

- 实现颜色/排版 Token、状态栏/导航栏 edge-to-edge 与 WindowInsets。
- 做首页三张精致大卡、独立画布缩略图、标题、说明、Settings 入口。
- 实现首页 → 3 个暂时空实验页 → 返回首页的真实导航；空实验页必须明确是当前阶段临时状态，最终不保留占位。
- 给交互控件加 Compose `testTag`（见第 11 节）。

**验收**：3 卡都能点击进入，返回正确，窄屏布局无溢出；Compose UI test 至少覆盖 3 个入口。

### Phase 2 — Jelly（先把一个作品打磨完整）

**任务**：

- 独立 `JellyPhysics` 纯 Kotlin 状态和 `step(dt, touch)` 逻辑。
- 先写弹性回归、边界、Reset、有限值单元测试；再接 Compose Canvas 和手势。
- 完成按下、拖动、释放、回弹、Reset/Pause、说明文案。
- 音效/触觉可先用接口或空实现，但必须在 Phase 5 接入真实设置。

**验收**：真实模拟器/设备连续玩 2 分钟，不崩溃；运动合理；单元测试通过；在 `docs/screenshots/` 放截图。

### Phase 3 — Magnet Dust

**任务**：

- 独立粒子物理，**固定随机种子 + 固定粒子数量 + 力/速度限制**。
- 单 Canvas 绘制，触摸吸引，拖动形成尾迹感，释放后自然漂浮。
- 物理单测：数量守恒、极短距离不除零、边界、暂停、长时运行。

**验收**：至少 5 分钟真实互动，长按与快速拖动正常，重置后回到相同可复现初始配置。

### Phase 4 — Marbles

**任务**：

- 实现圆球状态、重力、拖动/抛掷、墙壁碰撞、球球碰撞、重叠分离、Reset。
- 加 `Gravity` 可用开关。
- 单测覆盖守恒的合理范围（允许阻尼损失）、速度上限、重叠处理、撞墙反弹。

**验收**：多球互撞不穿透、不无上限加速；可反复拖动/抛掷；50 次快速交互正常。

### Phase 5 — 声音、触觉、设置、生命周期

**任务**：

- 生成并整合原创短音效，接入 `SoundPool`，正确释放资源。
- 接入可关闭/开启的触觉反馈；碰撞频率限流。
- 用 DataStore 持久化 3 个设置项；重启后值不丢。
- 实现 `Reduce motion`。
- 后台/锁屏/返回首页/切换实验时停止或暂停资源；恢复时没有突然大跳帧。

**验收**：设置开关每一项实测有效；断开音频设备或在不支持触觉的设备上不崩溃；后台往返 10 次正常。

### Phase 6 — UI/UX 打磨

**任务**：

- 统一插图比例、背景、卡片、字号、间距、图标、动画曲线。
- 测试不同尺寸、Display font scale、导航方式、深色系统栏、屏幕切换。
- 修复截断、溢出、错位、卡顿、低对比度、无说明按钮。
- 实现原创 Adaptive Icon 和 Splash 视觉（保持简单）。

**验收**：第 6.4 节 6 张截图齐全；对比前一轮视觉问题已修复；没有默认模板残留文本或图标。

### Phase 7 — 测试、性能和发布准备

**任务**：

- 完整执行第 11 节测试矩阵，记录每个命令退出码与测试输出路径。
- 真机/模拟器进行手势和稳定性冒烟；跑 `adb logcat` 检查异常。
- 分析真实帧耗时与卡顿，优先修复明显 jank；按设备条件执行 Macrobenchmark（第 12 节）。
- 执行 `assembleDebug`、`testDebugUnitTest`、`lintDebug`、`connectedDebugAndroidTest`（有设备）；另验证 release 构建配置。
- 整理交付清单、截图、`TEST-REPORT` 和 `KNOWN-ISSUES`，确认最终状态。

**完成条件**：满足第 0.2 节全部条件（没有设备时不能宣称全部完成，只能交付构建通过的阶段成果与待验证项）。

---

## 10. 物理模型的最低单测清单

所有模拟规则应尽量放在**不依赖 Android SDK 的纯 Kotlin 类**，方便 JVM 测试。

### 10.1 通用

- `dt = 0` 时不会不合理地变化；负 `dt` 必须拒绝或归零，不得倒退到爆炸。
- 很大 `dt` 被限制，不产生 NaN/Inf。
- 零尺寸画布不会除零/越界。
- 连续运行几万次 `step` 后，粒子数保持固定、位置/速度为有限值。
- 相同 seed + 相同操作序列 → 结果可复现（允许浮点容差）。
- Reset 清除所有临时手势捕获、速度、碰撞残留。
- Pause 时重复 `step` 不改变模拟状态（可以从控制器层面实现）。

### 10.2 Jelly

- 按压附近轮廓变化大于远端轮廓。
- 释放后能量衰减、形状逐步接近静止。
- 位移不超过定义的安全范围；形状封闭、不出现无穷坐标。

### 10.3 Magnet Dust

- 触摸时粒子朝吸引点移动的趋势可被测试（比较一小组无扰动测试粒子）。
- 粒子数量在交互、重置后不变化。
- 吸引点刚好等于粒子坐标不会除零。
- 高速/长时间运行不越界（或明确采用 wrap）、不产生超限速度。

### 10.4 Marbles

- 单球撞墙反弹方向正确、速率不过度上升。
- 两球正面碰撞不会获得大于合理初始值的动能（考虑离散误差与碰撞恢复系数）。
- 两球同坐标起始不会除零或永远重叠。
- 球在接近边界被拖放后仍位于有效区域。
- Gravity 切换和 Reset 结果符合状态定义。

---

## 11. 测试矩阵与可执行验收

### 11.1 Compose UI 自动化用例（最低要求）

建议稳定 `testTag`：

```text
home_screen
home_jelly_card
home_dust_card
home_marbles_card
settings_button
settings_sheet
setting_sound
setting_haptic
setting_reduce_motion
play_screen
play_back
play_pause
play_reset
play_canvas
marbles_gravity_toggle
```

必须实现用例：

1. 首页显示三个可点击卡片；它们的文本/语义符合预期。
2. 点 Jelly → 出现 Jelly 页面 → 点 Back → 返回首页。
3. 点 Magnet Dust → 出现实验页面 → Reset 有效、不崩溃。
4. 点 Marbles → Gravity 状态切换 → Reset → 返回首页。
5. Settings 打开、设置三项开关、关闭、重新打开，值保持；杀进程持久化可用独立 instrumentation 测试或手工测试验证。
6. Pause 文案切换成 Resume，再切回来；暂停期间模型不前进。
7. 画布手势 smoke test：至少一次 down/move/up；由于 Canvas 物理结果不一定直接有无障碍节点，可用注入事件/状态快照验证，不要依赖逐像素颜色断言。
8. 系统返回键/手势返回正确。

注意 Compose 2026 新测试 API 有 v2 迁移建议；如果现有兼容环境使用 v1 测试工厂，先保证可运行，再按官方迁移。测试不应使用实际时间 `Thread.sleep()` 等待动画，优先 Compose test clock / idle 机制。

### 11.2 设备覆盖（有条件时）

| 配置 | 测试目标 | 最低结果 |
|---|---|---|
| Android API 36 或 37 模拟器，约 1080×2400 | 当前系统、手势导航 | 安装/操作/截图/无 Crash |
| Android API 26 或可用的较低版本 | minSdk 兼容 | 安装、启动、基本玩法正常 |
| 360dp 级窄屏 + 字体放大到 1.3–1.5 倍 | 布局适配 | 文字不遮挡按钮，功能可操作 |
| 一台真实 Android 手机（若已连接） | 触觉、音效、真实手势与性能 | 核心交互真实可用 |
| 三键导航（若可配置） | 系统栏 Insets | 不遮挡底部按钮 |

如没有真机或第二个模拟器，明确记录覆盖缺口，不虚构设备测试。

### 11.3 必须执行的命令与证据

| 检查项 | 命令/方法 | 证据 |
|---|---|---|
| 构建 | `:app:assembleDebug` | BUILD SUCCESSFUL、APK 路径 |
| JVM 单元测试 | `:app:testDebugUnitTest` | 测试数量、HTML/XML 路径、失败数 0 |
| 静态检查 | `:app:lintDebug` | lint 结果，无未处理严重错误 |
| UI 自动化 | `:app:connectedDebugAndroidTest` | 设备型号/Android 版本/通过数 |
| 应用启动 | `adb install` + `monkey` | 设备/启动结果 |
| 稳定性 | 每个玩具交互 5 分钟 + 后台往返 | 操作记录，logcat 中无相关 Crash/ANR |
| 设置持久化 | 修改开关→强制结束→重启 | 三项开关实测结果 |
| 视觉 | 6 张截图与检查说明 | `docs/screenshots/` 文件 |
| Release 配置 | `:app:assembleRelease` 或 `:app:bundleRelease` | 构建是否成功、签名状态、产物路径 |

**特别说明**：`assembleRelease` 产物能生成不代表可以直接发布；上架 Play 需要正确签名、AAB、商店素材、隐私和内容信息、政策核对等。**不能擅自上传或发布**。

---

## 12. 性能、功耗和稳定性

### 12.1 目标值（目标，不是已经实现的结果）

- 目标机上交互优先保持约 **60 FPS**，少卡顿；在 60Hz 设备上单帧时长约 **16.7ms** 是理想预算。
- 测试时记录**设备型号、系统版本、刷新率、构建类型**；高刷屏与模拟器数据不能简单套用 60Hz 指标。
- 除非发现实测问题，不要提前过度优化。
- 在交互高频阶段不创建无限 coroutine、SoundPool 播放流、particle 列表或大型 Bitmap。
- 在非当前实验、后台、暂停时应停止帧更新，避免空耗 CPU/电量。
- 15 分钟连续玩耍不 Crash/ANR；切换实验 30 次不产生明显资源持续增长。

### 12.2 建议性能验证流程

1. 先用 Profile/Release 类构建测量，不用 debug 模式的结果冒充最终性能。
2. 观察 Android Studio Profiler / System Trace / JankStats 等真实工具；记录明显掉帧时的具体玩法和操作。
3. 若工具链允许，创建 `:benchmark` 模块使用 **Macrobenchmark** 测启动、切屏、连续手势场景；保留 JSON / trace。不要为做漂亮数字用无意义空页面基准。
4. 第一次测量后才做优化；优化前后比较相同设备、构建类型和操作脚本。
5. 若无性能测量工具，写出**未测原因**及后续执行说明，不填虚假 FPS/内存数据。

参考：https://developer.android.com/topic/performance/benchmarking/macrobenchmark-overview

---

## 13. 错误处理与调试策略（Agent 必须遵守）

### 13.1 按类别排错

| 错误类别 | 优先检查 | 禁止做法 |
|---|---|---|
| Gradle 依赖解析失败 | 网络、仓库配置、代理、SDK 与插件兼容、依赖版本 | 盲目升级全项目或禁用 TLS 校验 |
| 编译错误 | 第一条真实错误、导入/插件/类型签名、当前官方 API | 批量删代码绕过错误 |
| 运行 Crash | 完整异常堆栈、对应操作、重现步骤 | `catch(Exception){}` 然后继续 |
| UI 交互失效 | Pointer 消费、坐标系、手势取消、遮挡层、生命周期 | 用假动画代替真实手势 |
| 动画跳帧 | 主线程工作、dt 过大、后台时间、每帧分配、重复重组 | 无证据增加线程或乱加 `delay` |
| 物理爆炸 | 除零、NaN/Inf、碰撞重叠、速度无界、力过大 | 简单隐藏损坏的球/粒子 |
| 音效异常 | SoundPool 加载/释放、重复播放、资源无效、音量 | 每帧触发播放 |
| 无设备 | `adb devices`、驱动/权限、模拟器可用性 | 在报告中写已设备验证 |

### 13.2 同一个问题最多做三轮无证据尝试

执行循环：**记录现象 → 采集日志 → 提出一个根因假设 → 做最小修改 → 重跑同一验证**。

如果三次修改都失败，则停下随机试错：

1. 写 `docs/KNOWN-ISSUES.md`：完整错误、环境、复现命令、3 次尝试和结果。
2. 回退至上一个构建成功状态，优先恢复主干可运行。
3. 查询当前版本官方文档或通过可用网络搜索；若不可联网，标记受限。
4. 在不破坏 P0 的前提下更换**最小范围**实现方式，并补针对性测试。
5. 仍阻塞时，在最终报告列明具体人工介入动作，不要说“已完成”。

### 13.3 日志规范

- 所有失败保留核心 `stderr`、命令、退出码、环境版本，不要求粘贴整个数百 MB Gradle 日志。
- 不对每一帧打印日志（否则影响性能）。
- 可在 debug 构建输出统计信息，但发布版不应包含敏感调试内容。
- 修 bug 后补一个能防止同类问题重现的测试（若可自动化）。

---

## 14. Git、CI 与质量门禁

### 14.1 Git 提交建议

```text
chore: bootstrap Android project
feat: implement TouchLab home and navigation
feat: add Jelly physics toy
feat: add Magnet Dust physics toy
feat: add Marbles physics toy
feat: add settings and feedback
style: polish TouchLab UI
 test: add coverage and final verification
```

实际提交去掉无意义空格；若当前文件夹不是用户许可的 Git 仓库，不要擅自往远程 push。`.gitignore` 排除 `.gradle/`、`**/build/`、`local.properties`、IDE 临时文件、密钥、Keystore；**不要忽略 Gradle Wrapper JAR**。

### 14.2 CI 最低要求

若仓库配置 GitHub Actions，可以建 `.github/workflows/android-ci.yml`，在干净 runner 中：

1. checkout；设置 JDK 17 和 Android SDK（如 runner 未提供）。
2. 验证 Gradle Wrapper 来源，启用可控缓存。
3. 运行 `:app:testDebugUnitTest :app:lintDebug :app:assembleDebug`。
4. 失败时 job 必须失败，不能 `continue-on-error: true`。
5. 如需 `connectedDebugAndroidTest`，单独配置 Android Emulator runner/job，不能把无设备造成的 skip 算作通过。
6. 允许将 debug APK/测试报告作为短期 CI artifact（避免上传密钥、个人数据）。

MVP 没有 GitHub 远程时，CI 不是阻塞条件，但本地门禁是必须的。

---

## 15. 最终报告与交付格式

### 15.1 `docs/PROGRESS.md` 每阶段模板

```markdown
## Phase N — 名称
- 状态：DONE / BLOCKED / IN PROGRESS
- 实现内容：...
- 修改文件：...
- 验证命令与退出码：...
- 证据：测试结果路径 / 截图路径 / 日志摘要
- 已知问题：...
- 下一步：...
```

### 15.2 `docs/TEST-REPORT.md` 最终模板

```markdown
# TouchLab 测试报告
- 测试日期：
- 代码 commit：
- OS / JDK / Gradle / AGP / Compose BOM：
- minSdk / targetSdk / compileSdk：
- 测试设备：设备型号、API、屏幕尺寸、刷新率

## 构建和静态检查
| 检查项 | 命令 | 结果 | 证据路径 |

## 自动化测试
| 类别 | 用例数 | 通过 | 失败 | 未运行与原因 |

## 手工验收
| 场景 | 操作步骤 | 预期 | 实际 | PASS/FAIL/NOT RUN |

## 性能与稳定性
- 构建类型：
- 测量方式：
- 观测值（若有）：
- 无法测量的项目：

## 截图清单
- ...

## 缺陷与风险
- 阻塞项：
- 非阻塞项：

## 最终判定
DONE / PARTIAL / BLOCKED（只选其一，并说明依据）
```

### 15.3 最终向用户汇报（必须精确）

按照以下顺序写：

1. **实际状态**：DONE / PARTIAL / BLOCKED，不得虚报。
2. **已实现的玩法**：Jelly / Magnet Dust / Marbles 分别说明真实效果。
3. **运行命令与产物路径**：确切 APK 位置；怎样 `adb install`，怎样启动。
4. **测试结果**：每个 Gradle 命令 PASS / FAIL / NOT RUN，注明设备与用例数。
5. **真实截图路径**：列出可打开的图片路径；没截图就写没截图。
6. **性能/稳定性事实**：有数据写数据；没有就如实记录。
7. **遗留问题**：按严重程度列出，提出下一项合理工作。

**只有代码存在，不等于完整交付。**

---

## 16. Agent 可直接复制的开始指令

> 以下指令可作为 Pi / 其他本地 Agent 的用户消息；它必须结合这份文档全文执行。

```text
你现在负责从零完成 TouchLab Android App。

请将当前工作目录中的《TouchLab-Android-Agent-Development-Guide.md》作为唯一主要执行规范，先全文阅读，尤其是第 0 节、3 节、8–15 节。

执行方式：
1. 先检查当前目录/Git 状态与 Android 开发环境，不覆盖已有用户文件。
2. 严格按 Phase 0 → Phase 7 依次实施。每阶段先实现，再运行构建/测试，然后更新 docs/PROGRESS.md。失败不得假装通过。
3. MVP 必须包含可真实操作的 Jelly、Magnet Dust、Marbles 三个玩具，原生 Kotlin + Jetpack Compose，实现统一精致 UI。
4. 优先解决环境和编译问题，不要一开始搭建复杂架构。
5. 对所有物理计算添加纯 Kotlin 单测。对首页导航、设置、暂停重置添加 Compose UI 测试。
6. 能使用 adb/模拟器/真机时必须真实安装、运行、截图和排查 Logcat；不能运行时必须说明原因。
7. 完成交付时提供 APK 准确路径、各项测试的真实结果、截图、已知问题、提交记录；未通过项目不得声称已完成。
8. 非必要不要向我提问；你有权限对产品细节做符合文档的合理决定。不要私自联网传输数据、支付费用、申请危险权限或发布到商店。

现在先执行 Phase 0；在每阶段完成后继续推进，直到全部完成或遇到无法自行解除的真实阻塞。
```

---

## 17. 官方参考与查询顺序

当文档示例和本地 API 发生冲突，先查当前使用版本的官方文档，不从搜索摘要拼接不兼容代码。

1. **Compose 配置与 BOM**：https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler
2. **AGP 与 Gradle 兼容性**：https://developer.android.com/build/releases/agp-9-2-0-release-notes
3. **AGP 9.x 内置 Kotlin**：https://developer.android.com/build/migrate-to-built-in-kotlin
4. **Canvas / 绘图**：https://developer.android.com/develop/ui/compose/graphics/draw/overview
5. **指针与手势**：https://developer.android.com/develop/ui/compose/touch-input/pointer-input
6. **拖动交互**：https://developer.android.com/develop/ui/compose/touch-input/pointer-input/drag-composables
7. **动画测试**：https://developer.android.com/develop/ui/compose/animation/testing
8. **Compose UI 自动化测试**：https://developer.android.com/develop/ui/compose/testing
9. **测试 API v2 迁移**：https://developer.android.com/develop/ui/compose/testing/migrate-v2
10. **Edge-to-edge 与 Insets**：https://developer.android.com/develop/ui/compose/system/setup-e2e
11. **无障碍与触摸区域**：https://developer.android.com/guide/topics/ui/accessibility/apps
12. **性能 Macrobenchmark**：https://developer.android.com/topic/performance/benchmarking/macrobenchmark-overview
13. **Google Play 2026 target API 要求**：https://support.google.com/googleplay/android-developer/answer/11926878

---

## 18. 项目范围之外的未来思路（仅 BACKLOG，不得先做）

- 真实设备倾斜控制弹珠；声音与震动更细腻的触感配置。
- 把自己调出来的软糖/粒子效果保存成主题与壁纸。
- 录制 5 秒互动短视频分享到社交平台。
- 低功耗 live wallpaper 与简易 Android 桌面小组件。
- 新玩具：磁流体、沙盒重力、橡皮筋网络、旋转轨道、重力摆。
- 完整平板设计、动态主题、多个语言、商店展示素材。

---

**最终原则**：TouchLab 不是给 Agent 展示代码量的项目，而是给真实用户随手玩、觉得舒服的 Android 小作品。少做无关功能，多验证触觉、性能、可理解性和完成度。所有结论必须来自**实际代码与可复查的测试**。
