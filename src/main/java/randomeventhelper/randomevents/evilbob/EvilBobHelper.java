/*
 * Copyright (c) 2026, Infinity <https://github.com/Infinitay>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package randomeventhelper.randomevents.evilbob;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.MenuAction;
import net.runelite.api.NPC;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.coords.Angle;
import net.runelite.api.coords.Direction;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;
import randomeventhelper.RandomEventHelperConfig;
import randomeventhelper.RandomEventHelperPlugin;
import randomeventhelper.pluginmodulesystem.PluginModule;

@Slf4j
@Singleton
public class EvilBobHelper extends PluginModule
{
	@Inject
	private ClientThread clientThread;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private EvilBobOverlay evilBobOverlay;
	@Inject
	private EvilBobItemOverlay evilBobItemOverlay;

	@Getter
	private GameObject evilBobPortal;
	@Getter
	private NPC evilBobNPC;
	@Getter
	private NPC servantNPC;
	@Getter
	private Set<GameObject> uncookingPotSet;
	private Map<EvilBobStatue, GameObject> evilBobStatueMap;
	@Getter
	private GameObject correctEvilBobStatue;
	private Set<GameObject> fishingSpotSet;
	@Getter
	private Set<GameObject> correctFishingSpotsSet;
	@Getter
	private boolean isFishInInventory;
	@Getter
	private boolean isCorrectFishInInventory;
	@Getter
	private Set<Tile> fishingNetTileSet;
	@Getter
	private boolean isFishingNetInInventory;
	@Getter
	private FishState fishState;
	@Getter
	private boolean isEventFinished;

	@Inject
	public EvilBobHelper(OverlayManager overlayManager, RandomEventHelperConfig config, Client client)
	{
		super(overlayManager, config, client);
	}

	@Override
	public void onStartUp()
	{
		this.overlayManager.add(this.evilBobOverlay);
		this.overlayManager.add(this.evilBobItemOverlay);
		this.initializeState();
	}

	@Override
	public void onShutdown()
	{
		this.overlayManager.remove(this.evilBobOverlay);
		this.overlayManager.remove(this.evilBobItemOverlay);
		this.evilBobPortal = null;
		this.evilBobNPC = null;
		this.servantNPC = null;
		this.uncookingPotSet = null;
		this.evilBobStatueMap = null;
		this.correctEvilBobStatue = null;
		this.fishingSpotSet = null;
		this.correctFishingSpotsSet = null;
		this.isFishInInventory = false;
		this.isCorrectFishInInventory = false;
		this.fishingNetTileSet = null;
		this.isFishingNetInInventory = false;
		this.fishState = null;
		this.isEventFinished = false;
	}

	@Override
	public boolean isEnabled()
	{
		return this.config.isEvilBobEnabled();
	}

	@Override
	public void onPlayerValidated()
	{
		this.initializeState();
		log.debug("Player validated, checking if Evil Bob statue notice widget is open");
		if (this.client.getWidget(InterfaceID.MacroEvilBob.CONTENT_TEXT1) != null)
		{
			WidgetLoaded evilBobStatueNoticeWidgetLoaded = new WidgetLoaded();
			evilBobStatueNoticeWidgetLoaded.setGroupId(InterfaceID.MACRO_EVIL_BOB);
			this.clientThread.invoke(() -> this.eventBus.post(evilBobStatueNoticeWidgetLoaded));
		}
	}

	private void initializeState()
	{
		log.debug("Initializing Evil Bob state");
		this.evilBobPortal = null;
		this.evilBobNPC = null;
		this.servantNPC = null;
		this.uncookingPotSet = Sets.newHashSet();
		this.evilBobStatueMap = Maps.newHashMap();
		this.correctEvilBobStatue = null;
		this.fishingSpotSet = Sets.newHashSet();
		this.correctFishingSpotsSet = Sets.newHashSet();
		this.isFishInInventory = false;
		this.isCorrectFishInInventory = false;
		this.fishingNetTileSet = Sets.newHashSet();
		this.isFishingNetInInventory = false;
		this.fishState = null;
		this.isEventFinished = false;
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		if (this.isBlockingEvents())
		{
			return;
		}
		if (!this.inEvilBobIslandRegion())
		{
			return;
		}
		if (npcSpawned.getNpc().getId() == NpcID.MACRO_EVIL_BOB_ISLAND)
		{
			this.evilBobNPC = npcSpawned.getNpc();
		}
		else if (npcSpawned.getNpc().getId() == NpcID.MACRO_EVIL_BOB_FEMALE_SERVANT)
		{
			this.servantNPC = npcSpawned.getNpc();
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		if (npcDespawned.getNpc().getId() == NpcID.MACRO_EVIL_BOB_ISLAND)
		{
			this.evilBobNPC = null;
		}
		else if (npcDespawned.getNpc().getId() == NpcID.MACRO_EVIL_BOB_FEMALE_SERVANT)
		{
			this.servantNPC = null;
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned gameObjectSpawned)
	{
		if (this.isBlockingEvents())
		{
			return;
		}
		if (!this.inEvilBobIslandRegion())
		{
			return;
		}
		GameObject gameObject = gameObjectSpawned.getGameObject();
		switch (gameObject.getId())
		{
			case ObjectID.MACRO_EVIL_BOB_UNCOOK_POT:
				this.uncookingPotSet.add(gameObject);
				break;
			case ObjectID.MACRO_EVIL_BOB_STATUE_1:
			case ObjectID.MACRO_EVIL_BOB_STATUE_2:
			case ObjectID.MACRO_EVIL_BOB_STATUE_3:
			case ObjectID.MACRO_EVIL_BOB_STATUE_4:
				this.evilBobStatueMap.put(EvilBobStatue.fromObjectID(gameObject.getId()), gameObject);
				break;
			case ObjectID.MACRO_EVIL_BOB_FISHING_SPOT:
				this.fishingSpotSet.add(gameObject);
				break;
			case ObjectID.MACRO_EVIL_BOB_PORTAL:
				this.evilBobPortal = gameObject;
				break;
			default:
				break;
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned gameObjectDespawned)
	{
		if (gameObjectDespawned.getGameObject().getId() == ObjectID.MACRO_EVIL_BOB_PORTAL)
		{
			log.debug("Evil Bob Portal despawned, event has ended, resetting state");
			this.evilBobPortal = null;
			this.evilBobNPC = null;
			this.servantNPC = null;
			this.uncookingPotSet.clear();
			this.evilBobStatueMap.clear();
			this.correctEvilBobStatue = null;
			this.fishingSpotSet.clear();
			this.correctFishingSpotsSet.clear();
			this.isFishInInventory = false;
			this.isCorrectFishInInventory = false;
			this.fishingNetTileSet.clear();
			this.isFishingNetInInventory = false;
			this.fishState = null;
			this.isEventFinished = false;
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (this.isBlockingEvents())
		{
			return;
		}
		if (this.inEvilBobIslandRegion() && event.getContainerId() == InventoryID.INV)
		{
			// Catching any fish will reset the state of the event back to where you will need to talk to the Servant again to be able to fish
			// Therefore, we'll have to reset statue and fishing spot state
			if (event.getItemContainer().contains(ItemID.EVIL_BOB_COOKED_FISH_CORRECT) || event.getItemContainer().contains(ItemID.EVIL_BOB_COOKED_FISH_INCORRECT))
			{
				this.correctEvilBobStatue = null;
				this.correctFishingSpotsSet.clear();
			}
			if (event.getItemContainer().contains(ItemID.EVIL_BOB_COOKED_FISH_CORRECT) || event.getItemContainer().contains(ItemID.EVIL_BOB_UNCOOKED_FISH_CORRECT))
			{
				this.isFishInInventory = true;
				this.isCorrectFishInInventory = true;
				// Don't worry too much about the double n^2 loop with #contains since we're only running this when the inventory changes and we're in the event area
				// So it shouldn't be happening often... ideally
				if (event.getItemContainer().contains(ItemID.EVIL_BOB_UNCOOKED_FISH_CORRECT))
				{
					this.fishState = FishState.UNCOOKED;
				}
				else
				{
					this.fishState = FishState.COOKED;
				}
			}
			else if (event.getItemContainer().contains(ItemID.EVIL_BOB_COOKED_FISH_INCORRECT) || event.getItemContainer().contains(ItemID.EVIL_BOB_UNCOOKED_FISH_INCORRECT))
			{
				this.isFishInInventory = true;
				this.isCorrectFishInInventory = false;
			}
			else
			{
				this.isFishInInventory = false;
				this.isCorrectFishInInventory = false;
			}
			this.isFishingNetInInventory = event.getItemContainer().contains(ItemID.EVIL_BOB_NET);
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		if (!this.inEvilBobIslandRegion())
		{
			return;
		}
		if (widgetLoaded.getGroupId() == InterfaceID.MACRO_EVIL_BOB)
		{
			log.debug("Evil Bob statue notice widget loaded, checking for correct statue");
			this.clientThread.invokeAtTickEnd(() -> {
				Direction cameraDirection = this.getCameraDirection();
				EvilBobStatue correctStatue = EvilBobStatue.fromDirection(cameraDirection);
				if (correctStatue != null)
				{
					this.correctEvilBobStatue = this.evilBobStatueMap.get(correctStatue);
					log.debug("Found correct Evil Bob Statue at {} of the island", cameraDirection);
					EvilBobFishingSpot correctFishingSpot = EvilBobFishingSpot.getFishingSpotFromStatue(correctStatue);
					this.correctFishingSpotsSet.clear();
					if (correctFishingSpot != null)
					{
						for (GameObject fishingSpot : this.fishingSpotSet)
						{
							if (fishingSpot == null)
							{
								return;
							}
							if (correctFishingSpot.containsFishingSpot(fishingSpot.getWorldLocation()))
							{
								this.correctFishingSpotsSet.add(fishingSpot);
							}
						}
					}
					else
					{
						log.debug("Could not find correct fishing spot for the given statue");
					}
				}
				else
				{
					log.debug("Could not find correct Evil Bob statue");
				}
			});
		}
	}

	@Subscribe
	public void onItemSpawned(ItemSpawned itemSpawned)
	{
		if (this.isBlockingEvents())
		{
			return;
		}
		if (!this.inEvilBobIslandRegion())
		{
			return;
		}
		if (itemSpawned.getItem().getId() == ItemID.EVIL_BOB_NET)
		{
			this.fishingNetTileSet.add(itemSpawned.getTile());
		}
	}

	@Subscribe
	public void onItemDespawned(ItemDespawned itemDespawned)
	{
		if (itemDespawned.getItem().getId() == ItemID.EVIL_BOB_NET)
		{
			this.fishingNetTileSet.remove(itemDespawned.getTile());
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded menuEntryAdded)
	{
		// Cheaper to null check than to region check for something like MEA
		if (this.getEvilBobPortal() != null)
		{
			// Drop the incorrect fish
			if ((menuEntryAdded.getItemId() == ItemID.EVIL_BOB_COOKED_FISH_INCORRECT || menuEntryAdded.getItemId() == ItemID.EVIL_BOB_UNCOOKED_FISH_INCORRECT))
			{
				if (!menuEntryAdded.getOption().equals("Drop"))
				{
					menuEntryAdded.getMenuEntry().setDeprioritized(true);
				}
				else
				{
					// Option is "Drop"
					// Allows left click to drop
					menuEntryAdded.getMenuEntry().setType(MenuAction.CC_OP);
				}
			}
			else if ((menuEntryAdded.getItemId() == ItemID.EVIL_BOB_COOKED_FISH_CORRECT || menuEntryAdded.getItemId() == ItemID.EVIL_BOB_UNCOOKED_FISH_CORRECT))
			{
				if (!menuEntryAdded.getOption().equals("Use"))
				{
					menuEntryAdded.getMenuEntry().setDeprioritized(true);
				}
			}
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		if (!this.inEvilBobIslandRegion())
		{
			return;
		}
		String sanitizedChatMessage = Text.sanitizeMultilineText(chatMessage.getMessage());
		if (chatMessage.getType() == ChatMessageType.DIALOG)
		{
			if (sanitizedChatMessage.startsWith("Evil Bob"))
			{
				sanitizedChatMessage = sanitizedChatMessage.replace("Evil Bob|", "");
				if (sanitizedChatMessage.startsWith(" Mmm, mmm... that's... mmm... delicious...") || sanitizedChatMessage.startsWith("Now, let me take... a little... catnap...") || sanitizedChatMessage.startsWith("ZZZ"))
				{
					log.debug("Player finished the Evil Bob random event");
					this.isEventFinished = true;
				}
				else if (
					// Wrong fish
					sanitizedChatMessage.startsWith("What was this? That was absolutely disgusting!")
						|| sanitizedChatMessage.startsWith("Don't you know what kind of fish I like? Talk to my other servants for some advice.")
						// Correct fish, but the player still has to catch another fish
						|| sanitizedChatMessage.startsWith("Mmm, mmm... that's delicious.")
						|| sanitizedChatMessage.startsWith("Now get me another one, you no good human!")
				)
				{
					log.debug("Player has to catch another fish for Evil Bob, resetting state");
					this.correctEvilBobStatue = null;
					this.correctFishingSpotsSet = Sets.newHashSet();
				}
			}
		}
	}

	private Direction getCameraDirection()
	{
		// Client#getCameraYaw -> N = 0 | E = 12288 | S = 4096 | W = 8192
		// Angle#getNearestDirection -> N = 1024 | E = 1536 | S = 0 | W = 512
		// Take East for example and find the ratio to scale it down -> 12288 / 1536 = 8
		// Angle says the class represents ints ranging from 0-2047 hence the AND to clamp the value to that range
		return new Angle((1024 - (this.client.getCameraYaw() / 8)) & 2047).getNearestDirection();
	}

	/**
	 * Also called ScapeRune
	 */
	public boolean inEvilBobIslandRegion()
	{
		return RandomEventHelperPlugin.getRegionIDFromCurrentLocalPointInstanced(client) == 10058;
	}
}
