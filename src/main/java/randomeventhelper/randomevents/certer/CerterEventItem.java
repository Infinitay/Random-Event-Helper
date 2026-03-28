package randomeventhelper.randomevents.certer;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CerterEventItem
{
	BOWL("A bowl.", 2807),
	RING("A ring.", 8834),
	AXE("An axe.", 8828),
	KITE_SHIELD("A shield.", 8832),
	SHEARS("A pair of shears.", 8835),
	HELMET("A helmet.", 8833),
	SALMON_OR_TROUT("A fish.", 8829),
	SPADE("A spade.", 8837);

	private final String optionText;
	private final int modelID;

	private static final Map<Integer, CerterEventItem> ITEM_MODEL_ID_MAP;

	static
	{
		ImmutableMap.Builder<Integer, CerterEventItem> itemModelIDBuilder = new ImmutableMap.Builder<>();

		for (CerterEventItem certerEventItem : values())
		{
			itemModelIDBuilder.put(certerEventItem.getModelID(), certerEventItem);
		}

		ITEM_MODEL_ID_MAP = itemModelIDBuilder.build();
	}

	private static final Map<String, CerterEventItem> ITEM_OPTION_TEXT_MAP;

	static
	{
		ImmutableMap.Builder<String, CerterEventItem> itemOptionTextBuilder = new ImmutableMap.Builder<>();

		for (CerterEventItem certerEventItem : values())
		{
			itemOptionTextBuilder.put(certerEventItem.getOptionText(), certerEventItem);
		}

		ITEM_OPTION_TEXT_MAP = itemOptionTextBuilder.build();
	}

	public static CerterEventItem fromModelID(int modelID)
	{
		return ITEM_MODEL_ID_MAP.get(modelID);
	}

	public static CerterEventItem fromOptionText(String optionText)
	{
		return ITEM_OPTION_TEXT_MAP.get(optionText);
	}

	@Override
	public String toString()
	{
		return String.format("%s (\"%s\" | Model ID: %d)", this.name(), this.optionText, this.modelID);
	}
}
