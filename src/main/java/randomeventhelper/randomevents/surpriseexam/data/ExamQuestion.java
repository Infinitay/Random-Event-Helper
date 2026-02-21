package randomeventhelper.randomevents.surpriseexam.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.runelite.client.util.RuntimeTypeAdapterFactory;

@Data
@RequiredArgsConstructor
public abstract class ExamQuestion
{
	/**
	 * The {@link ExamQuestionType} the type of the current question
	 */
	private final ExamQuestionType questionType;

	/**
	 * Whether the user answered the current question correctly or not. This will be null if the user has not yet submitted an answer for the current question.
	 */
	private Boolean wasAnsweredCorrectly;

	/**
	 * Creates a {@link RuntimeTypeAdapterFactory} for the {@link ExamQuestion} hierarchy.
	 * <p>
	 * This factory enables polymorphic (de)serialization by using the {@code "questionType"}
	 * field as a discriminator. This allows Gson to identify whether to instantiate a
	 * {@link MatchingCardsQuestion} or a {@link NextItemQuestion} when reading JSON.
	 * </p>
	 *
	 * @return A configured factory for the {@link ExamQuestion} class hierarchy.
	 * @see ExamQuestionType
	 * @see randomeventhelper.randomevents.surpriseexam.SurpriseExamHelper SurpriseExamHelper#EXAM_QUESTION_GSON_LOGGER for usage implementation.
	 */
	public static RuntimeTypeAdapterFactory<ExamQuestion> gsonTypeAdapterFactory()
	{
		return RuntimeTypeAdapterFactory
			.of(ExamQuestion.class, "questionType") // Uses 'questionType' field in JSON as the discriminator field to determine the subtypes for (de)serialization.
			.registerSubtype(MatchingCardsQuestion.class, ExamQuestionType.MATCHING_CARDS.name()) // Register the MatchingCardsQuestion class with the label "MATCHING_CARDS" that will be checked against "questionType" discriminator
			.registerSubtype(NextItemQuestion.class, ExamQuestionType.NEXT_ITEM.name()); // Register the NextItemQuestion class with the label "NEXT_ITEM" that will be checked against "questionType" discriminator
	}


}
