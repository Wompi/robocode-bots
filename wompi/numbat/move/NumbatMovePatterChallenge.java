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
package wompi.numbat.move;

import robocode.RobotStatus;
import wompi.numbat.misc.NumbatBattleField;
import wompi.numbat.target.ITargetManager;

public class NumbatMovePatterChallenge extends ANumbatMove
{

	@Override
	void setMove(RobotStatus status, ITargetManager targetMan)
	{
		// in this challenge no moving is allowed
	}

	@Override
	String getName()
	{
		return "Pattern Challenge";
	}

	@Override
	boolean checkActivateRule(RobotStatus status, ITargetManager targetMan)
	{
		boolean r1 = status.getOthers() == 1;
		boolean r2 = NumbatBattleField.getBattleState() == NumbatBattleField.ENumbatBattleState.MELEE;
		boolean r3 = targetMan.getGunTarget() != null && targetMan.getGunTarget().eName.startsWith("challenge.PatternBot");
		boolean r4 = status.getNumRounds() == 100;
		return r1 && r2 && r3 && r4;
	}

}
