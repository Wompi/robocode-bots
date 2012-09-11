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
	private static final double	WZ_SIZE_W			= FIELD - 2 * WZ;
	private static final double	WZ_SIZE_H			= FIELD - 2 * WZ;
	private static final double	WZ_SIZE_M_W			= FIELD - 2 * WZ_M;
	private static final double	WZ_SIZE_M_H			= FIELD - 2 * WZ_M;
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

	static TassieTarget			myTarget;

	static boolean				isLeader;
	static double				myX;
	static double				myY;

	static HashSet<Bullet>		myBullets;

	// debug
	//	DebugPointLists debugPoints = new DebugPointLists();
	//	static double teamDmg;
	//	Color myColor;
	//	static double maxRate = Double.MIN_VALUE;

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

		battleState = 4;
		isLeader = (getEnergy() > 150);
		//		myColor = Color.GREEN;
		//		if (isLeader) myColor = Color.RED;
		myBullets = new HashSet<Bullet>();

		minion = null;
		leader = null;

		while (true)
		{
			if (getRadarTurnRemaining() == 0.0) setTurnRadarRightRadians(Double.MAX_VALUE);

			try
			{

				setMainTarget();
				if (isLeader)
				{
					TassieLeadScanInfo lInfo = new TassieLeadScanInfo();
					lInfo.leadScan = myTarget.name;
					broadcastMessage(lInfo);
				}

				// dist200 && 2vs2 || 1vs2 && myleader
				if ((myTarget.eDistance <= 200 && battleState == 4) /*|| (battleState == -1 && isLeader)*/)
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
						pHelp.x = getX() + Math.sin(angle = Math.atan2(myTarget.x - getX(), myTarget.y - getY()))
								* (aDist = (myTarget.eDistance + 50));
						pHelp.y = getY() + Math.cos(angle) * aDist;
					}

					//					pHelp.x = myTarget.x;
					//					pHelp.y = myTarget.y;
					broadcastMessage(pHelp);
				}

				double mRate = Double.MAX_VALUE;
				double v0 = 0;
				double v1 = 0;
				boolean isBulletEvade = false;
				boolean isCloseCombat = false;
				double protectDist = 0;

				while ((v0 += DELTA_RISK_ANGLE) <= PI_360)
				{
					double x = DIST * Math.sin(v0) + myX;
					double y = DIST * Math.cos(v0) + myY;

					if (new Rectangle2D.Double(WZ_M, WZ_M, WZ_SIZE_M_W, WZ_SIZE_M_H).contains(x, y))
					{
						double r1 = 0;
						double force = TARGET_FORCE;
						if (protectHelp != null)
						{
							//							PaintHelper.drawArc(new Point2D.Double(protectHelp.x, protectHelp.y), 45.0, 0.0, PI_360, false, getGraphics(),Color.YELLOW);
							isCloseCombat = true;
							protectDist = Point2D.distance(protectHelp.x, protectHelp.y, getX(), getY());
							force = 10000;
							r1 -= 100000 / Point2D.distanceSq(protectHelp.x, protectHelp.y, x, y);
						}
						//else /* if (target.isAlive) */
						{
							try
							{
								r1 += force / Point2D.distanceSq(leader.x, leader.y, x, y);
							}
							catch (Exception e1)
							{}

							try
							{
								r1 += force / Point2D.distanceSq(minion.x, minion.y, x, y);
							}
							catch (Exception e1)
							{}
						}

						//								double tEnergy = target.energyField[target.lastScan];
						//								else if ((!isLeader && tEnergy < (getEnergy()-30)) || (isLeader && tEnergy < (getEnergy()-120)) || (enemys == 1 && tEnergy < getEnergy()-40))
						//								{
						//									isCloseCombat = true;
						//									myTarget = target;
						//									r1 -= 30000/Point2D.distanceSq(target.x, target.y, x, y);							
						//									PaintHelper.drawArc(new Point2D.Double(target.x, target.y), 60.0, 0.0, PI_360, false, getGraphics(),Color.CYAN);
						//								}

						try
						{
							//PaintHelper.drawPoint(new Point2D.Double(teamInfo.x,teamInfo.y), Color.MAGENTA, getGraphics(), 20);
							r1 += force / Point2D.distanceSq(teamInfo.x, teamInfo.y, x, y);

							for (Bullet bullet : teamInfo.teamBullets)
							{
								if (bullet.isActive())
								{
									double dist = Point2D.distance(bullet.getX(), bullet.getY(), myX, myY);
									if (dist <= DIST)
									{
										//PaintHelper.drawArc(new Point2D.Double(bullet.getX(), bullet.getY()), 20, 0, PI_360, true, getGraphics(), Color.BLUE);
										r1 += 100000 / Point2D.distanceSq(bullet.getX(), bullet.getY(), x, y);
										isBulletEvade = true;
									}
									//else PaintHelper.drawArc(new Point2D.Double(bullet.getX(), bullet.getY()), 20, 0, PI_360, true, getGraphics(), Color.RED);
								}
							}
						}
						catch (Exception ex)
						{}

						if (isCloseCombat)
						{
							r1 += (Math.abs(Math.sin(Math.atan2(myTarget.x - x, myTarget.y - y) - v0)));
						}
						else
						{
							r1 += (Math.abs(Math.cos(Math.atan2(myTarget.x - x, myTarget.y - y) - v0)));
						}

						if (Math.random() < 0.6 && r1 < mRate)
						{
							mRate = r1;
							v1 = v0;
						}
						// debug
						//						PaintMinRiskPoints.registerRiskPoint(getTime(), x, y, r1);
					}
				}

				//System.out.format("[%d] mRate=%3.2f\n",getTime(),mRate);

				if (Math.abs(getDistanceRemaining()) <= DIST_REMAIN || isBulletEvade || isCloseCombat || mRate > 1.3)
				{
					setTurnRightRadians(Math.tan(v1 -= getHeadingRadians()));
					setAhead(DIST * Math.cos(v1));
				}
				//else if (getTurnRemainingRadians() == 0) setTurnRightRadians(Math.sin(getDistanceRemaining()/30)/20);

			}
			catch (Exception ex)
			{}
			execute();
		}
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
			//			enemy.myDisplacer.registerPostion(enemy.x,enemy.y, getTime());

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

			setMainTarget();
			doGun();

			//System.out.format("[%d] batteState=%d send leadScan=%s scan=%s\n", getTime(),battleState,leadScanTarget,name);
			// (2vs1 && heat)  || 1vs2 - 1vs1 || not leadscan     .... 
			try
			{
				if ((battleState == 1 && getGunHeat() < RADAR_GUNLOCK) || battleState <= 0 || !leadScanTarget.equals(name))
				{
					doRadar(v3);
				}
			}
			catch (Exception e1)
			{
				// exception trown only for my leader and if the leader hasn't send his scan msg			
				doRadar(Math.atan2(myTarget.x - myX, myTarget.y - myY));
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
		battleState = getOthers() - 3;
		if (isTeammate(name = event.getName()))
		{
			battleState += 2;
			return;
		}

		if (leader != null && name.equals(leader.name)) // null check for last dead if leader died first ... grrr
		{
			leader = null;
		}
		else
		{
			minion = null;
		}
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

	//	public void onPaint(Graphics2D g)
	//	{
	//		//PaintRobotPath.onPaint(g, getName(), getTime(), getX(), getY(), Color.GREEN);
	//		PaintMinRiskPoints.onPaint(g);		
	//		debugPoints.onPaint(g);	
	//		if (debugPoints.targetPoint != null) PaintHelper.drawLine(new Point2D.Double(getX(), getY()), debugPoints.targetPoint, g, PaintHelper.whiteTrans);
	//		//if (myTarget != null ) myTarget.myDisplacer.onPaint(g, Color.lightGray);
	//	}

	private void doGun()
	{
		long i;
		double bPower = Math.min(Rules.MAX_BULLET_POWER, TARGET_DISTANCE / myTarget.eDistance);
		double v2 = myTarget.getAvgVelocity();
		double xg = myTarget.x - myX;
		double yg = myTarget.y - myY;
		double gHead = myTarget.headingField[myTarget.lastScan];
		double lastHead = myTarget.headingField[Math.max(myTarget.lastScan - 1, 0)];
		if (gHead == 0)
		{
			gHead = lastHead;
			lastHead = myTarget.headingField[Math.max(myTarget.lastScan - 2, 0)];
		}
		double headDiff;
		if (Math.abs(headDiff = (gHead - lastHead)) > 0.161442955809475 || gHead == 0) headDiff = 0;

		i = 0;
		//		debugPoints.reset();
		//double distance = Point2D.distance(myTarget.x+getX(), myTarget.y+getY(), getX(), getY());
		//double diplacerDist = myTarget.myDisplacer.avgDist(bPower,distance,getTime());

		//System.out.format("[%d] avgVelo=%3.2f gHead=%3.5f headDiff=%3.5f dist=%3.2f %s \n",getTime(),v2,gHead,headDiff,diplacerDist,myTarget.name);

		while (++i * (20.0 - 3.0 * bPower) < Math.hypot(xg, yg))
		{
			xg += (Math.sin(gHead) * v2);
			yg += (Math.cos(gHead) * v2);
			//double relD = Point2D.distance(xg, yg, myTarget.x-getX(), myTarget.y-getY());
			//System.out.format("[%d] xg=%3.2f yg=%3.2f relD=%3.2f \n", getTime(),xg+getX(),yg+getY(),relD);
			//boolean check = (relD > diplacerDist); 
			if (!new Rectangle2D.Double(WZ, WZ, WZ_SIZE_W, WZ_SIZE_H).contains(xg + myX, yg + myY) /*|| check*/)
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
		}

		if (getGunTurnRemainingRadians() == 0 && getEnergy() > bPower)
		{
			Bullet bullet;
			if ((bullet = setFireBullet(bPower)) != null)
			{
				myBullets.add(bullet);
				//				Point2D bulPos = new Point2D.Double(bullet.getX(), bullet.getY());
				//				PaintHelper.drawLine(bulPos, RobotMath.calculatePolarPoint(bullet.getHeadingRadians(), 500, bulPos), getGraphics(), Color.CYAN);	
			}
		}

		//		debugPoints.targetPoint = new Point2D.Double(xg+getX(), yg+getY());

		guessedX = getX() + xg;
		guessedY = getY() + yg;

		setTurnGunRightRadians(Utils.normalRelativeAngle(Math.atan2(xg, yg) - getGunHeadingRadians()));
	}

	public void doRadar(double angle)
	{
		setTurnRadarRightRadians(Utils.normalRelativeAngle(angle - getRadarHeadingRadians()) * RADAR_WIDE);
	}

	// --------------------------------------------- code save and helper functions ----------------------------------------------------------------
	public static TassieTarget getTarget(String name, double energy)
	{
		// holy wombat this is nasty but it saves alot of code size ... buahh i can't look it any longer
		try
		{
			try
			{
				if (name.equals(leader.name)) { return leader; }
			}
			catch (Exception e1)
			{
				if (energy > 150)
				{
					leader = new TassieTarget();
					leader.name = name;
					return leader;
				}
			}
			if (name.equals(minion.name)) { return minion; }
		}
		catch (Exception e2)
		{
			minion = new TassieTarget();
			minion.name = name;
		}
		return minion;
	}

	public void setMainTarget()
	{
		double tRate;
		try
		{
			tRate = leader.energyField[leader.lastScan] + leader.eDistance * 0.8;
			try
			{
				//double buffy;
				myTarget = (tRate < (/*buffy=*/(minion.energyField[minion.lastScan] + minion.eDistance * 0.8))) ? leader : minion;
				//System.out.format("[%d] leadRate=%3.2f minionRate=%3.2f \n", getTime(),tRate,buffy);
			}
			catch (Exception e2)
			{
				myTarget = leader;
			}
		}
		catch (Exception e1)
		{
			myTarget = minion;
		}

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
	double[]					energyField			= new double[6000]; // could be changed to just one energy variable but who knows for what it is usefull later
																		//	DisplacementVector myDisplacer = new DisplacementVector();

	double						eDistance;
	String						name;

	// ooks ike i should get rid of this and use a simple avg
	public double getAvgVelocity()
	{
		double lastVelocity = velocityField[lastScan];

		if (lastVelocity == 0) return 0; // this should be adjusted to probability depend on avg dir changes

		double count = 0;
		double result = 0;
		for (int i = 0; i <= lastScan; i++)
		{
			double velocity;
			if (Math.signum(velocity = velocityField[i]) == Math.signum(lastVelocity))
			{
				result += velocity;
				count++;
			}
		}
		return result / count;
	}
}
