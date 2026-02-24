package randomeventhelper.randomevents.surpriseexam;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
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
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;
import randomeventhelper.RandomEventHelperConfig;
import randomeventhelper.RandomEventHelperPlugin;
import randomeventhelper.pluginmodulesystem.PluginModule;
import randomeventhelper.randomevents.surpriseexam.data.ExamQuestion;
import randomeventhelper.randomevents.surpriseexam.data.MatchingCardsQuestion;
import randomeventhelper.randomevents.surpriseexam.data.NextItemQuestion;

@Slf4j
@Singleton
public class SurpriseExamHelper extends PluginModule
{
	@Inject
	private ClientThread clientThread;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private Gson gson;

	@Inject
	private SurpriseExamOverlay overlay;

	@Getter
	private ImmutableList<Widget> patternCardAnswerWidgets;

	@Getter
	private Widget patternNextAnswerWidget;

	private OSRSItemRelationshipSystem relationshipSystem;

	private ExamQuestion currentExamQuestion;

	private List<ExamQuestion> examQuestionHistory;

	private Gson gsonExamQuestionLogger;

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
		this.patternCardAnswerWidgets = null;
		this.patternNextAnswerWidget = null;
		this.currentExamQuestion = null;
		this.examQuestionHistory = null;
		this.relationshipSystem = new OSRSItemRelationshipSystem();
		this.gsonExamQuestionLogger = this.gson.newBuilder()
			.registerTypeAdapterFactory(ExamQuestion.gsonTypeAdapterFactory())
			.serializeNulls()
			.setPrettyPrinting()
			.create();

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
		this.patternCardAnswerWidgets = null;
		this.patternNextAnswerWidget = null;
		this.currentExamQuestion = null;
		this.examQuestionHistory = null;
		this.relationshipSystem = null;
		this.gsonExamQuestionLogger = null;
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
			log.debug("[Matching Cards Question] Loaded widget associated with matching cards exam question");
			this.clientThread.invokeLater(() -> {
				Widget examHintWidget = this.client.getWidget(InterfaceID.PatternCards.HINT);
				if (examHintWidget != null)
				{
					String questionHint = examHintWidget.getText();
					log.debug("[Matching Cards Question] Question Hint: {}", questionHint);
					MatchingCardsQuestion matchingCardsQuestion = new MatchingCardsQuestion();
					this.currentExamQuestion = matchingCardsQuestion;
					this.examQuestionHistory = this.examQuestionHistory != null ? this.examQuestionHistory : Lists.newArrayList();
					this.examQuestionHistory.add(this.currentExamQuestion);
					// Despite checking the hint later for null/empty, lets still set it here just in case
					matchingCardsQuestion.setMatchingHint(questionHint);
					if (questionHint != null && !questionHint.isEmpty())
					{
						List<RandomEventItem> availablePatternCardItems = this.getPatternCardMap().values().asList();
						log.debug("[Matching Cards Question] Available Cards: {}", availablePatternCardItems);
						List<RandomEventItem> answerItems = this.relationshipSystem.findItemsByHint(questionHint, availablePatternCardItems, 3);
						log.debug("[Matching Cards Question] Calculated Answer Items List for the given hint '{}': {}", questionHint, answerItems);
						matchingCardsQuestion.setMatchingAvailableCards(availablePatternCardItems);
						// Include all the answers because it could help modifying the relationship system in the future
						// Also, set the answerItems here because even if we don't find 3 answers, it could be important to log what the result was
						matchingCardsQuestion.setMatchingAnswerItems(answerItems);
						if (answerItems.size() >= 3)
						{
							List<RandomEventItem> finalThreeAnswerItems = answerItems.subList(0, 3);
							this.patternCardAnswerWidgets = finalThreeAnswerItems.stream()
								.map(item -> {
									Integer interfaceID = this.getKeyForValue(this.getPatternCardMap(), item);
									if (interfaceID == null)
									{
										log.warn("[Matching Cards Question] No interface ID found for item: {}", item);
										return null;
									}
									Widget interfaceWidget = this.client.getWidget(interfaceID);
									return interfaceWidget != null ? this.getPatternCardSelectionWidgetFromModel(interfaceWidget) : null;
								})
								.filter(Objects::nonNull)
								.collect(ImmutableList.toImmutableList());
							log.debug("[Matching Cards Question] Final Answer Item List: {}", finalThreeAnswerItems);
							log.debug("[Matching Cards Question] Final Answer Item Widgets: {}", this.patternCardAnswerWidgets.stream().map(Widget::getId).collect(ImmutableList.toImmutableList()));
						}
						else
						{
							log.warn("[Matching Cards Question] Found only {} potential answer item(s) for given hint '{}', expected 3.", answerItems.size(), questionHint);
							this.patternCardAnswerWidgets = null;
						}
					}
					else
					{
						log.warn("[Matching Cards Question] Question hint widget's text is empty or null.");
						this.patternCardAnswerWidgets = null;
						matchingCardsQuestion.setMatchingHint(null);
						matchingCardsQuestion.setMatchingAvailableCards(null);
						matchingCardsQuestion.setMatchingAnswerItems(null);
					}
				}
			});
		}

		if (widgetLoaded.getGroupId() == InterfaceID.PATTERN_NEXT)
		{
			log.debug("[Next Item Question] Loaded widget associated with next item exam question");
			this.clientThread.invokeLater(() -> {
				Widget whatsNextTextWidget = this.client.getWidget(InterfaceID.PatternNext.UNIVERSE_TEXT12);
				if (whatsNextTextWidget != null)
				{
					String whatsNextText = whatsNextTextWidget.getText();
					log.debug("[Next Item Question] \"What's next\" widget text loaded: {}", whatsNextText);
					NextItemQuestion nextItemQuestion = new NextItemQuestion();
					this.currentExamQuestion = nextItemQuestion;
					this.examQuestionHistory = this.examQuestionHistory != null ? this.examQuestionHistory : Lists.newArrayList();
					this.examQuestionHistory.add(this.currentExamQuestion);
					if (whatsNextText != null && !whatsNextText.isEmpty())
					{
						List<RandomEventItem> initialSelectionItems = this.getPatternNextInitialSelectionMap().values().asList();
						log.debug("[Next Item Question] Initial Item Sequence: {}", initialSelectionItems);
						nextItemQuestion.setNextInitialItemSequence(initialSelectionItems);
						List<RandomEventItem> choicesItems = this.getPatternNextChoicesMap().values().asList();
						log.debug("[Next Item Question] Next Available Items: {}", choicesItems);
						nextItemQuestion.setNextAvailableItems(choicesItems);
						RandomEventItem answerItem = this.relationshipSystem.findMissingItem(initialSelectionItems, choicesItems);
						log.debug("[Next Item Question] Calculated Next Answer Item: {}", answerItem);
						// Can set the answer item here since answerItem would be null if there is no match
						nextItemQuestion.setNextAnswerItem(answerItem);
						if (answerItem != null)
						{
							Integer interfaceID = getKeyForValue(this.getPatternNextChoicesMap(), answerItem);
							this.patternNextAnswerWidget = interfaceID != null ? this.client.getWidget(interfaceID) : null;
							log.debug("[Next Item Question] Final Answer Item Widget: {}", this.patternNextAnswerWidget != null ? this.patternNextAnswerWidget.getId() : null);
						}
						else
						{
							log.warn("[Next Item Question] No valid answer found");
							this.patternNextAnswerWidget = null;
						}
					}
					else
					{
						log.warn("[Next Item Question] \"What's next\" widget's text text is empty or null.");
						this.patternNextAnswerWidget = null;
						nextItemQuestion.setNextInitialItemSequence(null);
						nextItemQuestion.setNextAvailableItems(null);
						nextItemQuestion.setNextAnswerItem(null);
					}
				}
			});
		}
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed widgetClosed)
	{
		// Don't reset/modify the currentExamQuestion here because we still need to reference it to determine if the user's answer was correct or not as well as history tracking
		if (widgetClosed.getGroupId() == InterfaceID.PATTERN_CARDS)
		{
			log.debug("[Matching Cards Question] Closed widget associated with matching cards exam question, resetting final answer item widgets.");
			this.patternCardAnswerWidgets = null;
		}

		if (widgetClosed.getGroupId() == InterfaceID.PATTERN_NEXT)
		{
			log.debug("[Next Item Question] Closed widget associated with next item exam question, resetting next item answer widget.");
			this.patternNextAnswerWidget = null;
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked)
	{
		// If the player is not in the event instance or (the click is not "Select" or there is no widget) -> Return
		if (!isInSurpriseExamLocalInstance() || !menuOptionClicked.getMenuOption().equals("Select") || menuOptionClicked.getWidget() == null)
		{
			return;
		}

		Widget clickedWidget = Objects.requireNonNull(menuOptionClicked.getWidget(), "Widget is null for the clicked menu option");
		int clickedWidgetGroupID = WidgetUtil.componentToInterface(clickedWidget.getId());
		int clickedWidgetModelID = clickedWidget.getModelId();

		RandomEventItem clickedItem = null;
		// It turns out that when the user clicks on a Pattern Card, the game recognizes PatternCard.SELECT instead of PatternCard.CARD, so we need to fetch the corresponding CARD widget to fetch the model ID
		if (clickedWidgetGroupID == InterfaceID.PATTERN_CARDS)
		{
			Widget patternCardCardWidget = this.getCorrespondingPatternCardWidgetFromSelectionWidget(clickedWidget);
			if (patternCardCardWidget != null)
			{
				clickedItem = RandomEventItem.fromModelID(patternCardCardWidget.getModelId());
			}
		}
		else
		{
			clickedItem = RandomEventItem.fromModelID(clickedWidgetModelID);
		}

		if (clickedItem != null)
		{
			log.debug("Clicked widget model ID {} corresponds to item {}", clickedWidgetModelID, clickedItem);
		}
		else
		{
			log.warn("Clicked widget model ID {} does not correspond to any known RandomEventItem (Associated Widget ID: {})", clickedWidgetModelID, clickedWidget.getId());
			return;
		}

		// Don't worry about null checking currentExamQuestion because #onWidgetLoaded would have initialized it, and in this method we are checking clicks in those same Widget groups
		if (clickedWidgetGroupID == InterfaceID.PATTERN_CARDS && this.currentExamQuestion instanceof MatchingCardsQuestion)
		{
			List<RandomEventItem> selectedCards = ((MatchingCardsQuestion) this.currentExamQuestion).getMatchingSelectedCards();
			if (selectedCards == null)
			{
				selectedCards = Lists.newArrayListWithCapacity(3);
				((MatchingCardsQuestion) this.currentExamQuestion).setMatchingSelectedCards(selectedCards); // Empty list
			}

			// Case 1. Player clicked a card that is already within selectedCards list -> Remove the card from the selected answer cards list
			// Case 2. Player clicked a card that isn't already within selectedCards list -> Add the card to the selected answer cards list
			if (selectedCards.contains(clickedItem))
			{
				selectedCards.remove(clickedItem);
				log.debug("[Matching Cards Question] User unselected card item {}. Current selected cards: {}", clickedItem, selectedCards);
			}
			else
			{
				selectedCards.add(clickedItem);
				log.debug("[Matching Cards Question] User selected card item {}. Current selected cards: {}", clickedItem, selectedCards);
			}
		}

		if (clickedWidgetGroupID == InterfaceID.PATTERN_NEXT && this.currentExamQuestion instanceof NextItemQuestion)
		{
			((NextItemQuestion) this.currentExamQuestion).setNextSelectedItem(clickedItem);
			log.debug("[Next Item Question] User selected the item {} as the next next item in the sequence.", clickedItem);
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		if (npcSpawned.getNpc().getId() == NpcID.PATTERN_TEACHER)
		{
			log.debug("Mr. Mordaut NPC spawned, resetting current exam history.");
			// Probably should be checking the GameState and avoiding clears on relogs with the active session, but let's hold the player accountable
			this.examQuestionHistory = null;
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		if (npcDespawned.getNpc().getId() == NpcID.PATTERN_TEACHER)
		{
			log.debug("Mr. Mordaut NPC despawned, resetting all answers.");
			this.patternCardAnswerWidgets = null;
			this.patternNextAnswerWidget = null;
			this.currentExamQuestion = null;
			// Don't reset currentExamHistory here because what if the user wants to export it after the event? Instead, reset on NPC spawn
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		String sanitizedChatMessage = Text.sanitizeMultilineText(chatMessage.getMessage());
		if (chatMessage.getType() == ChatMessageType.DIALOG)
		{
			if (this.isInSurpriseExamLocalInstance() && sanitizedChatMessage.startsWith("Mr. Mordaut"))
			{
				sanitizedChatMessage = sanitizedChatMessage.replace("Mr. Mordaut|", "");
				Boolean answeredCorrectly = null;
				if (this.doesMessageIndicateIncorrectAnswer(sanitizedChatMessage))
				{
					if (this.currentExamQuestion != null)
					{
						answeredCorrectly = false;
						log.debug("The user answered the last question incorrectly: {}", this.currentExamQuestion);
					}
					// Formatting idea thanks to Resource Pack plugin (https://github.com/melkypie/resource-packs)
					String incorrectExamQuestionMessage = RandomEventHelperPlugin.getChatMessageBuilderWithPrefix().append("The exam question solution was answered incorrectly.")
						.append(" If the plugin's provided solution was incorrect, please report the issue on GitHub. You can type '\"")
						.append(ChatColorType.HIGHLIGHT)
						.append("::exportexamquestions")
						.append(ChatColorType.NORMAL)
						.append("' to export the exam question history to your logs and clipboard.")
						.build();
					// Has to be ChatMessageType.CONSOLE to properly render the formatting highlights - GAMEMESSAGE will highlight everything after the first highlight
					RandomEventHelperPlugin.sendChatMessage(this.chatMessageManager, incorrectExamQuestionMessage);
				}
				else if (this.doesMessageIndicateCorrectAnswer(sanitizedChatMessage))
				{
					if (this.currentExamQuestion != null)
					{
						answeredCorrectly = true;
						log.debug("The user answered the last question correctly: {}", this.currentExamQuestion);
					}
				}

				if (this.currentExamQuestion != null && answeredCorrectly != null)
				{
					this.currentExamQuestion.setWasAnsweredCorrectly(answeredCorrectly);
				}
			}
		}
	}

	@Subscribe
	public void onCommandExecuted(CommandExecuted executedCommand)
	{
		// In case people still use the old commands lets support all
		switch (executedCommand.getCommand().toLowerCase())
		{
			case "exportexampuzzle":
			case "exportexampuzzles":
			case "exportexamquestion":
			case "exportexamquestions":
				String json = gsonExamQuestionLogger.toJson(this.examQuestionHistory != null ? this.examQuestionHistory : ImmutableList.of());
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(new StringSelection(json), null);
				log.info(json);
				String exportedMessage = RandomEventHelperPlugin.getChatMessageBuilderWithPrefix()
					.append("Exported surprise exam question data to logs and also copied to clipboard.")
					.build();
				RandomEventHelperPlugin.sendChatMessage(this.chatMessageManager, exportedMessage);
				break;
			default:
				break;
		}
	}

	private ImmutableListMultimap<Integer, RandomEventItem> getPatternCardMap()
	{
		ImmutableListMultimap.Builder<Integer, RandomEventItem> builder = ImmutableListMultimap.builder();
		for (int patternCardSelectionInterfaceID : PATTERNCARDS_INTERFACEIDS_AVAILABLE_CARD_MODELS)
		{
			if (this.client.getWidget(InterfaceID.PatternCards.HINT) == null)
			{
				log.warn("[Matching Cards Question] Widget for matching cards question hint is null, can't build pattern card map.");
				return null;
			}
			int modelID = Objects.requireNonNull(this.client.getWidget(patternCardSelectionInterfaceID)).getModelId();
			RandomEventItem randomEventItem = RandomEventItem.fromModelID(modelID);
			if (randomEventItem == null)
			{
				log.warn("[Matching Cards Question] No RandomEventItem found for model ID: {}", modelID);
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
				log.warn("[Next Item Question] Widget for the \"What's next\" text is null");
				return null;
			}
			int modelID = Objects.requireNonNull(this.client.getWidget(patternNextInitialPatternInterfaceID)).getModelId();
			RandomEventItem randomEventItem = RandomEventItem.fromModelID(modelID);
			if (randomEventItem == null)
			{
				log.warn("[Next Item Question] No RandomEventItem found for model ID: {}", modelID);
				continue;
			}
			builder.put(patternNextInitialPatternInterfaceID, randomEventItem);
		}
		return builder.build();
	}

	private ImmutableListMultimap<Integer, RandomEventItem> getPatternNextChoicesMap()
	{
		ImmutableListMultimap.Builder<Integer, RandomEventItem> builder = ImmutableListMultimap.builder();
		for (int patternNextChoiceInterfaceID : PATTERNNEXT_INTERFACEIDS_CHOICES)
		{
			if (this.client.getWidget(InterfaceID.PatternNext.UNIVERSE_TEXT12) == null)
			{
				log.warn("[Next Item Question] Widget for the \"What's next\" text is null");
				return null;
			}
			int modelID = Objects.requireNonNull(this.client.getWidget(patternNextChoiceInterfaceID)).getModelId();
			RandomEventItem randomEventItem = RandomEventItem.fromModelID(modelID);
			if (randomEventItem == null)
			{
				log.warn("[Next Item Question] No RandomEventItem found for model ID: {}", modelID);
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
		log.warn("[Matching Cards Question] No matching selection card widget found for model card widget ID: {}", modelWidget.getId());
		return null;
	}

	public Widget getCorrespondingPatternCardWidgetFromSelectionWidget(Widget selectionWidget)
	{
		// This method retrieves the widget for the pattern card model based on the selection widget.
		// PATTERNCARDS_INTERFACEIDS_AVAILABLE_CARD_SELECTS[i] = PATTERNCARDS_INTERFACEIDS_AVAILABLE_CARD_MODELS[i]
		// So if selectionWidget corresponds to PATTERNCARDS_INTERFACEIDS_AVAILABLE_CARD_SELECTS[i] then it should return PATTERNCARDS_INTERFACEIDS_AVAILABLE_CARD_MODELS[i]
		for (int i = 0; i < PATTERNCARDS_INTERFACEIDS_AVAILABLE_CARD_SELECTS.length; i++)
		{
			if (selectionWidget.getId() == PATTERNCARDS_INTERFACEIDS_AVAILABLE_CARD_SELECTS[i])
			{
				return this.client.getWidget(PATTERNCARDS_INTERFACEIDS_AVAILABLE_CARD_MODELS[i]);
			}
		}
		log.warn("[Matching Cards Question] No matching model card widget found for selection card widget ID: {}", selectionWidget.getId());
		return null;
	}

	private boolean doesMessageIndicateCorrectAnswer(String sanitizedChatMessage)
	{
		return sanitizedChatMessage.startsWith("Wonderful!") || sanitizedChatMessage.startsWith("Finally") || sanitizedChatMessage.startsWith("That's correct!") || sanitizedChatMessage.startsWith("WELL DONE!");
	}

	private boolean doesMessageIndicateIncorrectAnswer(String sanitizedChatMessage)
	{
		return sanitizedChatMessage.startsWith("No") || sanitizedChatMessage.startsWith("That's WRONG") || sanitizedChatMessage.startsWith("How unfortunate, you FAILED");
	}

	private boolean isInSurpriseExamLocalInstance()
	{
		return RandomEventHelperPlugin.getRegionIDFromCurrentLocalPointInstanced(client) == 7502;
	}
}
