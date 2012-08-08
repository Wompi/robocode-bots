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
package wompi.echidna.radar.conditions;

import robocode.AdvancedRobot;
import robocode.Rules;
import robocode.util.Utils;
import wompi.Echidna;
import wompi.echidna.misc.ACondition;
import wompi.echidna.misc.ConditionType;

public class RadarRuleStartSearch extends ACondition
{
	double	rDirection;

	public RadarRuleStartSearch(AdvancedRobot robot)
	{
		super(robot);
		init();
	}

	@Override
	public boolean test()
	{
		boolean result = Echidna.myTargetHandler.getAllTargets().size() == myRobot.getOthers();
		System.out.format("[%d] %s = %b\n", myRobot.getTime(), getConditionName(), result);
		return result;
	}

	@Override
	public ConditionType getConditionType()
	{
		return ConditionType.CONDITION_RADAR_TYPE;
	}

	@Override
	public void processCommand()
	{
		myRobot.setTurnRadarRightRadians(rDirection * Rules.RADAR_TURN_RATE_RADIANS);
		// myRobot.setTurnRadarRight(rDirection * Double.POSITIVE_INFINITY);
	}

	private void init()
	{
		double centerX = myRobot.getBattleFieldWidth() / 2.0;
		double centerY = myRobot.getBattleFieldHeight() / 2.0;
		double bAngle = Math.atan2(centerX - myRobot.getX(), centerY - myRobot.getY());
		double rAngle = Utils.normalRelativeAngle(bAngle - myRobot.getRadarHeadingRadians());
		rDirection = Math.signum(rAngle);
		// processCommand();
	}
}
