package randomeventhelper.randomevents.quizmaster;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.OverlayManager;
import randomeventhelper.RandomEventHelperConfig;
import randomeventhelper.pluginmodulesystem.PluginModule;

@Slf4j
@Singleton
public class QuizMasterHelper extends PluginModule
{
	@Inject
	private ClientThread clientThread;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private QuizMasterOverlay quizMasterOverlay;

	@Getter
	private Widget quizAnswerWidget;

	@Inject
	public QuizMasterHelper(OverlayManager overlayManager, RandomEventHelperConfig config, Client client)
	{
		super(overlayManager, config, client);
	}

	@Override
	public void onStartUp()
	{
		this.overlayManager.add(quizMasterOverlay);
		this.quizAnswerWidget = null;

		if (this.isLoggedIn())
		{
			this.clientThread.invokeLater(() -> {
				if (this.client.getWidget(InterfaceID.MacroQuizshow.BUTTONS) != null)
				{
					WidgetLoaded quizMasterButtonsWidgetLoaded = new WidgetLoaded();
					quizMasterButtonsWidgetLoaded.setGroupId(InterfaceID.MACRO_QUIZSHOW);
					this.eventBus.post(quizMasterButtonsWidgetLoaded);
				}
			});
		}
	}

	@Override
	public void onShutdown()
	{
		this.overlayManager.remove(quizMasterOverlay);
		this.quizAnswerWidget = null;
	}

	@Override
	public boolean isEnabled()
	{
		return this.config.isQuizMasterEnabled();
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		if (widgetLoaded.getGroupId() == InterfaceID.MACRO_QUIZSHOW)
		{
			log.debug("Quiz Master event started");
			this.clientThread.invokeLater(() -> {
				Widget buttonContainerWidget = this.client.getWidget(InterfaceID.MacroQuizshow.BUTTONS);
				if (buttonContainerWidget != null && !buttonContainerWidget.isHidden())
				{
					Set<Widget> quizAnswerButtons = Arrays.stream(buttonContainerWidget.getDynamicChildren()).filter(Objects::nonNull).collect(Collectors.toSet());
					if (!quizAnswerButtons.isEmpty())
					{
						Map<QuizItem.Type, Set<Widget>> quizTypeButtonSetMap = quizAnswerButtons.stream().filter(Objects::nonNull).collect(Collectors.groupingBy(
							quizAnswerWidget -> QuizItem.fromModelID(quizAnswerWidget.getModelId()).getType(),
							Collectors.toSet()
						));
						boolean foundOddOneOut = false;
						for (Map.Entry<QuizItem.Type, Set<Widget>> entry : quizTypeButtonSetMap.entrySet())
						{
							QuizItem.Type quizSetType = entry.getKey();
							Set<Widget> quizSetButtons = entry.getValue();
							if (quizSetButtons.size() == 1)
							{
								Widget quizAnswerWidget = quizSetButtons.stream().findFirst().orElse(null); // Shouldn't be null ever since the set is non-empty
								QuizItem quizItemAnswer = QuizItem.fromModelID(quizAnswerWidget.getModelId());
								log.debug("Found the odd one out: {}", quizItemAnswer);
								this.quizAnswerWidget = quizAnswerWidget;
								foundOddOneOut = true;
								break;
							}
						}
						if (!foundOddOneOut)
						{
							log.warn("Could not find the odd one out in the quiz answer buttons.");
							this.quizAnswerWidget = null;
						}
					}
					else
					{
						log.warn("No quiz answer buttons found in the button container widget.");
						this.quizAnswerWidget = null;
					}
				}
			});
		}
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed widgetClosed)
	{
		if (widgetClosed.getGroupId() == InterfaceID.MACRO_QUIZSHOW)
		{
			log.debug("Quiz Master widget closed - Event ended");
			this.quizAnswerWidget = null;
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		if (npcDespawned.getNpc().getId() == NpcID.MACRO_MAGNESON)
		{
			log.debug("Quiz Master NPC despawned - Event ended");
			this.quizAnswerWidget = null;
		}
	}
}
