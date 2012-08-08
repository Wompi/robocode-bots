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
import robocode.util.Utils;
import wompi.Echidna;
import wompi.echidna.misc.ACondition;
import wompi.echidna.misc.ConditionType;
import wompi.echidna.target.ATarget;

public class RadarRuleGunLock extends ACondition
{
	private final static double	LOCK_OFFSET	= 2.0;

	ATarget						myLastTarget;
	ATarget						myTarget;

	public RadarRuleGunLock(AdvancedRobot robot)
	{
		super(robot);
	}

	@Override
	public ConditionType getConditionType()
	{
		return ConditionType.CONDITION_RADAR_TYPE;
	}

	@Override
	public boolean test()
	{
		boolean result = myRobot.getGunHeat() < 1.0;
		System.out.format("[%d] %s = %b\n", myRobot.getTime(), getConditionName(), result);
		return result;
	}

	@Override
	public void processCommand()
	{
		myLastTarget = myTarget;
		myTarget = Echidna.myTargetHandler.getMainTarget();
		double eAbsBearing = myTarget.getAbsBearing();
		double rDiff = Utils.normalRelativeAngle(eAbsBearing - myRobot.getRadarHeadingRadians());
		myRobot.setTurnRadarRightRadians(rDiff * LOCK_OFFSET);
	}

}
