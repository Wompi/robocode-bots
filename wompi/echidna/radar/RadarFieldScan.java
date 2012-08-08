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

import java.util.HashSet;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.StatusEvent;
import wompi.echidna.target.ATarget;

public class RadarFieldScan extends ARadar
{
	HashSet<String>	scannedEnemys	= new HashSet<String>();
	int				radarTick;
	int				noRadarTick;

	public RadarFieldScan(AdvancedRobot robot)
	{
		super(robot);
	}

	public void init()
	{
		myRobot.setAdjustRadarForGunTurn(true);
		myRobot.setTurnRadarRightRadians(Double.MAX_VALUE); // init stuff
	}

	public void onStatus(StatusEvent e)
	{
		if (noRadarTick >= 2)
		{
			System.out.format("STATUS CLEAR\n");
			scannedEnemys.clear();
			radarTick = 0;
		}
		noRadarTick++;
	}

	public void onRobotDeath(RobotDeathEvent e)
	{
		scannedEnemys.remove(e.getName());
	}

	public void onScannedRobot(ATarget target)
	{
		scannedEnemys.add(target.getName());

		if (scannedEnemys.size() == myRobot.getOthers())
		{
			scannedEnemys.clear();
			scannedEnemys.add(target.getName());
			System.out.format("SCAN CLEAR %d\n", radarTick);
			if (radarTick <= 4)
			{
				myRobot.setTurnRadarLeftRadians(myRobot.getRadarTurnRemainingRadians());
			}
			radarTick = 0;
		}
		noRadarTick = 0;

		doRadar();
	}

	@Override
	public void run()
	{
		if (scannedEnemys.size() > 0) radarTick++;
	}

	// ---------------------------- working stuff --------------------------------------
	private void doRadar()
	{}

}
