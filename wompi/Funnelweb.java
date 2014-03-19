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
import java.awt.Graphics2D;
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
import wompi.funnelweb.InfluenceMap;
import wompi.paint.PaintRiskFunction;
import wompi.stats.StatsWinLoose;

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

	static double								myDanger;

	// debug
	static int									enemyID				= 0;
	static PaintRiskFunction					myRisk				= new PaintRiskFunction();
	static InfluenceMap							influenceMap		= new InfluenceMap();
	static StatsWinLoose						myWinLooseStats		= new StatsWinLoose();

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
		myRisk.onInit(this, true);
		influenceMap.init();
		myWinLooseStats.onInit(this);
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		influenceMap.registerNodeOwner(10, Color.RED, getEnergy(), getX(), getY());

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

		//System.out.format("[%03d] %3.5f %s \n", getTime(), getEnergy() / sumEnergy, getName());
		for (FunnelTarget t : allTargets.values())
		{
			if (t.isAlive)
			{
				double dangerEnergy = t.tEnergy / sumEnergy;
				//double dangerDistance = 1 - t.tDistance / sumDistance;
				t.tDanger = dangerEnergy; // * dangerDistance;
				double force = TARGET_FORCE * (getOthers() + 1) * t.tDanger;
				//System.out.format("[%03d] %3.5f %3.0f %s ", getTime(), t.tDanger, force, t.tName);
				if (t.tName.equals(eName))
				{
					//System.out.format("*****");
				}
				//System.out.format("\n");
			}
		}
		myDanger = getEnergy() / sumEnergy;
		//System.out.format("\n");
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		FunnelTarget enemy = allTargets.get(e.getName());
		if (enemy == null)
		{
			enemy = new FunnelTarget();
			enemy.tName = e.getName();
			enemy.eID = ++enemyID;
			allTargets.put(e.getName(), enemy);
		}
		double aBear = getHeadingRadians() + e.getBearingRadians();
		double xGunRel = enemy.tx = Math.sin(aBear) * e.getDistance();
		double yGunRel = enemy.ty = Math.cos(aBear) * e.getDistance();

		// debug
		influenceMap.registerNodeOwner(enemy.eID, enemy.eColor, e.getEnergy(), xGunRel + getX(), yGunRel + getY());

		enemy.isAlive = true;
		enemy.tDistance = e.getDistance();
		enemy.tEnergy = e.getEnergy();
		enemy.avgVeloCounter++;
		enemy.avgVelocity += Math.abs(e.getVelocity()); // this is the velocity sum not the average
		double vGun = enemy.avgVelocity * Math.signum(e.getVelocity()) / enemy.avgVeloCounter;

		rDist = Math.min(Math.min(DIST, e.getDistance()), rDist += 5);
		boolean isClose = false;

		double cRate = e.getDistance();

		if (eRate > cRate || eName == e.getName())
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
			eRate = cRate;
			bPower = Math.min(BMAX, TARGET_DISTANCE / e.getDistance());

			double minDanger = Double.MAX_VALUE;
			double curDanger = 0;
			double angleDanger = 0;
			double gunTurnCounter = 0;
			double moveAngle = 0;
			double gunHeading = e.getHeadingRadians();

			double[] risk = new double[64];
			int riskCounter = 0;

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
//							double dSquare = Point2D.distanceSq(enemyTarget.tx + getX(), enemyTarget.ty + getY(),
//									getX(), getY());
							double danger = TARGET_FORCE * (getOthers() + 1) * enemyTarget.tDanger / dSquare;

							// TODO: check for nearest target

							curDanger += danger;

							// TODO: this is wrong if I have moved 
							isClose |= enemyTarget.tDistance < rDist;
						}
					}

					// debug
					risk[riskCounter] = curDanger;

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

				// debug
				riskCounter++;
			}
			// debug
			myRisk.addRiskFunctionValues("myTarget", risk);

			setTurnGunRightRadians(Utils.normalRelativeAngle(Math.atan2(xGunRel, yGunRel) - getGunHeadingRadians()));
			if (Math.abs(getDistanceRemaining()) <= DIST_REMAIN || isClose)
			{
				myRisk.addRiskFunctionValues("my Current Move", risk);
				setTurnRightRadians(Math.tan(moveAngle -= getHeadingRadians()));
				setAhead(rDist * Math.cos(moveAngle));
				influenceMap.calcuateInfluence();
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
			FunnelTarget dead = allTargets.get(e.getName());
			dead.isAlive = false;
			influenceMap.onRobotDeath(dead.eID);
		}
		catch (Exception e0)
		{

		}

	}

	@Override
	public void onPaint(Graphics2D g)
	{
		myRisk.onPaint(g);
		influenceMap.onPaint(g);
	}

	@Override
	public void onDeath(DeathEvent event)
	{
		printStats();
		myWinLooseStats.onDeath(event);
	}

	@Override
	public void onWin(WinEvent event)
	{
		printStats();
		myWinLooseStats.onWin(event);
	}

	private void printStats()
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
	int			eID		= -1;
	Color		eColor	= Color.RED;

	protected void reset()
	{
		tx = ty = tHeading = avgVelocity = tDistance = 0;
		tDanger = 0.1;
		avgVeloCounter = 0;
		tEnergy = 100;
		isAlive = false;
	}
}