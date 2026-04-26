package randomeventhelper.randomevents.surpriseexam;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import randomeventhelper.RandomEventHelperConfig;

@Slf4j
@Singleton
public class SurpriseExamOverlay extends Overlay
{
	private final Client client;
	private final RandomEventHelperConfig config;
	private final SurpriseExamHelper plugin;

	@Inject
	public SurpriseExamOverlay(Client client, RandomEventHelperConfig config, SurpriseExamHelper plugin)
	{
		this.client = client;
		this.config = config;
		this.plugin = plugin;
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
					OverlayUtil.renderPolygon(graphics2D, answerWidget.getBounds(), this.config.highlightColor());
				}
			}
		}

		if (plugin.getPatternNextAnswerWidget() != null)
		{
			OverlayUtil.renderPolygon(graphics2D, plugin.getPatternNextAnswerWidget().getBounds(), this.config.highlightColor());
		}
		return null;
	}
}
