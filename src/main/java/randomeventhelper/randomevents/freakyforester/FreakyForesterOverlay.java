package randomeventhelper.randomevents.freakyforester;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import randomeventhelper.RandomEventHelperConfig;

@Slf4j
@Singleton
public class FreakyForesterOverlay extends Overlay
{
	private final Client client;
	private final RandomEventHelperConfig config;
	private final FreakyForesterHelper plugin;

	@Inject
	public FreakyForesterOverlay(Client client, RandomEventHelperConfig config, FreakyForesterHelper plugin)
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
		switch (plugin.getPheasantHighlightMode())
		{
			case SPECIFIC:
				if (plugin.getSpecificPheasantNPC() != null && !plugin.getSpecificPheasantNPC().isDead())
				{
					OverlayUtil.renderPolygon(graphics2D, plugin.getSpecificPheasantNPC().getConvexHull(), this.config.highlightColor());
				}
				else if (plugin.getSpecificPheasantNPC() == null && plugin.getFreakyForesterNPC() != null)
				{
					OverlayUtil.renderActorOverlay(graphics2D, (Actor) plugin.getFreakyForesterNPC(), "Talk to Freaky Forester to determine pheasant", Color.YELLOW);
				}
				break;
			case NEAREST:
				if (plugin.getNearestPheasantNPC() != null && !plugin.getNearestPheasantNPC().isDead())
				{
					OverlayUtil.renderPolygon(graphics2D, plugin.getNearestPheasantNPC().getConvexHull(), this.config.highlightColor());
				}
				break;
			case ALL:
				for (NPC pheasantNPC : plugin.getPheasantNPCSet())
				{
					if (pheasantNPC != null && !pheasantNPC.isDead())
					{
						OverlayUtil.renderPolygon(graphics2D, pheasantNPC.getConvexHull(), this.config.highlightColor());
					}
				}
				break;
			default:
				break;
		}
		return null;
	}
}
