package randomeventhelper.randomevents.surpriseexam.data;

import java.util.List;
import lombok.Data;
import randomeventhelper.randomevents.surpriseexam.RandomEventItem;

@Data
public class ExamQuestion
{
	/**
	 * The type of the current question.
	 */
	private final ExamQuestionType questionType;

	/**
	 * Whether the user answered the current question correctly or not. This will be null if the user has not yet submitted an answer for the current question.
	 */
	private Boolean wasAnsweredCorrectly;

	// The following fields are related to ExamQuestion#MATCHING_CARDS type questions. They should be null for other question types.

	/**
	 * The hint supplied by the current question. This will only be available for {@link ExamQuestionType#MATCHING_CARDS} questions, otherwise it will be null.
	 */
	private String matchingHint;

	/**
	 * The list of all available {@link RandomEventItem} cards for the current {@link ExamQuestionType#MATCHING_CARDS} question.
	 */
	private List<RandomEventItem> matchingAvailableCards;

	/**
	 * The list of {@link RandomEventItem} cards the user has selected and submitted for the current {@link ExamQuestionType#MATCHING_CARDS} question.
	 */
	private List<RandomEventItem> matchingSelectedCards;

	/**
	 * The list of the correct {@link RandomEventItem} answers for the current {@link ExamQuestionType#MATCHING_CARDS} question.
	 * Note that the following items are not guaranteed to be what the user has selected as their answer. See {@link #matchingSelectedCards}.
	 */
	private List<RandomEventItem> matchingAnswerItems;

	// The following fields are related to ExamQuestion#NEXT_ITEM type questions. They should be null for other question types.

	/**
	 * The list of all available {@link RandomEventItem} items in the given initial sequence for the current {@link ExamQuestionType#NEXT_ITEM} question.
	 */
	private List<RandomEventItem> nextInitialItemSequence;

	/**
	 * The list of all available {@link RandomEventItem} items in the given solution set for the current {@link ExamQuestionType#NEXT_ITEM} question.
	 */
	private List<RandomEventItem> nextAvailableItems;

	/**
	 * The {@link RandomEventItem} selected by the user as their answer for the current {@link ExamQuestionType#NEXT_ITEM} question.
	 */
	private RandomEventItem nextSelectedItem;

	/**
	 * The correct {@link RandomEventItem} answer for the current {@link ExamQuestionType#NEXT_ITEM} question.
	 * Note that this item is not guaranteed to be what the user has selected as their answer. See {@link #nextSelectedItem}.
	 */
	private RandomEventItem nextAnswerItem;
}
