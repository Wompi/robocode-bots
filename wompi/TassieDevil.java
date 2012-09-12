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
import java.io.Serializable;
import java.util.HashSet;

import robocode.Bullet;
import robocode.HitRobotEvent;
import robocode.MessageEvent;
import robocode.RobotDeathEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.TeamRobot;
import robocode.util.Utils;

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
	private final static double	TARGET_FORCE		= 35000;
	private final static double	TARGET_DISTANCE		= 500.0;

	private final static double	PI_360				= Math.PI * 2.0;
	private final static double	DELTA_RISK_ANGLE	= Math.PI / 20.0;
	public static double		DIST				= 185;

	static TassieTarget			leader;
	static TassieTarget			minion;

	//static TassieTarget			myTarget;

	static boolean				isLeader;
	static double				myX;
	static double				myY;

	static HashSet<Bullet>		myBullets;
	static double				rDist;
	static double				cDist;

	// debug
	//	DebugPointLists debugPoints = new DebugPointLists();
	//	static double teamDmg;
	//	Color myColor;
	//	static double maxRate = Double.MIN_VALUE;
	//	Line2D						moveLine;
	//	Point2D						moveEnd;

	static TassieProtectHelp	protectHelp;
	static TassieTeamInfo		teamInfo;
	static String				leadScanTarget;

	static double				guessedX;
	static double				guessedY;

	static int					battleState;							// 4 = 2vs2  0 = 1vs1   1 = 2vs1   -1 = 1vs2  (me last)   

	@Override
	public void run()
	{
		setAllColors(Color.yellow);
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

		battleState = 7;
		isLeader = (getEnergy() > 150);
		//		myColor = Color.GREEN;
		//		if (isLeader) myColor = Color.RED;
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

				// dist200 && 2vs2 || 1vs2 && myleader
				if ((mainTarget.eDistance <= 200 && battleState == 7) /*|| (battleState == -1 && isLeader)*/)
				{
					//System.out.format("[%d] arg help me!\n",getTime());	
					TassieProtectHelp pHelp = new TassieProtectHelp();

					if (isLeader)
					{
						pHelp.x = guessedX;
						pHelp.y = guessedY;
					}
					else
					{
						double angle;
						double aDist;
						pHelp.x = myX + Math.sin(angle = Math.atan2(mainTarget.x - myX, mainTarget.y - myY)) * (aDist = (mainTarget.eDistance + 50));
						pHelp.y = myY + Math.cos(angle) * aDist;
					}

					broadcastMessage(pHelp);
				}

				double mRate = Double.MAX_VALUE;
				double v0 = 0;
				double v1 = 0;
				boolean isCloseCombat = false;

				double bDist = Math.min(leader.eDistance, minion.eDistance);

				if (cDist < DIST && bDist >= DIST - 10 && bDist <= DIST + 10)
				{
					cDist = DIST + 50;
				}

				rDist = Math.min(Math.max(DIST, cDist -= 5), rDist += 5);
				boolean isClose = bDist < DIST;
				while ((v0 += DELTA_RISK_ANGLE) <= PI_360)
				{
					double x = rDist * Math.sin(v0) + myX;
					double y = rDist * Math.cos(v0) + myY;

					if (new Rectangle2D.Double(WZ_M, WZ_M, WZ_M_W, WZ_M_H).contains(x, y))
					{
						double r1 = 0;
						double force = TARGET_FORCE;
						if (protectHelp != null)
						{
							//							PaintHelper.drawArc(new Point2D.Double(protectHelp.x, protectHelp.y), 45.0, 0.0, PI_360, false, getGraphics(),Color.YELLOW);
							isCloseCombat = true;
							force = 10000;
							r1 -= 100000 / protectHelp.distanceSq(x, y);
						}

						r1 += force / leader.distanceSq(x, y);
						r1 += force / minion.distanceSq(x, y);

						try
						{
							//PaintHelper.drawPoint(new Point2D.Double(teamInfo.x,teamInfo.y), Color.MAGENTA, getGraphics(), 20);
							r1 += force / teamInfo.distanceSq(x, y);

							for (Bullet bullet : teamInfo.teamBullets)
							{
								// TODO: if the bullet has past me it shouldn't generate a force
								if (bullet.isActive())
								{
									double dist = Point2D.distance(bullet.getX(), bullet.getY(), myX, myY);
									if (dist <= DIST)
									{
										//PaintHelper.drawArc(new Point2D.Double(bullet.getX(), bullet.getY()), 20, 0, PI_360, true, getGraphics(), Color.BLUE);
										r1 += 100000 / Point2D.distanceSq(bullet.getX(), bullet.getY(), x, y);
										isClose = true;
									}
									//else PaintHelper.drawArc(new Point2D.Double(bullet.getX(), bullet.getY()), 20, 0, PI_360, true, getGraphics(), Color.RED);
								}
							}
						}
						catch (Exception ex)
						{}

						double buffy = Math.atan2(mainTarget.x - x, mainTarget.y - y) - v0;
						r1 += Math.abs((isCloseCombat) ? Math.sin(buffy) : Math.cos(buffy));

						if (Math.random() < 0.6 && r1 < mRate)
						{
							mRate = r1;
							v1 = v0;
						}
						// debug
						//						PaintMinRiskPoints.registerRiskPoint(getTime(), x, y, r1);
					}
				}

				if (Math.abs(getDistanceRemaining()) <= DIST_REMAIN || isClose || isCloseCombat)
				{
					setTurnRightRadians(Math.tan(v1 -= getHeadingRadians()));
					setAhead(rDist * Math.cos(v1));

					// debug
					//					Point2D start = new Point2D.Double(getX(), getY());
					//					moveEnd = RobotMath.calculatePolarPoint(getTurnRemainingRadians() + getHeadingRadians(), getDistanceRemaining(), start);
					//					moveLine = new Line2D.Double(start, moveEnd);
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
			//System.out.format("[%d] msg send force[%3.2f] %s\n", getTime(),force,getName());
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
			if (isTeammate(name = e.getName())) return; // all team infos per msg

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

			doGun(enemy = getMainTarget());

			// (2vs1 && heat)  || 1vs2 - 1vs1 || not leadscan     .... 
			if (battleState <= 5 || (battleState == 6 && getGunHeat() < RADAR_GUNLOCK) || (battleState == 7 && isLeader))
			{
				doRadar(Math.atan2(enemy.x - getX(), enemy.y - getY()));
			}
			else if (leadScanTarget != null && !leadScanTarget.equals(name)) // the null because (2vs1 && gunheat) can be false .. grrr i hate null checks
			{
				doRadar(v3);
			}
		}
		catch (Exception ex)
		{

		}
	}

	public static int getIndex(TassieTarget enemy)
	{
		return (int) ((enemy.eDistance / 200) - 1);
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

	//	@Override
	//	public void onBulletHit(BulletHitEvent e)
	//	{
	//		if (isTeammate(e.getName()))
	//		{
	//			System.out.format("[%d] grr stop shooting at me dude!\n",getTime());
	//			teamDmg += Rules.getBulletDamage(e.getBullet().getPower());
	//		}
	//	}	
	//	
	//	@Override
	//	public void onDeath(DeathEvent e)
	//	{
	//		//lostCount++;
	//		System.out.format("TeamDmg: %3.2f LostCount: %d\n", teamDmg,0);
	//	}
	//	
	//	@Override 
	//	public void onWin(WinEvent e)
	//	{
	//		System.out.format("TeamDmg: %3.2f\n", teamDmg);
	//		//lostCount = 0;
	//	}
	//	

	//	@Override
	//	public void onPaint(Graphics2D g)
	//	{
	//		PaintRobotPath.onPaint(g, getName(), getTime(), getX(), getY(), Color.GREEN);
	//		//PaintMinRiskPoints.onPaint(g);
	//		//debugPoints.onPaint(g);
	//		//if (debugPoints.targetPoint != null) PaintHelper.drawLine(new Point2D.Double(getX(), getY()), debugPoints.targetPoint, g,
	//		//				PaintHelper.whiteTrans);
	//		//PaintHelper.drawLine(moveLine, g, Color.YELLOW);
	//		PaintHelper.drawLine(new Point2D.Double(getX(), getY()), moveEnd, g, Color.YELLOW);
	//	}

	private void doGun(final TassieTarget target)
	{
		double i;
		double bPower = Math.min(Rules.MAX_BULLET_POWER, Math.min(target.eEnergy / 4.0, TARGET_DISTANCE / target.eDistance));

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

		if (getGunHeat() < 0.6)
		{
			headdiff = (target.headAvg += Math.abs(headdiff)) / ++target.headCount * Math.signum(headdiff);
		}
		else
		{
			target.headAvg = 0;
			target.headCount = 0;
		}

		i = 0;
		while ((i += 0.9) * Rules.getBulletSpeed(bPower) < Math.hypot(xg, yg))
		{
			xg += (Math.sin(head) * v2);
			yg += (Math.cos(head) * v2);
			if (!new Rectangle2D.Double(WZ, WZ, WZ_G_W, WZ_G_H).contains(xg + myX, yg + myY) /*|| check*/)
			{
				v2 = -v2;
			}
			head += headdiff;
		}
		guessedX = myX + xg;
		guessedY = myY + yg;
		if (getGunTurnRemainingRadians() == 0 && getEnergy() > bPower)
		{
			Bullet bullet;
			if ((bullet = setFireBullet(bPower)) != null)
			{
				myBullets.add(bullet);
			}
		}
		setTurnGunRightRadians(Utils.normalRelativeAngle(Math.atan2(xg, yg) - getGunHeadingRadians()));
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
		//		double lRate = Math.max(1, leader.eEnergy - 50) * leader.eDistance * 0.7;
		//		double mRate = Math.max(1, minion.eEnergy) * minion.eDistance * 0.8;
		double lRate = Math.max(1, leader.eEnergy) + leader.eDistance * 0.8;
		double mRate = Math.max(1, minion.eEnergy) + minion.eDistance * 0.8;
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
