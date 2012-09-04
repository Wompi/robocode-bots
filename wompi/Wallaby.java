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

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

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
	private final static double			TARGET_FORCE			= 35000;							// 100000 low dmg high surv - 10000 high dmg low surv  
	private final static double			TARGET_DISTANCE			= 400.0;							// 400 last best - shoot at TARGET_DISTANCE with bullet 1.0

	private final static double			PI_360					= Math.PI * 2.0;
	private final static double			DELTA_RISK_ANGLE		= Math.PI / 32.0;
	private final static double			MAX_HEAD_DIFF			= 0.161442955809475;				// 9.25 degree
	private final static double			RANDOM_RATE				= 0.5;
	private final static int			MAX_RANDOM_OPPONENTS	= 5;
	private final static int			MAX_ENERGY_OPPONENTS	= 2;
	private final static double			ENERGY_ADJUST			= 3.0;
	private final static double			RATE_BORDER				= 3.0;								//2.8;
	private final static double			INF						= Double.POSITIVE_INFINITY;

	// index:  0:x 1:y 2:heading 3:avgVelocity 4:avgVelocityCounter
	static HashMap<String, double[]>	allTargets				= new HashMap<String, double[]>();

	static String						eName;
	static double						eRate;

	static double						avgHeading;
	static double						avgHeadCount;

	static double						bPower;

	//	static double						freeMove;

	@Override
	public void run()
	{
		//setAllColors(Color.RED);
		setAdjustGunForRobotTurn(true);
		setTurnRadarRightRadians(eRate = INF);
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		double[] enemy;
		double v0;
		double xg;
		double yg;
		double v1;
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
			allTargets.put(name, enemy = new double[6]);
		}
		xg = enemy[0] = Math.sin((rM = (getHeadingRadians() + e.getBearingRadians()))) * (v0 = enemy[5] = e.getDistance());
		yg = enemy[1] = Math.cos(rM) * v0;
		v2 = ((enemy[3] += (Math.abs(v1 = e.getVelocity()))) * Math.signum(v1)) / ++enemy[4];

		x = v0;
		if (getOthers() <= MAX_ENERGY_OPPONENTS)
		{
			x = e.getEnergy();
		}

		boolean isClose = false;
		if (eRate > x || eName == name)
		{
			eRate = x;

			if (getGunTurnRemaining() == 0 && getEnergy() > bPower)
			{
				setFire(bPower);
			}

			if (Math.abs(h0 = -enemy[2] + (h1 = enemy[2] = e.getHeadingRadians())) > MAX_HEAD_DIFF) // if bytes left use Utils.normalizeRelative(..)
			{
				h0 = avgHeading = avgHeadCount = 0;
			}
			if (getGunHeat() < GUNLOCK || getOthers() == 1)
			{
				h0 = (avgHeading += Math.abs(h0)) / ++avgHeadCount * Math.signum(h0);
				//System.out.format("[%d] lock %3.2f (%3.2f) %s\n", getTime(), Math.toDegrees(headDiff), Math.toDegrees(h0),e.getName());
				setTurnRadarRightRadians(INF * Utils.normalRelativeAngle(rM - getRadarHeadingRadians()));
			}
			eName = name;

			bPower = Math.min(Rules.MAX_BULLET_POWER, Math.min(e.getEnergy(), TARGET_DISTANCE / v0)); // save one byte and put rM = v0 to
			//if (eEnergy < getEnergy() && getOthers() == 1) bPower = 0.1;

			rM = Double.MAX_VALUE;
			v0 = i = 0;
			Rectangle2D bField;
			while ((v0 += DELTA_RISK_ANGLE) <= PI_360)
			{
				if ((bField = new Rectangle2D.Double(WZ_G, WZ_G, WZ_G_W, WZ_G_H)).contains((x = (DIST * Math.sin(v0))) + getX(),
						(y = (DIST * Math.cos(v0))) + getY()))
				{
					if ((r1 = Math.abs(Math.cos(Math.atan2(enemy[0] - x, enemy[1] - y) - v0))) < RANDOM_RATE && getOthers() <= MAX_RANDOM_OPPONENTS)
					{
						r1 = RANDOM_RATE * Math.random();
					}

					try
					{
						Iterator<double[]> iter = allTargets.values().iterator();
						while (true)
						{
							double[] coordinate;
							r1 += TARGET_FORCE / Point2D.distanceSq((coordinate = iter.next())[0], coordinate[1], x, y);
							isClose |= coordinate[5] < DIST;
						}
					}
					catch (Exception e1)
					{}

					if (r1 < rM)
					{
						rM = r1;
						v1 = v0;
					}
				}

				if (((i += 0.9) * Rules.getBulletSpeed(bPower) < Math.hypot(xg, yg)))
				{
					if (!bField.contains((xg += (Math.sin(h1) * v2)) + getX(), (yg += (Math.cos(h1) * v2)) + getY()))
					{
						v2 = -v2;
					}
					h1 += h0;
				}
			}
			setTurnGunRightRadians(Utils.normalRelativeAngle(Math.atan2(xg, yg) - getGunHeadingRadians()));
			if (Math.abs(getDistanceRemaining()) <= DIST_REMAIN || isClose)
			{
				setTurnRightRadians(Math.tan(v1 -= getHeadingRadians()));
				setAhead(DIST * Math.cos(v1));
			}
		}
	}

	@Override
	public void onRobotDeath(RobotDeathEvent e)
	{
		eRate = INF;
		allTargets.remove(e.getName());
	}
}
