package randomeventsolver;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;
import net.runelite.client.util.Text;

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
		{

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

		return null;
	}
}
