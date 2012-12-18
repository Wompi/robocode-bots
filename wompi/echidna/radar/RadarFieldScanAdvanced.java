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
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Vector;

import robocode.AdvancedRobot;
import robocode.DeathEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.WinEvent;
import robocode.util.Utils;
import wompi.echidna.misc.SimpleAverage;
import wompi.paint.PaintHelper;
import wompi.wallaby.WallabyPainter;

public class RadarFieldScanAdvanced extends ARadar
{
	// enemy values

	// control values

	// i have a strong feeing that i could get rid of this big set, if i just take the radarTicks and if i count to getOthers without names or
	// something
	// problem is every radar scan can have multiple enemys and if i turn the radar i scan the same enmy again and so i have no idear when i reach
	// the last on ... first workaround can calculate the bearing and set a bearing border to the last scaned enemy ....
	// onother problem is that you can scan enemys just on the edge ... and in next turn you get the same enemy again even with a bearing border...
	// but im sure there is an easy way to do it ... i dont have it just found ... this would save hugh code size
	HashSet<String>				scannedEnemys	= new HashSet<String>();
	int							noRadarTick;
	int							radarTick;
	Vector<ScannedRobotEvent>	myScans;									// if you dont want to do this you can put all the stuff in onStatus and
																			// dnt need this anymore

	// debug
	double						debugRadar;
	double						lastRadar;
	long						debugTime;									// needed because recursion
	SimpleAverage				avgRadarDelta;

	public RadarFieldScanAdvanced(AdvancedRobot robot)
	{
		super(robot);
	}

	@Override
	public void init()
	{
		avgRadarDelta = new SimpleAverage(4000, "blind");
		myRobot.setAdjustRadarForGunTurn(false);
		myRobot.setTurnRadarRightRadians(Double.MAX_VALUE); // init stuff
	}

	public void onStatus(StatusEvent e)
	{
		myScans = myRobot.getScannedRobotEvents();
	}

	public void onRobotDeath(RobotDeathEvent e)
	{
		scannedEnemys.remove(e.getName());
	}

	@Override
	public void run()
	{
		// System.out.format("\n");
		debugRadarDelta();  // debug
		noRadarTick++;

		for (ScannedRobotEvent e : myScans)
		{
			if (scannedEnemys.add(e.getName()))
			{
				if (scannedEnemys.size() == myRobot.getOthers())
				{
					// well this looks nice ... but be careful with recursion
					// Idear: if all enemys are scanned we delete the collection and call run again, because we need all scannedrobots to
					// put they in the scannedlist. This is the start of our new scansweep and of course we want to start with the curent scaned
					// enemys
					// because we clear the enemys right heare the recursion won't fail.. until!! all robots in on spot and we get a full list of
					// robot
					// this is very awkward and will disable your robot i think because you have an infinity recursion
					// to fix this i should probably check the size of getScannedRobotEvents() and call run only if this size is not getOthers
					// hmm this happens all the time if we only have one robat ... bummer
					scannedEnemys.clear();
					if (radarTick <= 4)
					{
						myRobot.setTurnRadarLeftRadians(myRobot.getRadarTurnRemainingRadians());
					}
					// System.out.format("RUN[%d] clear/switch radarTick=%d\n", myRobot.getTime(),radarTick);
					debugRadar = myRobot.getRadarHeadingRadians(); // debug
					if (myScans.size() < myRobot.getOthers())
					{
						// System.out.format("RECUSION\n");
						run();
					}
					radarTick = 0;
				}
				// System.out.format("RUN[%d] add %d %s\n", myRobot.getTime(),scannedEnemys.size(),e.getName());
				// radarTick++; // count only if we have a new target
			}
			noRadarTick = 0;
		}

		if (noRadarTick >= 2)
		{
			// System.out.format("RUN[%d] clear\n", myRobot.getTime());
			debugRadar = myRobot.getRadarHeadingRadians(); // debug
			scannedEnemys.clear();   // if we have more than two emty scans we delete the enemylist so we can start a new sweep with the first new one
			radarTick = 0;
		}
		radarTick++;
		// System.out.format("RUN[%d] radarTic=%d\n", myRobot.getTime(),radarTick);
	}

	@Override
	public void onPaint(Graphics2D g)
	{
		WallabyPainter.drawAngleLine(g, new Point2D.Double(myRobot.getX(), myRobot.getY()), debugRadar, 700, PaintHelper.yellowTrans);
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
		if (debugTime == myRobot.getTime()) return;  // because recursion this would be called twice so i cut this after the first call

		double rHead = myRobot.getRadarHeading();
		double SCAN_DIR = Math.signum(myRobot.getRadarTurnRemaining());

		// if deta < 0 the normalization gives the rigth angle (hopefully)
		// so delta is always positive then
		double deltaRadar = Utils.normalAbsoluteAngleDegrees((rHead - lastRadar) * SCAN_DIR);
		avgRadarDelta.avg(deltaRadar, myRobot.getTime());
		// System.out.format("RADAR[%d] rRemain=%3.2f gRemain=%3.2f dirRadar=%3.0f\n",myRobot.getTime(),deltaRadar,myRobot.getGunTurnRemaining(),SCAN_DIR);
		lastRadar = myRobot.getRadarHeading();
		debugTime = myRobot.getTime();
	}

}
