package randomeventhelper.randomevents.maze;

import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.gameval.ObjectID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.PluginChanged;
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
	private ClientThread clientThread;

	@Inject
	private ChatMessageManager chatMessageManager;

	private static final String PLUGIN_MESSAGE_SHORTEST_PATH_NAMESPACE = "shortestpath";
	private static final String PLUGIN_MESSAGE_SHORTEST_PATH_PATH_KEY = "path";
	private static final String PLUGIN_MESSAGE_SHORTEST_PATH_CLEAR_KEY = "clear";

	private GameObject mazeExitObject;

	@Inject
	public MazeHelper(OverlayManager overlayManager, RandomEventHelperConfig config, Client client)
	{
		super(overlayManager, config, client);
	}

	@Override
	public void onStartUp()
	{
		this.checkShortestPathPluginStatus();
		this.mazeExitObject = null;
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
		this.mazeExitObject = null;
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
				this.mazeExitObject = gameObjectSpawned.getGameObject();
				if (!this.checkShortestPathPluginStatus())
				{
					return;
				}
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
			this.mazeExitObject = null;
			this.sendShortestPathClear();
		}
	}

	@Subscribe
	public void onPluginChanged(PluginChanged pluginChanged)
	{
		log.debug("[#onPluginChanged] Plugin changed: {} | Loaded: {}", pluginChanged.getPlugin().getName(), pluginChanged.isLoaded());
		if (pluginChanged.getPlugin().getName().equals("Shortest Path") && pluginChanged.isLoaded())
		{
			log.debug("[#onPluginChanged] Shortest Path plugin was loaded, re-sending current maze exit object if it exists");
			if (this.mazeExitObject != null)
			{
				GameObjectSpawned gameObjectSpawned = new GameObjectSpawned();
				gameObjectSpawned.setGameObject(this.mazeExitObject);
				this.clientThread.invokeLater(() -> {
					this.onGameObjectSpawned(gameObjectSpawned);
					log.debug("[#onPluginChanged] Re-sent current maze exit object to Shortest Path plugin after it was enabled");
				});
			}
		}
	}

	private boolean checkShortestPathPluginStatus()
	{
		Optional<Plugin> shortestPathPlugin = pluginManager.getPlugins().stream().filter(plugin -> plugin.getName().equals("Shortest Path")).findAny();

		if (shortestPathPlugin.isPresent())
		{
			if (!pluginManager.isPluginEnabled(shortestPathPlugin.get()))
			{
				log.warn("[#onStartUp] ShortestPathPlugin is not enabled. Please enable it manually to ensure maze helper's functionality.");
				if (this.isLoggedIn())
				{
					String disabledPluginMessage = RandomEventHelperPlugin.getChatMessageBuilderWithPrefix()
						.append("You've enabled the maze helper module but have the Shortest Path plugin disabled. Please enable it manually to ensure functionality.")
						.build();
					RandomEventHelperPlugin.sendChatMessage(this.chatMessageManager, disabledPluginMessage);
					return false;
				}
			}
		}
		else
		{
			log.warn("[#onStartUp] Random Event Helper's Maze Helper requires the Shortest Path plugin found on the Plugin Hub to function properly. Please ensure Shortest Path is installed and enabled for Maze pathing support.");
			if (this.isLoggedIn())
			{
				String notInstalledPluginMessage = RandomEventHelperPlugin.getChatMessageBuilderWithPrefix()
					.append("You've enabled the maze helper module but don't have the Shortest Path plugin installed. Please ensure Shortest Path is installed and enabled for Maze pathing support.")
					.build();
				RandomEventHelperPlugin.sendChatMessage(this.chatMessageManager, notInstalledPluginMessage);
				return false;
			}
		}
		return true;
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
