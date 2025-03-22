# XaruChallenges - Hardcore SMP Challenge Plugin

A Minecraft Spigot/Paper plugin (1.21.4) that adds 13+ unique, punishing challenges to enhance your SMP experience. Perfect for creating intense gameplay scenarios and custom survival modes.


## 🌟 Features
- **13 Unique Challenges** with varying difficulty levels
- **Player-Specific Challenge Management** with individual tracking
- **Automatic Punishment System** for rule-breakers
- **Lose Counter Display** for tracking failures
- **Easy Commands** with player-specific operations
- **Compatible** with most SMP setups

## 📦 Installation
1. Download the latest `.jar` from Releases
2. Place in your server's `plugins/` folder
3. Restart/reload your server

## 🎮 Available Challenges

### 🔥 Core Challenges
| Challenge        | Description                                                                 |
|------------------|-----------------------------------------------------------------------------|
| **OreDeath**     | Instant death from forbidden ores (mining/having in inventory)              |
| **Piranha**      | Can only breathe underwater - air damages, fish/kelp prohibited             |
| **Mole**         | Restricted movement (only walk on dirt/coarse dirt/sand)                    |
| **Vegan**        | Divine punishment for animal products/attacking entities                    |
| **Vulture**      | Only eat rotten flesh - blocks other foods                                  |
| **Chinchilla**   | Dies in water/rain, can only eat fish                                       |
| **CaseOh**       | No sprinting + permanent Slowness                                           |
| **Daredevil**    | Permanent Blindness + Speed boost                                           |
| **Vampire**      | Sunlight burns, night/shadow only. Feed on players/villagers for survival   |
| **HollowBones**  | 6 HP max + complete inventory wipe on death                                 |
| **NakedAndAfraid** | No armor allowed                                                          |
| **Elf**          | Restricted to bows/crossbows only                                           |
| **Prasinophobia**| (WIP) Cannot touch green blocks                                             |

## ⚙️ Commands
Main command: `/xaruchallenges` or `/xc`
/xc help - Show command list
/xc list - Display available challenges
/xc add <challenge> <player> - Assign challenge to player
/xc remove <challenge> <player> - Remove challenge from player
/xc challenges <player> - Show active challenges
/xc losecounter <enable|disable> - Toggle death counter display
/xc reload - Reload plugin configuration


## 🔐 Permissions
xaruchallenges.use - Basic command access (default: true)

Allows: help, list, challenges
xaruchallenges.admin - Full administrative access (default: op)

Allows: add, remove, reload, losecounter


## 🤝 Contributing
1. Fork the repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

> **Warning**  
> This plugin creates intense gameplay experiences. Test challenges before deploying on production servers!

> **Note**  
> The losecounter feature tracks and displays player deaths caused by challenge failures (MAYBE NOT WORKING BECAUSE OF WIP)
