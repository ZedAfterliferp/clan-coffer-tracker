
package com.clancoffertracker;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CofferDepositParser
{
    private static final Pattern DEPOSIT_PATTERN =
            Pattern.compile(
                    "^(.+?) has deposited ([\\d,]+) coins? into the coffer\\.$",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern FORMATTED_DEPOSIT_PATTERN =
            Pattern.compile(
                    "^<int_0=(\\d+)>(.+?) has deposited <int_0:spacer> coins? into the coffer\\.$",
                    Pattern.CASE_INSENSITIVE
            );

    public static Deposit parse(String message)
    {
        if (message == null)
        {
            return null;
        }

        String text = message.trim();

        // Handle RuneLite's raw clan coffer announcement.
        Matcher formatted = FORMATTED_DEPOSIT_PATTERN.matcher(text);

        if (formatted.matches())
        {
            try
            {
                long amount = Long.parseLong(formatted.group(1));
                String player = normalizePlayer(formatted.group(2));

                if (player.isEmpty() || amount <= 0)
                {
                    return null;
                }

                return new Deposit(player, amount);
            }
            catch (NumberFormatException ex)
            {
                return null;
            }
        }

        // Handle ordinary messages containing a visible GP amount.
        Matcher matcher = DEPOSIT_PATTERN.matcher(text);

        if (!matcher.matches())
        {
            return null;
        }

        try
        {
            String player = normalizePlayer(matcher.group(1));
            long amount = Long.parseLong(
                    matcher.group(2).replace(",", "")
            );

            if (player.isEmpty() || amount <= 0)
            {
                return null;
            }

            return new Deposit(player, amount);
        }
        catch (NumberFormatException ex)
        {
            return null;
        }
    }

    private static String normalizePlayer(String player)
    {
        return player
                .replace("\u00A0", " ")
                .replace("┬á", " ")
                .trim();
    }

    public static class Deposit
    {
        public final String player;
        public final long amount;

        public Deposit(String player, long amount)
        {
            this.player = player;
            this.amount = amount;
        }
    }
}
