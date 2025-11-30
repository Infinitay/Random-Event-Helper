package randomeventhelper.randomevents.gravedigger;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.collect.Sets;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Item;
import net.runelite.api.NPC;
import net.runelite.api.Tile;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.overlay.OverlayManager;
import randomeventhelper.RandomEventHelperPlugin;

@Slf4j
@Singleton
public class GravediggerHelper
{
	@Inject
	private EventBus eventBus;

	@Inject
	private Client client;

	@Inject
	private ItemManager itemManager;

	@Inject
	private SpriteManager spriteManager;

	@Inject
	private OverlayManager overlayManager;

	private GravediggerOverlay gravediggerOverlay;

	@Inject
	private GravediggerItemOverlay gravediggerItemOverlay;

	private boolean initiallyEnteredGraveDiggerArea;

	// <Grave Number, Grave>
	@Getter
	private Map<GraveNumber, Grave> graveMap;

	@Getter
	private Map<Coffin, BufferedImage> coffinItemImageMap;

	private Multiset<Integer> previousInventory;
	private Multiset<Integer> currentInventoryItems;

	@Getter
	private Set<Integer> coffinsInInventory;

	public void startUp(GravediggerOverlay gravediggerOverlay)
	{
		this.gravediggerOverlay = gravediggerOverlay;
		this.eventBus.register(this);
		this.overlayManager.add(this.gravediggerOverlay);
		this.overlayManager.add(gravediggerItemOverlay);
		this.initiallyEnteredGraveDiggerArea = true;
		this.graveMap = Maps.newHashMapWithExpectedSize(5);
		this.coffinItemImageMap = Maps.newHashMapWithExpectedSize(5);
		this.previousInventory = HashMultiset.create();
		this.currentInventoryItems = HashMultiset.create();
		this.coffinsInInventory = Sets.newHashSetWithExpectedSize(5);
	}

	public void shutDown()
	{
		this.eventBus.unregister(this);
		if (this.gravediggerOverlay != null)
		{
			this.overlayManager.remove(gravediggerOverlay);
			this.gravediggerOverlay = null;
		}
		this.overlayManager.remove(gravediggerItemOverlay);
		this.initiallyEnteredGraveDiggerArea = true;
		this.graveMap = null;
		this.coffinItemImageMap = null;
		this.previousInventory = null;
		this.currentInventoryItems = null;
		this.coffinsInInventory = null;
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		// There is an edgecase where when you're at the grave digger random event if a varb is still 0 then it won't fire.
		// So lets handle this by checking to see if a player is in the grave digger random event area via NPC Leo spawn
		// And by using a separate variable to make sure not to run this constantly every game tick
		if (this.initiallyEnteredGraveDiggerArea)
		{
			if (this.currentInventoryItems.isEmpty())
			{
				ItemContainerChanged itemContainerChangedEvent = new ItemContainerChanged(InventoryID.INV, this.client.getItemContainer(InventoryID.INV));
				this.onItemContainerChanged(itemContainerChangedEvent);
			}

			if (this.graveMap.isEmpty())
			{
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
							}
						}
					}
				}
			}

			for (GraveNumber graveNumber : GraveNumber.values())
			{
				VarbitChanged graveTypeVarbitChangedEvent = new VarbitChanged();
				graveTypeVarbitChangedEvent.setVarbitId(graveNumber.getGraveTypeVarbitID());
				graveTypeVarbitChangedEvent.setValue(this.client.getVarbitValue(graveNumber.getGraveTypeVarbitID()));
				VarbitChanged placedCoffinVarbitChangedEvent = new VarbitChanged();
				placedCoffinVarbitChangedEvent.setVarbitId(graveNumber.getPlacedCoffinVarbitID());
				placedCoffinVarbitChangedEvent.setValue(this.client.getVarbitValue(graveNumber.getPlacedCoffinVarbitID()));
				this.onVarbitChanged(graveTypeVarbitChangedEvent);
				this.onVarbitChanged(placedCoffinVarbitChangedEvent);
			}

			if (this.coffinItemImageMap.isEmpty())
			{
				for (Coffin coffin : Coffin.values())
				{
					coffinItemImageMap.put(coffin, coffin.getItemImage(this.itemManager));
				}
			}

			this.initiallyEnteredGraveDiggerArea = false;
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged varbitChanged)
	{
		if (!RandomEventHelperPlugin.isInRandomEventLocalInstance(this.client))
		{
			return;
		}
		switch (varbitChanged.getVarbitId())
		{
			case VarbitID.MACRO_DIGGER_GRAVE_1: // Grave type/Gravestone
				this.updateRequiredCoffin(GraveNumber.ONE, varbitChanged.getValue());
				break;
			case VarbitID.MACRO_DIGGER_COFFIN_1: // Placed coffin into the grave
				this.updatePlacedCoffin(GraveNumber.ONE, varbitChanged.getValue());
				break;
			case VarbitID.MACRO_DIGGER_GRAVE_2:
				this.updateRequiredCoffin(GraveNumber.TWO, varbitChanged.getValue());
				break;
			case VarbitID.MACRO_DIGGER_COFFIN_2:
				this.updatePlacedCoffin(GraveNumber.TWO, varbitChanged.getValue());
				break;
			case VarbitID.MACRO_DIGGER_GRAVE_3:
				this.updateRequiredCoffin(GraveNumber.THREE, varbitChanged.getValue());
				break;
			case VarbitID.MACRO_DIGGER_COFFIN_3:
				this.updatePlacedCoffin(GraveNumber.THREE, varbitChanged.getValue());
				break;
			case VarbitID.MACRO_DIGGER_GRAVE_4:
				this.updateRequiredCoffin(GraveNumber.FOUR, varbitChanged.getValue());
				break;
			case VarbitID.MACRO_DIGGER_COFFIN_4:
				this.updatePlacedCoffin(GraveNumber.FOUR, varbitChanged.getValue());
				break;
			case VarbitID.MACRO_DIGGER_GRAVE_5:
				this.updateRequiredCoffin(GraveNumber.FIVE, varbitChanged.getValue());
				break;
			case VarbitID.MACRO_DIGGER_COFFIN_5:
				this.updatePlacedCoffin(GraveNumber.FIVE, varbitChanged.getValue());
				break;
			default:
				break;
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned gameObjectSpawned)
	{
		// Pinball and grave digger random even locations are in region 7758
		if (RandomEventHelperPlugin.isInRandomEventLocalInstance(this.client))
		{
			GameObject gameObject = gameObjectSpawned.getGameObject();
			if (GraveNumber.isGravestoneObjectID(gameObject.getId()))
			{
				GraveNumber graveNumber = GraveNumber.getGraveNumberFromGravestoneObjectID(gameObject.getId());
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
			else if (GraveNumber.isEmptyGraveObjectID(gameObject.getId()))
			{
				GraveNumber graveNumber = GraveNumber.getGraveNumberFromEmptyGraveObjectID(gameObject.getId());
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
			else if (GraveNumber.isFilledGraveObjectID(gameObject.getId()))
			{
				GraveNumber graveNumber = GraveNumber.getGraveNumberFromFilledGraveObjectID(gameObject.getId());
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

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		NPC npc = npcSpawned.getNpc();
		if (RandomEventHelperPlugin.isInRandomEventLocalInstance(this.client))
		{
			if (npc.getId() == NpcID.MACRO_GRAVEDIGGER)
			{
				log.debug("Grave Digger Leo NPC spawned in grave digger random event area.");
				this.initiallyEnteredGraveDiggerArea = true;
			}
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		if (npcDespawned.getNpc().getId() == NpcID.MACRO_GRAVEDIGGER)
		{
			log.debug("Grave Digger Leo NPC despawned, resetting grave digger area state.");
			this.initiallyEnteredGraveDiggerArea = false;
			this.graveMap.clear();
			this.coffinItemImageMap.clear();
			this.previousInventory.clear();
			this.currentInventoryItems.clear();
			this.coffinsInInventory.clear();
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged itemContainerChanged)
	{
		// In case of unequipping an item -> INVENTORY -> EQUIPMENT changes
		if (itemContainerChanged.getContainerId() == InventoryID.INV && RandomEventHelperPlugin.isInRandomEventLocalInstance(this.client))
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
			log.debug("Added Items: {}", addedItems);
			log.debug("Removed Items: {}", removedItems);

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
		if (RandomEventHelperPlugin.isInRandomEventLocalInstance(this.client))
		{
			if (Coffin.getCoffinFromItemID(menuEntryAdded.getItemId()) != null && menuEntryAdded.getOption().equals("Check"))
			{
				menuEntryAdded.getMenuEntry().setDeprioritized(true);
			}
		}
	}

	private void updateRequiredCoffin(GraveNumber graveNumber, int requiredCoffinVarbitValue)
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

	private void updatePlacedCoffin(GraveNumber graveNumber, int placedCoffinVarbitValue)
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
}
