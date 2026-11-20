package randomeventhelper;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Provides;
import java.util.Map;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import randomeventhelper.pluginmodulesystem.PluginModule;
import randomeventhelper.randomevents.beekeeper.BeekeeperHelper;
import randomeventhelper.randomevents.certer.CerterHelper;
import randomeventhelper.randomevents.drilldemon.DrillDemonHelper;
import randomeventhelper.randomevents.freakyforester.FreakyForesterHelper;
import randomeventhelper.randomevents.frog.FrogHelper;
import randomeventhelper.randomevents.gravedigger.GravediggerHelper;
import randomeventhelper.randomevents.maze.MazeHelper;
import randomeventhelper.randomevents.mime.MimeHelper;
import randomeventhelper.randomevents.pinball.PinballHelper;
import randomeventhelper.randomevents.pirate.PirateHelper;
import randomeventhelper.randomevents.prisonpete.PrisonPeteHelper;
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
	private RandomEventHelperConfig config;

	@Inject
	private BeekeeperHelper beekeeperHelper;

	@Inject
	private PirateHelper pirateHelper;

	@Inject
	private CerterHelper certerHelper;

	@Inject
	private DrillDemonHelper drillDemonHelper;

	@Inject
	private FreakyForesterHelper freakyForesterHelper;

	@Inject
	private GravediggerHelper gravediggerHelper;

	@Inject
	private FrogHelper frogHelper;

	@Inject
	private MazeHelper mazeHelper;

	@Inject
	private MimeHelper mimeHelper;

	@Inject
	private PinballHelper pinballHelper;

	@Inject
	private SandwichLadyHelper sandwichLadyHelper;

	@Inject
	private SurpriseExamHelper surpriseExamHelper;

	@Inject
	private QuizMasterHelper quizMasterHelper;

	@Inject
	private PrisonPeteHelper prisonPeteHelper;

	// <String, PluginModule> -> <configKeyForIsEnabled, PluginModuleInstance>
	private Map<String, PluginModule> pluginModulesMap;

	@Override
	protected void startUp() throws Exception
	{
		pluginModulesMap = ImmutableMap.<String, PluginModule>builder()
			.put("isBeekeeperEnabled", beekeeperHelper)
			.put("isCaptArnavChestEnabled", pirateHelper)
			.put("isCerterEnabled", certerHelper)
			.put("isDrillDemonEnabled", drillDemonHelper)
			.put("isFreakyForesterEnabled", freakyForesterHelper)
			.put("isGravediggerEnabled", gravediggerHelper)
			.put("isKissTheFrogEnabled", frogHelper)
			.put("isMazeEnabled", mazeHelper)
			.put("isMimeEnabled", mimeHelper)
			.put("isPinballEnabled", pinballHelper)
			.put("isPrisonPeteEnabled", prisonPeteHelper)
			.put("isSandwichLadyEnabled", sandwichLadyHelper)
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

	@Provides
	RandomEventHelperConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RandomEventHelperConfig.class);
	}

	// Accounts for local instances too such as inside the pinball and gravekeeper random event
	public static int getRegionIDFromCurrentLocalPointInstanced(Client client)
	{
		Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null)
		{
			return -1;
		}

		LocalPoint localLocation = localPlayer.getLocalLocation();
		if (localLocation == null)
		{
			return -1;
		}

		return WorldPoint.fromLocalInstance(client, localLocation).getRegionID();
	}

	public static boolean isInRandomEventLocalInstance(Client client)
	{
		return RandomEventHelperPlugin.getRegionIDFromCurrentLocalPointInstanced(client) == 7758;
	}

	public static ChatMessageBuilder getChatMessageBuilderWithPrefix()
	{
		return new ChatMessageBuilder()
			.append(ChatColorType.NORMAL)
			.append("[")
			.append(ChatColorType.HIGHLIGHT)
			.append("Random Event Helper")
			.append(ChatColorType.NORMAL)
			.append("] ");
	}

	public static void sendChatMessage(ChatMessageManager chatMessageManager, String runeLiteFormattedMessage)
	{
		chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.CONSOLE).runeLiteFormattedMessage(runeLiteFormattedMessage).build());
	}
}
