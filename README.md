# Advanced Wish - 高级许愿 (抽奖)

## 一个简单且高度可定制的许愿 (抽奖) 插件!

> Mcbbs
- https://www.mcbbs.net/x.html
---
> bStats
- https://bstats.org/plugin/bukkit/AdvancedWish/16990
---
> Gitee
- https://gitee.com/A2000000/advanced-wish
---
> 此插件的配置文件较多，但是非常简单，您可以通过查看 Wiki 来快速了解插件内容 -> https://gitee.com/A2000000/advanced-wish/wikis#
---
## 指令
> 主要指令 - 普通用户
- /aw list - 查看许愿池列表.
- /aw makeWish <许愿池> - 对规定的许愿池进行许愿.
- /aw amount <许愿池> - 获取指定许愿池的许愿次数.
- /aw guaranteed <许愿池> - 获取指定许愿池的保底率.
---
> 主要指令 - 管理员
- /aw list - 查看许愿池列表.
- /aw makeWish <许愿池> - 对规定的许愿池进行许愿.
- /aw makeWish <许愿池> <玩家> - 使指定的玩家对规定的许愿池进行许愿.
- /aw makeWishForce <许愿池> - 强制对规定的许愿池进行许愿，强制不会触发任何检查.
- /aw makeWishForce <许愿池> <玩家> - 强制使指定的玩家对规定的许愿池进行许愿，强制不会触发任何检查.
- /aw amount <许愿池> - 获取指定许愿池的许愿次数.
- /aw guaranteed <许愿池> - 获取指定许愿池的保底率.
- /aw getAmount <许愿池> <玩家> - 获取指定玩家指定许愿池的许愿次数.
- /aw getGuaranteed <许愿池> <玩家> - 获取指定玩家指定许愿池的保底率.
- /aw setAmount <许愿池> <玩家> <次数> - 设置指定玩家指定许愿池的许愿次数.
- /aw setGuaranteed <许愿池> <玩家> <保底率> - 设置指定玩家指定许愿池的保底率.
- /aw reload - 重载插件.
---
> 控制台指令
- /awc list - 查看许愿池列表.
- /awc makeWish <许愿池> <玩家> - 使指定的玩家对规定的许愿池进行许愿.
- /awc makeWishForce <许愿池> <玩家> - 强制使指定的玩家对规定的许愿池进行许愿，强制不会触发任何检查.
- /awc getAmount <许愿池> <玩家> - 获取指定玩家指定许愿池的许愿次数.
- /awc getGuaranteed <许愿池> <玩家> - 获取指定玩家指定许愿池的保底率.
- /awc setAmount <许愿池> <玩家> <次数> - 设置指定玩家指定许愿池的许愿次数.
- /awc setGuaranteed <许愿池> <玩家> <保底率> - 设置指定玩家指定许愿池的保底率.
- /awc reload - 重载插件.
---
## Placeholder API
- aw_amount_许愿池名 - 获取当前玩家指定许愿池的许愿次数.
- aw_guaranteed_许愿池名 - 获取当前玩家指定许愿池的保底率.
- aw_amount_许愿池名_玩家名 - 获取指定玩家指定许愿池的许愿次数.
- aw_guaranteed_许愿池名_玩家名 - 获取指定玩家指定许愿池的保底率.
---
## 注意事项
> 缺点
- 此插件不支持多次连抽，无论如何，多次连抽功能永远不会实现，或正在计划中，因为这是极其不稳定的。
- 如果您需要抽奖 Gui，那么您需要使用第三方的菜单插件制作，此插件不会自带菜单功能 (这是臃肿的，用户需要多学一种 Gui 的写法) 。
---
> Redis 跨服
- 如果您想要了解 Redis 在此插件的具体作用，那么您可以去看默认许愿池配置文件内的 WAIT-SET 项注释，那里写的非常清除。
---
## 兼容性
> 目前支持的经济插件
- Vault / PlayerPoints
---