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

import robocode.AdvancedRobot;
import robocode.DeathEvent;
import robocode.WinEvent;
import robocode.util.Utils;
import wompi.echidna.misc.SimpleAverage;

public class RadarSpinning extends ARadar
{

	SimpleAverage	avgRadarDelta;
	double			lastRadar;

	public RadarSpinning(AdvancedRobot robot)
	{
		super(robot);
	}

	@Override
	public void init()
	{
		avgRadarDelta = new SimpleAverage(4000, "blind");
		myRobot.setTurnRadarRightRadians(Double.MAX_VALUE);
	}

	@Override
	public void run()
	{
		debugRadarDelta();
	}

	public void onWin(WinEvent event)
	{
		avgRadarDelta.onPrint("RADAR AVERAGE", false);
	}

	public void onDeath(DeathEvent event)
	{
		avgRadarDelta.onPrint("RADAR AVERAGE", false);
	};

	// debug functions ----------------------------------------------------------------------------------------
	private void debugRadarDelta()
	{
		double rHead = myRobot.getRadarHeading();
		double SCAN_DIR = Math.signum(myRobot.getRadarTurnRemaining());

		// if deta < 0 the normalization gives the rigth angle (hopefully)
		// so delta is always positive then
		double deltaRadar = Utils.normalAbsoluteAngleDegrees((rHead - lastRadar) * SCAN_DIR);
		avgRadarDelta.avg(deltaRadar, myRobot.getTime());
		// System.out.format("RADAR[%d] rRemain=%3.2f gRemain=%3.2f dirRadar=%3.0f\n",myRobot.getTime(),deltaRadar,myRobot.getGunTurnRemaining(),SCAN_DIR);
		lastRadar = myRobot.getRadarHeading();
	}

}
