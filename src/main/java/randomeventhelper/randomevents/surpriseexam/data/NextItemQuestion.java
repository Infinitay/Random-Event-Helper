package randomeventhelper.randomevents.surpriseexam.data;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import randomeventhelper.randomevents.surpriseexam.RandomEventItem;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true) // Make sure to include wasAnsweredCorrectly
public class NextItemQuestion extends ExamQuestion
{
	/**
	 * Constructs a new NextItemQuestion with the question type set to {@link ExamQuestionType#NEXT_ITEM}.
	 */
	public NextItemQuestion()
	{
		super(ExamQuestionType.NEXT_ITEM);
	}

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
