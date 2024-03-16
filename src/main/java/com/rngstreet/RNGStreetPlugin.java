package com.rngstreet;

import com.google.gson.Gson;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import com.google.gson.JsonObject;
import com.google.inject.Provides;

import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.loottracker.LootReceived;
import java.util.Objects;

@Slf4j
@PluginDescriptor(
		name = "RNGStreet"
)

public class RNGStreetPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private RNGStreetConfig config;

	@Override
	protected void startUp() throws Exception
	{
		log.info("RNGStreet Plugin Started");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("RNGStreet Plugin Stopped");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
            Player localPlayer = client.getLocalPlayer();
            //log.info("PlayerID: {}", localPlayer.getId());
            log.info("Account Hash: {}", String.valueOf(client.getAccountHash()));

			//Just leaving this here to remember how to write to chat :) -
            // client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "RNGStreet Plugin loaded", null);
		}
	}
	@Subscribe
	public void onLootReceived(LootReceived lootReceived) {
        // Log the received loot information
        //log.info("Received loot: {}", lootReceived.getItems());
        //log.info("Mob: {}", lootReceived.getName());

        // Get the username of the player
        String username = client.getLocalPlayer().getName();

        // Get the clan name - ty for command random GitHub guy :)
        String clanName = Objects.requireNonNull(this.client.getClanSettings()).getName();
        //log.info("Clan name: {}", clanName);
        //log.info("getItems: {}", lootReceived.getItems());
        //log.info("name: {}", lootReceived.getName());
        //log.info("getAmount: {}", lootReceived.getAmount());
        //log.info("getType: {}", lootReceived.getType());

        // Convert lootReceived to JSON ready for some PUSH action.
        Gson gson = new Gson();
        // Construct a JSON object containing username, name, clan, and items
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("AccHash", client.getAccountHash());
        jsonObject.addProperty("username", username);
        jsonObject.addProperty("name", lootReceived.getName());
        jsonObject.addProperty("clan", clanName);
        jsonObject.add("items", gson.toJsonTree(lootReceived.getItems()));

        // Convert the JSON object to a string and log it :)
        String json = jsonObject.toString();
        //log.info("{}", json);

        // Create a new thread to handle HTTP Request, so we don't lag :)
        Thread pushThread = new Thread(() -> {
            try {
                // Create URL object
                URL url = new URL(config.pushUrl());

                // Connect
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                // Set connection as POST :)
                httpURLConnection.setRequestMethod("POST");

                // Set content as JSON
                httpURLConnection.setRequestProperty("Content-Type", "application/json");

                // Enable output and disable caching
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setUseCaches(false);

                // Write JSON data to the connection
                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(json.getBytes());
                outputStream.flush();
                outputStream.close();

                // Get response code (will always be 200 surely)
                int responseCode = httpURLConnection.getResponseCode();

                // Log the for sure 200 response code :)
                log.info("HTTP Response Code: {}", responseCode);

                // End the connection
                httpURLConnection.disconnect();
            } catch (Exception e) {
                // Log the exceptions
                log.error("Error sending data to server: {}", e.getMessage());
            }
        });

        // Start the thread
        pushThread.start();
    }


	@Provides
	RNGStreetConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RNGStreetConfig.class);
	}
}