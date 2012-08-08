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
package wompi.echidna.target;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class TargetSimple extends ATarget
{

	public TargetSimple(AdvancedRobot robot)
	{
		super(robot);
	}

	@Override
	public void init()
	{

	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		double rHead = myRobot.getHeadingRadians();

		eAbsBearing = e.getBearingRadians() + rHead;
		if (eHeading != Double.MAX_VALUE)
		{
			eHeadDiff = Utils.normalRelativeAngle(e.getHeadingRadians() - eHeading);
		}
		eHeading = e.getHeadingRadians();
		eDistance = e.getDistance();
		eLastEnergy = eEnergy;
		eEnergy = e.getEnergy();
		eLastVelocity = eVelocity;
		eVelocity = e.getVelocity();
		eLastScan = e.getTime();
		eName = e.getName();
		eBearing = e.getBearingRadians();

		eX = Math.sin(e.getBearingRadians() + rHead) * eDistance;
		eY = Math.cos(e.getBearingRadians() + rHead) * eDistance;

		eAbsX = myRobot.getX() + eX;
		eAbsY = myRobot.getY() + eY;

		if (eVelocity != 0)
		{
			eDir = (Math.sin(eHeading - eAbsBearing) * eVelocity < 0) ? -1 : 1;
		}
	}

}
