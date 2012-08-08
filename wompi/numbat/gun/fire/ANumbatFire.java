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
package wompi.numbat.gun.fire;

import robocode.AdvancedRobot;
import robocode.RobotStatus;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import wompi.numbat.target.ITargetManager;

public abstract class ANumbatFire
{
	double	bPower;
	boolean	hasFired;

	abstract void setFire(RobotStatus status, ITargetManager targetMan);

	abstract String getName();

	abstract boolean checkActivateRule(RobotStatus status, ITargetManager targetMan);

	public void init(RobotStatus status)
	{

	}

	public void excecute(AdvancedRobot robot)
	{

	}

	public void onScannedRobot(ScannedRobotEvent e, RobotStatus status, ITargetManager targetMan)
	{

	}

	public final double getBulletPower()
	{
		return bPower;
	}

	public final double getBulletSpeed()
	{
		return Rules.getBulletSpeed(bPower);
	}

	public final boolean hasFired()
	{
		return hasFired;
	}
}
