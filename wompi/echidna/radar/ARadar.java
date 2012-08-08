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
package wompi.echidna.radar;

import java.awt.Graphics2D;

import robocode.AdvancedRobot;
import robocode.CustomEvent;
import robocode.DeathEvent;
import robocode.RobotDeathEvent;
import robocode.StatusEvent;
import robocode.WinEvent;
import robocode.util.Utils;
import wompi.echidna.target.ATarget;

public abstract class ARadar
{
	// enemy values

	// robot values
	AdvancedRobot	myRobot;

	double			rDirection;

	// controll values

	// debug

	public ARadar(AdvancedRobot robot)
	{
		myRobot = robot;
	}

	public abstract void init();			// stuff that comes in run ... Onetimers but is related to Advancedrobot

	public abstract void run();

	public void onStatus(StatusEvent e)
	{}

	public void onRobotDeath(RobotDeathEvent e)
	{}

	public void onScannedRobot(ATarget target, boolean isMaintarget)
	{}

	public void onPaint(Graphics2D g)
	{}

	public void onCustomEvent(CustomEvent e)
	{}

	public void onWin(WinEvent event)
	{}

	public void onDeath(DeathEvent event)
	{}

	// all final functions
	protected final void startBestAngleScan()
	{
		double centerX = myRobot.getBattleFieldWidth() / 2.0;
		double centerY = myRobot.getBattleFieldHeight() / 2.0;
		double bAngle = Math.atan2(centerX - myRobot.getX(), centerY - myRobot.getY());
		double rAngle = Utils.normalRelativeAngle(bAngle - myRobot.getRadarHeadingRadians());
		rDirection = Math.signum(rAngle);
		myRobot.setTurnRadarRight(rDirection * Double.POSITIVE_INFINITY);

	}

}
