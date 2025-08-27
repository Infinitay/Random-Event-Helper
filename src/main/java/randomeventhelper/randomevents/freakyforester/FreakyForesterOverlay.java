package randomeventhelper.randomevents.freakyforester;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

@Slf4j
@Singleton
public class FreakyForesterOverlay extends Overlay
{
	private final Client client;
	private final FreakyForesterHelper plugin;

	@Inject
	public FreakyForesterOverlay(Client client, FreakyForesterHelper plugin)
	{
		this.client = client;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics2D)
	{
		if (plugin.getPheasantNPC() != null && !plugin.getPheasantNPC().isEmpty())
		{
			for (NPC pheasantNPC : plugin.getPheasantNPC())
			{
				if (pheasantNPC != null && !pheasantNPC.isDead())
				{
					OverlayUtil.renderPolygon(graphics2D, pheasantNPC.getConvexHull(), Color.GREEN);
				}
			}
		}
		return null;
	}
}
