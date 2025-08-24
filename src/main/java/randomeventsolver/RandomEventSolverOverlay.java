package randomeventsolver;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.util.Objects;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GroundObject;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

public class RandomEventSolverOverlay extends Overlay
{

	private final Client client;
	private final RandomEventSolverPlugin plugin;
	private final ModelOutlineRenderer modelOutlineRenderer;

	@Inject
	public RandomEventSolverOverlay(Client client, RandomEventSolverPlugin plugin, ModelOutlineRenderer modelOutlineRenderer)
	{
		this.client = client;
		this.plugin = plugin;
		this.modelOutlineRenderer = modelOutlineRenderer;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics2D)
	{
		if (plugin.getPatternCardAnswerWidgets() != null && !plugin.getPatternCardAnswerWidgets().isEmpty())
		{
			for (Widget answerWidget : plugin.getPatternCardAnswerWidgets())
			{
				if (answerWidget != null && !answerWidget.isHidden())
				{
					OverlayUtil.renderPolygon(graphics2D, answerWidget.getBounds(), Color.GREEN);
				}
			}
		}

		if (plugin.getPatternNextAnswer() != null)
		{
			OverlayUtil.renderPolygon(graphics2D, plugin.getPatternNextAnswerWidget().getBounds(), Color.GREEN);
		}

		if (plugin.getBeehiveAnswerWidgets() != null && !plugin.getBeehiveAnswerWidgets().isEmpty())
		{
			for (int i = 0; i < plugin.getBeehiveAnswerWidgets().size(); i++)
			{
				Widget answerWidget = plugin.getBeehiveAnswerWidgets().get(i);
				if (answerWidget != null && !answerWidget.isHidden() && answerWidget.getModelId() != -1)
				{
					graphics2D.setFont(graphics2D.getFont().deriveFont(18f));
					String text = String.valueOf(i + 1);
					Point textPoint = new Point(
						answerWidget.getBounds().x + (answerWidget.getBounds().width / 2) - (graphics2D.getFontMetrics().stringWidth(text) / 2),
						answerWidget.getBounds().y + (answerWidget.getBounds().height / 2) + (graphics2D.getFontMetrics().getHeight() / 2) - graphics2D.getFontMetrics().getDescent()
					);
					OverlayUtil.renderTextLocation(graphics2D, textPoint, text, Color.YELLOW);
				}
			}
		}

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

		if (plugin.getActivePinballPost() != null)
		{
			OverlayUtil.renderPolygon(graphics2D, plugin.getActivePinballPost().getConvexHull(), Color.GREEN);
		}

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

		return null;
	}
}
