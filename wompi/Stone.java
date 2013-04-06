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
package wompi;

import java.awt.Color;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

/**
 * Simple stone like target robot for Wombot
 * 
 * @author rschott
 */
public class Stone extends AdvancedRobot
{

	@Override
	public void run()
	{
		setBodyColor(Color.DARK_GRAY);
		setRadarColor(Color.RED);
		setGunColor(Color.GRAY);

		setTurnRadarRight(Double.POSITIVE_INFINITY);
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		setTurnRadarLeftRadians(getRadarTurnRemainingRadians());

		double absBearing = e.getBearingRadians() + getHeadingRadians();
		double latv = e.getVelocity() * Math.sin(e.getHeadingRadians() - absBearing);

		System.out.format("[%04d] latv=%3.20f round=%d\n", getTime(), latv, (int) latv);
	}
}
