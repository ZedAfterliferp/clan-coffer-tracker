
package com.clancoffertracker;

import com.google.inject.Provides;
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
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
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

	private static final MediaType JSON =
			MediaType.parse("application/json; charset=utf-8");

	private ExecutorService reportExecutor;

	@Inject
	private ClanCofferTrackerConfig config;

	@Inject
	private OkHttpClient httpClient;

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

		final String webhookUrl = webhook;

		try
		{
			executor.submit(() -> postDiscord(webhookUrl, json));
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
			okhttp3.HttpUrl url = okhttp3.HttpUrl.parse(webhook);

			if (url == null)
			{
				return false;
			}

			String host = url.host().toLowerCase(Locale.ROOT);

			return url.isHttps()
					&& (host.equals("discord.com")
					|| host.equals("discordapp.com"))
					&& url.port() == 443
					&& url.username().isEmpty()
					&& url.password().isEmpty()
					&& url.query() == null
					&& url.fragment() == null
					&& url.encodedPath().matches(
					"/api/webhooks/[0-9]+/[^/]+/?"
			);
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
		RequestBody body = RequestBody.create(JSON, json);

		Request request = new Request.Builder()
				.url(webhook)
				.post(body)
				.build();

		// Prevent redirects from forwarding webhook requests.
		OkHttpClient client = httpClient.newBuilder()
				.followRedirects(false)
				.followSslRedirects(false)
				.build();

		try (Response response = client.newCall(request).execute())
		{
			if (response.code() == 204 || response.code() == 200)
			{
				log.info("Discord notification sent successfully");
			}
			else
			{
				log.warn("Discord returned HTTP {}", response.code());
			}
		}
		catch (Exception ex)
		{
			// Do not log exceptions containing secret webhook URLs.
			log.warn("Failed to send Discord notification");
		}
	}
}
