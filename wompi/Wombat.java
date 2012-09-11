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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import robocode.AdvancedRobot;
import robocode.Bullet;
import robocode.BulletHitEvent;
import robocode.Condition;
import robocode.HitRobotEvent;
import robocode.RobotDeathEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;
import wompi.echidna.misc.painter.PaintRobotPath;
import wompi.robomath.RobotMath;
import wompi.wallaby.PaintHelper;

/**
 * What the ... is a Wombat? (See: http://en.wikipedia.org/wiki/Wombat)
 *  
 * To keep track of what i have done, i update a little development diary at: 
 * (if you are keen to read this ... be prepared for very bad English (i'm German)
 * 		https://github.com/Wompi/robocode-bots/wiki/Wombat
 * 
 * The official version history can be found at:
 *		http://robowiki.net/wiki/Wombat
 * 
 * If you want to talk about it - you find me at:
 * 		http://robowiki.net/wiki/User:Wompi
 * 
 * @author Wompi
 * @date 08/08/2012
 */
public class Wombat extends AdvancedRobot
{
	private static final double				FIELD_W				= 1000.0;
	private static final double				FIELD_H				= 1000.0;

	private static final double				WZ_G				= 17.0;
	private static final double				WZ_G_W				= FIELD_W - 2 * WZ_G;
	private static final double				WZ_G_H				= FIELD_H - 2 * WZ_G;

	private final static double				DIST				= 185;
	private final static double				DIST_REMAIN			= 20;

	private final static double				GUNLOCK				= 1.0;
	private final static double				TARGET_FORCE		= 55000;								// 100000 low dmg high surv - 10000 high dmg low surv  
	private final static double				TARGET_DISTANCE		= 450.0;								// 400 last best - shoot at TARGET_DISTANCE with bullet 1.0

	private final static double				PI_360				= Math.PI * 2.0;
	private final static double				DELTA_RISK_ANGLE	= Math.PI / 32.0;
	private final static double				MAX_HEAD_DIFF		= 0.161442955809475;					// 9.25 degree
	private final static double				ENERGY_ADJUST		= 4.0;
	private final static double				INF					= Double.POSITIVE_INFINITY;
	private final static double				BMAX				= Rules.MAX_BULLET_POWER;
	private final static double				BMIN				= Rules.MIN_BULLET_POWER;

	// index:  0:x 1:y 2:heading 3:avgVelocity 4:avgVelocityCounter 5: distance 6:xGuess 7:yGuess 8: ticks
	static HashMap<String, WombatTarget>	allTargets			= new HashMap<String, WombatTarget>();

	static String							eName;
	static double							eRate;

	static double							avgHeading;
	static double							avgHeadCount;

	static double							bPower;
	static double							rDist;

	@Override
	public void run()
	{
		setAllColors(Color.RED); // 7 byte
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setTurnRadarRightRadians(eRate = INF);
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		WombatTarget enemy;
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
			allTargets.put(name, enemy = new WombatTarget());
		}
		xg = enemy.x = Math.sin((rM = (getHeadingRadians() + e.getBearingRadians()))) * (v0 = enemy.eDistance = e.getDistance());
		yg = enemy.y = Math.cos(rM) * v0;
		enemy.isAlive = true;
		//v2 = ((enemy[3] += (Math.abs(v1 = e.getVelocity()))) * Math.signum(v1)) / ++enemy[4];
		v2 = v1 = e.getVelocity();

		rDist = Math.min(DIST, rDist += 5);
		boolean isClose = false;

		if (eRate > v0 || eName == name)
		{
			if (getEnergy() > bPower && getGunTurnRemaining() == 0 && (getTime() - enemy.eLastScan) == 1)
			{
				final Bullet shot = setFireBullet(bPower);
				if (shot != null)
				{
					final WombatTarget cEnemy = enemy;
					addCustomEvent(new Condition()
					{
						Bullet	myBullet	= shot;
						double	bx			= getX();
						double	by			= getY();
						double	ex			= cEnemy.eGuessX;
						double	ey			= cEnemy.eGuessY;
						double	time		= cEnemy.eGuessTime;

						boolean	locked;

						double	gAngle;
						double	nAngle;
						double	dist;
						double	gDist		= cEnemy.eGuessDistance;
						double	delta;
						Point2D	end;

						double	lastX		= bx;
						double	lastY		= by;

						@Override
						public boolean test()
						{

							if (!locked)
							{
								gAngle = Math.atan2(ex - bx, ey - by);
								nAngle = Math.atan2(cEnemy.x + lastX - bx, cEnemy.y + lastY - by);
								delta = Utils.normalRelativeAngle(nAngle - gAngle);

								dist = Point2D.distance(bx, by, cEnemy.x + lastX, cEnemy.y + lastY);
								end = new Point2D.Double(cEnemy.x + lastX, cEnemy.y + lastY);
								lastX = getX();
								lastY = getY();

							}

							if (getTime() >= time || !myBullet.isActive() || !cEnemy.isAlive)
							{
								if (!locked && cEnemy.isAlive && (getTime() - cEnemy.eLastScan) == 1)
								{
									System.out.format("[%d] delta=%3.2f dist=%3.2f gDist=%3.2f (%3.2f) [%3.2f:%3.2f]\n", getTime(),
											Math.toDegrees(delta), dist, gDist, dist - gDist, end.getX(), end.getY());
									DeltaBearing dBear = new DeltaBearing();
									dBear.myDist = dist;
									dBear.myAngle = delta;
									dBear.myGuessDist = gDist;
									cEnemy.myMissses.add(dBear);
								}
								locked = true;

								if (getTime() > time + 10 || !cEnemy.isAlive)
								{
									removeCustomEvent(this);
								}
							}
							PaintHelper.drawLine(new Point2D.Double(bx, by), end, getGraphics(), Color.YELLOW);
							PaintHelper.drawLine(new Point2D.Double(bx, by), new Point2D.Double(ex + bx, ey + by), getGraphics(), Color.BLUE);
							return false;
						}
					});
				}
			}
			eName = name;

			if (Math.abs(h0 = -enemy.eHeading + (h1 = enemy.eHeading = e.getHeadingRadians())) > MAX_HEAD_DIFF)
			{
				h0 = avgHeading = avgHeadCount = 0;
			}
			if (getGunHeat() < GUNLOCK || getOthers() == 1)
			{
				h0 = (avgHeading += Math.abs(h0)) / ++avgHeadCount * Math.signum(h0);
				setTurnRadarLeft(getRadarTurnRemaining());
				//if (!Utils.isNear(0.0, x = INF * Utils.normalRelativeAngle(rM - getRadarHeadingRadians()))) setTurnRadarRightRadians(x);
			}

			bPower = Math.min(BMAX, TARGET_DISTANCE / (eRate = v0));
			rM = Double.MAX_VALUE;
			v0 = i = 0;
			Rectangle2D bField;

			int countTicks = 0;
			while ((v0 += DELTA_RISK_ANGLE) <= PI_360)
			{
				if ((bField = new Rectangle2D.Double(WZ_G, WZ_G, WZ_G_W, WZ_G_H)).contains((x = (rDist * Math.sin(v0))) + getX(),
						(y = (rDist * Math.cos(v0))) + getY()))
				{
					r1 = Math.abs(Math.cos(Math.atan2(enemy.x - x, enemy.y - y) - v0));
					try
					{
						Iterator<WombatTarget> iter = allTargets.values().iterator();
						while (true)
						{
							WombatTarget target = iter.next();
							if (target.isAlive)
							{
								r1 += TARGET_FORCE / target.distanceSq(x, y);
								isClose |= target.eDistance < rDist;
							}
						}
					}
					catch (Exception e1)
					{}

					if (Math.random() < 0.6 && r1 < rM)
					{
						rM = r1;
						v1 = v0;
					}
				}

				if (((i += 0.9) * Rules.getBulletSpeed(bPower) < Math.hypot(xg, yg)))
				{
					h1 += h0;
					if (!bField.contains((xg += (Math.sin(h1) * v2)) + getX(), (yg += (Math.cos(h1) * v2)) + getY()))
					{
						v2 = -v2;
					}
					countTicks++;
				}
			}

			enemy.eGuessX = xg + getX();
			enemy.eGuessY = yg + getY();
			enemy.eGuessTime = getTime() + countTicks;
			enemy.eGuessDistance = Point2D.distance(getX(), getY(), enemy.eGuessX, enemy.eGuessY);

			setTurnGunRightRadians(Utils.normalRelativeAngle(Math.atan2(xg, yg) - getGunHeadingRadians()));
			if (Math.abs(getDistanceRemaining()) <= DIST_REMAIN || isClose)
			{
				if (isClose) setAllColors(Color.YELLOW); // 22 byte
				else setAllColors(Color.RED);
				setTurnRightRadians(Math.tan(v1 -= getHeadingRadians()));
				setAhead(rDist * Math.cos(v1));
			}
		}
		enemy.eLastScan = getTime();
	}

	@Override
	public void onHitRobot(HitRobotEvent e)
	{
		eName = e.getName();
		eRate = 0;
		rDist = 50;
	}

	@Override
	public void onBulletHit(BulletHitEvent e)
	{
		double bx = e.getBullet().getX();
		double by = e.getBullet().getY();
		try
		{
			WombatTarget hit = allTargets.get(e.getName());
			hit.eDmg += Rules.getBulletDamage(e.getBullet().getPower());
			hit.x = bx;
			hit.y = by;
			System.out.format("[%d] hit [%3.2f:%3.2f] (%d) %s\n", getTime(), bx, by, getTime() - hit.eLastScan, e.getName());
		}
		catch (Exception e0)
		{}
	}

	@Override
	public void onRobotDeath(RobotDeathEvent e)
	{
		eRate = INF;
		WombatTarget dead = allTargets.get(e.getName());
		dead.isAlive = false;
	}

	@Override
	public void onPaint(Graphics2D g)
	{
		PaintRobotPath.onPaint(g, getName(), getTime(), getX(), getY(), Color.GREEN);

		double depp = getGunTurnRemainingRadians() + getGunHeadingRadians();
		Point2D start = new Point2D.Double(getX(), getY());

		WombatTarget target = allTargets.get(eName);
		if (target != null)
		{
			for (DeltaBearing bear : target.myMissses)
			{
				if (target.eGuessDistance <= (bear.myGuessDist + 50) && target.eGuessDistance >= (bear.myGuessDist - 50))
				{
					Point2D end = RobotMath.calculatePolarPoint(depp + bear.myAngle, bear.myDist, start);
					PaintHelper.drawLine(start, end, g, Color.lightGray);
				}
			}
		}
		Point2D gun = RobotMath.calculatePolarPoint(getGunHeadingRadians(), 400, start);
		PaintHelper.drawLine(start, gun, g, Color.RED);
	}
}

class WombatTarget extends Point2D.Double
{
	private static final long	serialVersionUID	= 1L;

	double						eHeading;
	double						eVeloCount;
	double						eVeloAvg;
	double						eDistance;
	double						eDmg;

	long						eLastScan;
	boolean						isAlive;
	double						eGuessX;
	double						eGuessY;
	double						eGuessTime;
	double						eGuessDistance;

	ArrayList<DeltaBearing>		myMissses			= new ArrayList<DeltaBearing>();

}

class DeltaBearing
{
	double	myDist;
	double	myAngle;
	double	myGuessDist;
}
