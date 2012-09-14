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
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.HashSet;

import robocode.Bullet;
import robocode.BulletHitEvent;
import robocode.DeathEvent;
import robocode.HitRobotEvent;
import robocode.MessageEvent;
import robocode.RobotDeathEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.TeamRobot;
import robocode.WinEvent;
import robocode.util.Utils;
import wompi.echidna.misc.DebugPointLists;
import wompi.echidna.misc.painter.PaintMinRiskPoints;
import wompi.echidna.misc.painter.PaintRobotPath;
import wompi.robomath.RobotMath;
import wompi.wallaby.PaintHelper;

/**
 * What the ... is a TassieDevil? (See: http://en.wikipedia.org/wiki/Tasmanian_devil)
 *  
 * To keep track of what i have done, i update a little development diary at: 
 * (if you are keen to read this ... be prepared for very bad English (i'm German)
 * 		https://github.com/Wompi/robocode-bots/wiki/TassieDevils
 * 
 * The official version history can be found at:
 *		http://robowiki.net/wiki/TassieDevils
 * 
 * If you want to talk about it - you find me at:
 * 		http://robowiki.net/wiki/User:Wompi
 * 
 * @author Wompi 
 * @date 11/09/2012
**/
public class TassieDevil extends TeamRobot
{

	private static final double	FIELD				= 800.0;
	private static final double	WZ					= 17.0;
	private static final double	WZ_M				= 20.0;
	private static final double	WZ_G_W				= FIELD - 2 * WZ;
	private static final double	WZ_G_H				= FIELD - 2 * WZ;
	private static final double	WZ_M_W				= FIELD - 2 * WZ_M;
	private static final double	WZ_M_H				= FIELD - 2 * WZ_M;

	private final static double	DIST_REMAIN			= 20;

	private final static double	RADAR_GUNLOCK		= 1.0;
	private final static double	RADAR_WIDE			= 3.0;
	private final static double	TARGET_FORCE		= 45000;
	private final static double	TARGET_DISTANCE		= 350.0;

	private final static double	PI_360				= Math.PI * 2.0;
	private final static double	DELTA_RISK_ANGLE	= Math.PI / 20.0;
	public static double		DIST				= 100;

	static TassieTarget			leader;
	static TassieTarget			minion;
	static double				bPower;
	static boolean				isSave;

	static boolean				isLeader;
	static double				myX;
	static double				myY;

	static HashSet<Bullet>		myBullets;
	static double				rDist;

	// debug
	DebugPointLists				debugPoints			= new DebugPointLists();
	PaintMinRiskPoints			allRisk				= new PaintMinRiskPoints();
	static double				teamDmg;
	Color						myColor;
	Point2D						moveEnd;

	static TassieProtectHelp	protectHelp;
	static TassieTeamInfo		teamInfo;
	static String				leadScanTarget;

	static double				guessedX;
	static double				guessedY;

	static int					battleState;									// 7 = 2vs2 6 = 2vs1 5 = 1vs2 4 = 1vs1 (me last)

	@Override
	public void run()
	{
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

		battleState = 7;
		isLeader = (getEnergy() > 150);

		//debug
		myColor = Color.ORANGE;
		if (isLeader) myColor = Color.YELLOW;
		setAllColors(myColor);

		myBullets = new HashSet<Bullet>();

		//if (leader == null && minion == null) // maybe make this static
		{
			leader = new TassieTarget();
			minion = new TassieTarget();
		}

		while (true)
		{
			if (getRadarTurnRemaining() == 0.0) setTurnRadarRightRadians(Double.MAX_VALUE);

			try
			{

				TassieTarget mainTarget = getMainTarget();
				if (isLeader)
				{
					TassieLeadScanInfo lInfo = new TassieLeadScanInfo();
					lInfo.leadScan = mainTarget.eName;
					broadcastMessage(lInfo);
				}

				// dist200 && 2vs2
				//if ((mainTarget.eDistance <= 200 && battleState == 7))
				{
					TassieProtectHelp pHelp = new TassieProtectHelp();

					//					if (isLeader)
					//					{
					//						pHelp.x = guessedX;
					//						pHelp.y = guessedY;
					//					}
					//					else
					//					{
					double angle;
					double aDist;
					pHelp.x = myX + Math.sin(angle = Math.atan2(mainTarget.x - myX, mainTarget.y - myY)) * (aDist = (mainTarget.eDistance + 100));
					pHelp.y = myY + Math.cos(angle) * aDist;
					//					}

					broadcastMessage(pHelp);
				}

				double mRate = Double.MAX_VALUE;
				double v0 = 0;
				double v1 = 0;
				boolean isCloseCombat = false;

				rDist = Math.min(DIST, rDist += 5);
				double buffyDist = Math.max(100, (mainTarget.eDistance - 50) * 8.0 / Rules.getBulletSpeed(bPower));

				boolean isClose = Math.min(leader.eDistance, minion.eDistance) < buffyDist;
				while ((v0 += DELTA_RISK_ANGLE) <= PI_360)
				{
					double x = buffyDist * Math.sin(v0) + myX;
					double y = buffyDist * Math.cos(v0) + myY;

					if (new Rectangle2D.Double(WZ_M, WZ_M, WZ_M_W, WZ_M_H).contains(x, y))
					{
						double r1 = 0;
						double force = TARGET_FORCE;
						if (protectHelp != null)
						{
							PaintHelper.drawArc(new Point2D.Double(protectHelp.x, protectHelp.y), 45.0, 0.0, PI_360, false, getGraphics(),
									Color.YELLOW);
							isCloseCombat = true;
							force = 10000;
							r1 -= 100000 / protectHelp.distanceSq(x, y);
						}

						if (!isCloseCombat)
						{
							r1 += (force * leader.eEnergy / 100.0) / leader.distanceSq(x, y);
							r1 += (force * minion.eEnergy / 100.0) / minion.distanceSq(x, y);
							//							r1 += force / leader.distanceSq(x, y);
							//							r1 += force / minion.distanceSq(x, y);
						}

						try
						{
							r1 += 30000 / teamInfo.distanceSq(x, y);
							isClose |= teamInfo.distance(myX, myY) < rDist;

							for (Bullet bullet : teamInfo.teamBullets)
							{
								// TODO: if the bullet has past me it shouldn't generate a force
								if (bullet.isActive())
								{
									double dist = Point2D.distance(bullet.getX(), bullet.getY(), myX, myY);
									if (dist <= DIST)
									{
										PaintHelper.drawArc(new Point2D.Double(bullet.getX(), bullet.getY()), 5, 0, PI_360, true, getGraphics(),
												Color.BLUE);
										r1 += 100000 / Point2D.distanceSq(bullet.getX(), bullet.getY(), x, y);
										isClose = true;
									}
									else PaintHelper.drawArc(new Point2D.Double(bullet.getX(), bullet.getY()), 5, 0, PI_360, true, getGraphics(),
											Color.RED);
								}
							}
						}
						catch (Exception ex)
						{}

						double adjust = 1.0;
						if (isClose) adjust = (Math.min(leader.eDistance, minion.eDistance) / DIST);

						double buffy = Math.atan2(mainTarget.x - x, mainTarget.y - y) - v0;
						r1 += Math.abs((isCloseCombat) ? Math.sin(buffy) : Math.cos(buffy) * adjust);

						//						if (!isLeader)
						//						{
						//							double tDist = 0;
						//							//if (battleState == 7 )
						//							{
						//								double tDist_pos = RobotMath.calculatePolarPoint(mainTarget.eHeading, 300, mainTarget).distance(x, y);
						//								double tDist_neg = RobotMath.calculatePolarPoint(mainTarget.eHeading - Math.PI, 300, mainTarget).distance(x, y);
						//								tDist = Math.min(tDist_pos, tDist_neg);
						//								r1 += tDist / 400;
						//							}
						//						}
						//
						if ((isClose || Math.random() < 0.6) && r1 < mRate)
						{
							mRate = r1;
							v1 = v0;
						}
						// debug
						allRisk.registerRiskPoint(getTime(), x, y, r1, myX, myY, buffyDist);
					}
				}

				if (Math.abs(getDistanceRemaining()) <= DIST_REMAIN || isClose || isCloseCombat)
				{
					setTurnRightRadians(Math.tan(v1 -= getHeadingRadians()));

					setAhead(buffyDist * Math.cos(v1));

					// debug
					Point2D start = new Point2D.Double(getX(), getY());
					moveEnd = RobotMath.calculatePolarPoint(getTurnRemainingRadians() + getHeadingRadians(), getDistanceRemaining(), start);
				}

			}
			catch (Exception ex)
			{}
			execute();
		}
	}

	@Override
	public void onHitRobot(HitRobotEvent event)
	{
		rDist = 50;
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		try
		{
			myX = getX();
			myY = getY();
			protectHelp = null;
			teamInfo = null;

			TassieTeamInfo tInfo = new TassieTeamInfo();
			tInfo.teamBullets = myBullets; // should be striped of the inactive bullets
			tInfo.x = myX;
			tInfo.y = myY;
			broadcastMessage(tInfo);
		}
		catch (Exception e2)
		{}
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		try
		{
			String name;
			if (isTeammate(name = e.getName())) return;

			TassieTarget enemy = getTarget(name, e.getEnergy());

			double v3;
			enemy.eDistance = e.getDistance();

			TassieEnemyInfo eInfo = new TassieEnemyInfo();
			eInfo.lastScan = enemy.lastScan = getTime();
			eInfo.x = enemy.x = myX + Math.sin((v3 = (getHeadingRadians() + e.getBearingRadians()))) * e.getDistance();
			eInfo.y = enemy.y = myY + Math.cos(v3) * e.getDistance();

			enemy.veloAvg[getIndex(enemy)] += Math.abs(eInfo.eVelocity = enemy.eVelocity = e.getVelocity());
			enemy.veloCount[getIndex(enemy)]++;
			enemy.eLastHeading = enemy.eHeading;
			eInfo.eHeading = enemy.eHeading = e.getHeadingRadians();
			eInfo.eEnergy = enemy.eEnergy = e.getEnergy();
			eInfo.eName = name;

			if (battleState == 7) broadcastMessage(eInfo);

			isSave = doGun(enemy = getMainTarget());
			if (!isSave)
			{
				if (enemy == leader && !Double.isInfinite(minion.eDistance)) isSave = doGun(minion);
				if (enemy == minion && !Double.isInfinite(leader.eDistance)) isSave = doGun(leader);
			}

			// (2vs1 && heat)  || 1vs2 - 1vs1 || not leadscan     .... 
			if (battleState <= 5 || (battleState == 6 && getGunHeat() < RADAR_GUNLOCK) || (battleState == 7 && isLeader))
			{
				doRadar(Math.atan2(enemy.x - getX(), enemy.y - getY()));
			}
			else if (leadScanTarget != null && !leadScanTarget.equals(name))
			{
				doRadar(v3);
			}
		}
		catch (Exception ex)
		{}
	}

	public static int getIndex(TassieTarget enemy)
	{
		return (int) ((enemy.eDistance / 400) - 1);
	}

	@Override
	public void onMessageReceived(MessageEvent event)
	{
		((ITassieMessage) event.getMessage()).proccedMessage();
	}

	@Override
	public void onRobotDeath(RobotDeathEvent event)
	{
		String name;
		if (isTeammate(name = event.getName()))
		{
			teamInfo = null;
			battleState--;
			return;
		}
		battleState -= 2;
		if (name.equals(leader.eName))
		{
			leader.setDead();
			return;
		}
		minion.setDead();
	}

	@Override
	public void onBulletHit(BulletHitEvent e)
	{
		if (isTeammate(e.getName()))
		{
			System.out.format("[%d] grr stop shooting at me dude!\n", getTime());
			teamDmg += Rules.getBulletDamage(e.getBullet().getPower());
		}
	}

	@Override
	public void onDeath(DeathEvent e)
	{
		System.out.format("TeamDmg: %3.2f LostCount: %d\n", teamDmg, 0);
	}

	@Override
	public void onWin(WinEvent e)
	{
		System.out.format("TeamDmg: %3.2f\n", teamDmg);
	}

	@Override
	public void onPaint(Graphics2D g)
	{
		PaintRobotPath.onPaint(g, getName(), getTime(), getX(), getY(), Color.GREEN);
		//PaintMinRiskPoints.onPaint(g);

		PaintHelper.drawString(g, String.format("%d", battleState), 100, 700, Color.PINK);

		// target geometry
		if (!Double.isInfinite(minion.eDistance) && !Double.isInfinite(leader.eDistance))
		{
			PaintHelper.drawLine(getPositionLine(minion), g, Color.BLUE);
			PaintHelper.drawLine(minion, leader, g, Color.LIGHT_GRAY);
			PaintHelper.drawLine(getPositionLine(leader), g, Color.BLUE);
		}

		debugPoints.onPaint(g);
		if (debugPoints.targetPoint != null) PaintHelper.drawLine(new Point2D.Double(getX(), getY()), debugPoints.targetPoint, g,
				PaintHelper.whiteTrans);
		PaintHelper.drawLine(new Point2D.Double(getX(), getY()), moveEnd, g, Color.YELLOW);

		double mDist = getPositionLine(minion).ptSegDist(getX(), getY());
		double lDist = getPositionLine(leader).ptSegDist(getX(), getY());
		PaintHelper.drawString(g, String.format("%3.2f / %3.2f", mDist, lDist), getX(), getY() + 40, Color.RED);

		allRisk.onPaint(g, false);
	}

	// debug
	private Line2D getPositionLine(TassieTarget target)
	{
		double angle = RobotMath.calculateAngle(leader, minion);
		if (target != minion) angle -= Math.PI;
		Point2D end = RobotMath.calculatePolarPoint(angle, 200, target);
		return new Line2D.Double(target, end);
	}

	private boolean doGun(final TassieTarget target)
	{
		double i;
		if (getGunTurnRemainingRadians() == 0 && getEnergy() > bPower && isSave)
		{
			Bullet bullet;
			if ((bullet = setFireBullet(bPower)) != null)
			{
				myBullets.add(bullet);
			}
		}

		double bDist = TARGET_DISTANCE / target.eDistance;
		//if (protectHelp != null) bDist *= 3;
		bPower = Math.min(Rules.MAX_BULLET_POWER, Math.min(target.eEnergy / 4.0, bDist));

		double avg = target.veloAvg[getIndex(target)];
		double count = target.veloCount[getIndex(target)];
		double v2 = target.eVelocity;
		if (count > 0)
		{
			v2 = (avg / count) * Math.signum(target.eVelocity);
		}

		double xg = target.x - myX;
		double yg = target.y - myY;
		double headdiff;
		double head;
		if (Math.abs(headdiff = ((head = target.eHeading) - target.eLastHeading)) > 0.161442955809475) headdiff = 0;

		if (getGunHeat() < 0.4)
		{
			headdiff = (target.headAvg += Math.abs(headdiff)) / ++target.headCount * Math.signum(headdiff);
		}
		else
		{
			target.headAvg = 0;
			target.headCount = 0;
		}

		i = 0;

		// debug
		debugPoints.reset();

		while ((i += 0.9) * Rules.getBulletSpeed(bPower) < Math.hypot(xg, yg))
		{
			xg += (Math.sin(head) * v2);
			yg += (Math.cos(head) * v2);
			if (!new Rectangle2D.Double(WZ, WZ, WZ_G_W, WZ_G_H).contains(xg + myX, yg + myY))
			{
				v2 = -v2;
				debugPoints.badPoints.add(new Point2D.Double(xg + myX, yg + myY));
			}
			else
			{
				debugPoints.goodPoints.add(new Point2D.Double(xg + myX, yg + myY));
			}
			head += headdiff;
		}
		guessedX = myX + xg;
		guessedY = myY + yg;

		try
		{
			Rectangle2D mate = new Rectangle2D.Double(teamInfo.x - 18, teamInfo.y - 18, 36, 36);
			Line2D targetLine = new Line2D.Double(getX(), getY(), guessedX, guessedY);

			if (targetLine.intersects(mate))
			{
				System.out.format("[%d] i would hit my mate! NO SHOOTING\n", getTime());
				return false;
			}
		}
		catch (Exception e) // teaminfo can be null;
		{}

		//debug
		debugPoints.targetPoint = new Point2D.Double(guessedX, guessedY);
		setTurnGunRightRadians(Utils.normalRelativeAngle(Math.atan2(xg, yg) - getGunHeadingRadians()));
		return true;
	}

	public void doRadar(double angle)
	{
		setTurnRadarRightRadians(Utils.normalRelativeAngle(angle - getRadarHeadingRadians()) * RADAR_WIDE);
	}

	// --------------------------------------------- code save and helper functions ----------------------------------------------------------------
	public static TassieTarget getTarget(String name, double energy)
	{
		if (energy > 150 || name.equals(leader.eName)) // if energy drop below 150 the leader should have its name
		{
			leader.eName = name;
			return leader;
		}
		minion.eName = name;
		return minion;
	}

	public TassieTarget getMainTarget()
	{
		//		double lRate = Math.max(1, leader.eEnergy) * leader.eDistance * 0.6;
		//		double mRate = Math.max(1, minion.eEnergy) * minion.eDistance * 0.8;
		double lRate = leader.eEnergy + leader.eDistance * 0.2;
		double mRate = minion.eEnergy + minion.eDistance * 0.8;
		return (lRate < mRate) ? leader : minion;
	}
}

interface ITassieMessage
{
	public void proccedMessage();
}

class TassieLeadScanInfo implements Serializable, ITassieMessage
{
	private static final long	serialVersionUID	= 1L;

	String						leadScan;

	@Override
	public void proccedMessage()
	{
		TassieDevil.leadScanTarget = leadScan;
	}
}

class TassieTeamInfo extends Point2D.Double implements ITassieMessage
{
	private static final long	serialVersionUID	= 2L;

	HashSet<Bullet>				teamBullets;

	@Override
	public void proccedMessage()
	{
		TassieDevil.teamInfo = this;
	}
}

class TassieProtectHelp extends Point2D.Double implements ITassieMessage
{
	private static final long	serialVersionUID	= 3L;

	@Override
	public void proccedMessage()
	{
		TassieDevil.protectHelp = this;
	}
}

class TassieEnemyInfo extends Point2D.Double implements ITassieMessage
{
	private static final long	serialVersionUID	= 4L;

	long						lastScan;
	double						eVelocity;
	double						eHeading;
	String						eName;
	double						eEnergy;

	@Override
	public void proccedMessage()
	{
		TassieTarget target = TassieDevil.getTarget(eName, eEnergy);
		target.eHeading = eHeading;
		target.eEnergy = eEnergy;
		target.eDistance = this.distance(TassieDevil.myX, TassieDevil.myY);

		target.veloAvg[TassieDevil.getIndex(target)] += Math.abs(eVelocity);
		target.veloCount[TassieDevil.getIndex(target)]++;

		// advance the target by one step
		target.x = x + (Math.sin(eHeading) * eVelocity); // maybe take the headingDiff 
		target.y = y + (Math.cos(eHeading) * eVelocity);
		target.lastScan = Math.max(target.lastScan, lastScan);
	}
}

class TassieTarget extends Point2D.Double
{
	private static final long	serialVersionUID	= 5L;
	long						lastScan;
	double[]					veloAvg				= new double[11];
	double[]					veloCount			= new double[11];
	double						eHeading;
	double						eLastHeading;
	double						eEnergy;
	double						eVelocity;

	double						headAvg;
	double						headCount;

	double						eDistance;
	String						eName;

	public void setDead()
	{
		x = y = eDistance = java.lang.Double.POSITIVE_INFINITY;
	}
}
