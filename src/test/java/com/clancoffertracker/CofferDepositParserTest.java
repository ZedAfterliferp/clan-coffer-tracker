
package com.clancoffertracker;

import org.junit.Test;
import static org.junit.Assert.*;

public class CofferDepositParserTest
{
    @Test
    public void testNoMercyDeposit()
    {
        CofferDepositParser.Deposit deposit =
                CofferDepositParser.parse(
                        "NoMercy has deposited 1,000,000 coins into the coffer."
                );

        assertNotNull(deposit);
        assertEquals("NoMercy", deposit.player);
        assertEquals(1000000L, deposit.amount);
    }

    @Test
    public void testInvalidMessage()
    {
        assertNull(CofferDepositParser.parse(
                "NoMercy has withdrawn 1,000,000 coins."
        ));
    }

    @Test
    public void testZeroDeposit()
    {
        assertNull(CofferDepositParser.parse(
                "NoMercy has deposited 0 coins into the coffer."
        ));
    }


    @Test
    public void testRealClanCofferDeposit()
    {
        String message =
                "<int_0=10000000>Ember\u00A0Ego has deposited "
                        + "<int_0:spacer> coins into the coffer.";

        CofferDepositParser.Deposit deposit =
                CofferDepositParser.parse(message);

        assertNotNull(deposit);
        assertEquals("Ember Ego", deposit.player);
        assertEquals(10000000L, deposit.amount);
    }

}
