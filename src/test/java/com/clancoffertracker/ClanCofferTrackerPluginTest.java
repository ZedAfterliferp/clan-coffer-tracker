
package com.clancoffertracker;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ClanCofferTrackerPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(
				ClanCofferTrackerPlugin.class
		);

		RuneLite.main(args);
	}
}
