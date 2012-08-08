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

import java.awt.Graphics2D;

import robocode.AdvancedRobot;
import robocode.HitRobotEvent;
import robocode.RobotStatus;
import robocode.ScannedRobotEvent;
import wompi.numbat.target.ITargetManager;

public abstract class ANumbatMove
{
	abstract void setMove(RobotStatus status, ITargetManager targetMan);

	abstract String getName();

	abstract boolean checkActivateRule(RobotStatus status, ITargetManager targetMan);

	public ANumbatMove()
	{

	}

	protected void init(RobotStatus status)
	{

	}

	protected void excecute(AdvancedRobot myBot)
	{}

	public void onScannedRobot(ScannedRobotEvent e, RobotStatus myBotStatus, ITargetManager targetMan)
	{}

	public void onPaint(Graphics2D g, RobotStatus status)
	{

	}

	public void onHitRobot(HitRobotEvent e, RobotStatus botStatus)
	{

	}

}
