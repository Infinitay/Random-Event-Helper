package randomeventsolver;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.DynamicObject;
import net.runelite.api.GameObject;
import net.runelite.api.GroundObject;
import net.runelite.api.Item;
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
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;
import randomeventsolver.data.Coffin;
import randomeventsolver.data.Grave;
import randomeventsolver.data.RandomEventItem;
import randomeventsolver.randomevents.beekeeper.BeekeeperHelper;
import randomeventsolver.randomevents.freakyforester.FreakyForesterHelper;
import randomeventsolver.randomevents.pinball.PinballHelper;
import randomeventsolver.randomevents.surpriseexam.SurpriseExamHelper;
import randomeventsolver.randomevents.surpriseexam.SurpriseExamOverlay;

@Slf4j
@PluginDescriptor(
	name = "Random Event Solver"
)
public class RandomEventSolverPlugin extends Plugin
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
	private RandomEventSolverConfig config;

	@Inject
	private RandomEventSolverOverlay overlay;

	@Inject
	private RandomEventSolverItemOverlay itemOverlay;

	@Inject
	private SurpriseExamHelper surpriseExamHelper;

	@Inject
	private BeekeeperHelper beekeeperHelper;

	@Inject
	private FreakyForesterHelper freakyForesterHelper;

	@Inject
	private PinballHelper pinballHelper;

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
	}

	@Override
	protected void shutDown() throws Exception
	{
		this.overlayManager.remove(overlay);
		this.overlayManager.remove(itemOverlay);
		this.exerciseMatsAnswerList.clear();
		this.exerciseMatsMultimap.clear();
		this.exerciseVarbitMatMultimap.clear();
		surpriseExamHelper.shutDown();
		beekeeperHelper.shutDown();
		freakyForesterHelper.shutDown();
		pinballHelper.shutDown();
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
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged animationChanged)
	{
		Actor actor = animationChanged.getActor();
		if (actor instanceof NPC)
		{
			log.debug("NPC Animation changed: {} - New Animation ID: {}", ((NPC) actor).getName(), actor.getAnimation());
		}
		else if (actor instanceof GameObject)
		{
			log.debug("GameObject Animation changed: {} - New Animation ID: {}", ((GameObject) actor).getId(), actor.getAnimation());
		}
		else if (actor instanceof Player)
		{
			log.debug("Player Animation changed: {} - New Animation ID: {}", ((Player) actor).getName(), actor.getAnimation());
		}
		else if (actor instanceof DynamicObject)
		{
			log.debug("DynamicObject Animation changed: {} - New Animation ID: {}", ((DynamicObject) actor).getModel().getSceneId(), actor.getAnimation());
		}
		else
		{
			log.debug("Unknown Actor Animation changed: {} - New Animation ID: {}", actor.getName(), actor.getAnimation());
		}
	}

	private boolean initiallyEnteredGraveDiggerArea;

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		// log.debug("isInstanced: {} | WorldLocation Region ID: {} | LocalLocation Region ID: {}", this.client.getTopLevelWorldView().isInstance(), this.client.getLocalPlayer().getWorldLocation().getRegionID(), this.getRegionIDFromCurrentLocalPointInstanced());
		// There is an edgecase where when you're at the grave digger random event if a varb is still 0 then it won't fire.
		// So lets handle this by checking to see if a player is in the grave digger random event area via NPC Leo spawn
		// And by using a separate variable to make sure not to run this constantly every game tick
		if (this.initiallyEnteredGraveDiggerArea)
		{
			for (Grave.GraveNumber graveNumber : Grave.GraveNumber.values())
			{
				VarbitChanged graveTypeVarbitChangedEvent = new VarbitChanged();
				graveTypeVarbitChangedEvent.setVarbitId(graveNumber.getGraveTypeVarbitID());
				graveTypeVarbitChangedEvent.setValue(this.client.getVarbitValue(graveNumber.getGraveTypeVarbitID()));
				VarbitChanged placedCoffinVarbitChangedEvent = new VarbitChanged();
				placedCoffinVarbitChangedEvent.setVarbitId(graveNumber.getPlacedCoffinVarbitID());
				placedCoffinVarbitChangedEvent.setValue(this.client.getVarbitValue(graveNumber.getPlacedCoffinVarbitID()));
				this.onVarbitChanged(graveTypeVarbitChangedEvent);
				this.onVarbitChanged(placedCoffinVarbitChangedEvent);
				this.initiallyEnteredGraveDiggerArea = false;
			}

		}
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
		NPC npc = npcSpawned.getNpc();
		if (RandomEventSolverPlugin.isInRandomEventLocalInstance(this.client))
		{
			if (npc.getId() == NpcID.MACRO_GRAVEDIGGER)
			{
				log.debug("Grave Digger Leo NPC spawned in grave digger random event area.");
				this.initiallyEnteredGraveDiggerArea = true;
				// Take this opportunity to initialize the BufferedImage for the coffin items
				for (Coffin coffin : Coffin.values())
				{
					coffinItemImageMap.put(coffin, coffin.getItemImage(this.itemManager));
				}
			}
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		if (npcDespawned.getNpc().getId() == NpcID.MACRO_DRILLDEMON)
		{
			log.debug("Drill Demon NPC despawned, resetting exercise mats and mappings.");
			this.exerciseMatsAnswerList.clear();
			this.exerciseMatsMultimap.clear();
			this.exerciseVarbitMatMultimap.clear();
		}
		else if (npcDespawned.getNpc().getId() == NpcID.MACRO_GRAVEDIGGER)
		{
			log.debug("Grave Digger Leo NPC despawned, resetting grave digger area state.");
			this.initiallyEnteredGraveDiggerArea = false;
			this.graveMap.clear();
			this.coffinsInInventory.clear();
			this.coffinItemImageMap.clear();
		}
	}

	@Getter
	private Map<Coffin, BufferedImage> coffinItemImageMap = Maps.newHashMap();

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned gameObjectSpawned)
	{
		GameObject gameObject = gameObjectSpawned.getGameObject();
		// Pinball and grave digger random even locations are in region 7758
		if (RandomEventSolverPlugin.isInRandomEventLocalInstance(this.client))
		{
			if (Grave.GraveNumber.isGravestoneObjectID(gameObject.getId()))
			{
				Grave.GraveNumber graveNumber = Grave.GraveNumber.getGraveNumberFromGravestoneObjectID(gameObject.getId());
				if (graveNumber != null)
				{
					log.debug("A new gravestone object ({}) spawned with ID: {}, updating grave map.", graveNumber.name(), gameObject.getId());
					Grave grave = this.graveMap.get(graveNumber);
					if (grave == null)
					{
						grave = new Grave(graveNumber);
					}
					grave.setGraveStone(gameObject);
					this.graveMap.put(graveNumber, grave);
				}
				else
				{
					log.warn("Gravestone object ID {} does not map to a known grave number.", gameObject.getId());
				}
			}
			else if (Grave.GraveNumber.isEmptyGraveObjectID(gameObject.getId()))
			{
				Grave.GraveNumber graveNumber = Grave.GraveNumber.getGraveNumberFromEmptyGraveObjectID(gameObject.getId());
				if (graveNumber != null)
				{
					log.debug("A new empty grave object ({}) spawned with ID: {}, updating grave map.", graveNumber.name(), gameObject.getId());
					Grave grave = this.graveMap.get(graveNumber);
					if (grave == null)
					{
						grave = new Grave(graveNumber);
					}
					grave.setEmptyGrave(gameObject);
					this.graveMap.put(graveNumber, grave);
				}
				else
				{
					log.warn("Empty grave object ID {} does not map to a known grave number.", gameObject.getId());
				}
			}
			else if (Grave.GraveNumber.isFilledGraveObjectID(gameObject.getId()))
			{
				Grave.GraveNumber graveNumber = Grave.GraveNumber.getGraveNumberFromFilledGraveObjectID(gameObject.getId());
				if (graveNumber != null)
				{
					log.debug("A new filled grave object ({}) spawned with ID: {}, updating grave map.", graveNumber.name(), gameObject.getId());
					Grave grave = this.graveMap.get(graveNumber);
					if (grave == null)
					{
						grave = new Grave(graveNumber);
					}
					grave.setFilledGrave(gameObject);
					this.graveMap.put(graveNumber, grave);
				}
				else
				{
					log.warn("Filled grave object ID {} does not map to a known grave number.", gameObject.getId());
				}
			}
		}
	}

	private Set<String> DRILL_DEMON_EXERCISE_STRINGS = ImmutableSet.of(DrillExercise.JOG.drillSergeantText, DrillExercise.SIT_UP.drillSergeantText, DrillExercise.PUSH_UP.drillSergeantText, DrillExercise.STAR_JUMP.drillSergeantText);

	@Getter
	private List<GroundObject> exerciseMatsAnswerList = Lists.newArrayList();

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		log.debug("Chat message ({}) received: {}", chatMessage.getType(), chatMessage.getMessage());
		String sanitizedChatMessage = Text.sanitizeMultilineText(chatMessage.getMessage());
		if (chatMessage.getType() == ChatMessageType.DIALOG)
		{
			if (this.client.getLocalPlayer().getWorldLocation().getRegionID() == 12619)
			{
				this.exerciseMatsAnswerList.clear();
				DrillExercise exercise = DrillExercise.getExerciseFromText(sanitizedChatMessage);
				if (exercise != null)
				{
					log.debug("Drill Demon requested exercise: {}", exercise.name());
					this.exerciseMatsAnswerList = Lists.newArrayList(this.exerciseVarbitMatMultimap.get(exercise.getVarbitValue()));
					log.debug("Drill Demon exercise mats list set to: {}", this.exerciseMatsAnswerList);
				}
				else
				{
					log.warn("Drill Demon requested unknown exercise: {}", sanitizedChatMessage);
					this.exerciseMatsAnswerList.clear();
				}
			}
		}
	}

	// <Grave Number, Grave>
	@Getter
	private Map<Grave.GraveNumber, Grave> graveMap = Maps.newHashMap();

	@Subscribe
	public void onVarbitChanged(VarbitChanged varbitChanged)
	{
		switch (varbitChanged.getVarbitId())
		{
			case VarbitID.MACRO_DRILLDEMON_POST_1:
				this.updateExerciseMappings(varbitChanged.getValue(), 1);
				break;
			case VarbitID.MACRO_DRILLDEMON_POST_2:
				this.updateExerciseMappings(varbitChanged.getValue(), 2);
				break;
			case VarbitID.MACRO_DRILLDEMON_POST_3:
				this.updateExerciseMappings(varbitChanged.getValue(), 3);
				break;
			case VarbitID.MACRO_DRILLDEMON_POST_4:
				this.updateExerciseMappings(varbitChanged.getValue(), 4);
				break;
			case VarbitID.MACRO_DIGGER_GRAVE_1: // Grave type/Gravestone
				this.updateRequiredCoffin(Grave.GraveNumber.ONE, varbitChanged.getValue());
				break;
			case VarbitID.MACRO_DIGGER_COFFIN_1: // Placed coffin into the grave
				this.updatePlacedCoffin(Grave.GraveNumber.ONE, varbitChanged.getValue());
				break;
			case VarbitID.MACRO_DIGGER_GRAVE_2:
				this.updateRequiredCoffin(Grave.GraveNumber.TWO, varbitChanged.getValue());
				break;
			case VarbitID.MACRO_DIGGER_COFFIN_2:
				this.updatePlacedCoffin(Grave.GraveNumber.TWO, varbitChanged.getValue());
				break;
			case VarbitID.MACRO_DIGGER_GRAVE_3:
				this.updateRequiredCoffin(Grave.GraveNumber.THREE, varbitChanged.getValue());
				break;
			case VarbitID.MACRO_DIGGER_COFFIN_3:
				this.updatePlacedCoffin(Grave.GraveNumber.THREE, varbitChanged.getValue());
				break;
			case VarbitID.MACRO_DIGGER_GRAVE_4:
				this.updateRequiredCoffin(Grave.GraveNumber.FOUR, varbitChanged.getValue());
				break;
			case VarbitID.MACRO_DIGGER_COFFIN_4:
				this.updatePlacedCoffin(Grave.GraveNumber.FOUR, varbitChanged.getValue());
				break;
			case VarbitID.MACRO_DIGGER_GRAVE_5:
				this.updateRequiredCoffin(Grave.GraveNumber.FIVE, varbitChanged.getValue());
				break;
			case VarbitID.MACRO_DIGGER_COFFIN_5:
				this.updatePlacedCoffin(Grave.GraveNumber.FIVE, varbitChanged.getValue());
				break;
			default:
				break;
		}
	}

	private void updateRequiredCoffin(Grave.GraveNumber graveNumber, int requiredCoffinVarbitValue)
	{
		log.debug("Grave {} required coffin varbit changed to value: {}", graveNumber.name(), requiredCoffinVarbitValue);
		Coffin coffin = Coffin.getCoffinFromVarbitValue(requiredCoffinVarbitValue);
		if (coffin != null)
		{
			log.debug("Grave {} requires {} coffin", graveNumber.name(), coffin.name());
			Grave grave = this.graveMap.getOrDefault(graveNumber, new Grave(graveNumber));
			grave.setRequiredCoffin(coffin);
			this.graveMap.put(graveNumber, grave);
		}
		else
		{
			log.warn("Grave {} required coffin varbit changed to unknown coffin value: {}", graveNumber.name(), requiredCoffinVarbitValue);
		}
	}

	private void updatePlacedCoffin(Grave.GraveNumber graveNumber, int placedCoffinVarbitValue)
	{
		log.debug("Grave {} placed coffin varbit changed to value: {}", graveNumber.name(), placedCoffinVarbitValue);
		Coffin coffin = Coffin.getCoffinFromVarbitValue(placedCoffinVarbitValue);
		if (coffin != null)
		{
			log.debug("Found {} coffin placed into Grave {}", coffin.name(), graveNumber.name());
			Grave grave = this.graveMap.getOrDefault(graveNumber, new Grave(graveNumber));
			grave.setPlacedCoffin(coffin);
			this.graveMap.put(graveNumber, grave);
		}
		else
		{
			log.warn("Grave {} placed coffin varbit changed to unknown coffin value: {}", graveNumber.name(), placedCoffinVarbitValue);
		}
	}

	private void updateExerciseMappings(int exerciseVarbitValue, int postNumber)
	{
		DrillExercise exercise = DrillExercise.VARBIT_TO_EXERCISE_MAP.get(exerciseVarbitValue);
		if (exercise != null)
		{
			log.debug("Drill Demon exercise of Post_{} changed to: {} ({})", postNumber, exercise.getVarbitValue(), exercise.name());
			this.exerciseVarbitMatMultimap.replaceValues(exerciseVarbitValue, this.exerciseMatsMultimap.get(postNumber));
		}
		else
		{
			log.warn("Drill Demon exercise varbit changed to unknown value: {}", exerciseVarbitValue);
			this.exerciseVarbitMatMultimap.replaceValues(exerciseVarbitValue, ImmutableSet.of());
		}
	}

	// <Post Number, Mat>
	private Multimap<Integer, GroundObject> exerciseMatsMultimap = HashMultimap.create(4, 2);

	// <Exercise Varbit, Mat>
	private Multimap<Integer, GroundObject> exerciseVarbitMatMultimap = HashMultimap.create(4, 2);

	@Subscribe
	public void onGroundObjectSpawned(GroundObjectSpawned groundObjectSpawned)
	{
		if (this.client.getLocalPlayer().getWorldLocation().getRegionID() == 12619)
		{
			switch (groundObjectSpawned.getGroundObject().getId())
			{
				case ObjectID.BARRACK_MAT_1:
					exerciseMatsMultimap.put(1, groundObjectSpawned.getGroundObject());
					log.debug("Added exercise mat with ID {} to post 1", groundObjectSpawned.getGroundObject().getId());
					break;
				case ObjectID.BARRACK_MAT_2:
					exerciseMatsMultimap.put(2, groundObjectSpawned.getGroundObject());
					log.debug("Added exercise mat with ID {} to post 2", groundObjectSpawned.getGroundObject().getId());
					break;
				case ObjectID.BARRACK_MAT_3:
					exerciseMatsMultimap.put(3, groundObjectSpawned.getGroundObject());
					log.debug("Added exercise mat with ID {} to post 3", groundObjectSpawned.getGroundObject().getId());
					break;
				case ObjectID.BARRACK_MAT_4:
					exerciseMatsMultimap.put(4, groundObjectSpawned.getGroundObject());
					log.debug("Added exercise mat with ID {} to post 4", groundObjectSpawned.getGroundObject().getId());
					break;
				default:
					break;
			}
		}
	}

	private Multiset<Integer> previousInventory = HashMultiset.create();
	private Multiset<Integer> currentInventoryItems = HashMultiset.create();
	@Getter
	private Set<Integer> coffinsInInventory = new HashSet<>();

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged itemContainerChanged)
	{
		// In case of unequipping an item -> INVENTORY -> EQUIPMENT changes
		if (itemContainerChanged.getContainerId() == InventoryID.INV)
		{
			this.currentInventoryItems.clear();
			List<Item> itemStream = Arrays.stream(itemContainerChanged.getItemContainer().getItems()).filter(item -> item.getId() != -1).collect(Collectors.toList());
			itemStream.forEach(item -> this.currentInventoryItems.add(item.getId(), this.itemManager.getItemComposition(item.getId()).isStackable() ? 1 : item.getQuantity()));

			Multiset<Integer> currentInventory = HashMultiset.create();
			List<Item> inventoryItems = Arrays.stream(itemContainerChanged.getItemContainer().getItems()).filter(item -> item.getId() != -1).collect(Collectors.toList());
			inventoryItems.forEach(item -> currentInventory.add(item.getId(), item.getQuantity()));

			// Remember that for set operations difference A - B != B - A
			Multiset<Integer> addedItems = Multisets.difference(currentInventory, this.previousInventory);
			Multiset<Integer> removedItems = Multisets.difference(this.previousInventory, currentInventory);
			log.debug("Added Items: " + addedItems);
			log.debug("Removed Items: " + removedItems);

			for (Integer itemID : addedItems.elementSet())
			{
				if (Coffin.getCoffinFromItemID(itemID) != null)
				{
					this.coffinsInInventory.add(itemID);
					log.debug("Found {} coffin in inventory", Coffin.getCoffinFromItemID(itemID).name());
				}
			}

			this.previousInventory = currentInventory;
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded menuEntryAdded)
	{
		if (RandomEventSolverPlugin.isInRandomEventLocalInstance(this.client))
		{
			if (Coffin.getCoffinFromItemID(menuEntryAdded.getItemId()) != null && menuEntryAdded.getOption().equals("Check"))
			{
				menuEntryAdded.getMenuEntry().setDeprioritized(true);
			}
		}
	}

	@Provides
	RandomEventSolverConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RandomEventSolverConfig.class);
	}

	// Accounts for local instances too such as inside the pinball and gravekeeper random event
	public static int getRegionIDFromCurrentLocalPointInstanced(Client client)
	{
		return WorldPoint.fromLocalInstance(client, client.getLocalPlayer().getLocalLocation()).getRegionID();
	}

	public static boolean isInRandomEventLocalInstance(Client client) {
		return RandomEventSolverPlugin.getRegionIDFromCurrentLocalPointInstanced(client) == 7758;
	}

	@Getter
	@AllArgsConstructor
	enum DrillExercise
	{
		JOG(1, "Get yourself over there and jog on that mat, private!"),
		SIT_UP(2, "Get on that mat and give me sit ups, private!"),
		PUSH_UP(3, "Drop and give me push ups on that mat, private!"),
		STAR_JUMP(4, "I want to see you on that mat doing star jumps, private!");

		private final int varbitValue;
		private final String drillSergeantText;

		private static final Map<Integer, DrillExercise> VARBIT_TO_EXERCISE_MAP = Maps.uniqueIndex(ImmutableList.copyOf(values()), DrillExercise::getVarbitValue);

		private static DrillExercise getExerciseFromText(String text)
		{
			for (DrillExercise exercise : values())
			{
				if (text.toLowerCase().endsWith(exercise.getDrillSergeantText().toLowerCase()))
				{
					return exercise;
				}
			}
			return null;
		}
	}
}
