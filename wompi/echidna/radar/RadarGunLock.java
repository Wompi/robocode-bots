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
import robocode.util.Utils;
import wompi.echidna.target.ATarget;

public class RadarGunLock extends ARadar
{
	private static final double	LOCK_HEAT	= 1.0;
	private static final double	LOCK_OFFSET	= 2.0;

	ATarget						myTarget;
	ATarget						myLastTarget;

	public RadarGunLock(AdvancedRobot robot)
	{
		super(robot);
	}

	public void init()
	{
		// myRobot.setAdjustRadarForGunTurn(true);
		startBestAngleScan();
	}

	public void run()
	{
		if (myRobot.getRadarTurnRemaining() == 0.0) myRobot.setTurnRadarRightRadians(Double.MAX_VALUE);
	}

	public void onScannedRobot(ATarget target, boolean isMaintarget)
	{
		if (isMaintarget)
		{
			myLastTarget = myTarget;
			myTarget = target;
			// double eAbsBearing = Math.atan2(myTarget.getAbsX()-myRobot.getX(),myTarget.getAbsY()-myRobot.getY());
			double eAbsBearing = myTarget.getAbsBearing();
			double rDiff = Utils.normalRelativeAngle(eAbsBearing - myRobot.getRadarHeadingRadians());
			if (myRobot.getGunHeat() < LOCK_HEAT || myRobot.getOthers() == 1 || myLastTarget != myTarget) myRobot.setTurnRadarRightRadians(rDiff
					* LOCK_OFFSET);
		}
	}

	public void onPaint(Graphics2D g)
	{};
}
