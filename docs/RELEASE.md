# TouchLab — GitHub 发布指南（CI Release）

目的：把本地工程发布为 GitHub 仓库 + 第一次 Release，用 GitHub Actions
自动构建并附带 APK 安装包。

## 1. 创建 GitHub 仓库

在 github.com 新建仓库（Public 或 Private 均可）：

- 仓库名：`TouchLab`
- 建议 Description（英文）：
  `A pocket tactile physics laboratory — squish jelly, gather magnet dust and flick marbles, with soft sound and haptic feedback. Kotlin + Jetpack Compose.`
- 可选 Description（中文）：`一个装在口袋里的触觉互动实验室：捏软糖、聚拢磁力尘埃、拨动弹珠，带轻柔音效与触觉反馈。离线、无广告。`
- Topics 建议：`android` `jetpack-compose` `kotlin` `physics` `toy` `offline`
- **不要**勾选 "Add a README / .gitignore / license"（本工程已自带，避免冲突）。

## 2. 关联本地仓库并推送

在仓库网页拿到远程地址后（Git Bash / PowerShell）：

```bash
cd /c/Users/luxia/project/private/TouchLab
git remote add origin https://github.com/<你的用户名>/TouchLab.git
git push -u origin main
```

推送时按提示用浏览器/Token 完成 GitHub 认证（首次会要求授权）。推送成功后，
`.github/workflows/android-ci.yml` 会自动在每次 push/PR 运行：单元测试 + lint + 构建。

## 3. 配置签名 Secret（可选但推荐）

Release 默认构建**未签名** APK（`app-release-unsigned.apk`）。要产出签名 APK：

1. 生成 keystore：
   ```bash
   KEYS_PASS='这里填一个强密码' bash tools/create_release_keystore.sh
   ```
   脚本会在仓库根目录生成 `touchlab-release.keystore` 并输出 base64 内容
   （该 keystore 与 `*.b64` 已被 .gitignore 忽略，**切勿提交**）。
2. 打开仓库 → Settings → Secrets and variables → Actions → New repository secret，
   添加四个：
   - `ANDROID_KEYSTORE_BASE64`：上一步输出的 base64 字符串
   - `ANDROID_KEYSTORE_PASSWORD`：KEYS_PASS
   - `ANDROID_KEY_ALIAS`：`touchlab`
   - `ANDROID_KEY_PASSWORD`：KEYS_PASS

没有 Secret 时工作流也会正常运行，只是产出 unsigned APK（可侧载安装，不能上架）。

## 4. 打 tag 触发 Release

版本号已在 `app/build.gradle.kts` 中为 `versionName = "0.1.0"`，tag 与之对应：

```bash
git tag v0.1.0
git push origin v0.1.0
```

推送 tag 后，`.github/workflows/release.yml` 会自动：
- 运行单元测试；
- 构建 `app-release.apk`（有 Secret 时签名）+ `app-debug.apk`；
- 创建名为 `TouchLab v0.1.0` 的 GitHub Release，并把 APK 作为 Release 资产附上。

也可以在 Actions 页面手动触发（workflow_dispatch）重新生成。

## 5. 验证

- Actions 页查看两个工作流是否绿色。
- Releases 页确认 `TouchLab v0.1.0` 下已挂 `app-release*.apk` / `app-debug.apk`。
- 下载 APK 侧载到手机验证安装与运行（启用"允许安装未知来源"）。

## 关于上架 Google Play

GitHub Release 的 APK 用于侧载与分发；上架 Play 还需：
- 生成/保留独立的 Play 上架 keystore（**必须留存**，丢失无法更新应用）；
- AAB（`./gradlew :app:bundleRelease`）与 Play Console 建立开发者账号、隐私政策、
  内容与政策核对；
- 确认 `dev.touchlab.app` 包名唯一性（可先在本仓库持续开发，上架前再定正式包名）。
这些不在 CI 范围内，后续需要时再单独配置。