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
import robocode.Bullet;
import robocode.HitRobotEvent;
import robocode.RobotDeathEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;
import wompi.paint.PaintHelper;

/**
 * What the ... is a Wallaby? (See: http://en.wikipedia.org/wiki/Wallaby)
 * 
 * To keep track of what i have done, i update a little development diary at: (if you are keen to read this ... be prepared for very bad English (i'm
 * German) https://github.com/Wompi/robocode-bots/wiki/Wallaby
 * 
 * The official version history can be found at: http://robowiki.net/wiki/Walaby
 * 
 * If you want to talk about it - you find me at: http://robowiki.net/wiki/User:Wompi
 * 
 * @author Wompi
 * @date 08/08/2012
 */
public class Wallaby extends AdvancedRobot
{
	private static final double			FIELD_W				= 1000.0;
	private static final double			FIELD_H				= 1000.0;

	private static final double			WZ_G				= 17.0;
	private static final double			WZ_G_W				= FIELD_W - 2 * WZ_G;
	private static final double			WZ_G_H				= FIELD_H - 2 * WZ_G;

	private final static double			DIST				= 185;
	private final static double			DIST_REMAIN			= 20;

	private final static double			GUNLOCK				= 1.0;
	private final static double			TARGET_FORCE		= 55000;							// 100000 low dmg high surv - 10000 high dmg low surv  
	private final static double			TARGET_DISTANCE		= 450.0;							// 400 last best - shoot at TARGET_DISTANCE with bullet 1.0

	private final static double			PI_360				= Math.PI * 2.0;
	private final static double			DELTA_RISK_ANGLE	= Math.PI / 32.0;
	private final static double			MAX_HEAD_DIFF		= Rules.getTurnRateRadians(1.0);	// 9.25 degree
	private final static double			ENERGY_ADJUST		= 4.0;
	private final static double			INF					= Double.POSITIVE_INFINITY;
	private final static double			BMAX				= Rules.MAX_BULLET_POWER;
	private final static double			BMIN				= Rules.MIN_BULLET_POWER;

	private final static double			GUN_TURN_OFFSET		= 1;

	// index:  0:x 1:y 2:heading 3:avgVelocity 4:avgVelocityCounter 5: distance 6: lastScan
	static HashMap<String, double[]>	allTargets;

	static String						eName;
	static double						eRate;

	static double						avgHead;
	static double						avgHeadCount;

	static double						bPower;
	static double						rDist;

	@Override
	public void run()
	{
		allTargets = new HashMap<String, double[]>();
		setAllColors(Color.RED); // 7 byte
		setAdjustGunForRobotTurn(true);
		//setAdjustRadarForGunTurn(true);
		setTurnRadarRightRadians(eRate = INF);
	}

	static double	avgV0;
	static double	avgV1;
	static double	avgCountV1;

	static double	lastV;

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		double[] enemy;
		double v0;
		double xg;
		double yg;
		double v1 = 0;
		double h0;
		double h1;
		double i;
		double r1;
		double rM;
		double v2;
		double x;
		double y;
		String name;
		if ((enemy = allTargets.get(name = e.getName())) == null)
		{
			allTargets.put(name, enemy = new double[7]);
		}
		xg = enemy[0] = Math.sin((rM = (getHeadingRadians() + e.getBearingRadians())))
				* (v0 = enemy[5] = e.getDistance());
		yg = enemy[1] = Math.cos(rM) * v0;

		double deltaScan = -enemy[6] + (enemy[6] = e.getTime());

		rDist = Math.min(DIST, rDist += 5);

		boolean isClose = false;
		if (eRate > v0 || eName == name)
		{
			if (getEnergy() > bPower && Math.abs(getGunTurnRemaining()) < GUN_TURN_OFFSET)
			{
				Bullet b = setFireBullet(bPower);
				if (b != null) System.out.format("fire\n");
			}
			eName = name;

			h0 = -enemy[2] + e.getHeadingRadians();
			if (deltaScan > 1) h0 = avgHead = 0;

			h1 = enemy[2] = e.getHeadingRadians();
			if (getGunHeat() < GUNLOCK || getOthers() == 1)
			{
				//else
				//System.out.format("[%04d] head 0 for scandiff=%3.0f %s\n", getTime(), deltaScan, eName);

				if (avgHead == 0) avgHead = h0;
				h0 = avgHead = rollingAvg(avgHead, h0, 0.9, 1.0);
//				System.out.format("[%04d] avg head avg=%3.5f (%3.5f) for %s\n", getTime(), Math.toDegrees(avgHead),
//						Math.toDegrees(h0), eName);

//				setTurnRadarRightRadians(-getRadarTurnRemainingRadians());
				if (!Double.isNaN(x = (Utils.normalRelativeAngle(rM - getRadarHeadingRadians()) * INF)))
				{
					setTurnRadarRightRadians(x);
				}
			}
			bPower = Math.min(BMAX, TARGET_DISTANCE / (eRate = v0));
			rM = Double.MAX_VALUE;
			v0 = i = 0;
			v2 = e.getVelocity();
			Rectangle2D bField;

			while ((v0 += DELTA_RISK_ANGLE) <= PI_360)
			{
				if ((bField = new Rectangle2D.Double(WZ_G, WZ_G, WZ_G_W, WZ_G_H)).contains((x = (rDist * Math.sin(v0)))
						+ getX(), (y = (rDist * Math.cos(v0))) + getY()))
				{
					r1 = Math.abs(Math.cos(Math.atan2(enemy[0] - x, enemy[1] - y) - v0));
					try
					{
						Iterator<double[]> iter = allTargets.values().iterator();
						double[] coordinate;
						while (true)
						{
							r1 += TARGET_FORCE / Point2D.distanceSq((coordinate = iter.next())[0], coordinate[1], x, y);
							isClose |= coordinate[5] < rDist;
						}
					}
					catch (Exception e1)
					{}

					if (Math.random() < 0.8 && r1 < rM)
					{
						rM = r1;
						v1 = v0;
					}
				}

				if (((i += 0.9) * Rules.getBulletSpeed(bPower) < Math.hypot(xg, yg)))
				{
					double d = Math.signum(e.getVelocity());
					//System.out.format("[%04d] v2=%3.5f d=%3.2f v=%3.5f\n", getTime(), v2, d, e.getVelocity());

					if (v2 * d < 0) d *= 2;
					if (((d = v2 + d) * v2) < 0) d /= 2.0;
					v2 = Math.max(-8.0, Math.min(d, 8.0));

					h1 += h0;
					if (!bField.contains((xg += (Math.sin(h1) * v2)) + getX(), (yg += (Math.cos(h1) * v2)) + getY()))
					{
						v2 = -v2;
					}
				}
			}

			setTurnGunRightRadians(Utils.normalRelativeAngle(Math.atan2(xg, yg) - getGunHeadingRadians()));
			if (Math.abs(getDistanceRemaining()) <= DIST_REMAIN || isClose)
			{
				//				if (isClose) setAllColors(Color.YELLOW); // 22 byte
				//				else setAllColors(Color.RED);
				setTurnRightRadians(Math.tan(v1 -= getHeadingRadians()));
				setAhead(rDist * Math.cos(v1));
			}
		}
	}

	public void printFieldCenter()
	{
		double avgX = 0;
		double avgY = 0;
		double avgCount = 0;

		try
		{
			Iterator<double[]> iter = allTargets.values().iterator();
			double[] coordinate;
			while (true)
			{
				coordinate = iter.next();
				avgX += coordinate[0];
				avgY += coordinate[1];
				avgCount++;
			}
		}
		catch (Exception e1)
		{}
		PaintHelper.drawPoint(new Point2D.Double((avgX / avgCount) + getX(), (avgY / avgCount) + getY()), Color.YELLOW,
				getGraphics(), 10);

	}

	@Override
	public void onPaint(Graphics2D g)
	{
		printFieldCenter();
	}

	public static double rollingAvg(double value, double newEntry, double n, double weighting)
	{
		return (value * n + newEntry * weighting) / (n + weighting);
	}

	@Override
	public void onHitRobot(HitRobotEvent e)
	{
		eName = e.getName();
		eRate = 0;
		rDist = 50.0;
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
		allTargets.remove(e.getName());
	}

	//	@Override
	//	public void onPaint(Graphics2D g)
	//	{
	//		PaintRobotPath.onPaint(g, getName(), getTime(), getX(), getY(), Color.GREEN);
	//
	//	}
}
