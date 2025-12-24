package randomeventhelper.randomevents.mime;

import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.OverlayManager;
import randomeventhelper.RandomEventHelperConfig;
import randomeventhelper.RandomEventHelperPlugin;
import randomeventhelper.pluginmodulesystem.PluginModule;

@Slf4j
@Singleton
public class MimeHelper extends PluginModule
{
	@Inject
	private ClientThread clientThread;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private MimeOverlay mimeOverlay;

	@Getter
	private NPC mimeNPC;

	@Getter
	private MimeEmote currentMimeEmote;

	@Getter
	private Widget mimeEmoteAnswerWidget;

	private static final int MIME_RANDOM_EVENT_REGION_ID = 8010;

	@Inject
	public MimeHelper(OverlayManager overlayManager, RandomEventHelperConfig config, Client client)
	{
		super(overlayManager, config, client);
	}

	@Override
	public void onStartUp()
	{
		this.overlayManager.add(mimeOverlay);
		this.mimeNPC = null;
		this.currentMimeEmote = null;
		this.mimeEmoteAnswerWidget = null;
	}

	@Override
	public void onShutdown()
	{
		this.overlayManager.remove(mimeOverlay);
		this.mimeNPC = null;
		this.currentMimeEmote = null;
		this.mimeEmoteAnswerWidget = null;
	}

	@Override
	public boolean isEnabled()
	{
		return this.config.isMimeEnabled();
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged animationChanged)
	{
		if (this.client.getLocalPlayer() != null && RandomEventHelperPlugin.getRegionIDFromCurrentLocalPointInstanced(this.client) != MIME_RANDOM_EVENT_REGION_ID)
		{
			return;
		}
		NPC mime;
		if (animationChanged.getActor() != null && animationChanged.getActor() instanceof NPC)
		{
			mime = (NPC) animationChanged.getActor();
		}
		else
		{
			return;
		}
		if (mime.getAnimation() != -1 && mime.getAnimation() != 858)
		{
			MimeEmote mimeEmote = MimeEmote.getMimeEmoteFromAnimationID(mime.getAnimation());
			this.currentMimeEmote = mimeEmote;
			if (mimeEmote != null)
			{
				log.debug("Mime Animation Detected: {}", mimeEmote);
			}
			else
			{
				log.debug("Unknown Mime Animation Detected: Animation ID = {}", mime.getAnimation());
			}
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		if (this.currentMimeEmote != null && widgetLoaded.getGroupId() == InterfaceID.MACRO_MIME_EMOTES)
		{
			this.clientThread.invokeLater(() -> {
				log.debug("Mime Emote Widget Loaded, attempting to set answer widget for emote: {}", this.currentMimeEmote);
				Widget emoteAnswerWidgetButtonContainer = this.client.getWidget(this.currentMimeEmote.getButtonWidgetID());
				if (emoteAnswerWidgetButtonContainer != null && !emoteAnswerWidgetButtonContainer.isHidden())
				{
					Widget[] emoteAnswerWidgetChildren = emoteAnswerWidgetButtonContainer.getDynamicChildren();
					if (emoteAnswerWidgetChildren != null && emoteAnswerWidgetChildren.length > 0)
					{
						this.mimeEmoteAnswerWidget = emoteAnswerWidgetChildren[emoteAnswerWidgetChildren.length - 1];
						log.debug("Mime Emote Answer Widget set to: {}", this.mimeEmoteAnswerWidget);
					}
					else
					{
						log.debug("Mime Emote Answer Widget Children is null or empty");
					}
				}
				else
				{
					log.debug("Mime Emote Answer Widget Button Container is null or hidden");
				}
			});
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		if (npcSpawned.getNpc().getId() == NpcID.MACRO_MIME)
		{
			this.mimeNPC = npcSpawned.getNpc();
			log.debug("Mime NPC Spawned, setting mimeNPC: {}", this.mimeNPC);
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		if (npcDespawned.getNpc().getId() == NpcID.MACRO_MIME)
		{
			this.mimeNPC = null;
			this.currentMimeEmote = null;
			this.mimeEmoteAnswerWidget = null;
			log.debug("Mime NPC Despawned, clearing Mime Random Event data");
		}
	}
}
