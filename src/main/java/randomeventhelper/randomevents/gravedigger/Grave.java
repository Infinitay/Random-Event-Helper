package randomeventhelper.randomevents.gravedigger;

import lombok.Data;
import net.runelite.api.GameObject;

@Data
public class Grave
{
	private GraveNumber graveNumber;
	private GameObject graveStone;
	private GameObject emptyGrave;
	private GameObject filledGrave;
	private Coffin requiredCoffin;
	private Coffin placedCoffin;

	public Grave(GraveNumber graveNumber)
	{
		this.graveNumber = graveNumber;
		this.graveStone = null;
		this.emptyGrave = null;
		this.filledGrave = null;
		this.requiredCoffin = null;
		this.placedCoffin = null;
	}

	public Grave(GraveNumber graveNumber, Coffin requiredCoffin)
	{
		this.graveNumber = graveNumber;
		this.graveStone = null;
		this.emptyGrave = null;
		this.filledGrave = null;
		this.requiredCoffin = requiredCoffin;
		this.placedCoffin = null;
	}
}
