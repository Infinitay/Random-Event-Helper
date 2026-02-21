package randomeventhelper.randomevents.surpriseexam.data;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import randomeventhelper.randomevents.surpriseexam.RandomEventItem;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true) // Make sure to include wasAnsweredCorrectly
public class MatchingCardsQuestion extends ExamQuestion
{
	/**
	 * Constructs a new MatchingCardsQuestion with the question type set to {@link ExamQuestionType#MATCHING_CARDS}.
	 */
	public MatchingCardsQuestion()
	{
		super(ExamQuestionType.MATCHING_CARDS);
	}

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
}
