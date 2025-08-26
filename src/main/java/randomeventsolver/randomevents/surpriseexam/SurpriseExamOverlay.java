package randomeventsolver.randomevents.surpriseexam;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;
import randomeventsolver.RandomEventSolverPlugin;

public class SurpriseExamOverlay extends Overlay
{
	private final Client client;
	private final SurpriseExamHelper plugin;

	@Inject
	public SurpriseExamOverlay(Client client, SurpriseExamHelper plugin, ModelOutlineRenderer modelOutlineRenderer, SpriteManager spriteManager)
	{
		this.client = client;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
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
		return null;
	}
}
