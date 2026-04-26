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
import randomeventhelper.RandomEventHelperConfig;

public class FrogOverlay extends Overlay
{
	private final Client client;
	private final RandomEventHelperConfig config;
	private final FrogHelper plugin;

	@Inject
	public FrogOverlay(Client client, RandomEventHelperConfig config, FrogHelper plugin)
	{
		this.client = client;
		this.config = config;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics2D)
	{
		if (this.plugin.isEventActiveForPlayer() && this.plugin.getCrownedFrogNPC() != null)
		{
			OverlayUtil.renderPolygon(graphics2D, this.plugin.getCrownedFrogNPC().getConvexHull(), this.config.highlightColor());
		}
		return null;
	}
}
