# 惜的朋友版 · Android

在自己的手机上解析和保存抖音、B站作品。无需惜年服务器，也不需要电脑保持开机。

## 下载与安装

在本仓库 **Releases** 中下载最新版 APK，发送到 Android 手机上，用文件管理器安装。最低 Android 10。已有同名朋友版可直接覆盖升级；原版“惜年”是独立应用。

## 使用

1. 粘贴作品分享链接，或从系统分享菜单选择惜的朋友版。
2. 点解析，等待原作品打开；平台要求验证时，在内置页面手动完成。
3. 核对封面、标题和作者，选择清晰度或图片保存。
4. 下载期间保持 App 在前台。完成后仅短暂提示，资源按钮变为“已保存 · 打开”。

视频保存到 Movies/惜年，照片保存到 Pictures/惜年。右上角可切换白茶、晴空、暮紫、夜航四套皮肤。

## 当前能力与限制

- 视频、图文与实况片段；实况目前是图片和对应 MP4，不封装为手机原生实况照片。
- B站高清由设备直接下载并合并 AVC/AAC；清晰度受作品和本机账号权限影响，不保证所有作品都有1080P。
- 锁定原作品编号，发现跳到推荐视频时停止读取，提供重试原链接。
- 一次一个下载，可取消；单个输入文件上限1GB。尚不支持可靠后台下载、断点续传、批量主页或直播。
- 平台页面和限制会变化，不能保证每条链接均可解析；不绕过付费、私密或账号权限。

## 隐私

不使用惜年中转服务器、广告或分析 SDK。请求由手机直接发送给作品平台及媒体 CDN；平台网页自身可能连接其使用的其他服务。账号登录状态只保存于本 App 的本机 WebView，可在“使用说明与隐私”退出登录。不上传下载记录给开发者。

## 从源码构建（Windows）

准备 PowerShell 7、JDK 17、Android SDK Platform 35 和 Build-tools 35.0.0。
设置 JAVA_HOME 与 ANDROID_HOME 后运行：

```powershell
pwsh -File ./build.ps1
```

产物为 release/xinian-friends-0.2.3.apk。脚本首次为个人构建生成本地开发签名，保存在被忽略的 .signing 目录。自行构建的签名与官方 APK 不同，不能覆盖安装官方版；请自行保管密钥。
维护者可通过 -SigningKey 与 -SigningPasswordFile 指定外部签名文件。不要将签名密钥、密码、登录缓存或 Cookie 提交到仓库。

没有 Gradle 或第三方运行时依赖，构建使用 SDK 自带 aapt、D8、zipalign 和 apksigner。手机不需要安装 Node 或 FFmpeg。

## 测试与反馈

JS 读取测试：node tests/capture.test.mjs。
JVM 测试：使用 JDK、Android SDK android.jar 与 JSON-java 测试依赖编译 Media.java、Network.java、WorkTarget.java、tests/CoreTest.java，然后运行 cn.xinian.app.CoreTest。未传可选参数时只使用合成数据，不需要任何账号。

请在 Issues 中提供版本、安卓版本、平台、复现步骤和错误截图。不要提交账号密码、Cookie、令牌或私密作品。发布构建已通过编译和签名检查；不同手机的 WebView、验证和媒体合并仍需要真机测试。

第三方及图片说明见 THIRD_PARTY_NOTICES.md。
