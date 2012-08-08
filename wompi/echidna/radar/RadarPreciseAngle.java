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
import java.util.Comparator;
import java.util.TreeSet;

import robocode.AdvancedRobot;
import robocode.DeathEvent;
import robocode.RobotDeathEvent;
import robocode.StatusEvent;
import robocode.WinEvent;
import robocode.util.Utils;
import wompi.echidna.misc.SimpleAverage;
import wompi.echidna.target.ATarget;
import wompi.robomath.RobotMath;
import wompi.wallaby.PaintHelper;
import wompi.wallaby.WallabyPainter;

public class RadarPreciseAngle extends ARadar
{
	// enemy values

	// control values
	ArrayList<ATarget>	myTargets	= new ArrayList<ATarget>();
	int					noRadarTick;
	// int radarTick;
	double				startRadarHead;

	// debug
	double				lastRadar;
	long				debugTime;								// needed because recursion
	SimpleAverage		avgRadarDelta;

	public RadarPreciseAngle(AdvancedRobot robot)
	{
		super(robot);
	}

	@Override
	public void init()
	{
		avgRadarDelta = new SimpleAverage(4000, "blind");
		// myRobot.setAdjustRadarForGunTurn(false);
		myRobot.setTurnRadarRightRadians(Double.MAX_VALUE); // init stuff
		startRadarHead = myRobot.getRadarHeadingRadians();;
	}

	public void onStatus(StatusEvent event)
	{
		System.out.format("STATUS[%d] \n", myRobot.getTime());

	}

	@Override
	public void onScannedRobot(ATarget target, boolean isMaintarget)
	{
		if (!myTargets.contains(target))
		{
			myTargets.add(target);
		}
	}

	public void onRobotDeath(RobotDeathEvent e)
	{
		// scannedEnemys.remove(e.getName());

		for (ATarget target : myTargets)
		{
			if (target.getName() == e.getName())
			{
				myTargets.remove(target);
				break;
			}
		}
	}

	// debug functions ----------------------------------------------------------------------------------------
	@Override
	public void run()
	{

		debugRadarDelta();  // debug
		final Point2D rP = new Point2D.Double(myRobot.getX(), myRobot.getY());

		Comparator<ATarget> compi = new Comparator<ATarget>()
		{
			@Override
			public int compare(ATarget o1, ATarget o2)
			{
				double a1 = RobotMath.calculateAngle(rP, new Point2D.Double(o1.getX(), o1.getY()));
				double a2 = RobotMath.calculateAngle(rP, new Point2D.Double(o2.getX(), o2.getY()));
				double angle1 = Utils.normalAbsoluteAngle(a1);
				double angle2 = Utils.normalAbsoluteAngle(a2);
				return Double.compare(angle1, angle2);
			}
		};
		TreeSet<ATarget> depp = new TreeSet<ATarget>(compi);
		depp.addAll(myTargets);

		double max = 0;
		ATarget lastT = null;
		double lastAngle = 0;
		double firstAngle = 0;
		double diff;
		for (ATarget t : depp)
		{
			double a = RobotMath.calculateAngle(rP, new Point2D.Double(t.getX(), t.getY()));
			double angle = Utils.normalAbsoluteAngle(a);

			if (lastT == null)
			{
				System.out.format("[%d] angle=%3.2f %s \n", myRobot.getTime(), Math.toDegrees(angle), t.getName());
				firstAngle = angle;
			}
			else
			{
				diff = angle - lastAngle;
				System.out.format("[%d] angle=%3.2f diff=%3.2f %s \n", myRobot.getTime(), Math.toDegrees(angle), Math.toDegrees(diff), t.getName());
			}
			lastT = t;
			lastAngle = angle;
		}
		diff = (Math.toRadians(360) - lastAngle) + firstAngle;
		System.out.format("[%d] roundDiff diff=%3.2f \n", myRobot.getTime(), Math.toDegrees(diff));

	}

	@Override
	public void onPaint(Graphics2D g)
	{
		WallabyPainter.drawAngleLine(g, new Point2D.Double(myRobot.getX(), myRobot.getY()), startRadarHead, 700, PaintHelper.yellowTrans);
	}

	public void onWin(WinEvent event)
	{
		avgRadarDelta.onPrint("RADAR AVERAGE", false);
	}

	public void onDeath(DeathEvent event)
	{
		avgRadarDelta.onPrint("RADAR AVERAGE", false);
	};

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
		debugTime = myRobot.getTime();
	}

	class ScanTarget
	{

	}

}
