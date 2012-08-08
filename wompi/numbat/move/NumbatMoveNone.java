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

import robocode.AdvancedRobot;
import robocode.RobotStatus;
import wompi.numbat.target.ITargetManager;

public class NumbatMoveNone extends ANumbatMove
{

	@Override
	void setMove(RobotStatus status, ITargetManager targetMan)
	{}

	@Override
	String getName()
	{
		return "No Move";
	}

	protected void excecute(AdvancedRobot myBot)
	{
		myBot.setAhead(0);
	}

	@Override
	boolean checkActivateRule(RobotStatus status, ITargetManager targetMan)
	{
		boolean r1 = status.getOthers() == 0;
		boolean r2 = targetMan.getGunTarget() == null;
		// boolean r3 = targetMan.getCloseBots() == 0;
		return r1 || r2;// || r3;
	}

}
