
package com.clancoffertracker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;


@ConfigGroup("clancoffertracker")
public interface ClanCofferTrackerConfig extends Config
{
	@ConfigItem(
			keyName = "enableTracking",
			name = "Enable Deposit Tracking",
			description = "Detect clan coffer deposits"
	)
	default boolean enableTracking()
	{
		return true;
	}



	@ConfigItem(
			keyName = "clanId",
			name = "Clan ID",
			description = "Identifier for your clan's deposit records"
	)
	default String clanId()
	{
		return "";
	}


	@ConfigItem(
			keyName = "discordWebhook",
			name = "Discord Webhook URL",
			description = "Discord webhook for clan coffer deposit notifications",
			secret = true,
			position = 3
	)
	default String discordWebhook()
	{
		return "";
	}


	@ConfigItem(
			keyName = "enableDiscord",
			name = "Enable Discord Reporting",
			description = "Sends player names and coffer deposit amounts to the configured Discord webhook. Only enable this if you agree to share this information with Discord.",
			position = 4
	)
	default boolean enableDiscord()
	{
		return false;
	}



}
