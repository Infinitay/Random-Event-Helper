package randomeventhelper.randomevents.mime;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import randomeventhelper.RandomEventHelperConfig;
import randomeventhelper.RandomEventHelperOverlay;

@Slf4j
@Singleton
public class MimeOverlay extends Overlay
{
	private final Client client;
	private final RandomEventHelperConfig config;
	private final MimeHelper plugin;

	@Inject
	public MimeOverlay(Client client, RandomEventHelperConfig config, MimeHelper plugin)
	{
		this.client = client;
		this.config = config;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}


	@Override
	public Dimension render(Graphics2D graphics2D)
	{
		if (plugin.getMimeEmoteAnswerWidget() != null && !plugin.getMimeEmoteAnswerWidget().isHidden())
		{
			RandomEventHelperOverlay.renderOverlayWithFill(graphics2D, this.plugin.getMimeEmoteAnswerWidget().getBounds(), this.config.borderColor(), this.config.fillColor());
		}

		if (plugin.getMimeNPC() != null)
		{
			String mimeEmoteText = plugin.getCurrentMimeEmote() != null ? plugin.getCurrentMimeEmote().name() : "Waiting for emote";
			int mimeHeight = plugin.getMimeNPC().getLogicalHeight();
			int mimeTextOffset = plugin.getMimeNPC().getAnimationHeightOffset();
			Point textPoint = plugin.getMimeNPC().getCanvasTextLocation(graphics2D, mimeEmoteText, mimeHeight + mimeTextOffset);
			OverlayUtil.renderTextLocation(graphics2D, textPoint, mimeEmoteText, Color.WHITE);
		}
		return null;
	}
}
