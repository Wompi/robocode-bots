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
package wompi.numbat.gun;

import java.awt.Graphics2D;

import robocode.AdvancedRobot;
import robocode.RobotStatus;
import robocode.ScannedRobotEvent;
import wompi.numbat.gun.fire.ANumbatFire;
import wompi.numbat.target.ITargetManager;

public abstract class ANumbatGun
{
	protected double	gTurn;

	abstract void setGun(RobotStatus status, ITargetManager targetMan, ANumbatFire fire);

	abstract String getName();

	abstract boolean checkActivateRule(RobotStatus status, ITargetManager targetMan);

	public ANumbatGun()
	{

	}

	protected void init(RobotStatus status)
	{

	}

	protected void excecute(AdvancedRobot myBot)
	{
		myBot.setAdjustGunForRobotTurn(true);  // TODO: probably bad design to call this every turn, but still better than spreading AdvancedRobot all
												// over the code
		myBot.setTurnGunRightRadians(gTurn);
	}

	public void onScannedRobot(ScannedRobotEvent e, RobotStatus myBotStatus, ITargetManager targetMan)
	{}

	public void onStatus(RobotStatus status)
	{}

	public void onPaint(Graphics2D g, RobotStatus status)
	{}
}
