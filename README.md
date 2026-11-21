
# Clan Coffer Tracker

Clan Coffer Tracker is a RuneLite plugin for Old School RuneScape that detects clan coffer deposits and optionally sends notifications to a Discord channel.

## Features

- Detects clan coffer deposits announced in clan chat.
- Identifies the player who made the deposit.
- Tracks the amount of GP deposited in each detected announcement.
- Sends optional Discord webhook notifications.
- Allows you to enable or disable deposit tracking and Discord notifications.

## How to Use

1. Install Clan Coffer Tracker from the RuneLite Plugin Hub.
2. Open RuneLite's plugin configuration.
3. Search for "Clan Coffer Tracker".
4. Enable deposit tracking.
5. Join your clan in Old School RuneScape.
6. When a clan coffer deposit is announced, the plugin detects the deposit.

## Discord Notifications

To receive clan coffer deposit notifications in Discord:

1. Open your Discord server.
2. Select the channel where you want notifications.
3. Open Edit Channel > Integrations > Webhooks.
4. Create a webhook and copy its URL.
5. Open Clan Coffer Tracker's settings in RuneLite.
6. Enable Discord notifications.
7. Paste your webhook URL into the Discord Webhook setting.

When a supported deposit announcement appears, the plugin sends a message containing:

- The player's name
- The amount of GP deposited

## Important Information

- Clan Coffer Tracker detects deposit announcements while RuneLite is running and the plugin is enabled.
- Discord notifications require a valid Discord webhook URL.
- The plugin does not automatically retrieve historical clan coffer deposits.
- Keep your Discord webhook URL private.

## Support

If you encounter a bug or have a suggestion, please open an issue on GitHub:

https://github.com/ZedAfterliferp/clan-coffer-tracker/issues

## Disclaimer

Clan Coffer Tracker is a community-created RuneLite plugin and is not affiliated with Jagex.
