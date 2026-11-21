
package com.clancoffertracker;

import com.google.inject.Provides;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PluginDescriptor(
		name = "Clan Coffer Tracker",
		description = "Tracks clan coffer deposits with optional Discord notifications",
		tags = {"clan", "coffer", "deposits"}
)
public class ClanCofferTrackerPlugin extends Plugin
{
	private static final Logger log =
			LoggerFactory.getLogger(ClanCofferTrackerPlugin.class);

	private ExecutorService reportExecutor;

	@Inject
	private ClanCofferTrackerConfig config;

	@Provides
	ClanCofferTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ClanCofferTrackerConfig.class);
	}

	@Override
	protected void startUp()
	{
		reportExecutor = Executors.newSingleThreadExecutor();
	}

	@Override
	protected void shutDown()
	{
		if (reportExecutor != null)
		{
			reportExecutor.shutdownNow();
			reportExecutor = null;
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (!config.enableTracking() || !config.enableDiscord())
		{
			return;
		}

		ChatMessageType type = event.getType();

		if (type != ChatMessageType.CLAN_MESSAGE
				&& type != ChatMessageType.CLAN_GIM_MESSAGE
				&& type != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}

		String message = Text.removeTags(event.getMessage()).trim();

		if (!message.toLowerCase(Locale.ROOT).contains("coffer"))
		{
			return;
		}

		CofferDepositParser.Deposit deposit =
				CofferDepositParser.parse(message);

		if (deposit == null)
		{
			return;
		}

		String webhook = config.discordWebhook();

		if (webhook == null || webhook.trim().isEmpty())
		{
			return;
		}

		webhook = webhook.trim();

		if (!isDiscordWebhook(webhook))
		{
			log.warn("Discord webhook URL is invalid");
			return;
		}

		log.info("Clan coffer deposit detected: {} GP", deposit.amount);

		sendDiscordNotification(webhook, deposit.player, deposit.amount);
	}

	private void sendDiscordNotification(
			String webhook,
			String player,
			long amount
	)
	{
		ExecutorService executor = reportExecutor;

		if (executor == null || executor.isShutdown())
		{
			return;
		}

		String message = String.format(
				"Clan Coffer Deposit!\nPlayer: %s\nAmount: %,d GP",
				player,
				amount
		);

		String json =
				"{\"content\":\"" + escapeJson(message)
						+ "\",\"allowed_mentions\":{\"parse\":[]}}";

		try
		{
			executor.submit(() -> postDiscord(webhook, json));
		}
		catch (java.util.concurrent.RejectedExecutionException ex)
		{
			log.debug("Discord notification skipped during shutdown");
		}
	}

	private static boolean isDiscordWebhook(String webhook)
	{
		try
		{
			URL url = new URL(webhook);
			String host = url.getHost().toLowerCase(Locale.ROOT);
			String path = url.getPath();

			return "https".equalsIgnoreCase(url.getProtocol())
					&& (host.equals("discord.com")
					|| host.equals("discordapp.com"))
					&& url.getPort() == -1
					&& url.getUserInfo() == null
					&& url.getQuery() == null
					&& url.getRef() == null
					&& path.matches("/api/webhooks/[0-9]+/[^/]+/?");
		}
		catch (Exception ex)
		{
			return false;
		}
	}

	private static String escapeJson(String value)
	{
		StringBuilder escaped = new StringBuilder();

		for (int i = 0; i < value.length(); i++)
		{
			char c = value.charAt(i);

			switch (c)
			{
				case '"':
					escaped.append("\\\"");
					break;
				case '\\':
					escaped.append("\\\\");
					break;
				case '\n':
					escaped.append("\\n");
					break;
				case '\r':
					escaped.append("\\r");
					break;
				case '\t':
					escaped.append("\\t");
					break;
				default:
					if (c < 0x20)
					{
						escaped.append(String.format("\\u%04x", (int) c));
					}
					else
					{
						escaped.append(c);
					}
					break;
			}
		}

		return escaped.toString();
	}

	private void postDiscord(String webhook, String json)
	{
		HttpURLConnection connection = null;

		try
		{
			connection = (HttpURLConnection)
					new URL(webhook).openConnection();

			connection.setInstanceFollowRedirects(false);
			connection.setRequestMethod("POST");
			connection.setRequestProperty(
					"Content-Type",
					"application/json"
			);
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);
			connection.setDoOutput(true);

			try (OutputStream os = connection.getOutputStream())
			{
				os.write(json.getBytes(StandardCharsets.UTF_8));
			}

			int status = connection.getResponseCode();

			if (status == 204 || status == 200)
			{
				log.info("Discord notification sent successfully");
			}
			else
			{
				log.warn("Discord returned HTTP {}", status);
			}
		}
		catch (Exception ex)
		{
			// Do not log exceptions containing secret webhook URLs.
			log.warn("Failed to send Discord notification");
		}
		finally
		{
			if (connection != null)
			{
				connection.disconnect();
			}
		}
	}
}
