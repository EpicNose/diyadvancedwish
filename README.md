# Advanced Wish - 高级许愿 (抽奖)

## 一个简单且高度可定制的许愿 (抽奖) 插件!

> Mcbbs
- https://www.mcbbs.net/thread-1397853-1-1.html
---
> bStats
- https://bstats.org/plugin/bukkit/AdvancedWish/16990
---
> Gitee
- https://gitee.com/A2000000/advanced-wish
---
> 此插件的配置文件较多，但是非常简单，您可以通过查看 Wiki 来快速了解插件内容 -> https://gitee.com/A2000000/advanced-wish/wikis#
---
## 开源协议
- 此项目使用 WTFPL (Do What The Fuck You Want To Public License) 开源协议，中文翻译: 你他妈的想干嘛就干嘛公共许可证。 字面意思，您可以使用 Advanced Wish 做任何事情，不受限制。
---
## 指令介绍
> 主要指令 - 普通用户
- /aw list - 查看许愿池列表.
- /aw makeWish <许愿池> - 对规定的许愿池进行许愿.
- /aw amount <许愿池> - 获取指定许愿池的许愿次数.
- /aw guaranteed <许愿池> - 获取指定许愿池的保底率.
- /aw limitAmount <许愿池> - 获取指定许愿池的限制许愿次数.
---
> 主要指令 - 管理员
- /aw list - 查看许愿池列表.
- /aw makeWish <许愿池> - 对规定的许愿池进行许愿.
- /aw makeWish <许愿池> <玩家> - 使指定的玩家对规定的许愿池进行许愿.
- /aw makeWishForce <许愿池> - 强制对规定的许愿池进行许愿，强制不会触发任何检查.
- /aw makeWishForce <许愿池> <玩家> - 强制使指定的玩家对规定的许愿池进行许愿，强制不会触发任何检查.
- /aw amount <许愿池> - 获取指定许愿池的许愿次数.
- /aw guaranteed <许愿池> - 获取指定许愿池的保底率.
- /aw limitAmount <许愿池> - 获取指定许愿池的限制许愿次数.
- /aw getAmount <许愿池> <玩家> - 获取指定玩家指定许愿池的许愿次数.
- /aw getGuaranteed <许愿池> <玩家> - 获取指定玩家指定许愿池的保底率.
- /aw getLimitAmount <许愿池> <玩家> - 获取指定玩家指定许愿池的限制许愿次数.
- /aw setAmount <许愿池> <玩家> <次数> - 设置指定玩家指定许愿池的许愿次数.
- /aw setGuaranteed <许愿池> <玩家> <保底率> - 设置指定玩家指定许愿池的保底率.
- /aw setLimitAmount <许愿池> <玩家> <次数> - 设置指定玩家指定许愿池的限制许愿次数.
- /aw resetLimitAmount <许愿池> - 重置所有玩家指定许愿池的限制许愿次数.
- /aw queryWish <许愿池> <玩家> <开始条数> <结束条数> - 查询指定玩家指定许愿池的指定条数日志.
- /aw reload - 重载插件.
---
> 控制台指令
- /awc list - 查看许愿池列表.
- /awc makeWish <许愿池> <玩家> - 使指定的玩家对规定的许愿池进行许愿.
- /awc makeWishForce <许愿池> <玩家> - 强制使指定的玩家对规定的许愿池进行许愿，强制不会触发任何检查.
- /awc getAmount <许愿池> <玩家> - 获取指定玩家指定许愿池的许愿次数.
- /awc getGuaranteed <许愿池> <玩家> - 获取指定玩家指定许愿池的保底率.
- /awc getLimitAmount <许愿池> <玩家> - 获取指定玩家指定许愿池的限制许愿次数.
- /awc setAmount <许愿池> <玩家> <次数> - 设置指定玩家指定许愿池的许愿次数.
- /awc setGuaranteed <许愿池> <玩家> <保底率> - 设置指定玩家指定许愿池的保底率.
- /awc setLimitAmount <许愿池> <玩家> <次数> - 设置指定玩家指定许愿池的限制许愿次数.
- /awc resetLimitAmount <许愿池> - 重置所有玩家指定许愿池的限制许愿次数.
- /awc queryWish <许愿池> <玩家> <开始条数> <结束条数> - 查询指定玩家指定许愿池的指定条数日志.
- /awc reload - 重载插件.
---
## Placeholder API
- aw_amount_许愿池名 - 获取当前玩家指定许愿池的许愿次数.
- aw_guaranteed_许愿池名 - 获取当前玩家指定许愿池的保底率.
- aw_limit_amount_许愿池名 - 获取当前玩家指定许愿池的限制许愿次数.
- aw_amount_许愿池名_玩家名 - 获取指定玩家指定许愿池的许愿次数.
- aw_guaranteed_许愿池名_玩家名 - 获取指定玩家指定许愿池的保底率.
- aw_limit_amount_许愿池名_玩家名 - 获取指定玩家指定许愿池的限制许愿次数.
---
## 注意事项
> 注意
- 此插件在 0.0.3.0-SNAPSHOT 后已经支持连抽，如果您是低版本，那么您需要重新生成配置文件来保证连抽正常使用。
- 如果您需要许愿 Gui，那么您需要使用第三方的菜单插件制作，此插件不会自带菜单功能 (这是臃肿的，用户需要多学一种 Gui 的写法) 。
- 我们提供了 DeluxeMenu 的许愿预设，您可以参考此预设进行编写 -> [DeluxeMenu 配套预设](https://gitee.com/A2000000/advanced-wish/tree/main/DeluxeMenu%20%E9%85%8D%E5%A5%97%E8%8F%9C%E5%8D%95)
- 我们提供了 DragonCore 的许愿预设，您可以参考此预设对龙之核心进行兼容 -> [DragonCore 配套预设](https://gitee.com/A2000000/advanced-wish/tree/main/DragonCore%20%E9%85%8D%E5%A5%97%E7%95%8C%E9%9D%A2)
- 如果您想要提供更多的菜单预设，比如 TrMenu 那么您可以联系我或发送 Pull Request.
---
> Redis 跨服
- 如果您想要了解 Redis 在此插件的具体作用，那么您可以去看默认许愿池配置文件内的 WAIT-SET 项注释，那里写的非常清除。
---
## 兼容性
> 目前支持的经济插件
- Vault / PlayerPoints
---
> 目前支持的数据保存形式
- 目前 Advanced Wish 支持使用 [MongoDB](https://www.mongodb.com/) 来存储插件数据，如果不开启则使用 Json 来存储玩家数据。
---