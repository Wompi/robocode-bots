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
package wompi.echidna.target;

import java.awt.Graphics2D;

import robocode.AdvancedRobot;
import robocode.Bullet;
import robocode.DeathEvent;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.WinEvent;
import robocode.util.Utils;
import wompi.echidna.misc.RollingAverage;
import wompi.echidna.misc.SimpleAverage;
import wompi.echidna.stats.HitStats;
import wompi.wallaby.TargetDistanceTracker;

public class TargetAveraged extends ATarget
{
	// enemy values
	double					aVelocity;
	double					aHeadDiff;

	// control values
	SimpleAverage			avgVelocity;
	SimpleAverage			avgHeadDiff;
	SimpleAverage			avgBlindTime;

	RollingAverage			ravgVelocity;
	RollingAverage			ravg200Velocity;

	RollingAverage			ravgHeadDiff;
	RollingAverage			ravgBlindTime;

	TargetDistanceTracker	targetTracker;
	// DisplacementVector myDistDisplacement;

	double					energySwitch;

	// debug
	HitStats				hitStats;
	private static int		colorIndex;

	public TargetAveraged(AdvancedRobot robot)
	{
		super(robot);
		colorIndex++;
		avgVelocity = new SimpleAverage(5000, "velo");
		avgHeadDiff = new SimpleAverage(1, "headDiff");
		avgBlindTime = new SimpleAverage(4000, "blindTime");

		ravgVelocity = new RollingAverage(30, "rvelo");
		ravg200Velocity = new RollingAverage(4000, "rvelo");
		ravgHeadDiff = new RollingAverage(1, "rheadDiff");
		ravgBlindTime = new RollingAverage(20, "rblindTime");

		// myDistDisplacement = new DisplacementVector();

		targetTracker = new TargetDistanceTracker(1000);
		hitStats = new HitStats();
	}

	public void init()
	{
		// hitStats.printStats(eName); // debug
		eScan = 0;
		eHeading = Double.MAX_VALUE;
		eLiveShotPower = 0;
		eLastEnergy = 100;  // be careful with teams
		eLastScan = 0;
	}

	public void onScannedRobot(ScannedRobotEvent e)
	{
		double rHead = myRobot.getHeadingRadians();
		if (eHeading != Double.MAX_VALUE)
		{
			eHeadDiff = Utils.normalRelativeAngle(e.getHeadingRadians() - eHeading);    // put this in an averaged object
			aHeadDiff = avgHeadDiff.avg(eHeadDiff, eScan);
		}
		isAlive = true;							// don't forget this to set
		eHeading = e.getHeadingRadians();
		eDistance = e.getDistance();
		eLastEnergy = eEnergy;
		eEnergy = e.getEnergy();
		eLastVelocity = eVelocity;
		eVelocity = e.getVelocity();

		eLastScan = eScan;
		eScan = e.getTime();
		eName = e.getName();
		eBearing = e.getBearingRadians();
		eAbsBearing = eBearing + rHead;

		// eX = myRobot.getX() + Math.sin(e.getBearingRadians() + rHead) * eDistance;
		// eY = myRobot.getY() + Math.cos(e.getBearingRadians() + rHead) * eDistance;
		eX = Math.sin(e.getBearingRadians() + rHead) * eDistance;
		eY = Math.cos(e.getBearingRadians() + rHead) * eDistance;
		eAbsX = myRobot.getX() + eX;
		eAbsY = myRobot.getY() + eY;

		// myDistDisplacement.registerPostion(eY+myRobot.getX(), eY+myRobot.getY(),eScan);

		aVelocity = avgVelocity.avg(eVelocity, eScan);
		// aVelocity = e.getVelocity();
		avgBlindTime.avg(eScan - eLastScan, eScan);

		// double arVelocity = ravgVelocity.avg(eVelocity,eScan,10);
		// double test = ravg200Velocity.avg(eVelocity,eScan,1);
		// System.out.format("AVG[%d]: v=%3.2f avg=%3.2f ravg=%3.2f r200=%3.2f %s \n", e.getTime(),eVelocity,aVelocity,arVelocity,test,e.getName());

		// aVelocity = aVelocity;
		ravgBlindTime.avg(eScan - eLastScan, eScan, 1);

		if (eVelocity != 0)
		{
			eDir = (Math.sin(eHeading - eAbsBearing) * eVelocity < 0) ? -1 : 1;
		}
	}

	@Override
	public double getVelocity()
	{
		return aVelocity;
	}

	@Override
	public double getHeadDiff()
	{
		return aHeadDiff;
	}

	// debug
	public void onBulletStatsDebug(Bullet shot)
	{
		hitStats.myBullets.add(shot);
	}

	public void onWin(WinEvent event)
	{
		// avgBlindTime.onPrint(eName,false);
		// avgVelocity.onPrint(eName,false);
		// avgHeadDiff.onPrint(eName,false);
		hitStats.printStats(eName);
	};

	public void onDeath(DeathEvent event)
	{
		// avgBlindTime.onPrint(eName,false);
		// avgVelocity.onPrint(eName,false);
		// avgHeadDiff.onPrint(eName,false);
		hitStats.printStats(eName);
	};

	public void onStatus(StatusEvent e)
	{
		// avgVelocity.onPrint(eName,true);
		// avgHeadDiff.onPrint(eName,true);
	}

	public void onPaint(Graphics2D g)
	{
		// myDistDisplacement.onPaint(g, PaintHelper.colorField[colorIndex-1]);
	}

}
