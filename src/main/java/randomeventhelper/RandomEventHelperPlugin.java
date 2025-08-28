package randomeventhelper;

import com.google.inject.Provides;
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
import randomeventhelper.randomevents.beekeeper.BeekeeperHelper;
import randomeventhelper.randomevents.drilldemon.DrillDemonHelper;
import randomeventhelper.randomevents.freakyforester.FreakyForesterHelper;
import randomeventhelper.randomevents.gravedigger.GravediggerHelper;
import randomeventhelper.randomevents.pinball.PinballHelper;
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
	private SurpriseExamHelper surpriseExamHelper;

	@Inject
	private BeekeeperHelper beekeeperHelper;

	@Inject
	private FreakyForesterHelper freakyForesterHelper;

	@Inject
	private PinballHelper pinballHelper;

	@Inject
	private DrillDemonHelper drillDemonHelper;

	@Inject
	private GravediggerHelper gravediggerHelper;

	@Override
	protected void startUp() throws Exception
	{
		this.overlayManager.add(overlay);
		this.overlayManager.add(itemOverlay);
		if (config.isSurpriseExamEnabled())
		{
			surpriseExamHelper.startUp();
		}
		if (config.isBeekeeperEnabled())
		{
			beekeeperHelper.startUp();
		}
		if (config.isFreakyForesterEnabled())
		{
			freakyForesterHelper.startUp();
		}
		if (config.isPinballEnabled())
		{
			pinballHelper.startUp();
		}
		if (config.isDrillDemonEnabled())
		{
			drillDemonHelper.startUp();
		}
		if (config.isGravediggerEnabled())
		{
			gravediggerHelper.startUp();
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		this.overlayManager.remove(overlay);
		this.overlayManager.remove(itemOverlay);
		surpriseExamHelper.shutDown();
		beekeeperHelper.shutDown();
		freakyForesterHelper.shutDown();
		pinballHelper.shutDown();
		drillDemonHelper.shutDown();
		gravediggerHelper.shutDown();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (configChanged.getGroup().equals("randomeventhelper"))
		{
			log.debug("Config changed: {} | New value: {}", configChanged.getKey(), configChanged.getNewValue());
			if (configChanged.getKey().equals("isSurpriseExamEnabled"))
			{
				if (config.isSurpriseExamEnabled())
				{
					surpriseExamHelper.startUp();
				}
				else
				{
					surpriseExamHelper.shutDown();
				}
			}
			else if (configChanged.getKey().equals("isBeekeeperEnabled"))
			{
				if (config.isBeekeeperEnabled())
				{
					beekeeperHelper.startUp();
				}
				else
				{
					beekeeperHelper.shutDown();
				}
			}
			else if (configChanged.getKey().equals("isFreakyForesterEnabled"))
			{
				if (config.isFreakyForesterEnabled())
				{
					freakyForesterHelper.startUp();
				}
				else
				{
					freakyForesterHelper.shutDown();
				}
			}
			else if (configChanged.getKey().equals("isPinballEnabled"))
			{
				if (config.isPinballEnabled())
				{
					pinballHelper.startUp();
				}
				else
				{
					pinballHelper.shutDown();
				}
			}
			else if (configChanged.getKey().equals("isDrillDemonEnabled"))
			{
				if (config.isDrillDemonEnabled())
				{
					drillDemonHelper.startUp();
				}
				else
				{
					drillDemonHelper.shutDown();
				}
			}
			else if (configChanged.getKey().equals("isGravediggerEnabled"))
			{
				if (config.isGravediggerEnabled())
				{
					gravediggerHelper.startUp();
				}
				else
				{
					gravediggerHelper.shutDown();
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
