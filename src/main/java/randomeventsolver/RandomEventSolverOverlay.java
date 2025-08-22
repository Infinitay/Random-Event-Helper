package randomeventsolver;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
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
			for (Widget answerWidget : plugin.getPatternCardAnswerWidgets()) {
				if (answerWidget != null && !answerWidget.isHidden())
				{
					Widget parentWidget = answerWidget.getParent();
					OverlayUtil.renderPolygon(graphics2D, answerWidget.getBounds(), Color.GREEN);
				}
			}
		}
		{

		}

		if (plugin.getPatternNextAnswer() != null) {
			OverlayUtil.renderPolygon(graphics2D, plugin.getPatternNextAnswerWidget().getBounds(), Color.GREEN);
		}

		return null;
	}
}
