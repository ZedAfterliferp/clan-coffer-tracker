
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

    public static Deposit parse(String message)
    {
        if (message == null)
        {
            return null;
        }

        Matcher matcher = DEPOSIT_PATTERN.matcher(message.trim());

        if (!matcher.matches())
        {
            return null;
        }

        try
        {
            String player = matcher.group(1).trim();
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
