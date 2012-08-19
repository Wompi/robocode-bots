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
	private static final double				FIELD_W					= 1000.0;
	private static final double				FIELD_H					= 1000.0;

	private static final double				WZ						= 20.0;
	private static final double				WZ_W					= FIELD_W - 2 * WZ;
	private static final double				WZ_H					= FIELD_H - 2 * WZ;
	private static final double				WZ_G					= 17.0;
	private static final double				WZ_G_W					= FIELD_W - 2 * WZ_G;
	private static final double				WZ_G_H					= FIELD_H - 2 * WZ_G;

	private final static double				DIST					= 185;
	private final static double				DIST_REMAIN				= 20;

	private final static double				GUNLOCK					= 1.0;
	private final static double				TARGET_FORCE			= 65000;								// 100000 low dmg high surv - 10000 high dmg low surv  
	private final static double				TARGET_DISTANCE			= 400.0;								// 400 last best - shoot at TARGET_DISTANCE with bullet 1.0

	private final static double				PI_360					= Math.PI * 2.0;
	private final static double				DELTA_RISK_ANGLE		= Math.PI / 32.0;
	private final static double				MAX_HEAD_DIFF			= 0.161442955809475;					// 9.25 degree
	private final static double				RANDOM_RATE				= 0.5;
	private final static int				MAX_RANDOM_OPPONENTS	= 5;
	private final static int				MAX_ENERGY_OPPONENTS	= 2;
	private final static double				ENERGY_ADJUST			= 3.0;
	private final static double				RATE_BORDER				= 3.0;									//2.8;
	private final static double				INF						= Double.POSITIVE_INFINITY;

	static HashMap<String, WallabyTarget>	allTargets				= new HashMap<String, WallabyTarget>();

	static String							eName;
	static double							eDistance;
	static double							eEnergy;
	static double							bPower;

	@Override
	public void run()
	{
		//setAllColors(Color.RED);
		setAdjustGunForRobotTurn(true);
		setTurnRadarRightRadians(eDistance = eEnergy = INF);
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		WallabyTarget enemy;
		if ((enemy = allTargets.get(e.getName())) == null)
		{
			allTargets.put(e.getName(), enemy = new WallabyTarget());
		}
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

		xg = enemy.x = Math.sin((rM = (getHeadingRadians() + e.getBearingRadians()))) * (v0 = e.getDistance());
		yg = enemy.y = Math.cos(rM) * v0;
		v2 = ((enemy.vAvg += (Math.abs(v1 = e.getVelocity()))) * Math.signum(v1)) / ++enemy.avgCount;

		if (Math.abs(h0 = (-enemy.eHeading + (h1 = enemy.eHeading = e.getHeadingRadians()))) > MAX_HEAD_DIFF) h0 = 0;

		if (((getOthers() <= MAX_ENERGY_OPPONENTS) ? (eEnergy > e.getEnergy()) : (eDistance > v0)) || eName == e.getName())
		{
			eName = e.getName();

			if (getGunTurnRemaining() == 0) setFire(bPower);
			if (getGunHeat() < GUNLOCK || getOthers() == 1) setTurnRadarRightRadians(INF * Utils.normalRelativeAngle(rM - getRadarHeadingRadians()));

			bPower = Math.min(Rules.MAX_BULLET_POWER, Math.min((eEnergy = e.getEnergy()) / ENERGY_ADJUST, TARGET_DISTANCE / (eDistance = v0))); // save one byte and put rM = v0 to
			if (eEnergy < getEnergy() && getOthers() == 1) bPower = 0.1;

			rM = Double.MAX_VALUE;
			v0 = i = 0;
			while ((v0 += DELTA_RISK_ANGLE) <= PI_360)
			{
				if ((++i * (18.0 - 2.7 * bPower) < Math.hypot(xg, yg)))
				{
					if (!new Rectangle2D.Double(WZ_G, WZ_G, WZ_G_W, WZ_G_H).contains((xg += (Math.sin(h1) * v2)) + getX(),
							(yg += (Math.cos(h1) * v2)) + getY()))
					{
						v2 = -v2;
					}
					h1 += h0;
				}

				if (new Rectangle2D.Double(WZ, WZ, WZ_W, WZ_H).contains((x = (DIST * Math.sin(v0))) + getX(), (y = (DIST * Math.cos(v0))) + getY()))
				{
					if ((r1 = Math.abs(Math.cos(Math.atan2(enemy.x - x, enemy.y - y) - v0))) < RANDOM_RATE && getOthers() <= MAX_RANDOM_OPPONENTS)
					{
						r1 = RANDOM_RATE * Math.random();
					}

					try
					{
						Iterator<WallabyTarget> iter = allTargets.values().iterator();
						while (true)
						{
							r1 += TARGET_FORCE / ((iter.next()).distanceSq(x, y));
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
			}
			setTurnGunRightRadians(Utils.normalRelativeAngle(Math.atan2(xg, yg) - getGunHeadingRadians()));
			if (Math.abs(getDistanceRemaining()) <= DIST_REMAIN || rM > RATE_BORDER)
			{
				setTurnRightRadians(Math.tan(v1 -= getHeadingRadians()));
				setAhead(DIST * Math.cos(v1));
			}
		}
	}

	@Override
	public void onRobotDeath(RobotDeathEvent e)
	{
		eDistance = eEnergy = INF;
		allTargets.remove(e.getName());
	}
}

class WallabyTarget extends Point2D.Double
{
	private static final long	serialVersionUID	= -5406737205536713408L;

	double						eHeading;
	double						vAvg;
	long						avgCount;
	//	double						eVelocity;
	//long						eScan;
}
