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
import java.awt.geom.Point2D;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.Condition;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.RobotStatus;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.util.Utils;

public class Irukandji extends AdvancedRobot
{
	public static final double			DIST		= 100;
	public static final double			RADAR		= 1.9;

	private static ScannedRobotEvent	scan;
	private static StatusEvent			status;

	private static double				bPower;
	private static double[][]			guessFactor	= new double[13][31];

	public Irukandji()
	{

	}

	@Override
	public void run()
	{
		setAllColors(Color.RED);
		setAdjustGunForRobotTurn(true);
		scan = null;
		while (true)
		{
			if (scan != null) setTurnRadarRightRadians(Utils.normalRelativeAngle(getHeadingRadians() + scan.getBearingRadians()
					- getRadarHeadingRadians())
					* RADAR);
			if (getRadarTurnRemaining() == 0) setTurnRadarRightRadians(Double.POSITIVE_INFINITY);

			doMove();
			doGun();
			execute();
		}
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		status = e;
	}

	@Override
	public void onScannedRobot(final ScannedRobotEvent e)
	{
		scan = e;

		addCustomEvent(new Condition()
		{
			ScannedRobotEvent	myScan		= e;
			RobotStatus			myStatus	= status.getStatus();
			double				myPower		= bPower;

			@Override
			public boolean test()
			{
				double eX = getX() + Math.sin(getHeadingRadians() + scan.getBearingRadians()) * scan.getDistance();
				double eY = getY() + Math.cos(getHeadingRadians() + scan.getBearingRadians()) * scan.getDistance();

				if (Point2D.distance(getX(), getY(), eX, eY) <= (getTime() - myScan.getTime()) * Rules.getBulletSpeed(myPower))
				{
					double cBear = Math.atan2(eX - myStatus.getX(), eY - myStatus.getY());
					double sBear = myStatus.getHeadingRadians() + myScan.getBearingRadians();
					double angleOffset = Utils.normalRelativeAngle(cBear - sBear);

					// TODO: if direction is zero ... delay the direction and wait till he moves than take the direction
					double eLat = Math.sin(myScan.getHeadingRadians() - sBear) * myScan.getVelocity();
					double gf = Math.max(-1, Math.min(1, angleOffset / getMEA(myPower))) * Math.signum(eLat);
					int index = (int) Math.round(15 * (gf + 1));
					guessFactor[(int) (myScan.getDistance() / 100)][index]++;
					removeCustomEvent(this);
					return true;
				}
				return false;
			}
		});
	}

	public void doMove()
	{}

	public void doGun()
	{
		if (scan == null) return;

		if (getGunTurnRemaining() == 0)
		{
			setFire(bPower);
		}

		bPower = Math.max(3.0, Math.min(scan.getEnergy() / 4.0, 600 / scan.getDistance()));
		int bestindex = 15;
		int dIndex = (int) (scan.getDistance() / 100);
		for (int i = 0; i < 31; i++)
			if (guessFactor[dIndex][bestindex] < guessFactor[dIndex][i]) bestindex = i;

		double gf = (double) (bestindex - 15) / 15;
		double eBear = getHeadingRadians() + scan.getBearingRadians();
		double eLat = Math.sin(scan.getHeadingRadians() - eBear) * scan.getVelocity();
		double adjust = Math.signum(eLat) * gf * getMEA(bPower);
		setTurnGunRightRadians(Utils.normalRelativeAngle(eBear - getGunHeadingRadians() + adjust));
	}

	@Override
	public void onHitWall(HitWallEvent event)
	{}

	@Override
	public void onRobotDeath(RobotDeathEvent e)
	{}

	@Override
	public void onBulletHit(BulletHitEvent e)
	{}

	public void onHitRobot(HitRobotEvent e, RobotStatus status)
	{}

	private double getMEA(double bPower)
	{
		return Math.sin(8.0 / Rules.getBulletSpeed(bPower));
	}
}
