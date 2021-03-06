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
package wompi.numbat.radar;

import java.awt.Graphics2D;
import java.util.HashSet;

import robocode.AdvancedRobot;
import robocode.RobotStatus;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;
import wompi.numbat.misc.NumbatBattleField;
import wompi.numbat.target.ITargetManager;

public abstract class ANumbatRadar
{
	protected boolean					isInit;
	protected double					rTurn;

	protected static HashSet<String>	startSet;	// it has to be static, to have all radars the same start rule

	abstract void setRadar(RobotStatus status, ITargetManager targetMan);

	abstract String getName();

	abstract boolean checkActivateRule(RobotStatus status);

	public ANumbatRadar()
	{}

	protected void init(RobotStatus status)
	{
		isInit = true;
		startSet = new HashSet<String>();
		if (isStartSearch(status))
		{
			rTurn = getBestAngleStart(status);
		}
	}

	protected void excecute(AdvancedRobot myBot)
	{
		if (isInit)
		{
			myBot.setAdjustRadarForGunTurn(true);
			isInit = false;
		}
		myBot.setTurnRadarRightRadians(rTurn);
	}

	public void onScannedRobot(ScannedRobotEvent e, RobotStatus status)
	{
		startSet.add(e.getName());
	}

	public final boolean isStartSearch(RobotStatus status)
	{
		if (startSet.size() == status.getOthers() || status.getTime() > 8) return false;
		return true;
	}

	// aergs bad design
	public final boolean isStartSearch(int others, long time)
	{
		if (startSet.size() == others || time > 8) return false;
		return true;
	}

	protected final double getBestAngleStart(RobotStatus status)
	{
		double centerX = NumbatBattleField.BATTLE_FIELD_W / 2.0;
		double centerY = NumbatBattleField.BATTLE_FIELD_H / 2.0;
		double bAngle = Math.atan2(centerX - status.getX(), centerY - status.getY());
		double rAngle = Utils.normalRelativeAngle(bAngle - status.getRadarHeadingRadians());
		return (Double.POSITIVE_INFINITY * Math.signum(rAngle));
	}

	protected void onPaint(Graphics2D g, RobotStatus botStatus, ITargetManager myTargetMan)
	{}

}
