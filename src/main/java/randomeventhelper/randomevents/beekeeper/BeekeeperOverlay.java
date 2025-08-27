package randomeventhelper.randomevents.beekeeper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

@Slf4j
@Singleton
public class BeekeeperOverlay extends Overlay
{
	private final Client client;
	private final BeekeeperHelper plugin;

	@Inject
	public BeekeeperOverlay(Client client, BeekeeperHelper plugin)
	{
		this.client = client;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics2D)
	{
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
