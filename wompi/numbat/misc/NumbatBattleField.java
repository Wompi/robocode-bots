package wompi.numbat.misc;

public class NumbatBattleField
{
	// because of a little design flaw this information is not included in RobotStatus and has to be announced this way
	public static double	BATTLE_FIELD_H	= 1000;
	public static double	BATTLE_FIELD_W	= 1000;

	public enum ENumbatBattleState
	{
		SINGLE, MELEE, TWIN;
	}

	public static ENumbatBattleState getBattleState()
	{
		if (BATTLE_FIELD_H == 1000 && BATTLE_FIELD_W == 1000) return ENumbatBattleState.MELEE;
		else if (BATTLE_FIELD_H == 600 && BATTLE_FIELD_W == 800) return ENumbatBattleState.SINGLE;
		else if (BATTLE_FIELD_H == 800 && BATTLE_FIELD_W == 800) return ENumbatBattleState.TWIN;

		return ENumbatBattleState.MELEE; // if in doubt melee
	}

	public static double getCenterX()
	{
		return BATTLE_FIELD_W / 2.0;
	}

	public static double getCenterY()
	{
		return BATTLE_FIELD_H / 2.0;
	}

}
