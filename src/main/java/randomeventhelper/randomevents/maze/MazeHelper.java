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
import net.runelite.api.GameObject;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameTick;
import net.runelite.api.gameval.ObjectID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.PluginMessage;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginManager;
import randomeventhelper.RandomEventHelperPlugin;

@Slf4j
@Singleton
public class MazeHelper
{
	@Inject
	private EventBus eventBus;

	@Inject
	private Client client;

	@Inject
	private PluginManager pluginManager;

	@Inject
	private ConfigManager configManager;

	private static final String PLUGIN_MESSAGE_SHORTEST_PATH_NAMESPACE = "shortestpath";
	private static final String PLUGIN_MESSAGE_SHORTEST_PATH_PATH_KEY = "path";
	private static final String PLUGIN_MESSAGE_SHORTEST_PATH_CLEAR_KEY = "clear";

	private boolean isFirstRun;
	private GameObject mazeExitObject; // Only purpose this serves is to avoid unnecessary onGameTick

	public void startUp()
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
			return;
		}
		this.eventBus.register(this);
		this.isFirstRun = true;
		this.mazeExitObject = null;
	}

	public void shutDown()
	{
		this.eventBus.unregister(this);
		Optional<Plugin> shortestPathPlugin = pluginManager.getPlugins().stream().filter(plugin -> plugin.getName().equals("Shortest Path")).findAny();
		if (shortestPathPlugin.isPresent())
		{
			if (!pluginManager.isPluginEnabled(shortestPathPlugin.get()))
			{
				if (this.isFirstRun)
				{
					this.sendShortestPathClear();
				}
			}
		}
		this.isFirstRun = true;
		this.mazeExitObject = null;
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
				this.isFirstRun = false;
				this.mazeExitObject = gameObjectSpawned.getGameObject();
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
			this.isFirstRun = false;
			this.mazeExitObject = null;
			this.sendShortestPathClear();
		}
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		if (this.isFirstRun && this.isInMazeLocalInstance() && this.mazeExitObject == null)
		{
			log.debug("Cold start detected in maze instance, searching for maze exit object");
			this.isFirstRun = false;

			Tile[][][] sceneTiles = this.client.getTopLevelWorldView().getScene().getTiles(); // [Plane][x][y]
			Tile[][] tilesInZ = sceneTiles[this.client.getTopLevelWorldView().getPlane()]; // Tiles at [z]
			for (Tile[] tilesInZX : tilesInZ) // Tiles at [z][x]
			{
				for (Tile tile : tilesInZX) // Tiles at [z][x][y]
				{
					if (tile != null && tile.getGameObjects() != null)
					{
						for (GameObject gameObject : tile.getGameObjects())
						{
							// There seemed to be some case where the game object was null
							if (gameObject == null)
							{
								continue;
							}
							GameObjectSpawned gameObjectSpawnedEvent = new GameObjectSpawned();
							gameObjectSpawnedEvent.setGameObject(gameObject);
							this.onGameObjectSpawned(gameObjectSpawnedEvent);
							if (this.mazeExitObject != null)
							{
								return;
							}
						}
					}
				}
			}
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
		WorldPoint startingWorldPoint = client.getLocalPlayer().getWorldLocation();
		if (startingWorldPoint == null)
		{
			log.warn("[#generatePathPayload-WorldPoint] Player's starting world point is null, cannot generate payload starting point");
			return Map.of();
		}
		return this.generatePathPayload(startingWorldPoint, destinationWorldPoint);
	}

	private Map<String, Object> generatePathPayload(WorldPoint startingWorldPoint, WorldPoint destinationWorldPoint)
	{
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
