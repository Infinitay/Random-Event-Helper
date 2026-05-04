package randomeventhelper.randomevents.quizmaster;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import randomeventhelper.RandomEventHelperConfig;
import randomeventhelper.RandomEventHelperOverlay;

@Slf4j
@Singleton
public class QuizMasterOverlay extends Overlay
{
	private final Client client;
	private final RandomEventHelperConfig config;
	private final QuizMasterHelper plugin;

	@Inject
	public QuizMasterOverlay(Client client, RandomEventHelperConfig config, QuizMasterHelper plugin)
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
		if (plugin.getQuizAnswerWidget() != null && !plugin.getQuizAnswerWidget().isHidden())
		{
			RandomEventHelperOverlay.renderOverlayWithFill(graphics2D, this.plugin.getQuizAnswerWidget().getBounds(), this.config.borderColor(), this.config.fillColor());
		}
		return null;
	}
}
