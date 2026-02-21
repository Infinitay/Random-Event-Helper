package randomeventhelper.randomevents.surpriseexam;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import randomeventhelper.randomevents.surpriseexam.data.ExamQuestionType;

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

	private ExamQuestion currentExamQuestion;

	private List<ExamQuestion> examQuestionHistory;

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
		this.currentExamQuestion = null;
		this.examQuestionHistory = null;
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
		this.currentExamQuestion = null;
		this.examQuestionHistory = null;
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
						List<RandomEventItem> availablePatternCardItems = this.getPatternCardMap().values().asList();
						log.debug("Exam available pattern card items: {}", availablePatternCardItems);
						List<RandomEventItem> answerItems = this.relationshipSystem.findItemsByHint(this.patternCardHint, availablePatternCardItems, 3);
						log.debug("Found answer items for exam hint '{}': {}", this.patternCardHint, answerItems);
						this.currentExamQuestion = new ExamQuestion(ExamQuestionType.MATCHING_CARDS);
						this.examQuestionHistory = this.examQuestionHistory != null ? this.examQuestionHistory : Lists.newArrayList();
						this.examQuestionHistory.add(this.currentExamQuestion);
						this.currentExamQuestion.setMatchingHint(this.patternCardHint);
						this.currentExamQuestion.setMatchingAvailableCards(availablePatternCardItems);
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
							this.currentExamQuestion.setMatchingAnswerItems(answerItems.subList(0, 3));
						}
						else
						{
							log.warn("Found {} items for exam hint '{}', expected 3.", answerItems.size(), this.patternCardHint);
							this.patternCardAnswers = null;
							this.patternCardAnswerWidgets = null;
							this.currentExamQuestion.setMatchingAnswerItems(null); // Okay to use ImmutableList here since we always set it to either this empty list or a sublist, and not modify it
						}
					}
					else
					{
						log.warn("Exam hint widget text is empty or null.");
						this.patternCardHint = null;
						this.patternCardAnswers = null;
						this.patternCardAnswerWidgets = null;
						this.currentExamQuestion.setMatchingHint(null);
						this.currentExamQuestion.setMatchingAvailableCards(null);
						this.currentExamQuestion.setMatchingAnswerItems(null);
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
						this.currentExamQuestion = new ExamQuestion(ExamQuestionType.NEXT_ITEM);
						this.examQuestionHistory = this.examQuestionHistory != null ? this.examQuestionHistory : Lists.newArrayList();
						this.examQuestionHistory.add(this.currentExamQuestion);
						this.currentExamQuestion.setNextInitialItemSequence(initialSelectionItems);
						this.currentExamQuestion.setNextAvailableItems(choicesItems);
						if (answerItem != null)
						{
							this.patternNextAnswer = answerItem;
							Integer interfaceID = getKeyForValue(this.getPatternNextChoicesMap(), answerItem);
							this.patternNextAnswerWidget = interfaceID != null ? this.client.getWidget(interfaceID) : null;
							log.debug("Pattern next answer set to: {}", this.patternNextAnswer);
							this.currentExamQuestion.setNextAnswerItem(answerItem);
						}
						else
						{
							log.warn("No valid answer found for what's next text '{}'.", whatsNextText);
							this.patternNextAnswer = null;
							this.patternNextAnswerWidget = null;
							this.currentExamQuestion.setNextAnswerItem(null);
						}
					}
					else
					{
						log.warn("Next hint widget text is empty or null.");
						this.patternNextAnswer = null;
						this.patternNextAnswerWidget = null;
						this.currentExamQuestion.setNextInitialItemSequence(null);
						this.currentExamQuestion.setNextAvailableItems(null);
						this.currentExamQuestion.setNextAnswerItem(null);
					}
				}
			});
		}
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed widgetClosed)
	{
		// Don't clear the currentExamQuestion here because we still need to reference it to determine if the user's answer was correct or not
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
	public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked)
	{
		// If the player is not in the event instance or (the click is not "Select" + there is no widget) -> Return
		if (!isInSurpriseExamLocalInstance() || (!menuOptionClicked.getMenuOption().equals("Select") && menuOptionClicked.getWidget() == null))
		{
			return;
		}

		Widget clickedWidget = Objects.requireNonNull(menuOptionClicked.getWidget(), "Widget is null for the clicked menu option");
		int clickedWidgetGroupID = WidgetUtil.componentToInterface(clickedWidget.getId());
		int clickedWidgetModelID = clickedWidget.getModelId();
		RandomEventItem clickedItem = RandomEventItem.fromModelID(clickedWidgetModelID);
		if (clickedItem != null)
		{
			log.debug("Clicked widget model ID {} corresponds to item {}", clickedWidgetModelID, clickedItem);
		}
		else
		{
			log.warn("Clicked widget model ID {} does not correspond to any known RandomEventItem", clickedWidgetModelID);
			return;
		}

		// Don't worry about null checking currentExamQuestion because #onWidgetLoaded would have initialized it, and in this method we are checking clicks in those same Widget groups
		if (clickedWidgetGroupID == InterfaceID.PATTERN_CARDS)
		{
			List<RandomEventItem> selectedCards = this.currentExamQuestion.getMatchingSelectedCards();
			if (selectedCards == null)
			{
				selectedCards = Lists.newArrayListWithCapacity(3);
				this.currentExamQuestion.setMatchingSelectedCards(selectedCards); // Empty list
			}

			// Case 1. Player clicked a card that is already selected as part of their answer -> Remove the card from the selected answer cards
			// Case 2. Player clicked a card and there isn't 3 cards currently selected as the answer -> Add the card to the selected answer cards
			// Case 3. List size == 3 and player clicked a card, so do nothing because the event doesn't allow more than 3 active selections and ignores the click
			if (selectedCards.contains(clickedItem))
			{
				selectedCards.remove(clickedItem);
				log.debug("User unselected card item {}. Current selected cards: {}", clickedItem, selectedCards);
			}
			else if (selectedCards.size() < 3)
			{
				selectedCards.add(clickedItem);
				log.debug("User selected card item {}. Current selected cards: {}", clickedItem, selectedCards);
			}
		}

		if (clickedWidgetGroupID == InterfaceID.PATTERN_NEXT)
		{
			this.currentExamQuestion.setNextSelectedItem(clickedItem);
			log.debug("User selected the item {} as the next next item in the sequence.", clickedItem);
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
			this.patternCardHint = null;
			this.patternCardAnswers = null;
			this.patternCardAnswerWidgets = null;
			this.patternNextAnswer = null;
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
				if (sanitizedChatMessage.startsWith("No") || sanitizedChatMessage.startsWith("That's WRONG") || sanitizedChatMessage.startsWith("How unfortunate, you FAILED"))
				{
					if (this.currentExamQuestion != null)
					{
						answeredCorrectly = false;
						log.debug("The user answered the last question incorrectly: {}", this.currentExamQuestion);
					}
					// Formatting idea thanks to Resource Pack plugin (https://github.com/melkypie/resource-packs)
					String incorrectPuzzleMessage = RandomEventHelperPlugin.getChatMessageBuilderWithPrefix().append("The puzzle solution was incorrect. You can type '")
						.append(ChatColorType.HIGHLIGHT)
						.append("::exportexampuzzles")
						.append(ChatColorType.NORMAL)
						.append("' to export the data to your logs and clipboard. Please share it by opening an issue on GitHub.")
						.build();
					// Has to be ChatMessageType.CONSOLE to properly render the formatting highlights - GAMEMESSAGE will highlight everything after the first highlight
					RandomEventHelperPlugin.sendChatMessage(this.chatMessageManager, incorrectPuzzleMessage);
				}
				else if (sanitizedChatMessage.startsWith("Wonderful!") || sanitizedChatMessage.startsWith("Finally") || sanitizedChatMessage.startsWith("That's correct!") || sanitizedChatMessage.startsWith("WELL DONE!"))
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
		// In case people still use the old command name, lets add support both
		if (executedCommand.getCommand().equalsIgnoreCase("exportexampuzzle") || executedCommand.getCommand().equalsIgnoreCase("exportexampuzzles"))
		{
			Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
			String json = gson.toJson(this.examQuestionHistory != null ? this.examQuestionHistory : ImmutableList.of());
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(new StringSelection(json), null);
			log.info(json);
			String exportedMessage = RandomEventHelperPlugin.getChatMessageBuilderWithPrefix()
				.append("Exported surprise exam puzzle data to logs and also copied to clipboard.")
				.build();
			RandomEventHelperPlugin.sendChatMessage(this.chatMessageManager, exportedMessage);
		}
	}

	private ImmutableListMultimap<Integer, RandomEventItem> getPatternCardMap()
	{
		ImmutableListMultimap.Builder<Integer, RandomEventItem> builder = ImmutableListMultimap.builder();
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

	private ImmutableListMultimap<Integer, RandomEventItem> getPatternNextChoicesMap()
	{
		ImmutableListMultimap.Builder<Integer, RandomEventItem> builder = ImmutableListMultimap.builder();
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

	private boolean isInSurpriseExamLocalInstance()
	{
		return RandomEventHelperPlugin.getRegionIDFromCurrentLocalPointInstanced(client) == 7502;
	}
}
