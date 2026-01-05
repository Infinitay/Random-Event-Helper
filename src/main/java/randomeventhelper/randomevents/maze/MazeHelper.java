package randomeventhelper.randomevents.maze;

import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.gameval.ObjectID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.PluginMessage;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.ui.overlay.OverlayManager;
import randomeventhelper.RandomEventHelperConfig;
import randomeventhelper.RandomEventHelperPlugin;
import randomeventhelper.pluginmodulesystem.PluginModule;

@Slf4j
@Singleton
public class MazeHelper extends PluginModule
{
	@Inject
	private PluginManager pluginManager;

	@Inject
	private ConfigManager configManager;

	private static final String PLUGIN_MESSAGE_SHORTEST_PATH_NAMESPACE = "shortestpath";
	private static final String PLUGIN_MESSAGE_SHORTEST_PATH_PATH_KEY = "path";
	private static final String PLUGIN_MESSAGE_SHORTEST_PATH_CLEAR_KEY = "clear";

	@Inject
	public MazeHelper(OverlayManager overlayManager, RandomEventHelperConfig config, Client client)
	{
		super(overlayManager, config, client);
	}

	@Override
	public void onStartUp()
	{
		Optional<Plugin> shortestPathPlugin = pluginManager.getPlugins().stream().filter(plugin -> plugin.getName().equals("Shortest Path")).findAny();

		if (shortestPathPlugin.isPresent())
		{
			if (!pluginManager.isPluginEnabled(shortestPathPlugin.get()))
			{
				log.warn("[#onStartUp] ShortestPathPlugin is not enabled. Please enable it manually to ensure maze helper's functionality.");
				this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "[Random Event Helper] You've enabled the maze helper module but have the Shortest Path plugin disabled. Please enable it manually to ensure functionality.", null);
			}
		}
		else
		{
			log.warn("[#onStartUp] Clue Path Finder requires the Shortest Path plugin found on the Plugin Hub to function properly, and so this plugin has been disabled. Please ensure Shortest Path is installed and enabled before enabling this plugin.");
			// Show a message popup dialog to the user
			SwingUtilities.invokeLater(() -> {
				JOptionPane.showMessageDialog(
					client.getCanvas(),
					"Random Event Helper's Maze Helper requires the Shortest Path plugin found on the Plugin Hub to function properly, and so this plugin has been disabled. Please ensure Shortest Path is installed and enabled before enabling Maze support.",
					"Random Event Helper - Plugin Dependency Missing",
					JOptionPane.WARNING_MESSAGE
				);
			});
			// Remember to disable Maze config option as well
			this.configManager.setConfiguration("randomeventhelper", "isMazeEnabled", false);
		}
	}

	@Override
	public void onShutdown()
	{
		Optional<Plugin> shortestPathPlugin = pluginManager.getPlugins().stream().filter(plugin -> plugin.getName().equals("Shortest Path")).findAny();
		if (shortestPathPlugin.isPresent())
		{
			if (pluginManager.isPluginEnabled(shortestPathPlugin.get()))
			{
				this.sendShortestPathClear();
			}
		}
	}

	@Override
	public boolean isEnabled()
	{
		return this.config.isMazeEnabled();
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned gameObjectSpawned)
	{
		if (gameObjectSpawned.getGameObject().getId() == ObjectID.MACRO_MAZE_COMPLETE)
		{
			if (gameObjectSpawned.getGameObject() != null)
			{
				LocalPoint shrineLocalPoint = gameObjectSpawned.getGameObject().getLocalLocation();
				WorldPoint instancedShrineWorldPoint = WorldPoint.fromLocalInstance(this.client, shrineLocalPoint);
				log.debug("Detected maze exit object spawn, setting shortest path to it");
				this.sendShortestPathDestination(instancedShrineWorldPoint);
			}
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned gameObjectDespawned)
	{
		if (gameObjectDespawned.getGameObject().getId() == ObjectID.MACRO_MAZE_COMPLETE)
		{
			log.debug("Detected maze exit object despawn, clearing shortest path");
			this.sendShortestPathClear();
		}
	}

	private boolean sendShortestPathDestination(WorldPoint destinationWorldPoint)
	{
		if (destinationWorldPoint == null)
		{
			log.warn("[#sendShortestPathDestinations] The destination is null so we can't set a path");
			return false;
		}

		Map<String, Object> payload = this.generatePathPayload(destinationWorldPoint);
		if (payload.isEmpty())
		{
			log.warn("[#sendShortestPathDestinations] Failed to generate payload for path generation");
			return false;
		}

		this.sendShortestPathClear();
		this.eventBus.post(new PluginMessage(PLUGIN_MESSAGE_SHORTEST_PATH_NAMESPACE, PLUGIN_MESSAGE_SHORTEST_PATH_PATH_KEY, payload));
		return true;
	}

	private Map<String, Object> generatePathPayload(WorldPoint destinationWorldPoint)
	{
		LocalPoint startingLocalPoint = client.getLocalPlayer().getLocalLocation();

		if (startingLocalPoint == null)
		{
			log.warn("[#generatePathPayload(WorldPoint)] Player's starting local point is null, cannot generate payload starting point");
			return Map.of();
		}

		WorldPoint localInstanceStartingWorldPoint = WorldPoint.fromLocalInstance(this.client, startingLocalPoint);
		log.debug("[#generatePathPayload(WorldPoint)] Converted player's local point {} to local instance world point {}", startingLocalPoint, localInstanceStartingWorldPoint);
		return this.generatePathPayload(localInstanceStartingWorldPoint, destinationWorldPoint);
	}

	private Map<String, Object> generatePathPayload(WorldPoint startingWorldPoint, WorldPoint destinationWorldPoint)
	{
		log.debug("[#generatePathPayload(WorldPoint, WorldPoint)] Generated path payload with starting point: {} and destination point: {}", startingWorldPoint, destinationWorldPoint);
		return Map.of(
			"start", startingWorldPoint,
			"target", destinationWorldPoint
		);
	}

	private void sendShortestPathClear()
	{
		this.eventBus.post(new PluginMessage(PLUGIN_MESSAGE_SHORTEST_PATH_NAMESPACE, PLUGIN_MESSAGE_SHORTEST_PATH_CLEAR_KEY));
	}

	private boolean isInMazeLocalInstance()
	{
		return RandomEventHelperPlugin.getRegionIDFromCurrentLocalPointInstanced(client) == 11591;
	}
}
