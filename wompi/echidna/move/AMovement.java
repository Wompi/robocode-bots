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

import java.awt.Graphics2D;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.CustomEvent;
import robocode.DeathEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.WinEvent;
import wompi.echidna.target.ATarget;

public abstract class AMovement
{
	// enemy values

	// robot values
	AdvancedRobot	myRobot;

	// control values

	// debug

	public AMovement(AdvancedRobot robot)
	{
		myRobot = robot;
	}

	public abstract void init();

	public abstract void run();

	public void onScannedRobot(ATarget target, boolean isMainTarget)
	{}

	public void onBulletHit(BulletHitEvent e)
	{}

	public void onHitByBullet(HitByBulletEvent e)
	{}

	public void onHitWall(HitWallEvent e)
	{}

	public void onHitRobot(HitRobotEvent e)
	{}

	public void onRobotDeath(RobotDeathEvent e)
	{}

	public void onPaint(Graphics2D g)
	{}

	public void onDeath(DeathEvent e)
	{}

	public void onWin(WinEvent e)
	{}

	public void onCustomEvent(CustomEvent e)
	{}
}
