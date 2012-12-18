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
import java.util.ArrayList;
import java.util.HashSet;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.Rules;
import robocode.StatusEvent;
import robocode.util.Utils;
import wompi.echidna.target.ATarget;
import wompi.paint.PaintHelper;
import wompi.wallaby.WallabyPainter;

public class RadarPreciseFieldScan extends ARadar
{
	// enemy values
	ATarget				myTarget;

	// robot values

	// control values
	HashSet<String>		scannedEnemys		= new HashSet<String>();
	ArrayList<String>	roundScanedEnemys	= new ArrayList<String>();
	double				scanTurn;
	double				noScanTurn;
	int					SCAN_DIR;
	double				targetAngle;

	double				startRadarHead;

	public RadarPreciseFieldScan(AdvancedRobot robot)
	{
		super(robot);
	}

	@Override
	public void init()
	{
		SCAN_DIR = 1;
		myRobot.setAdjustRadarForGunTurn(true);
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		System.out.format("\n");
		roundScanedEnemys.clear();
	}

	public void onRobotDeath(RobotDeathEvent e)
	{
		scannedEnemys.remove(e.getName());
	}

	public void onScannedRobot(ATarget target)
	{
		myTarget = target;
		doRadar();
	}

	@Override
	public void run()
	{
		double rHead = myRobot.getRadarHeading();

		// if deta < 0 the normalization gives the rigth angle (hopefully)
		// so delta is always positive then
		double deltaRadar = Utils.normalAbsoluteAngleDegrees((rHead - startRadarHead) * SCAN_DIR);

		System.out.format("deltaradar=%3.10f start=%3.2f radarhead=%3.2f DIR=%d\n", deltaRadar, startRadarHead, rHead, SCAN_DIR);
		System.out.format("BOOL = %b\n", (deltaRadar <= 180) ? true : false);

		if (roundScanedEnemys.size() > 0)
		{
			scannedEnemys.addAll(roundScanedEnemys);
		}

		if (scannedEnemys.size() == 0)
		{
			startRadarHead = 0;
			System.out.format("RESET EMPTY\n");
		}
		else if (scannedEnemys.size() == myRobot.getOthers())				// wenn der neu scan die liste gefuellt hat loesche sie aber addiere den aktuellen
																// wieder dazu
		{
			scannedEnemys.clear();
			scannedEnemys.addAll(roundScanedEnemys);

			// startRadarHead = rHead;
			startRadarHead = Math.toDegrees(targetAngle);

			if (deltaRadar <= 180 && deltaRadar > 0)   // 360 degree if delta is 0 ... very rare i think
			{
				System.out.format("SCAN SWITCH\n");
				SCAN_DIR = -SCAN_DIR;
			}
			else
			{
				System.out.format("SCAN RESET\n");
			}
		}
		else
		{
			if (startRadarHead == 0)
			{
				// startRadarHead = rHead;
				startRadarHead = Math.toDegrees(targetAngle);
				System.out.format("START");
			}
		}

		// System.out.format("RUN[%d] gun=%3.2f \n", myRobot.getTime(),myRobot.getGunTurnRemaining());
		System.out.format("RUN[%d] radarHead=%3.2f size=%d start=%3.2f\n", myRobot.getTime(), myRobot.getRadarHeading(), scannedEnemys.size(),
				startRadarHead);
		// System.out.format("RUN[%d] SCAN_DIR=%d \n", myRobot.getTime(),SCAN_DIR);
		// System.out.format("RUN[%d] size=%d \n", myRobot.getTime(),scannedEnemys.size());
		// System.out.format("RUN[%d] scanTurn=%3.2f \n", myRobot.getTime(),scanTurn);
		//

		myRobot.setTurnRadarRight(SCAN_DIR * Rules.RADAR_TURN_RATE);
	}

	@Override
	public void onPaint(Graphics2D g)
	{
		WallabyPainter.drawAngleLine(g, new Point2D.Double(myRobot.getX(), myRobot.getY()), Math.toRadians(startRadarHead), 700,
				PaintHelper.yellowTrans);

	}

	// ---------------------------- working stuff --------------------------------------
	private void doRadar()
	{
		roundScanedEnemys.add(myTarget.getName());

		double angle = Utils.normalAbsoluteAngle(myTarget.getBearing() + myRobot.getHeadingRadians());
		// if (SCAN_DIR == 1) targetAngle = Math.max(Math.PI*2 - angle, targetAngle);
		targetAngle = angle;

		WallabyPainter.drawAngleLine(myRobot.getGraphics(), new Point2D.Double(myRobot.getX(), myRobot.getY()), targetAngle, myTarget.getDistance(),
				PaintHelper.greenTrans);
		System.out.format("targetAngle=%3.2f starAngle=%3.2f dir=%d \n", Math.toDegrees(targetAngle), startRadarHead, SCAN_DIR);

	}
}
