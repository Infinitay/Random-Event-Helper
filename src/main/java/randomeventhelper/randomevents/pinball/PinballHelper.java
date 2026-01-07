package randomeventhelper.randomevents.pinball;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;
import randomeventhelper.RandomEventHelperConfig;
import randomeventhelper.RandomEventHelperPlugin;
import randomeventhelper.pluginmodulesystem.PluginModule;

@Slf4j
@Singleton
public class PinballHelper extends PluginModule
{
	@Inject
	private ClientThread clientThread;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private PinballOverlay pinballOverlay;

	@Getter
	private GameObject activePinballPost;

	// <Varbit value, Object ID>
	private Map<Integer, GameObject> pinballPostsMap;

	@Inject
	public PinballHelper(OverlayManager overlayManager, RandomEventHelperConfig config, Client client)
	{
		super(overlayManager, config, client);
	}

	@Override
	public void onStartUp()
	{
		this.overlayManager.add(pinballOverlay);
		this.activePinballPost = null;
		this.pinballPostsMap = Maps.newHashMap();

		if (this.isLoggedIn())
		{
			this.initiateActivePost();
		}
	}

	@Override
	public void onShutdown()
	{
		this.overlayManager.remove(pinballOverlay);
		this.activePinballPost = null;
		this.pinballPostsMap = null;
	}

	@Override
	public boolean isEnabled()
	{
		return this.config.isPinballEnabled();
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		if (npcDespawned.getNpc().getId() == NpcID.PINBALL_TROLL_LFT && npcDespawned.getNpc().getId() == NpcID.PINBALL_TROLL_RHT)
		{
			log.debug("A pinball troll despawned, resetting active pinball post.");
			this.activePinballPost = null;
			this.pinballPostsMap = Maps.newHashMap();
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned gameObjectSpawned)
	{
		GameObject gameObject = gameObjectSpawned.getGameObject();
		// Pinball and grave digger random even locations are in region 7758
		if (RandomEventHelperPlugin.isInRandomEventLocalInstance(this.client))
		{
			PinballPost pinballPost = PinballPost.fromObjectID(gameObject.getId());
			if (pinballPost != null)
			{
				log.debug("A pinball post has spawned: {}", pinballPost);
				this.pinballPostsMap.put(pinballPost.getVarbitValue(), gameObject);
				if (this.activePinballPost == null)
				{
					this.initiateActivePost();
				}
			}
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged varbitChanged)
	{
		if (RandomEventHelperPlugin.isInRandomEventLocalInstance(this.client) && varbitChanged.getVarbitId() == VarbitID.MACRO_PINBALL_CURRENT)
		{
			int value = varbitChanged.getValue();
			PinballPost pinballPost = PinballPost.fromVarbitValue(value);
			if (pinballPost != null)
			{
				log.debug("The active pinball post has changed to: {}", pinballPost);
				this.activePinballPost = this.pinballPostsMap.get(value);
				if (this.activePinballPost == null)
				{
					log.warn("The active pinball post game object is null for pinball post: {}", pinballPost);
				}
			}
			else
			{
				log.debug("The active pinball post varbit changed to an invalid value: {}", value);
				this.activePinballPost = null;
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
				this.pinballPostsMap = Maps.newHashMap();
			}
		}
	}

	private void initiateActivePost()
	{
		this.clientThread.invokeLater(() ->
		{
			int postVarbitValue = this.client.getVarbitValue(VarbitID.MACRO_PINBALL_CURRENT);
			VarbitChanged varbitChangedEvent = new VarbitChanged();
			varbitChangedEvent.setVarbitId(VarbitID.MACRO_PINBALL_CURRENT);
			varbitChangedEvent.setValue(postVarbitValue);
			this.onVarbitChanged(varbitChangedEvent);
		});
	}
}
