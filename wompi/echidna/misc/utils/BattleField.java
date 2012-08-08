/*******************************************************************************
 * Copyright (c)  2012  Wompi 
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the ZLIB
 * which accompanies this distribution, and is available at
 * http://robowiki.net/wiki/ZLIB
 * 
 * Contributors:
 *     Wompi - initial API and implementation
 ******************************************************************************/
package wompi.echidna.misc.utils;

public class BattleField
{
	// because of a little design flaw this information is not included in RobotStatus and has to be announced this way
	public static double	BATTLE_FIELD_H	= 1000;
	public static double	BATTLE_FIELD_W	= 1000;

	public enum EBattleState
	{
		SINGLE, MELEE, TWIN;
	}

	public static EBattleState getBattleState()
	{
		if (BATTLE_FIELD_H == 1000 && BATTLE_FIELD_W == 1000) return EBattleState.MELEE;
		else if (BATTLE_FIELD_H == 600 && BATTLE_FIELD_W == 800) return EBattleState.SINGLE;
		else if (BATTLE_FIELD_H == 800 && BATTLE_FIELD_W == 800) return EBattleState.TWIN;

		return EBattleState.MELEE; // if in doubt melee
	}
}
