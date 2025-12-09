package randomeventhelper.randomevents.gravedigger;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum GravediggerHighlightMode
{
	GRAVESTONE_ICON("Gravestone Icon"), // The icon over the gravestove
	COFFIN_ICON("Coffin Icon"), // The icon over the coffin in inventory
	HIGHLIGHT_GRAVE("Highlight Grave"), // Highlight overlay on the grave and gravestone
	HIGHLIGHT_COFFIN("Highlight Coffin"), // Highlight overlay on the coffin in inventory
	TEXT_GRAVE("Grave Text"), // Text overlay on the grave
	TEXT_COFFIN("Coffin Text"); // Text overlay on the coffin in inventory

	private final String displayName;

	@Override
	public String toString()
	{
		return this.displayName;
	}
}
