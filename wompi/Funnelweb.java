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
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import robocode.AdvancedRobot;
import robocode.Bullet;
import robocode.DeathEvent;
import robocode.HitRobotEvent;
import robocode.RobotDeathEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.WinEvent;
import robocode.util.Utils;
import wompi.echidna.stats.HitStats;

public class Funnelweb extends AdvancedRobot
{
	private static final double					FIELD_W				= 1000.0;
	private static final double					FIELD_H				= 1000.0;

	private static final double					WZ_G				= 17.0;
	private static final double					WZ_G_W				= FIELD_W - 2 * WZ_G;
	private static final double					WZ_G_H				= FIELD_H - 2 * WZ_G;

	private final static double					DIST				= 185;
	private final static double					DIST_REMAIN			= 20;

	private final static double					GUNLOCK				= 1.0;
	private final static double					TARGET_FORCE		= 55000;												// 100000 low dmg high surv - 10000 high dmg low surv  
	private final static double					TARGET_DISTANCE		= 450.0;												// 400 last best - shoot at TARGET_DISTANCE with bullet 1.0

	private final static double					PI_360				= Math.PI * 2.0;
	private final static double					DELTA_RISK_ANGLE	= Math.PI / 32.0;
	private final static double					MAX_HEAD_DIFF		= 0.161442955809475;									// 9.25 degree
	private final static double					ENERGY_ADJUST		= 4.0;
	private final static double					INF					= Double.POSITIVE_INFINITY;
	private final static double					BMAX				= Rules.MAX_BULLET_POWER;
	private final static double					BMIN				= Rules.MIN_BULLET_POWER;

	private final static Rectangle2D			bField				= new Rectangle2D.Double(WZ_G, WZ_G, WZ_G_W, WZ_G_H);

	// index:  0:x 1:y 2:heading 3:avgVelocity 4:avgVelocityCounter 5: distance
	static final HashMap<String, FunnelTarget>	allTargets			= new HashMap<String, FunnelTarget>();

	static String								eName;
	static double								eRate;

	static double								avgHeading;
	static double								avgHeadCount;

	static double								bPower;
	static double								rDist;

	@Override
	public void run()
	{
		setAllColors(Color.RED); // 7 byte
		setAdjustGunForRobotTurn(true);
		//setAdjustRadarForGunTurn(true);
		setTurnRadarRightRadians(eRate = INF);

		for (FunnelTarget t : allTargets.values())
		{
			t.reset();
		}

	}

	@Override
	public void onStatus(StatusEvent e)
	{
		double sumEnergy = getEnergy();
		//double sumDistance = 0;
		for (FunnelTarget t : allTargets.values())
		{
			if (t.isAlive)
			{
				sumEnergy += t.tEnergy;
				//sumDistance += t.tDistance;
			}
		}

		System.out.format("[%03d] %3.5f %s \n", getTime(), getEnergy() / sumEnergy, getName());
		for (FunnelTarget t : allTargets.values())
		{
			if (t.isAlive)
			{
				double dangerEnergy = t.tEnergy / sumEnergy;
				//double dangerDistance = 1 - t.tDistance / sumDistance;
				t.tDanger = dangerEnergy; // * dangerDistance;
				System.out.format("[%03d] %3.5f  %s \n", getTime(), t.tDanger, t.tName);
			}
		}
		System.out.format("\n");
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		FunnelTarget enemy = allTargets.get(e.getName());
		if (enemy == null)
		{
			enemy = new FunnelTarget();
			enemy.tName = e.getName();
			allTargets.put(e.getName(), enemy);
		}
		double aBear = getHeadingRadians() + e.getBearingRadians();
		double xGunRel = enemy.tx = Math.sin(aBear) * e.getDistance();
		double yGunRel = enemy.ty = Math.cos(aBear) * e.getDistance();

		enemy.isAlive = true;
		enemy.tDistance = e.getDistance();
		enemy.tEnergy = e.getEnergy();
		enemy.avgVeloCounter++;
		enemy.avgVelocity += Math.abs(e.getVelocity()); // this is the velocity sum not the average
		double vGun = enemy.avgVelocity * Math.signum(e.getVelocity()) / enemy.avgVeloCounter;

		rDist = Math.min(DIST, rDist += 5);
		boolean isClose = true;

		if (eRate > e.getDistance() || eName == e.getName())
		{
			eName = e.getName();

			// Fire rules
			if (getEnergy() > bPower && getGunTurnRemaining() == 0)
			{
				Bullet b = setFireBullet(bPower);
				if (b != null)
				{
					enemy.tStats.addBullet(b, getOthers());
				}
			}

			// Heading reset if target not seen the last turn (MAX_HEAD_DIFF) translates to roughly 1 turn 
			double dHeading = e.getHeadingRadians() - enemy.tHeading;
			enemy.tHeading = e.getHeadingRadians();
			if (Math.abs(dHeading) > MAX_HEAD_DIFF)
			{
				// this might be a problem if I see the wrong target?
				dHeading = avgHeading = avgHeadCount = 0;
			}

			if (getGunHeat() < GUNLOCK)
			{
				// calculate the heading delta  
				avgHeading += Math.abs(dHeading);
				avgHeadCount++;
				dHeading = avgHeading * Math.signum(dHeading) / avgHeadCount;

				// turn the radar if the gun lock is near shooting
				// TODO: think again about normal radar lock - might be not so bad 
				double rTurn = INF * Utils.normalRelativeAngle(aBear - getRadarHeadingRadians());
				if (!Utils.isNear(0.0, rTurn))
				{
					setTurnRadarRightRadians(rTurn);
				}

			}

			// the bullet power for this turn - needs to be capped for the gun formula below
			eRate = e.getDistance();
			bPower = Math.min(BMAX, TARGET_DISTANCE / eRate);

			double minDanger = Double.MAX_VALUE;
			double curDanger = 0;
			double angleDanger = 0;
			double gunTurnCounter = 0;
			double moveAngle = 0;
			double gunHeading = e.getHeadingRadians();

			while ((angleDanger += DELTA_RISK_ANGLE) <= PI_360)
			{
				double checkX = rDist * Math.sin(angleDanger);
				double checkY = rDist * Math.cos(angleDanger);
				if (bField.contains(checkX + getX(), checkY + getY()))
				{
					double angleToEnemy = Math.atan2(enemy.tx - checkX, enemy.ty - checkY);
					curDanger = Math.abs(Math.cos(angleToEnemy - angleDanger));

					for (Map.Entry<String, FunnelTarget> entry : allTargets.entrySet())
					{
						String enemyName = entry.getKey();
						FunnelTarget enemyTarget = entry.getValue();

						if (enemyTarget.isAlive)
						{
							double dSquare = Point2D.distanceSq(enemyTarget.tx, enemyTarget.ty, checkX, checkY);
							curDanger += TARGET_FORCE * (getOthers() + 1) * enemyTarget.tDanger / dSquare;

							// TODO: check for nearest target

							// TODO: this is wrong if I have moved 
							isClose |= enemyTarget.tDistance < rDist;
						}
					}

					if (Math.random() < 0.8 && curDanger < minDanger)
					{
						minDanger = curDanger;
						moveAngle = angleDanger;
					}
				}

				if (((gunTurnCounter += 0.9) * Rules.getBulletSpeed(bPower) < Math.hypot(xGunRel, yGunRel)))
				{
					gunHeading += dHeading;
					xGunRel += Math.sin(gunHeading) * vGun;
					yGunRel += Math.cos(gunHeading) * vGun;

					if (!bField.contains(xGunRel + getX(), yGunRel + getY()))
					{
						vGun = -vGun;
					}
				}
			}
			setTurnGunRightRadians(Utils.normalRelativeAngle(Math.atan2(xGunRel, yGunRel) - getGunHeadingRadians()));
			if (Math.abs(getDistanceRemaining()) <= DIST_REMAIN || isClose)
			{
				setTurnRightRadians(Math.tan(moveAngle -= getHeadingRadians()));
				setAhead(rDist * Math.cos(moveAngle));
			}
		}
	}

	@Override
	public void onHitRobot(HitRobotEvent e)
	{
		eName = e.getName();
		eRate = 0;
		rDist = 50;
	}

	//	@Override
	//	public void onBulletHit(BulletHitEvent e)
	//	{
	//		allTargets.get(e.getName())[6] += 20;
	//	}

	@Override
	public void onRobotDeath(RobotDeathEvent e)
	{
		eRate = INF;
		try
		{
			allTargets.get(e.getName()).isAlive = false;
		}
		catch (Exception e0)
		{

		}
	}

	//	@Override
	//	public void onPaint(Graphics2D g)
	//	{
	//		PaintRobotPath.onPaint(g, getName(), getTime(), getX(), getY(), Color.GREEN);
	//
	//	}

	@Override
	public void onDeath(DeathEvent event)
	{
		onWin(null);
	}

	@Override
	public void onWin(WinEvent event)
	{
		for (Map.Entry<String, FunnelTarget> entry : allTargets.entrySet())
		{
			entry.getValue().tStats.printStats(entry.getKey(), true);
		}
	}
}

class FunnelTarget
{
	double		tx;
	double		ty;

	double		tHeading;
	double		avgVelocity;
	int			avgVeloCounter;

	double		tDistance;

	double		tDanger;

	// Debug
	double		tEnergy;
	boolean		isAlive;
	String		tName;
	HitStats	tStats	= new HitStats();

	protected void reset()
	{
		tx = ty = tHeading = avgVelocity = tDistance = 0;
		tDanger = 0.1;
		avgVeloCounter = 0;
		tEnergy = 100;
		isAlive = false;
	}
}