# TouchLab — 已知问题与覆盖缺口

最后更新：2026-10-21

## 覆盖缺口

1. **已真机验证（2026-10-21，Xiaomi POCO F5 / Android 14 / 120Hz）**：
   三玩具交互、音效开关、设置持久化、后台/旋转/稳定性、UI 自动化 9/9、
   gfxinfo 帧统计（p50 14ms）。真实触觉体感与音效听感待用户主观确认。
2. **性能**：真机 `gfxinfo` 已记录；未创建 `:benchmark` Macrobenchmark 模块
   （已列入 backlog）。
3. **API 37 / minSdk 26 设备未实测**：`system-images;android-37.0;
   google_apis;x86_64` 已安装但未建 AVD 运行；minSdk 26 边界未实测。
4. **15 分钟连续游玩** 未完整执行（本次脚本交互约 5 分钟无 Crash/ANR）。

## 已知问题

1. **转动屏幕时实验重置**：配置变化会重建 Activity，物理状态按设计重置为
   安全初始态（README 已说明）。home 页面状态由 `rememberSaveable` 保留。
2. **Pause 覆盖层会拦截后续触控**：这是预期行为；点 Resume 后恢复。
3. **compose test API v1 弃用告警**：测试使用 v1 工厂 + 手动时钟推进，编译期
   有 deprecation 提示，已满足可运行前提；v2 迁移列入 backlog。
4. **首次进入某实验时 Hint 提示约 5 秒后自动消失**：简化为全局提示，未做
   "仅首次" 记忆（Guide 允许简化）。
5. **Marbles 默认开启重力**：进入后小球会在几秒内落底再弹跳至静止约 20 秒；
   属玩法选择，关闭重力即可漂浮。
6. **设置面板中关闭音效的瞬间仍会播一次点击音**（该次点击使用旧设置值），
   属可接受的最小体验瑕疵。

## 曾遇到并已解决的问题（防止复发）

- `local.properties` 反斜杠转义导致 `Invalid file path` → 改用
  `C\:/...`（lint 推荐格式）并已在环境文档记录。
- 物理循环使 Compose 测试永不 idle → 改为
  `mainClock.autoAdvance = false` + 手动 `advanceTimeBy`。
- frame 计数器在组合期读取导致每帧重组 → 改为仅在 Canvas 绘制期读取
  `State` 对象。
- Dust 吸引不够强/粒子散开 → 调大引力、调小旋涡，并加核排斥形成光环。
- Marbles 落底弹跳 20 秒才稳定 → 属阻尼参数选择，不足 5 秒时速度偏高，
  真机复核后再定。

## 真机自动化注意（MIUI，2026-10-21 实测确认）

1. **每次重装 APK 后需重新授权**：MIUI 重装后 UID 变化，「后台弹出界面」
   授权随之失效，测试进程拉起 App 会被拦截（`MIUILOG- Permission Denied
   Activity`）。需在「应用管理 → TouchLab → 权限管理 → 后台弹出界面 =
   允许」重新授权；授权后用 `am instrument` 直跑可反复执行（不重装即稳定）。
2. **玩具用例需关闭系统动画**：玩具用例用 Compose 手动帧时钟
   （`autoAdvance=false`），真机动画开启时 Espresso 等 idle 会超时挂起；
   测试前将开发者选项三个动画缩放临时设为 0，测后恢复。
3. 关闭「MIUI 优化」不能替代上述 per-app 授权。

## 环境特定限制

- OS-default JDK 为 8，所有 Android 工具链需显式使用 Zulu JDK 17；
  未修改系统级 PATH/安全设置。
- `ANDROID_HOME` 未设置于系统，SDK 通过 `local.properties` 指定
  （该文件已 git-ignore）。
- 本仓库为本地 Git 仓库，未向任何远程推送；未上传或发布任何 APK。