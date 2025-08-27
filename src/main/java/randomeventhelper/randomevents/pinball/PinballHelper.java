package randomeventhelper.randomevents.pinball;

import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.DynamicObject;
import net.runelite.api.GameObject;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;
import randomeventhelper.RandomEventHelperPlugin;

@Slf4j
@Singleton
public class PinballHelper
{
	@Inject
	private EventBus eventBus;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private PinballOverlay pinballOverlay;

	@Getter
	private GameObject activePinballPost;

	private final Set<Integer> PINBALL_POST_OBJECTS_SET = ImmutableSet.of(ObjectID.PINBALL_POST_TREE_INACTIVE, ObjectID.PINBALL_POST_IRON_INACTIVE, ObjectID.PINBALL_POST_COAL_INACTIVE, ObjectID.PINBALL_POST_FISHING_INACTIVE, ObjectID.PINBALL_POST_ESSENCE_INACTIVE);
	private Set<GameObject> pinballPostsSet = new HashSet<>();

	public void startUp()
	{
		this.eventBus.register(this);
		this.overlayManager.add(pinballOverlay);
	}

	public void shutDown()
	{
		this.eventBus.unregister(this);
		this.overlayManager.remove(pinballOverlay);
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		if (RandomEventHelperPlugin.isInRandomEventLocalInstance(this.client))
		{
			for (GameObject pinballObject : this.pinballPostsSet)
			{
				if (pinballObject != null && pinballObject.getRenderable() instanceof DynamicObject)
				{
					DynamicObject dynamicPinballObject = (DynamicObject) pinballObject.getRenderable();
					if (dynamicPinballObject != null && dynamicPinballObject.getAnimation().getId() == 4005)
					{
						this.activePinballPost = pinballObject;
						log.debug("Active pinball post found with ID: {}", this.activePinballPost.getId());
						break; // Exit the loop once we find the active post
					}
				}
			}
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		if (npcDespawned.getNpc().getId() == NpcID.PINBALL_TROLL_LFT && npcDespawned.getNpc().getId() == NpcID.PINBALL_TROLL_RHT)
		{
			log.debug("A pinball troll despawned, resetting active pinball post.");
			this.activePinballPost = null;
			this.pinballPostsSet = new HashSet<>();
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned gameObjectSpawned)
	{
		GameObject gameObject = gameObjectSpawned.getGameObject();
		// Pinball and grave digger random even locations are in region 7758
		if (RandomEventHelperPlugin.isInRandomEventLocalInstance(this.client))
		{
			if (PINBALL_POST_OBJECTS_SET.contains(gameObject.getId()))
			{
				log.debug("A new pinball post object spawned with ID: {}, adding to the set.", gameObject.getId());
				this.pinballPostsSet.add(gameObject);
			}
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		String sanitizedChatMessage = Text.sanitizeMultilineText(chatMessage.getMessage());
		if (chatMessage.getType() == ChatMessageType.GAMEMESSAGE)
		{
			if (RandomEventHelperPlugin.isInRandomEventLocalInstance(this.client) && sanitizedChatMessage.equals("You may now leave the game area."))
			{
				log.debug("Pinball game has ended so resetting active pinball post and pinball posts set.");
				this.activePinballPost = null;
				this.pinballPostsSet = new HashSet<>();
			}
		}
	}
}
