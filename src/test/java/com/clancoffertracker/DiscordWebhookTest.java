
package com.clancoffertracker;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DiscordWebhookTest
{
    public static void main(String[] args) throws Exception
    {
        // Paste your Discord webhook here temporarily.
        String webhook = System.getenv("DISCORD_WEBHOOK_TEST_URL");

        if (webhook == null || webhook.isEmpty())
        {
            System.out.println("Set DISCORD_WEBHOOK_TEST_URL first.");
            return;
        }

        String json = "{\"content\":\"Clan Coffer Tracker Test!\\nPlayer: TestPlayer\\nAmount: 500,000 GP\"}";

        HttpURLConnection connection =
                (HttpURLConnection) new URL(webhook).openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty(
                "Content-Type", "application/json"
        );
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setDoOutput(true);

        try (OutputStream output = connection.getOutputStream())
        {
            output.write(json.getBytes(StandardCharsets.UTF_8));
        }

        System.out.println(
                "Discord HTTP Status: " + connection.getResponseCode()
        );

        connection.disconnect();
    }
}
