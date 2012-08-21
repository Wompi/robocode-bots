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
import java.util.Iterator;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.RobotDeathEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;
import wompi.echidna.misc.painter.PaintMinRiskPoints;
import wompi.echidna.misc.painter.PaintRobotPath;
import wompi.wallaby.PaintHelper;

/**
 * What the ... is a Wallaby? (See: http://en.wikipedia.org/wiki/Wallaby)
 *  
 * To keep track of what i have done, i update a little development diary at: 
 * (if you are keen to read this ... be prepared for very bad English (i'm German)
 * 		https://github.com/Wompi/robocode-bots/wiki/Wallaby
 * 
 * The official version history can be found at:
 *		http://robowiki.net/wiki/Walaby
 * 
 * If you want to talk about it - you find me at:
 * 		http://robowiki.net/wiki/User:Wompi
 * 
 * @author Wompi
 * @date 08/08/2012
 */
public class Wallaby extends AdvancedRobot
{
	private static final double			FIELD_W					= 1000.0;
	private static final double			FIELD_H					= 1000.0;

	private static final double			WZ						= 20.0;
	private static final double			WZ_W					= FIELD_W - 2 * WZ;
	private static final double			WZ_H					= FIELD_H - 2 * WZ;
	private static final double			WZ_G					= 17.0;
	private static final double			WZ_G_W					= FIELD_W - 2 * WZ_G;
	private static final double			WZ_G_H					= FIELD_H - 2 * WZ_G;

	private final static double			DIST					= 185;
	private final static double			DIST_REMAIN				= 20;

	private final static double			GUNLOCK					= 1.0;
	private final static double			TARGET_FORCE			= 45000;					// 100000 low dmg high surv - 10000 high dmg low surv  
	private final static double			TARGET_DISTANCE			= 400.0;					// 400 last best - shoot at TARGET_DISTANCE with bullet 1.0

	private final static double			PI_360					= Math.PI * 2.0;
	private final static double			DELTA_RISK_ANGLE		= Math.PI / 32.0;
	private final static double			MAX_HEAD_DIFF			= 0.174532925199433;		// 0.161442955809475;				// 9.25 degree
	private final static double			RANDOM_RATE				= 0.5;
	private final static int			MAX_RANDOM_OPPONENTS	= 5;
	private final static double			ENERGY_ADJUST			= 3.0;
	private final static double			INF						= Double.POSITIVE_INFINITY;

	// index:  0:x 1:y 2:heading  3:distance 4: score
	static HashMap<String, double[]>	allTargets;

	static String						eName;
	static double						eRate;
	static double						bPower;

	static double						avgHeading;
	static double						avgVelocity;
	static double						avgLockCount;

	// debug
	PaintMinRiskPoints					debugRiskPerp			= new PaintMinRiskPoints();
	PaintMinRiskPoints					debugRiskForce			= new PaintMinRiskPoints();
	PaintMinRiskPoints					debugRiskAll			= new PaintMinRiskPoints();
	PaintMinRiskPoints					debugRiskPerpRand		= new PaintMinRiskPoints();

	@Override
	public void run()
	{
		setAllColors(Color.RED);
		allTargets = new HashMap<String, double[]>();
		setAdjustGunForRobotTurn(true);
		setTurnRadarRightRadians(eRate = INF);
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		double[] enemy;
		if ((enemy = getTarget(e.getName())) == null)
		{
			allTargets.put(e.getName(), enemy = new double[5]);
		}
		double v0;
		double xg;
		double yg;
		double v1;
		double v2;
		double h0;
		double h1;
		double i;
		double r1;
		double rM;
		double x;
		double y;

		xg = enemy[0] = Math.sin((rM = (getHeadingRadians() + e.getBearingRadians()))) * (enemy[3] = v0 = e.getDistance());
		yg = enemy[1] = Math.cos(rM) * v0;

		if (eRate > (x = (v0 - enemy[4])) || eName == e.getName())
		{
			eRate = x;
			eName = e.getName();

			v2 = e.getVelocity();
			if (Math.abs(h0 = (-enemy[2] + (h1 = enemy[2] = e.getHeadingRadians()))) > MAX_HEAD_DIFF)
			{
				h0 = avgHeading = avgLockCount = avgVelocity = 0;
			}
			if (getGunHeat() < GUNLOCK || getOthers() == 1)
			{
				v2 = ((avgVelocity += Math.abs(v2)) / ++avgLockCount) * Math.signum(v2);
				h0 = (avgHeading += h0) / avgLockCount;
				setTurnRadarRightRadians(INF * Utils.normalRelativeAngle(rM - getRadarHeadingRadians()));
				setFire(bPower);
			}

			bPower = Math.min(Rules.MAX_BULLET_POWER, (rM = TARGET_DISTANCE) / v0); // save one byte and put rM = v0 to

			v0 = v1 = i = 0;

			Rectangle2D bField = new Rectangle2D.Double(WZ_G, WZ_G, WZ_G_W, WZ_G_H);
			boolean isClose = false;
			while ((v0 += DELTA_RISK_ANGLE) <= PI_360)
			{
				if ((++i * (20 - 3.0 * bPower) < Math.hypot(xg, yg))) // 0.85  17 2.55adjust
				{
					if (!bField.contains((xg += (Math.sin(h1) * v2)) + getX(), (yg += (Math.cos(h1) * v2)) + getY()))
					{
						v2 = -v2;
					}
					h1 += h0;
				}

				// debug
				double debugPerpRand = 0;
				double debugPerp = 0;
				double debugForce = 0;
				double debugAll = 0;

				if (bField.contains((x = (DIST * Math.sin(v0))) + getX(), (y = (DIST * Math.cos(v0))) + getY()))
				{

					if ((r1 = Math.abs(Math.cos(Math.atan2(enemy[0] - x, enemy[1] - y) - v0))) < RANDOM_RATE && getOthers() <= 5)
					{
						r1 = RANDOM_RATE * Math.random();
					}
					debugPerpRand = r1;

					try
					{
						Iterator<double[]> iter = allTargets.values().iterator();
						while (true)
						{
							double[] coordinate;
							r1 += TARGET_FORCE / Point2D.distanceSq((coordinate = iter.next())[0], coordinate[1], x, y);
							isClose |= (coordinate[3] < DIST);
						}
					}
					catch (Exception e1)
					{}
					debugForce = r1 - debugPerpRand;

					if (r1 < rM)
					{
						rM = r1;
						v1 = v0;
					}

					debugAll = r1;
					//debugRiskPerp.registerRiskPoint(getTime(), x + getX(), y + getY(), debugPerp, getX(), getY(), DIST - 10);
					debugRiskPerpRand.registerRiskPoint(getTime(), x + getX(), y + getY(), debugPerpRand, getX(), getY(), DIST);
					//				debugRiskForce.registerRiskPoint(getTime(), x + getX(), y + getY(), debugForce, getX(), getY(), DIST - 60);
					//				debugRiskAll.registerRiskPoint(getTime(), x + getX(), y + getY(), debugAll, getX(), getY(), DIST);
				}

			}
			setTurnGunRightRadians(Utils.normalRelativeAngle(Math.atan2(xg, yg) - getGunHeadingRadians()));
			double dist = enemy[3] * 8.0 / Rules.getBulletSpeed(bPower);
			if (Math.abs(getDistanceRemaining()) <= DIST_REMAIN || isClose)
			{
				if (isClose) setAllColors(Color.YELLOW);
				else setAllColors(Color.RED);

				//				double dist = (1 - getOthers() / 10.0) * DIST;

				setTurnRightRadians(Math.tan(v1 -= getHeadingRadians()));
				setAhead(dist * Math.cos(v1));
			}
			PaintHelper.drawArc(new Point2D.Double(getX(), getY()), dist, 0, PI_360, false, getGraphics(), Color.YELLOW);
		}
	}

	private double[] getTarget(String name)
	{
		return allTargets.get(name);
	}

	@Override
	public void onBulletHit(BulletHitEvent e)
	{
		try
		{
			getTarget(e.getName())[4] += 30;
		}
		catch (Exception e0)
		{}
	}

	@Override
	public void onHitByBullet(HitByBulletEvent e)
	{
		double delta = Utils.normalRelativeAngleDegrees(getHeading() - e.getHeading());
		System.out.format("[%d] head=%3.5f bulet=%3.5f delta=%3.5f\n", getTime(), Utils.normalRelativeAngleDegrees(getHeading()),
				Utils.normalRelativeAngleDegrees(e.getHeading()), delta);
	}

	@Override
	public void onHitRobot(HitRobotEvent e)
	{
		getTarget(e.getName())[3] = 1;
	}

	@Override
	public void onRobotDeath(RobotDeathEvent e)
	{
		eRate = INF;
		allTargets.remove(e.getName());
	}

	@Override
	public void onPaint(Graphics2D g)
	{
		PaintRobotPath.onPaint(g, getName(), getTime(), getX(), getY(), Color.GREEN);

		boolean values = true;
		debugRiskPerp.onPaint(g, values);
		debugRiskPerpRand.onPaint(g, values);
		//		debugRiskAll.onPaint(g, values);
		//		debugRiskForce.onPaint(g, values);

	}
}
