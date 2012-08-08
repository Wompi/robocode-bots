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
package wompi.echidna.gun;

import java.awt.Graphics2D;

import robocode.AdvancedRobot;
import robocode.CustomEvent;
import robocode.RobotDeathEvent;
import robocode.StatusEvent;
import wompi.echidna.gun.fire.AFire;
import wompi.echidna.target.ATarget;

public abstract class AGun
{
	AdvancedRobot	myRobot;
	AFire			myFire;

	public AGun(AdvancedRobot robot)
	{
		myRobot = robot;
	}

	public abstract void init(AFire fireControl);			// stuff that comes in run ... Onetimers but is related to Advancedrobot

	public abstract void run();

	public void onStatus(StatusEvent e)
	{};

	public void onScannedRobot(ATarget target, boolean isMaintarget)
	{};

	public void onPaint(Graphics2D g)
	{};

	public void onRobotDeath(RobotDeathEvent e)
	{};

	public void onCustomEvent(CustomEvent e)
	{}
}
