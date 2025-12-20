package randomeventhelper.randomevents.drilldemon;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GroundObject;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

@Slf4j
@Singleton
public class DrillDemonOverlay extends Overlay
{
	private final Client client;
	private final DrillDemonHelper plugin;

	@Inject
	public DrillDemonOverlay(Client client, DrillDemonHelper plugin)
	{
		this.client = client;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics2D)
	{
		if (plugin.getExerciseMatsAnswerList() != null && !plugin.getExerciseMatsAnswerList().isEmpty())
		{
			// Get the non-null ground objects, map it to their convex hulls, and then combine them into a single shape
			Area combinedMatHull = new Area();
			plugin.getExerciseMatsAnswerList().stream().filter(Objects::nonNull).map(GroundObject::getConvexHull).forEach(hull -> combinedMatHull.add(new Area(hull)));
			for (GroundObject exerciseGroundObject : plugin.getExerciseMatsAnswerList())
			{
				if (exerciseGroundObject != null)
				{
					OverlayUtil.renderPolygon(graphics2D, combinedMatHull, Color.GREEN);
				}
			}
		}

		if (plugin.getDrillDemonNPC() != null)
		{
			if (plugin.getRequestedExercise() != null)
			{
				OverlayUtil.renderActorOverlay(graphics2D, plugin.getDrillDemonNPC(), plugin.getRequestedExercise().name(), Color.WHITE);
			}
			else
			{
				OverlayUtil.renderActorOverlay(graphics2D, plugin.getDrillDemonNPC(), "Talk to Sergeant Damien to determine the required exercise", Color.YELLOW);
			}
		}
		return null;
	}
}
