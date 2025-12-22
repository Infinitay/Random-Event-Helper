package randomeventhelper.randomevents.surpriseexam;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.CommandExecuted;
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
public class SurpriseExamHelper extends PluginModule
{
	@Inject
	private ClientThread clientThread;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private SurpriseExamOverlay overlay;

	private String patternCardHint;

	@Getter
	private ImmutableSet<RandomEventItem> patternCardAnswers;

	@Getter
	private ImmutableSet<Widget> patternCardAnswerWidgets;

	@Getter
	private RandomEventItem patternNextAnswer;

	@Getter
	private Widget patternNextAnswerWidget;

	private OSRSItemRelationshipSystem relationshipSystem;

	private static final int[] PATTERNCARDS_INTERFACEIDS_AVAILABLE_CARD_MODELS = {
		InterfaceID.PatternCards.CARD_0,
		InterfaceID.PatternCards.CARD_1,
		InterfaceID.PatternCards.CARD_2,
		InterfaceID.PatternCards.CARD_3,
		InterfaceID.PatternCards.CARD_4,
		InterfaceID.PatternCards.CARD_5,
		InterfaceID.PatternCards.CARD_6,
		InterfaceID.PatternCards.CARD_7,
		InterfaceID.PatternCards.CARD_8,
		InterfaceID.PatternCards.CARD_9,
		InterfaceID.PatternCards.CARD_10,
		InterfaceID.PatternCards.CARD_11,
		InterfaceID.PatternCards.CARD_12,
		InterfaceID.PatternCards.CARD_13,
		InterfaceID.PatternCards.CARD_14
	};

	private static final int[] PATTERNCARDS_INTERFACEIDS_AVAILABLE_CARD_SELECTS = {
		InterfaceID.PatternCards.SELECT_0,
		InterfaceID.PatternCards.SELECT_1,
		InterfaceID.PatternCards.SELECT_2,
		InterfaceID.PatternCards.SELECT_3,
		InterfaceID.PatternCards.SELECT_4,
		InterfaceID.PatternCards.SELECT_5,
		InterfaceID.PatternCards.SELECT_6,
		InterfaceID.PatternCards.SELECT_7,
		InterfaceID.PatternCards.SELECT_8,
		InterfaceID.PatternCards.SELECT_9,
		InterfaceID.PatternCards.SELECT_10,
		InterfaceID.PatternCards.SELECT_11,
		InterfaceID.PatternCards.SELECT_12,
		InterfaceID.PatternCards.SELECT_13,
		InterfaceID.PatternCards.SELECT_14
	};

	private static final int[] PATTERNNEXT_INTERFACEIDS_INITIAL_PATTERN = {
		InterfaceID.PatternNext._0,
		InterfaceID.PatternNext._1,
		InterfaceID.PatternNext._2
	};

	private static final int[] PATTERNNEXT_INTERFACEIDS_CHOICES = {
		InterfaceID.PatternNext.SELECT_0,
		InterfaceID.PatternNext.SELECT_1,
		InterfaceID.PatternNext.SELECT_2,
		InterfaceID.PatternNext.SELECT_3
	};

	@Inject
	public SurpriseExamHelper(OverlayManager overlayManager, RandomEventHelperConfig config, Client client)
	{
		super(overlayManager, config, client);
	}

	@Override
	public void onStartUp()
	{
		this.overlayManager.add(overlay);
		this.patternCardHint = null;
		this.patternCardAnswers = null;
		this.patternCardAnswerWidgets = null;
		this.patternNextAnswer = null;
		this.patternNextAnswerWidget = null;
		this.relationshipSystem = new OSRSItemRelationshipSystem();

		if (this.isLoggedIn())
		{
			this.clientThread.invokeLater(() -> {
				if (this.client.getWidget(InterfaceID.PatternCards.HINT) != null)
				{
					WidgetLoaded matchingCardsWidgetLoaded = new WidgetLoaded();
					matchingCardsWidgetLoaded.setGroupId(InterfaceID.PATTERN_CARDS);
					this.eventBus.post(matchingCardsWidgetLoaded);
				}
				if (this.client.getWidget(InterfaceID.PatternNext.UNIVERSE_TEXT12) != null)
				{
					WidgetLoaded whatsNextWidgetLoaded = new WidgetLoaded();
					whatsNextWidgetLoaded.setGroupId(InterfaceID.PATTERN_NEXT);
					this.eventBus.post(whatsNextWidgetLoaded);
				}
			});
		}
	}

	@Override
	public void onShutdown()
	{
		this.overlayManager.remove(overlay);
		this.patternCardHint = null;
		this.patternCardAnswers = null;
		this.patternCardAnswerWidgets = null;
		this.patternNextAnswer = null;
		this.patternNextAnswerWidget = null;
		this.relationshipSystem = null;
	}

	@Override
	public boolean isEnabled()
	{
		return this.config.isSurpriseExamEnabled();
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		if (widgetLoaded.getGroupId() == InterfaceID.PATTERN_CARDS)
		{
			this.clientThread.invokeLater(() -> {
				Widget examHintWidget = this.client.getWidget(InterfaceID.PatternCards.HINT);
				if (examHintWidget != null)
				{
					this.patternCardHint = examHintWidget.getText();
					log.debug("Exam hint widget loaded with text: {}", this.patternCardHint);
					if (this.patternCardHint != null && !this.patternCardHint.isEmpty())
					{
						log.debug("Exam available pattern card items: {}", this.getPatternCardMap().values().asList());
						List<RandomEventItem> answerItems = this.relationshipSystem.findItemsByHint(this.patternCardHint, this.getPatternCardMap().values().asList(), 3);
						log.debug("Found answer items for exam hint '{}': {}", this.patternCardHint, answerItems);
						if (answerItems.size() >= 3)
						{
							this.patternCardAnswers = ImmutableSet.copyOf(answerItems);
							this.patternCardAnswerWidgets = answerItems.subList(0, 3).stream()
								.map(item -> {
									Integer interfaceID = this.getKeyForValue(this.getPatternCardMap(), item);
									if (interfaceID == null)
									{
										log.warn("No interface ID found for item: {}", item);
										return null;
									}
									Widget interfaceWidget = this.client.getWidget(interfaceID);
									return interfaceWidget != null ? this.getPatternCardSelectionWidgetFromModel(interfaceWidget) : null;
								})
								.filter(Objects::nonNull)
								.collect(ImmutableSet.toImmutableSet());
							log.debug("Pattern card answers set to: {}", this.patternCardAnswers);
						}
						else
						{
							log.warn("Found {} items for exam hint '{}', expected 3.", answerItems.size(), this.patternCardHint);
							this.patternCardAnswers = null;
							this.patternCardAnswerWidgets = null;
						}
					}
					else
					{
						log.warn("Exam hint widget text is empty or null.");
						this.patternCardHint = null;
						this.patternCardAnswers = null;
						this.patternCardAnswerWidgets = null;
					}
				}
			});
		}

		if (widgetLoaded.getGroupId() == InterfaceID.PATTERN_NEXT)
		{
			this.clientThread.invokeLater(() -> {
				Widget whatsNextTextWidget = this.client.getWidget(InterfaceID.PatternNext.UNIVERSE_TEXT12);
				if (whatsNextTextWidget != null)
				{
					String whatsNextText = whatsNextTextWidget.getText();
					log.debug("What's next widget text loaded: {}", whatsNextText);
					if (whatsNextText != null && !whatsNextText.isEmpty())
					{
						List<RandomEventItem> initialSelectionItems = this.getPatternNextInitialSelectionMap().values().asList();
						List<RandomEventItem> choicesItems = this.getPatternNextChoicesMap().values().asList();
						log.debug("Exam next initial selection items: {}", initialSelectionItems);
						log.debug("Exam next choice items: {}", choicesItems);
						RandomEventItem answerItem = this.relationshipSystem.findMissingItem(initialSelectionItems, choicesItems);
						if (answerItem != null)
						{
							this.patternNextAnswer = answerItem;
							Integer interfaceID = getKeyForValue(this.getPatternNextChoicesMap(), answerItem);
							this.patternNextAnswerWidget = interfaceID != null ? this.client.getWidget(interfaceID) : null;
							log.debug("Pattern next answer set to: {}", this.patternNextAnswer);
						}
						else
						{
							log.warn("No valid answer found for what's next text '{}'.", whatsNextText);
							this.patternNextAnswer = null;
							this.patternNextAnswerWidget = null;
						}
					}
					else
					{
						log.warn("Next hint widget text is empty or null.");
						this.patternNextAnswer = null;
						this.patternNextAnswerWidget = null;
					}
				}
			});
		}
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed widgetClosed)
	{
		if (widgetClosed.getGroupId() == InterfaceID.PATTERN_CARDS)
		{
			log.debug("Pattern cards widget closed, resetting pattern card answers.");
			this.patternCardHint = null;
			this.patternCardAnswers = null;
			this.patternCardAnswerWidgets = null;
		}

		if (widgetClosed.getGroupId() == InterfaceID.PATTERN_NEXT)
		{
			log.debug("Pattern next widget closed, resetting pattern next answer.");
			this.patternNextAnswer = null;
			this.patternNextAnswerWidget = null;
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		if (npcDespawned.getNpc().getId() == NpcID.PATTERN_TEACHER)
		{
			log.debug("Mr. Mordaut NPC despawned, resetting all answers.");
			this.patternCardHint = null;
			this.patternCardAnswers = null;
			this.patternCardAnswerWidgets = null;
			this.patternNextAnswer = null;
			this.patternNextAnswerWidget = null;
		}
	}

	@Subscribe
	public void onCommandExecuted(CommandExecuted executedCommand)
	{
		if (executedCommand.getCommand().equalsIgnoreCase("exportexampuzzle"))
		{
			StringBuilder sb = new StringBuilder();
			sb.append("Pattern Card Matching Hint: ");
			sb.append(this.patternCardHint != null ? this.patternCardHint : "NULL");
			sb.append("\n");
			sb.append("Pattern Card Matching Available Items: ");
			sb.append(this.getPatternCardMap() != null ? this.getPatternCardMap().values().asList().toString() : "NULL");
			sb.append("\n");
			sb.append("Pattern Card Matching Calculated Answers: ");
			sb.append(this.patternCardAnswers != null ? this.patternCardAnswers.toString() : "NULL");
			sb.append("\n");
			sb.append("Pattern Next Item Initial Items: ");
			sb.append(this.getPatternNextInitialSelectionMap() != null ? this.getPatternNextInitialSelectionMap().values().asList().toString() : "NULL");
			sb.append("\n");
			sb.append("Pattern Next Item Choices: ");
			sb.append(this.getPatternNextChoicesMap() != null ? this.getPatternNextChoicesMap().values().asList().toString() : "NULL");
			sb.append("\n");
			sb.append("Pattern Next Item Calculated Answer: ");
			sb.append(this.patternNextAnswer != null ? this.patternNextAnswer.toString() : "NULL");
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(new StringSelection(sb.toString()), null);
			log.info(sb.toString());
		}
	}

	private ImmutableMultimap<Integer, RandomEventItem> getPatternCardMap()
	{
		ImmutableMultimap.Builder<Integer, RandomEventItem> builder = ImmutableMultimap.builder();
		for (int patternCardSelectionInterfaceID : PATTERNCARDS_INTERFACEIDS_AVAILABLE_CARD_MODELS)
		{
			if (this.client.getWidget(InterfaceID.PatternCards.HINT) == null)
			{
				log.warn("Widget for matching pattern puzzle hint is null");
				return null;
			}
			int modelID = Objects.requireNonNull(this.client.getWidget(patternCardSelectionInterfaceID)).getModelId();
			RandomEventItem randomEventItem = RandomEventItem.fromModelID(modelID);
			if (randomEventItem == null)
			{
				log.warn("No RandomEventItem found for model ID: {}", modelID);
				continue;
			}
			builder.put(patternCardSelectionInterfaceID, randomEventItem);
		}
		return builder.build();
	}

	private ImmutableMap<Integer, RandomEventItem> getPatternNextInitialSelectionMap()
	{
		ImmutableMap.Builder<Integer, RandomEventItem> builder = ImmutableMap.builder();
		for (int patternNextInitialPatternInterfaceID : PATTERNNEXT_INTERFACEIDS_INITIAL_PATTERN)
		{
			if (this.client.getWidget(InterfaceID.PatternNext.UNIVERSE_TEXT12) == null)
			{
				log.warn("Widget for next missing item puzzle text is null");
				return null;
			}
			int modelID = Objects.requireNonNull(this.client.getWidget(patternNextInitialPatternInterfaceID)).getModelId();
			RandomEventItem randomEventItem = RandomEventItem.fromModelID(modelID);
			if (randomEventItem == null)
			{
				log.warn("No RandomEventItem found for model ID: {}", modelID);
				continue;
			}
			builder.put(patternNextInitialPatternInterfaceID, randomEventItem);
		}
		return builder.build();
	}

	private ImmutableMultimap<Integer, RandomEventItem> getPatternNextChoicesMap()
	{
		ImmutableMultimap.Builder<Integer, RandomEventItem> builder = ImmutableMultimap.builder();
		for (int patternNextChoiceInterfaceID : PATTERNNEXT_INTERFACEIDS_CHOICES)
		{
			if (this.client.getWidget(InterfaceID.PatternNext.UNIVERSE_TEXT12) == null)
			{
				log.warn("Widget for next missing item puzzle text is null");
				return null;
			}
			int modelID = Objects.requireNonNull(this.client.getWidget(patternNextChoiceInterfaceID)).getModelId();
			RandomEventItem randomEventItem = RandomEventItem.fromModelID(modelID);
			if (randomEventItem == null)
			{
				log.warn("No RandomEventItem found for model ID: {}", modelID);
				continue;
			}
			builder.put(patternNextChoiceInterfaceID, randomEventItem);
		}
		return builder.build();
	}

	private Integer getKeyForValue(Multimap<Integer, RandomEventItem> map, RandomEventItem value)
	{
		for (Map.Entry<Integer, RandomEventItem> entry : map.entries())
		{
			if (entry.getValue().equals(value))
			{
				return entry.getKey();
			}
		}
		return null;
	}

	public Widget getPatternCardSelectionWidgetFromModel(Widget modelWidget)
	{
		// This method retrieves the widget for the pattern card selection based on the model widget.
		// PATTERNCARDS_INTERFACEIDS_AVAILABLE_CARD_MODELS[i] = PATTERNCARDS_INTERFACEIDS_AVAILABLE_CARD_SELECTS[i]
		// So if modelWidget corresponds to PATTERNCARDS_INTERFACEIDS_AVAILABLE_CARD_MODELS[i] then it should return PATTERNCARDS_INTERFACEIDS_AVAILABLE_CARD_SELECTS[i]
		for (int i = 0; i < PATTERNCARDS_INTERFACEIDS_AVAILABLE_CARD_MODELS.length; i++)
		{
			if (modelWidget.getId() == PATTERNCARDS_INTERFACEIDS_AVAILABLE_CARD_MODELS[i])
			{
				return this.client.getWidget(PATTERNCARDS_INTERFACEIDS_AVAILABLE_CARD_SELECTS[i]);
			}
		}
		log.warn("No matching selection widget found for model widget ID: {}", modelWidget.getId());
		return null;
	}
}
