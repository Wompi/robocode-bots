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

public class RadarLock extends ARadar
{
	private static final double	LOCK_OFFSET	= 3.0;

	ATarget						myTarget;

	public RadarLock(AdvancedRobot robot)
	{
		super(robot);
	}

	public void init()
	{
		myRobot.setAdjustRadarForGunTurn(true);
	}

	public void run()
	{
		if (myRobot.getRadarTurnRemaining() == 0.0) myRobot.setTurnRadarRightRadians(Double.MAX_VALUE);
	}

	public void onScannedRobot(ATarget target, boolean isMaintarget)
	{
		if (isMaintarget) myTarget = target;
		double eAbsBearing = myTarget.getBearing() + myRobot.getHeadingRadians();
		double rDiff = Utils.normalRelativeAngle(eAbsBearing - myRobot.getRadarHeadingRadians());
		myRobot.setTurnRadarRightRadians(rDiff * LOCK_OFFSET);
	}

	public void onPaint(Graphics2D g)
	{};
}
