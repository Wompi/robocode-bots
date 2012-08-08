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
import java.awt.geom.Point2D;

import robocode.AdvancedRobot;
import robocode.Bullet;
import robocode.BulletHitEvent;
import robocode.DeathEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.WinEvent;
import robocode.util.Utils;

public abstract class ATarget
{
	// enemy values
	double			eDistance;
	double			eHeading;
	double			eHeadDiff;
	double			eEnergy;
	double			eLastEnergy;
	double			eLastVelocity;
	double			eVelocity;
	long			eLastScan;
	long			eScan;
	double			eBearing;
	String			eName;
	double			eX;
	double			eY;

	// some stuff that is calculated
	double			eAbsBearing;
	double			eDir;
	double			eAbsX;
	double			eAbsY;
	double			eLiveShotPower;

	boolean			isAlive;

	// robot values
	AdvancedRobot	myRobot;

	public ATarget(AdvancedRobot robot)
	{
		myRobot = robot;
		isAlive = true;
	}

	public abstract void init();

	public abstract void onScannedRobot(ScannedRobotEvent e);

	// if you overload this be sure to call (suer.onRobotDeath...) or set the target death there
	public void onRobotDeath(RobotDeathEvent e)
	{
		isAlive = false;
	}

	// debug only
	public void onBulletStatsDebug(Bullet shot)
	{} // statndard nothing but not nescacary

	public void onWin(WinEvent event)
	{};

	public void onDeath(DeathEvent event)
	{};

	public void onStatus(StatusEvent event)
	{};

	public void onPaint(Graphics2D g)
	{};

	public void onBulletHit(BulletHitEvent e)
	{
		eEnergy = e.getEnergy();
		// System.out.format("[%d] bullethit energy = %3.2f shot=%3.2f  \n", myRobot.getTime(),eEnergy,eLiveShotPower);
	}

	// ----------------------------------- GETTER ----------------------------------------------------------------

	public final AdvancedRobot getRobot()
	{
		return myRobot;
	}

	public boolean isAlive()
	{
		return isAlive;
	}

	// if calculated within run
	public double getBlindAbsBearing()
	{
		return Utils.normalAbsoluteAngle(Math.atan2(getAbsX() - myRobot.getX(), getAbsY() - myRobot.getY()));
	}

	public double getAbsBearing()
	{
		return eAbsBearing;
	}

	public double getLastEnergy()
	{
		return eLastEnergy;
	}

	public long getTime()
	{
		return eScan;
	}

	// if calculated within run and no radar lock
	public double getBlindDistance()
	{
		return Point2D.distance(getAbsX(), getAbsY(), myRobot.getX(), myRobot.getY());
	}

	public double getDistance()
	{
		return eDistance;
	}

	public double getHeading()
	{
		return eHeading;
	}

	public double getHeadDiff()
	{
		return eHeadDiff;
	}

	public double getEnergy()
	{
		return eEnergy;
	}

	public double getLastVelocity()
	{
		return eLastVelocity;
	}

	public double getVelocity()
	{
		return eVelocity;
	}

	public long getLastScan()
	{
		return eLastScan;
	}

	public double getBearing()
	{
		return eBearing;
	}

	public String getName()
	{
		return eName;
	}

	public double getX()
	{
		return eX;
	}

	public double getY()
	{
		return eY;
	}

	public double getAbsX()
	{
		return eAbsX;
	}

	public double getAbsY()
	{
		return eAbsY;
	}

	public double getDirection()
	{
		return eDir;
	}

	public double getLiveShotPower()
	{
		return eLiveShotPower;
	}

	public void addLiveShotPower(double bulletPower)
	{
		eLiveShotPower += bulletPower;
	}

	public void removeLiveShotPower(double bulletPower)
	{
		eLiveShotPower -= bulletPower;
	}

	public long getLastScanDiff()
	{
		return myRobot.getTime() - eScan;
	}

}
