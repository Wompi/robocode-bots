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
package wompi.echidna.target.targethandler;

import java.awt.Graphics2D;
import java.util.Collection;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.DeathEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.WinEvent;
import wompi.echidna.target.ATarget;

public abstract class ATargetHandler
{
	AdvancedRobot	myRobot;
	ATarget			myTarget;
	ATarget			myLastScannedTarget;

	public ATargetHandler(AdvancedRobot robot)
	{
		myRobot = robot;
	}

	public abstract void init();

	public abstract Collection<ATarget> getAllTargets();

	public void onScannedRobot(ScannedRobotEvent e)
	{}

	public void run()
	{}

	public void onRobotDeath(RobotDeathEvent e)
	{}

	public void onPaint(Graphics2D g)
	{}

	public void onStatus(StatusEvent e)
	{}

	public void onWin(WinEvent e)
	{}

	public void onDeath(DeathEvent e)
	{}

	public void onBulletHit(BulletHitEvent e)
	{}

	protected abstract ATarget getTargetForName(String name);

	protected abstract void calculateMainTarget();

	public final boolean isMainTarget(ATarget target)
	{
		return myTarget == target;
	}

	public final ATarget getMainTarget()
	{
		return myTarget;
	}

	public final ATarget getLastScannedTarget()
	{
		return myLastScannedTarget;
	}
}
