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
package wompi.echidna.move;

import robocode.AdvancedRobot;

public class MoveJustSpinning extends AMovement
{
	// / enemy values

	// robot values

	// controlling values

	// debug

	public MoveJustSpinning(AdvancedRobot robot)
	{
		super(robot);
	}

	public void init()
	{
		myRobot.setTurnRightRadians(Double.MAX_VALUE);
	}

	@Override
	public void run()
	{}
}
