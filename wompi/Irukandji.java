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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.HitRobotEvent;
import robocode.RobotDeathEvent;
import robocode.RobotStatus;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class Irukandji extends AdvancedRobot
{
	public final static double				MAX_RADAR_RATE			= 0.07;									// less is more search
	public final static double				DEFAULT_RADAR_WIDTH		= 5.0;
	private final static double				TARGET_DISTANCE			= 600.0;
	private static final double				WZ						= 20.0;
	private static final double				WZ_SIZE_W				= 1000 - 2 * WZ;
	private static final double				WZ_SIZE_H				= 1000 - 2 * WZ;
	private static final double				WZ_G					= 17.0;
	private static final double				WZ_G_SIZE_W				= 1000 - 2 * WZ_G;
	private static final double				WZ_G_SIZE_H				= 1000 - 2 * WZ_G;

	private final static double				RADAR_GUNLOCK			= 1.0;
	private final static double				DIST					= 185;
	private final static double				DIST_REMAIN				= 20;
	private final static double				PI_360					= Math.PI * 2.0;
	private final static double				DELTA_RISK_ANGLE		= Math.PI / 32.0;
	private final static double				DEFAULT_RANDOM_RATE		= 0.5;
	private final static double				TARGET_FORCE			= 65000;									// 100000 low dmg high surv - 10000
																												// high dmg low surv

	public final static int					DEFAULT_PATTERN_LENGTH	= 30;
	private final int						DELTA_HEADING_INDEX		= 20;
	private final int						VELOCITY_INDEX			= 16;
	private final double					HEAD_FACTOR				= 2.0;
	private final double					VELO_FACTOR				= 2.0;

	static HashMap<String, IrukandjiTarget>	allTargets				= new HashMap<String, IrukandjiTarget>();

	static String							eName;
	static double							eRate;
	static double							eEnergy;

	public Irukandji()
	{

	}

	public void run()
	{
		setAllColors(Color.RED);
		setAdjustGunForRobotTurn(true);
		setTurnRadarRightRadians(eRate = eEnergy = Double.POSITIVE_INFINITY);
	}

	public void onScannedRobot(ScannedRobotEvent e)
	{
		IrukandjiTarget enemy;
		if ((enemy = allTargets.get(e.getName())) == null) allTargets.put(e.getName(), enemy = new IrukandjiTarget());

		double d0; // absBearing
		double d1; // distance -> bPower
		double xg, yg;

		xg = enemy.x = getX() + Math.sin((d0 = (getHeadingRadians() + e.getBearingRadians()))) * (d1 = e.getDistance());
		yg = enemy.y = getY() + Math.cos(d0) * d1;
		enemy.isAlive = true;

		// gun feeding
		int tick;
		if ((e.getTime() - enemy.eLastScan) == 1)
		{
			tick = encodeTick(Utils.normalRelativeAngle(e.getHeadingRadians() - enemy.eLastHeading), e.getVelocity());

			for (int i = 0; i <= enemy.eHistory.length(); ++i)
			{
				int pHash;
				IrukandjiMultiTick multiTick; // automatic cast to Integer
				if ((multiTick = enemy.matcherMap.get(pHash = enemy.eHistory.substring(0, i).hashCode())) == null)
				{
					enemy.matcherMap.put(pHash, multiTick = new IrukandjiMultiTick(tick));
				}
				multiTick.incrementCount(tick);
			}
			enemy.eHistory.insert(0, (char) (tick)).setLength(Math.min(DEFAULT_PATTERN_LENGTH, enemy.eHistory.length()));
		}
		else enemy.eHistory.setLength(0); // TODO: maybe return

		if (((getOthers() <= 2) ? (eEnergy > e.getEnergy()) : (eRate > (d1 - enemy.eScore * 3))) || eName == e.getName())
		{
			eName = e.getName();

			// gun
			eRate = d1 - enemy.eScore * 3;
			d1 = Math.min(Rules.MAX_BULLET_POWER, Math.min(((eEnergy = e.getEnergy()) / 3.0), TARGET_DISTANCE / (d1)));

			if (getGunHeat() < RADAR_GUNLOCK || getOthers() == 1) setTurnRadarRightRadians(Double.POSITIVE_INFINITY
					* Utils.normalRelativeAngle(d0 - getRadarHeadingRadians()));

			if (eEnergy < getEnergy() && getOthers() == 1) d1 = 0.1;

			if (getGunTurnRemaining() == 0) setFire(d1);

			double heading = e.getHeadingRadians();
			double velocity = 0;
			double pHeadChange = 0;
			StringBuilder ePattern = new StringBuilder(enemy.eHistory);

			if (ePattern.length() > 0)
			{
				boolean check = true;
				for (double bDist = 0; bDist < Point2D.distance(getX(), getY(), xg, yg); bDist += Rules.getBulletSpeed(d1))
				{
					int nextStep = 0;
					int patternLength = Math.min(DEFAULT_PATTERN_LENGTH, ePattern.length());
					IrukandjiMultiTick sTick = null;
					int len = 0;
					for (len = patternLength; sTick == null; --len)
					{
						sTick = enemy.matcherMap.get(ePattern.substring(0, len).hashCode());	// automatic cast to Integer
					}
					if (check)
					{
						enemy.avgPatternLength += len + 1;
						enemy.avgPatternCount++;
						setDebugProperty("AVG: " + e.getName(), String.format("%3.2f", enemy.avgPatternLength / enemy.avgPatternCount));
						check = false;
					}
					nextStep = sTick.getMaxKey();

					pHeadChange = Math.toRadians((double) ((nextStep >> 9) - DELTA_HEADING_INDEX) / HEAD_FACTOR);		// delta heading
					velocity = (double) (((nextStep >> 3) - ((nextStep >> 9) << 6)) - VELOCITY_INDEX) / VELO_FACTOR;								// velocity

					heading += pHeadChange;

					xg += velocity * Math.sin(heading);
					yg += velocity * Math.cos(heading);

					if (!new Rectangle2D.Double(WZ_G, WZ_G, WZ_G_SIZE_W, WZ_G_SIZE_H).contains(xg, yg))
					{
						nextStep = encodeTick(pHeadChange, velocity);
					}
					ePattern.insert(0, (char) nextStep);
				}
			}
			setTurnGunRightRadians(Utils.normalRelativeAngle(Math.atan2(xg - getX(), yg - getY()) - getGunHeadingRadians()));

			// move
			double rM = Double.MAX_VALUE;
			double v0 = 0, v1 = 0;
			double x, y, r1;
			while ((v0 += DELTA_RISK_ANGLE) <= PI_360)
			{
				if (new Rectangle2D.Double(WZ, WZ, WZ_SIZE_W, WZ_SIZE_H).contains(x = DIST * Math.sin(v0) + getX(), y = DIST * Math.cos(v0) + getY()))
				{
					if (((r1 = Math.abs(Math.cos(Math.atan2(enemy.x - x, enemy.y - y) - v0))) < DEFAULT_RANDOM_RATE && getOthers() <= 5))
					{
						r1 = DEFAULT_RANDOM_RATE * Math.random();
					}

					try
					{
						Iterator<IrukandjiTarget> iter = allTargets.values().iterator();
						while (true)
						{
							IrukandjiTarget target;
							if ((target = iter.next()).isAlive)
							{
								r1 += TARGET_FORCE / (target.distanceSq(x, y));
							}
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
			if (Math.abs(getDistanceRemaining()) <= DIST_REMAIN || rM > 9.0)
			{
				setTurnRightRadians(Math.tan(v1 -= getHeadingRadians()));
				setAhead(DIST * Math.cos(v1));
			}
		}
		enemy.eLastScan = e.getTime();
		enemy.eLastHeading = e.getHeadingRadians();
	}

	@Override
	public void onRobotDeath(RobotDeathEvent e)
	{
		eRate = eEnergy = Double.POSITIVE_INFINITY;
		IrukandjiTarget target;
		try
		{
			(target = allTargets.get(e.getName())).isAlive = false;
			target.eScore = 0;
		}
		catch (Exception e2)
		{}
	}

	public void onBulletHit(BulletHitEvent e)
	{
		allTargets.get(e.getName()).eScore += Rules.getBulletDamage(e.getBullet().getPower());
		// eEnergy -= dmg;
	}

	public void onHitRobot(HitRobotEvent e, RobotStatus status)
	{
		// eAbsBearing = status.getHeadingRadians() + e.getBearingRadians();
		//
		// eLastVelocity = eVelocity;
		// eVelocity = 0;
		//
		// eLastEnergy = eEnergy;
		// eEnergy = e.getEnergy();
		//
		// x = Math.sin(eAbsBearing) * eDistance + status.getX();
		// y = Math.cos(eAbsBearing) * eDistance + status.getY();
	}

	private int encodeTick(double deltaHeading, double velocity)
	{
		return ((((int) Math.rint(Math.toDegrees(deltaHeading * HEAD_FACTOR)) + DELTA_HEADING_INDEX) << 6) + ((int) (Math
				.rint(velocity * VELO_FACTOR)) + VELOCITY_INDEX)) << 3;
	}
}

class IrukandjiTarget extends Point2D.Double
{
	private static final long				serialVersionUID	= 8658460686386618844L;

	public boolean							isAlive;

	// radar
	double									eSlipDir;
	long									eLastScan;

	// gun
	StringBuilder							eHistory;
	public Map<Integer, IrukandjiMultiTick>	matcherMap;
	double									eLastHeading;

	double									avgPatternLength;
	double									avgPatternCount;

	double									eScore;

	public IrukandjiTarget()
	{
		eHistory = new StringBuilder(Irukandji.DEFAULT_PATTERN_LENGTH);
		matcherMap = new LinkedHashMap<Integer, IrukandjiMultiTick>(500000);
	}
}

class IrukandjiCountTick implements Comparable<IrukandjiCountTick>

{
	public int	myKey;
	public int	myCount;

	@Override
	public int compareTo(IrukandjiCountTick o)
	{
		return o.myCount - this.myCount; // descending order
	}
}

class IrukandjiMultiTick
{
	public ArrayList<IrukandjiCountTick>	vCount; // sorted list with max vCount at index 0

	public IrukandjiMultiTick(int key)
	{
		IrukandjiCountTick newTick = new IrukandjiCountTick();
		newTick.myKey = key;
		(vCount = new ArrayList<IrukandjiCountTick>()).add(newTick);
	}

	public int getMaxKey()
	{
		return vCount.get(0).myKey;
	}

	public void incrementCount(int key)
	{
		for (IrukandjiCountTick singleTick : vCount)
		{
			if (singleTick.myKey == key)
			{
				singleTick.myCount++;
				return;
			}
		}
		IrukandjiCountTick newTick = new IrukandjiCountTick();
		newTick.myCount++;
		newTick.myKey = key;
		vCount.add(newTick);
		Collections.sort(vCount);
	}
}
