package com.rngstreet;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("RNGStreet")
public interface RNGStreetConfig extends Config
{
	@ConfigItem(
			keyName = "pushUrl",
			name = "Push URL",
			description = "The URL to which mob drops will be sent"
	)
	default String pushUrl()
	{
		return "http://127.0.0.1:5000/";
	}
}
