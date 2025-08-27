package randomeventhelper.randomevents.pinball;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

@Slf4j
@Singleton
public class PinballOverlay extends Overlay
{
	private final Client client;
	private final PinballHelper plugin;

	@Inject
	public PinballOverlay(Client client, PinballHelper plugin)
	{
		this.client = client;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics2D)
	{
		if (plugin.getActivePinballPost() != null)
		{
			OverlayUtil.renderPolygon(graphics2D, plugin.getActivePinballPost().getConvexHull(), Color.GREEN);
		}
		return null;
	}
}
