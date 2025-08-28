package randomeventhelper.randomevents.mime;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import randomeventhelper.randomevents.gravedigger.GravediggerHelper;

@Slf4j
@Singleton
public class MimeOverlay extends Overlay
{
	private final Client client;
	private final MimeHelper plugin;

	@Inject
	public MimeOverlay(Client client, MimeHelper plugin)
	{
		this.client = client;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}


	@Override
	public Dimension render(Graphics2D graphics2D)
	{
		if (plugin.getMimeEmoteAnswerWidget() != null && !plugin.getMimeEmoteAnswerWidget().isHidden())
		{
			OverlayUtil.renderPolygon(graphics2D, plugin.getMimeEmoteAnswerWidget().getBounds(), Color.GREEN);
		}
		if (plugin.getMimeNPC() != null && plugin.getCurrentMimeEmote() != null)
		{
			String mimeEmoteText = plugin.getCurrentMimeEmote().name();
			graphics2D.setFont(graphics2D.getFont().deriveFont(18f));
			int mimeHeight = plugin.getMimeNPC().getLogicalHeight();
			int mimeTextOffset = plugin.getMimeNPC().getAnimationHeightOffset();
			Point textPoint = plugin.getMimeNPC().getCanvasTextLocation(graphics2D, mimeEmoteText, mimeHeight + mimeTextOffset);
			OverlayUtil.renderTextLocation(graphics2D, textPoint, mimeEmoteText, Color.WHITE);
		}
		return null;
	}
}