package randomeventhelper.randomevents.frog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class FrogOverlay extends Overlay
{
	private final Client client;
	private final FrogHelper plugin;

	@Inject
	public FrogOverlay(Client client, FrogHelper plugin)
	{
		this.client = client;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics2D)
	{
		if (this.plugin.isEventActiveForPlayer() && this.plugin.getCrownedFrogNPC() != null)
		{
			OverlayUtil.renderPolygon(graphics2D, this.plugin.getCrownedFrogNPC().getConvexHull(), Color.GREEN);
		}
		return null;
	}
}
