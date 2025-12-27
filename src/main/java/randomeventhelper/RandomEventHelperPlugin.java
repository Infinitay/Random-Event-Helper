package randomeventhelper;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Provides;
import java.util.Map;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.DynamicObject;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import randomeventhelper.pluginmodulesystem.PluginModule;
import randomeventhelper.randomevents.beekeeper.BeekeeperHelper;
import randomeventhelper.randomevents.drilldemon.DrillDemonHelper;
import randomeventhelper.randomevents.freakyforester.FreakyForesterHelper;
import randomeventhelper.randomevents.gravedigger.GravediggerHelper;
import randomeventhelper.randomevents.gravedigger.GravediggerOverlay;
import randomeventhelper.randomevents.maze.MazeHelper;
import randomeventhelper.randomevents.mime.MimeHelper;
import randomeventhelper.randomevents.pinball.PinballHelper;
import randomeventhelper.randomevents.pirate.PirateHelper;
import randomeventhelper.randomevents.sandwichlady.SandwichLadyHelper;
import randomeventhelper.randomevents.quizmaster.QuizMasterHelper;
import randomeventhelper.randomevents.surpriseexam.SurpriseExamHelper;

@Slf4j
@PluginDescriptor(
	name = "Random Event Helper"
)
public class RandomEventHelperPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private RandomEventHelperConfig config;

	@Inject
	private RandomEventHelperOverlay overlay;

	@Inject
	private RandomEventHelperItemOverlay itemOverlay;

	@Inject
	private BeekeeperHelper beekeeperHelper;

	@Inject
	private PirateHelper pirateHelper;

	@Inject
	private DrillDemonHelper drillDemonHelper;

	@Inject
	private MimeHelper mimeHelper;

	@Inject
	private SurpriseExamHelper surpriseExamHelper;

	@Inject
	private QuizMasterHelper quizMasterHelper;

	// <String, PluginModule> -> <configKeyForIsEnabled, PluginModuleInstance>
	private Map<String, PluginModule> pluginModulesMap;

	@Override
	protected void startUp() throws Exception
	{
		this.overlayManager.add(overlay);
		this.overlayManager.add(itemOverlay);

		pluginModulesMap = ImmutableMap.<String, PluginModule>builder()
			.put("isBeekeeperEnabled", beekeeperHelper)
			.put("isCaptArnavChestEnabled", pirateHelper)
			.put("isDrillDemonEnabled", drillDemonHelper)
			.put("isMimeEnabled", mimeHelper)
			.put("isSurpriseExamEnabled", surpriseExamHelper)
			.put("isQuizMasterEnabled", quizMasterHelper)
			.build();
		// Start only the enabled modules
		for (PluginModule module : pluginModulesMap.values())
		{
			if (module.isEnabled())
			{
				module.startUp();
			}
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		this.overlayManager.remove(overlay);
		this.overlayManager.remove(itemOverlay);
		// Shutdown all modules regardless of their enabled state
		for (PluginModule module : pluginModulesMap.values())
		{
			module.shutdown();
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (configChanged.getGroup().equals("randomeventhelper"))
		{
			log.debug("Config changed: {} | New value: {}", configChanged.getKey(), configChanged.getNewValue());
			// Let's first handle plugin module updates - so lets first check to see if the changed config key is a mapped module
			PluginModule module = pluginModulesMap.get(configChanged.getKey());
			if (module != null)
			{
				if (module.isEnabled())
				{
					module.startUp();
				}
				else
				{
					module.shutdown();
				}
			}
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged animationChanged)
	{
		Actor actor = animationChanged.getActor();
		if (actor instanceof NPC)
		{
			// log.debug("NPC Animation changed: {} - New Animation ID: {}", ((NPC) actor).getName(), actor.getAnimation());
		}
		else if (actor instanceof GameObject)
		{
			// log.debug("GameObject Animation changed: {} - New Animation ID: {}", ((GameObject) actor).getId(), actor.getAnimation());
		}
		else if (actor instanceof Player)
		{
			// log.debug("Player Animation changed: {} - New Animation ID: {}", ((Player) actor).getName(), actor.getAnimation());
		}
		else if (actor instanceof DynamicObject)
		{
			// log.debug("DynamicObject Animation changed: {} - New Animation ID: {}", ((DynamicObject) actor).getModel().getSceneId(), actor.getAnimation());
		}
		else
		{
			// log.debug("Unknown Actor Animation changed: {} - New Animation ID: {}", actor.getName(), actor.getAnimation());
		}
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		// log.debug("isInstanced: {} | WorldLocation Region ID: {} | LocalLocation Region ID: {}", this.client.getTopLevelWorldView().isInstance(), this.client.getLocalPlayer().getWorldLocation().getRegionID(), this.getRegionIDFromCurrentLocalPointInstanced());
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		// log.debug("Widget loaded with group ID: {}", widgetLoaded.getGroupId());
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed widgetClosed)
	{
		// log.debug("Widget closed with group ID: {}", widgetClosed.getGroupId());
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{

	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{

	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned gameObjectSpawned)
	{
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		// log.debug("Chat message ({}) received: {}", chatMessage.getType(), chatMessage.getMessage());
		// String sanitizedChatMessage = Text.sanitizeMultilineText(chatMessage.getMessage());
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged varbitChanged)
	{

	}

	@Subscribe
	public void onGroundObjectSpawned(GroundObjectSpawned groundObjectSpawned)
	{

	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged itemContainerChanged)
	{

	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded menuEntryAdded)
	{

	}

	@Provides
	RandomEventHelperConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RandomEventHelperConfig.class);
	}

	// Accounts for local instances too such as inside the pinball and gravekeeper random event
	public static int getRegionIDFromCurrentLocalPointInstanced(Client client)
	{
		return WorldPoint.fromLocalInstance(client, client.getLocalPlayer().getLocalLocation()).getRegionID();
	}

	public static boolean isInRandomEventLocalInstance(Client client)
	{
		return RandomEventHelperPlugin.getRegionIDFromCurrentLocalPointInstanced(client) == 7758;
	}
}
