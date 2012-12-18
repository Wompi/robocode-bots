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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Vector;

import robocode.AdvancedRobot;
import robocode.DeathEvent;
import robocode.RobotDeathEvent;
import robocode.Rules;
import robocode.StatusEvent;
import robocode.WinEvent;
import robocode.util.Utils;
import wompi.echidna.misc.SimpleAverage;
import wompi.echidna.target.ATarget;
import wompi.paint.PaintHelper;
import wompi.wallaby.WallabyPainter;

public class RadarFieldAngleDependent extends ARadar
{
	private static final double	PI				= Math.PI;
	private static final double	PI_360			= Math.PI * 2.0;
	private static final double	PI_135			= Math.PI * 0.75;
	private static final double	SCAN_OFFSET		= 10.0;

	// enemy values
	ATarget						myTarget;

	// control values
	HashSet<String>				scannedEnemys	= new HashSet<String>();
	Vector<ATarget>				currentScans	= new Vector<ATarget>();
	ATarget						minTarget;
	ATarget						maxTarget;

	ATarget						firstTarget;
	double						max;
	double						min;
	double						lastKnownDir;

	double						noRadarAngle;
	double						lastRadar;
	double						deltaRadar;

	boolean						isReset;

	double						rStart;
	double						rEnd;

	// debug
	SimpleAverage				avgRadarDelta;
	boolean						isLog			= true;
	int							logLevel		= 0;

	public RadarFieldAngleDependent(AdvancedRobot robot)
	{
		super(robot);
		avgRadarDelta = new SimpleAverage(4000, "radar");
	}

	@Override
	public void init()
	{
		double rHead = myRobot.getRadarHeadingRadians();

		myRobot.setAdjustRadarForGunTurn(true);
		// myRobot.setTurnRadarRightRadians(Double.MAX_VALUE); // init stuff
		startBestAngleScan();
		lastRadar = rHead;
		noRadarAngle = 0;
		rStart = rHead;
		rEnd = rHead;
		log(String.format("INIT[%d] start = end = lastRadar = %3.2f noRadar=0\n", myRobot.getTime(), Math.toDegrees(rHead)), 3);
	}

	public void onStatus(StatusEvent event)
	{
		if (event.getTime() == 0) return;   // damn init will first start on turn 1
		double rHead = myRobot.getRadarHeadingRadians();
		deltaRadar = Utils.normalRelativeAngle((rHead - lastRadar));
		currentScans.clear();
		minTarget = null;
		maxTarget = null;
		max = Double.NEGATIVE_INFINITY;
		min = Double.POSITIVE_INFINITY;
		noRadarAngle += deltaRadar;
		isReset = false;

		// debug
		avgRadarDelta.avg(deltaRadar, event.getTime());
		log(String.format("\nSTATUS[%d] deltaRadar=%3.2f noRadarAngle=%3.2f rHead=%3.2f \n", event.getTime(), Math.toDegrees(deltaRadar),
				Math.toDegrees(noRadarAngle), Math.toDegrees(rHead)), 1);
	}

	@Override
	public void onRobotDeath(RobotDeathEvent e)
	{
		scannedEnemys.remove(e.getName());
		log(String.format("ROBOT_DEATH[%d] others=%d killed %s\n", myRobot.getTime(), myRobot.getOthers(), e.getName()), 3);
	}

	@Override
	public void onScannedRobot(ATarget target, boolean isMaintarget)
	{
		calculateBorderTarget(target);
		currentScans.add(target);
		log(String.format("SCAN[%d] add %s\n", myRobot.getTime(), target.getName()), 2);
	}

	@Override
	public void onPaint(Graphics2D g)
	{
		Point2D rP = new Point2D.Double(myRobot.getX(), myRobot.getY());

		if (minTarget != null && maxTarget != null)
		{
			double eMinBearing = minTarget.getBearing() + myRobot.getHeadingRadians();
			double eMaxBearing = maxTarget.getBearing() + myRobot.getHeadingRadians();
			double dMax = 0;
			double dMin = 0;
			// double dMax = Math.signum(deltaRadar) * Math.toRadians(SCAN_OFFSET);
			// double dMin = Math.signum(deltaRadar) * Math.toRadians(SCAN_OFFSET);

			double eMaxDistance = maxTarget.getDistance();
			double eMinDistance = minTarget.getDistance();
			WallabyPainter.drawAngleLine(myRobot.getGraphics(), rP, eMaxBearing + dMax, eMaxDistance, Color.red);
			WallabyPainter.drawAngleLine(myRobot.getGraphics(), rP, eMinBearing - dMin, eMinDistance, Color.YELLOW);
			log(String.format("PAINT[%d] min=%3.2f max=%3.2f\n", myRobot.getTime(), Math.toDegrees(eMinBearing), Math.toDegrees(eMaxBearing)), 3);
		}

		WallabyPainter.drawAngleLine(g, rP, rStart, 1200, PaintHelper.yellowTrans);
		WallabyPainter.drawAngleLine(g, rP, rEnd, 1200, PaintHelper.redTrans);
		double arc = Utils.normalRelativeAngle(rEnd - rStart);
		PaintHelper.drawArc(rP, 1200, rStart, arc, true, g, PaintHelper.whiteTrans);

		log(String.format("PAINT[%d] arc=%3.2f start=%3.2f end=%3.2f\n", myRobot.getTime(), Math.toDegrees(arc), Math.toDegrees(rStart),
				Math.toDegrees(rEnd)), 3);
	}

	@Override
	public void run()
	{
		if (myRobot.getOthers() == scannedEnemys.size())
		{
			scannedEnemys.clear();
			if (Math.abs(deltaRadar) <= PI)   // change this to real angle
			{
				double rRemain = myRobot.getRadarTurnRemainingRadians();
				double rDir = Math.signum(rRemain);
				if (firstTarget != null)
				{
					double absBearing = firstTarget.getBearing() + myRobot.getHeadingRadians();
					double dOff = Math.signum(deltaRadar) * Math.toRadians(SCAN_OFFSET);
					rEnd = Utils.normalAbsoluteAngle(absBearing - dOff);
				}
				else rEnd = rStart;

				double rToEnd = Utils.normalRelativeAngle(myRobot.getRadarHeadingRadians() - rEnd);
				myRobot.setTurnRadarLeftRadians(rToEnd);
				lastKnownDir = -Math.signum(myRobot.getRadarTurnRemainingRadians());

				// rEnd = rStart;
				log(String.format("RUN[%d] SWITCH BREAK deltaRadar=%3.2f rEnd=%3.2f remain=%3.2f \n", myRobot.getTime(),
						Math.toDegrees(Math.abs(deltaRadar)), Math.toDegrees(rEnd), Math.toDegrees(myRobot.getRadarTurnRemainingRadians())), 1);
			}
			else
			{
				log(String.format("RUN[%d] SPIN BREAK deltaRadar=%3.2f \n", myRobot.getTime(), Math.toDegrees(deltaRadar)), 2);
			}

			// if we spin or switch the current targets going bac in the scannedlist
			for (ATarget target : currentScans)
			{
				calculateBorderTarget(target);
				// scannedEnemys.add(target.getName());
				isReset = true;								// reset only if we have a target
				log(String.format("RUN[%d] refill %s\n", myRobot.getTime(), target.getName()), 2);
			}
		}

		if (myRobot.getRadarTurnRemainingRadians() == 0)
		{
			myRobot.setTurnRadarLeftRadians(Rules.RADAR_TURN_RATE_RADIANS * lastKnownDir);
			log(String.format("RUN[%d] READJUST\n", myRobot.getTime()), 1);
		}

		if (isReset)
		{
			firstTarget = maxTarget;
			double absBearing = maxTarget.getBearing() + myRobot.getHeadingRadians();
			double dOff = Math.signum(deltaRadar) * Math.toRadians(SCAN_OFFSET);
			rStart = Utils.normalAbsoluteAngle(absBearing + dOff);
			log(String.format("RUN[%d] rStart=%3.2f \n", myRobot.getTime(), Math.toDegrees(rStart)), 1);
		}

		if (Math.abs(noRadarAngle) >= PI_135)
		{
			scannedEnemys.clear();
			log(String.format("RUN[%d] CLEAR noRadar=%3.2f \n", myRobot.getTime(), Math.toDegrees(noRadarAngle)), 2);
		}
		else
		{
			log(String.format("RUN[%d] noRadar=%3.2f \n", myRobot.getTime(), Math.toDegrees(noRadarAngle)), 2);
		}
		lastRadar = myRobot.getRadarHeadingRadians();
	}

	private void calculateBorderTarget(ATarget target)
	{
		noRadarAngle = 0;
		double eAbsBearing = target.getBearing() + myRobot.getHeadingRadians();
		double diff = Math.abs(Utils.normalRelativeAngle(eAbsBearing - lastRadar));

		if (diff > max)
		{
			max = diff;
			maxTarget = target;
		}
		if (diff < min)
		{
			min = diff;
			minTarget = target;
		}
		isReset = scannedEnemys.isEmpty();
		scannedEnemys.add(target.getName());
		log(String.format("CALC[%d] size=[%d,%d] bear=%3.2f diff=%3.2f last=%3.2f %s\n", myRobot.getTime(), currentScans.size(), myRobot.getOthers(),
				Math.toDegrees(eAbsBearing), Math.toDegrees(diff), Math.toDegrees(lastRadar), target.getName()), 2);
	}

	// debug functions ----------------------------------------------------------------------------------------
	public void onWin(WinEvent event)
	{
		avgRadarDelta.onPrint("RADAR AVERAGE", false);
	}

	public void onDeath(DeathEvent event)
	{
		avgRadarDelta.onPrint("RADAR AVERAGE", false);
	};

	private void log(String msg, int level)
	{
		if (isLog && level <= logLevel)
		{
			System.out.format(msg);
		}
	}

}
