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
import robocode.Condition;
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

		if (leader == null && minion == null) // maybe make this static
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

			int scanTime;
			double v3;
			enemy.eDistance = e.getDistance();

			TassieEnemyInfo eInfo = new TassieEnemyInfo();
			eInfo.lastScan = enemy.lastScan = scanTime = (int) getTime();
			eInfo.x = enemy.x = myX + Math.sin((v3 = (getHeadingRadians() + e.getBearingRadians()))) * e.getDistance();
			eInfo.y = enemy.y = myY + Math.cos(v3) * e.getDistance();
			eInfo.eVelocity = enemy.velocityField[scanTime] = e.getVelocity();
			eInfo.eHeading = enemy.headingField[scanTime] = e.getHeadingRadians();
			eInfo.eEnergy = enemy.energyField[scanTime] = e.getEnergy();
			eInfo.eName = name;
			broadcastMessage(eInfo);

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
		{}
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
		double bPower = Math.min(Rules.MAX_BULLET_POWER, Math.min(target.energyField[target.lastScan] / 4.0, TARGET_DISTANCE / target.eDistance));
		//double v2 = target.getAvgVelocity((int) (target.eDistance / Rules.getBulletSpeed(bPower)), target.velocityField[target.lastScan]);
		double v2 = target.velocityField[target.lastScan];
		double xg = target.x - myX;
		double yg = target.y - myY;
		double gHead = target.headingField[target.lastScan];
		double lastHead = target.headingField[Math.max(target.lastScan - 1, 0)];
		if (gHead == 0)
		{
			gHead = lastHead;
			lastHead = target.headingField[Math.max(target.lastScan - 2, 0)];
		}
		double headDiff;
		if (Math.abs(headDiff = (gHead - lastHead)) > 0.161442955809475 || gHead == 0) headDiff = 0;

		i = 0;

		final double hDiff = headDiff;
		final double vAvg = v2;

		//		debugPoints.reset();
		//double distance = Point2D.distance(myTarget.x+getX(), myTarget.y+getY(), getX(), getY());
		//System.out.format("[%d] avgVelo=%3.2f gHead=%3.5f headDiff=%3.5f dist=%3.2f %s \n",getTime(),v2,gHead,headDiff,diplacerDist,myTarget.name);

		int ticks = 0;
		while ((i += 0.9) * Rules.getBulletSpeed(bPower) < Math.hypot(xg, yg))
		{
			xg += (Math.sin(gHead) * v2);
			yg += (Math.cos(gHead) * v2);
			//double relD = Point2D.distance(xg, yg, myTarget.x-getX(), myTarget.y-getY());
			//System.out.format("[%d] xg=%3.2f yg=%3.2f relD=%3.2f \n", getTime(),xg+getX(),yg+getY(),relD);
			//boolean check = (relD > diplacerDist); 
			if (!new Rectangle2D.Double(WZ, WZ, WZ_G_W, WZ_G_H).contains(xg + myX, yg + myY) /*|| check*/)
			{
				//System.out.format("[%d] not xg=%3.2f yg=%3.2f \n", getTime(),xg+getX(),yg+getY());
				v2 = -v2;
				//headDiff = -headDiff;
				//				debugPoints.badPoints.add(new Point2D.Double(xg+getX(), yg+getY()));
			}
			else
			{
				//				debugPoints.goodPoints.add(new Point2D.Double(xg+getX() ,yg+getY()));
			}
			gHead += headDiff;
			ticks++;
		}

		guessedX = myX + xg;
		guessedY = myY + yg;
		final long hit = getTime() + ticks;

		if (getGunTurnRemainingRadians() == 0 && getEnergy() > bPower)
		{
			Bullet bullet;
			if ((bullet = setFireBullet(bPower)) != null)
			{
				myBullets.add(bullet);
				//				Point2D bulPos = new Point2D.Double(bullet.getX(), bullet.getY());
				//				PaintHelper.drawLine(bulPos, RobotMath.calculatePolarPoint(bullet.getHeadingRadians(), 500, bulPos), getGraphics(), Color.CYAN);
				addCustomEvent(new Condition()
				{
					double		bx		= getX();
					double		by		= getY();
					double		ex		= guessedX;
					double		ey		= guessedY;
					long		time	= hit;

					int			vIndex	= (int) Math.round(vAvg) + 8;
					int			hIndex	= (int) Math.round(Math.toDegrees(hDiff)) + 10;
					double[]	adjust	= target.targetAdjust[vIndex][hIndex];

					@Override
					public boolean test()
					{
						if (getTime() >= time)
						{
							if (getTime() - target.lastScan == 1)
							{
								try
								{
									double gAngle = Math.atan2(ex - bx, ey - by);
									double nAngle = Math.atan2(target.x - bx, target.y - by);
									int delta = (int) Math.round(Math.toDegrees(Utils.normalRelativeAngle(nAngle - gAngle)));
									adjust[delta + 90]++;
									System.out.format("[%d] delta=%d count=%3.2f vIndex=%d hIndex=%d\n", getTime(), delta, adjust[delta + 90],
											vIndex, hIndex);
								}
								catch (Exception e)
								{
									e.printStackTrace();
								}
							}
							//System.out.format("[%d] remove\n", getTime());
							removeCustomEvent(this);
						}
						return false;
					}
				});

			}
		}

		//		debugPoints.targetPoint = new Point2D.Double(xg+getX(), yg+getY());
		double adjust = target.getAdjust(v2, hDiff);

		//System.out.format("[%d] adjust=%3.2f\n", getTime(), adjust);
		setTurnGunRightRadians(Utils.normalRelativeAngle(Math.atan2(xg, yg) - adjust - getGunHeadingRadians()));
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
		//		double lRate = Math.max(1, leader.energyField[leader.lastScan] - 100) * leader.eDistance * 0.6;
		//		double mRate = Math.max(1, minion.energyField[minion.lastScan]) * minion.eDistance * 0.8;
		double lRate = Math.max(1, leader.energyField[leader.lastScan]) + leader.eDistance * 0.8;
		double mRate = Math.max(1, minion.energyField[minion.lastScan]) + minion.eDistance * 0.8;

		return (lRate < mRate) ? leader : minion;
		//		PaintHelper.drawArc(new Point2D.Double(myTarget.x, myTarget.y), 50, 0, PI_360, false, getGraphics(), myColor);
		//System.out.format("[%d] myTarget=%s distance=%3.2f\n", getTime(),myTarget.name,myTarget.eDistance);	
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
		//		PaintHelper.drawArc(new Point2D.Double(x,y), 40, 0, PI_360, false, getGraphics(), myColor);
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

	int							lastScan;
	double						eVelocity;
	double						eHeading;
	String						eName;
	double						eEnergy;

	@Override
	public void proccedMessage()
	{
		TassieTarget target = TassieDevil.getTarget(eName, eEnergy);
		target.velocityField[lastScan] = eVelocity;
		target.headingField[lastScan] = eHeading;
		target.energyField[lastScan] = eEnergy;
		// advance the target by one step
		target.x = x + (Math.sin(eHeading) * eVelocity); // maybe take the headingDiff 
		target.y = y + (Math.cos(eHeading) * eVelocity);
		target.lastScan = Math.max(target.lastScan, lastScan);
		target.eDistance = Point2D.distance(x, y, TassieDevil.myX, TassieDevil.myY);
		//		target.myDisplacer.registerPostion(target.x, target.y, tInfo.lastScan);
		//System.out.format("[%d] updated target %s\n", getTime(),target.name);

	}
}

class TassieTarget extends Point2D.Double
{
	private static final long	serialVersionUID	= 5L;
	int							lastScan;
	double[]					velocityField		= new double[6000];
	double[]					headingField		= new double[6000];
	double[]					energyField			= new double[6000];				// could be changed to just one energy variable but who knows for what it is useful later

	private static int			VELO				= 16;
	private static int			HEAD				= 20;
	private static int			ADJUST				= 180;
	double[][][]				targetAdjust		= new double[VELO][HEAD][ADJUST];

	double						eDistance;
	String						eName;

	//boolean						isAlive;

	public void setDead()
	{
		x = y = eDistance = java.lang.Double.POSITIVE_INFINITY;
	}

	public double getAdjust(double velo, double hDiff)
	{
		double[] adjust = targetAdjust[(int) Math.round(velo) + 8][(int) Math.round(Math.toDegrees(hDiff)) + 10];
		double max = 0;
		double result = 0;
		for (int i = 0; i < ADJUST; i++)
		{
			if (adjust[i] > 0 && adjust[i] > max)
			{
				max = adjust[i];
				result = i;
			}
		}
		//System.out.format("max=%3.2f\n", max);
		if (max > 0) return Math.toRadians(result - 90);
		return 0;
	}

	// looks like i should get rid of this and use a simple avg
	public double getAvgVelocity(int ticks, double velo)
	{
		double lastVelocity = velocityField[lastScan];

		if (lastVelocity == 0) return 0; // this should be adjusted to probability depend on avg dir changes

		double count = 0;
		double result = 0;
		for (int i = 0; i <= lastScan; i++)
		{
			double velocity = velocityField[i];
			if (Utils.isNear(Math.round(velocity), Math.round(velo)))
			{
				for (int j = i; j <= Math.min(i + ticks, lastScan); j++)
				{
					result += velocityField[j];
					count++;
				}
			}
		}
		return result / count;
	}
}
