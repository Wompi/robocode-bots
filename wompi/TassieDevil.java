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

import robocode.Bullet;
import robocode.Condition;
import robocode.MessageEvent;
import robocode.RobotDeathEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.TeamRobot;
import robocode.util.Utils;

/**
 * Made by Wompi
 * Code is open source, released under the RoboWiki Public Code License:
 * http://robowiki.net/cgi-bin/robowiki?RWPCL
 * What the ... is a TassieDevil? (See: http://en.wikipedia.org/wiki/Tasmanian_devil)
 * Size: 1300+ v1.3
 * Well not much to say. I just include the source, so you can see my horrible programming draft. I really would spend more time with this, but
 * unfortunately
 * do i have not much time these days. The tassie devils have a little more team spirit and the leader will be guarded at certain battle field states.
 * I really would like to implement more TeamMessages for the enemy state and change the radar and and and ... but well no time. This one is a very
 * rough
 * draft so be gentle with me :).
 * I know there are a lot of code fails and way to naive functions but for a quick look it should be enough. I don't have the right feeling right now
 * for
 * the twin rumble but i'm sure this will come after more serious testing and implementing.
 * Credit: not much to say, its all based on Wallaby glued with naive team functions
 * Size: 1952 v1.4
 * Just a more enhanced draft this time. I like the new team messages, they really make the Tassies look like a team. Now they share every information
 * on targets and give them to the other team member. Also new is the scan behavior. The leader takes his target (closest) and send a message to the
 * minion. The minion now looks for the other target and scan lock it. Because every bot has the same information about the position, heading, avg
 * velocity and so on every bot can shoot blind on his best target and don't have to loose the scan lock, very neat i guess :). I'm still disappointed
 * about my gun. The gun is still useless against the 1v1 stallions but i don't want to switch to a more enhanced weapon until i didn't squeeze the
 * last
 * bit of performance out of the 'simple' gun. So still no chance against wave teams but very nice performance against everyone else.
 * The code is still draft status and could really use some love. Right now i reached the 2000byte barrier but there is still huge code size left if
 * proper coded.
 * Credit: i still try my own ideas
 * Man i'm so hooked into this twin stuff this is really good clean fun.
 * Size 1888 v1.5
 * This one got some nice improvements and i started to clean the code a little bit. For now both teammembers react on protect calls and help
 * each other in serious situations. I changed the fire target distance by 100 to save some energy against the long shot teams. The movement got
 * a little tweak by lowering the calculated RiskPoints and a sin move if the bot goes to the riskpoint. The targets are now hold by its own
 * variables, this saved a lot of code size but got uglier than ever. The radar got some love and lock now a little bit better. The protect react
 * force is now 100k to make a faster engagement of the protected target. Nice new battlestate variable for clean code size and smooth decision
 * making. The target is now not only the closest it is rated by energy and distance. This means if the target is near it will be selected but
 * if it is far away it depends on his energy state. The target distance is now correct calculated and if the target gets an update by team messages
 * it is advanced by 1 step (roughly guessed). The team message only has the values of the last round and if the target shots blind it was always one
 * step
 * behind the target.
 * Rednexala has changed his LunarTwins and after this they have beaten me very badly. This should be fixed with this version. But i call it a success
 * if the top leader changes his bots just because of me.
 * Credit: hmm
 * Still hooked and excited about this robocode stuff.
 * Size 1996 v1.6
 * Another day without sleep and i could fix some issues of the last version. It lost major score against some mid class bots just because the
 * movement was wrong. So i got rid of the 'jiggle' stuff. Some minor changes to the movement if the bot goes for close combat and a different
 * protectpoint for both bots. The minion now goes for protection to the last guessed point (collected by the gun) and the leader goes to the
 * opposite site (so the enemy stays between those both). The code size is a little bit of a problem (mostly to bring in some stuff against the top
 * teams)
 * The overall forces got a little change (not sure about this). The distance gets more weight for the targeting now.
 * I spotted some serious issues of the movement and this need some heavy debugging soon. Sometimes the call for help got no response and the bot
 * drives to the opposite (cowardly) direction. Also sometimes the bot get stuck in the middle of the field (i guess the risk function reaches it
 * local maximum .. escpecially if the dodge variables are true).
 * I played with lostCounts and going close combat if i loose to much but this wasn't really successful.
 * The gun has much trouble to even hit on a very close distance to some teams and this bugs me most. I think i should change the avgVeocity to
 * something more dynamic to picture the velocity changes more precise.
 * Credit: nothing today
 * Overtyred but still excited.
 * 06/06/20012
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
	private final static double	TARGET_DISTANCE		= 600.0;

	private final static double	PI_360				= Math.PI * 2.0;
	private final static double	DELTA_RISK_ANGLE	= Math.PI / 20.0;
	public static double		DIST				= 185;
	private final static double	DEFAULT_FORCE		= DIST * DIST;		// normalize force to 1.0 at dist
	private final static double	TARGET_FORCE		= 35000;

	static TassieTarget			leader;
	static TassieTarget			minion;

	static boolean				isLeader;
	static boolean				isMateDead;
	static int					battleState;							// 7 = 2vs2 6 = 2vs1 5 = 1vs2 4 = 1vs1
	static TassieTeamInfo		myInfo;
	static TassieTeamInfo		teamInfo;

	@Override
	public void run()
	{
		setAllColors(Color.yellow);
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

		battleState = 7;
		isLeader = (getEnergy() > 150);

		if (leader == null && minion == null) // maybe make this static
		{
			leader = new TassieTarget();
			minion = new TassieTarget();
		}
		myInfo = new TassieTeamInfo();

		while (true)
		{
			if (getRadarTurnRemaining() == 0.0) setTurnRadarRightRadians(Double.MAX_VALUE);

			TassieTarget target = getMainTarget();

			if (isLeader && minion.isAlive) target = minion;
			boolean isCloseCombat = (leader.isAlive && leader.eDistance < 300) || (minion.isAlive && minion.eDistance < 300);

			if (battleState == 7 /* || battleState == 5 */)
			{
				myInfo.x = getX();
				myInfo.y = getY();
				myInfo.protectPoint = null;

				if (!myInfo.teamBullets.isEmpty() && !myInfo.teamBullets.get(0).isActive()) myInfo.teamBullets.remove(0);
				if (isLeader) myInfo.leadScan = target.eName;

				// isClose || (1vs2 && leader)
				if (isCloseCombat || (battleState == 5))
				{
					myInfo.protectPoint = new Point2D.Double();
					double angle;
					double aDist;
					myInfo.protectPoint.x = getX() + Math.sin(angle = Math.atan2(target.x - getX(), target.y - getY()))
							* (aDist = (target.eDistance + 50));
					myInfo.protectPoint.y = getY() + Math.cos(angle) * aDist;
				}
				else myInfo.protectPoint = null;
				try
				{
					broadcastMessage(myInfo);
				}
				catch (Exception e1)
				{}
			}

			double mRate = Double.MAX_VALUE;
			double v0 = 0;
			double v1 = 0;
			boolean isBulletEvade = false;
			double protectDist = 0;

			while ((v0 += DELTA_RISK_ANGLE) <= PI_360)
			{
				double x = DIST * Math.sin(v0) + getX();
				double y = DIST * Math.cos(v0) + getY();

				if (new Rectangle2D.Double(WZ_M, WZ_M, WZ_SIZE_M_W, WZ_SIZE_M_H).contains(x, y))
				{
					double r1 = 0;
					double force = TARGET_FORCE;
					if (teamInfo != null && teamInfo.protectPoint != null)
					{
						isCloseCombat = true;
						protectDist = teamInfo.protectPoint.distance(getX(), getY());
						force = 10000;
						r1 -= 100000 / teamInfo.protectPoint.distanceSq(x, y);
					}
					r1 += (leader.isAlive) ? force / leader.distanceSq(x, y) : 0;
					r1 += (minion.isAlive) ? force / minion.distanceSq(x, y) : 0;

					if (teamInfo != null)
					{
						r1 += force / teamInfo.distanceSq(x, y);

						for (Bullet bullet : teamInfo.teamBullets)
						{
							if (bullet.isActive())
							{
								double dist = Point2D.distance(bullet.getX(), bullet.getY(), getX(), getY());
								if (dist <= DIST)
								{
									r1 += 100000 / Point2D.distanceSq(bullet.getX(), bullet.getY(), x, y);
									isBulletEvade = true;
								}
							}
						}
					}

					if (isCloseCombat)
					{
						r1 += (Math.abs(Math.sin(Math.atan2(target.x - x, target.y - y) - v0)));
					}
					else
					{
						r1 += (Math.abs(Math.cos(Math.atan2(target.x - x, target.y - y) - v0)));
					}

					if (r1 < mRate)
					{
						mRate = r1;
						v1 = v0;
					}
				}
			}

			if (Math.abs(getDistanceRemaining()) <= DIST_REMAIN || isBulletEvade || isCloseCombat || mRate > 1.3)
			{
				setTurnRightRadians(Math.tan(v1 -= getHeadingRadians() + ((isCloseCombat) ? Math.sin(protectDist / 30.0) / 10.0 : 0)));
				setAhead(DIST * Math.cos(v1));
			}
			execute();
		}

	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		String name;
		if (isTeammate(name = e.getName())) return;   // all team informations per message

		TassieTarget enemy = getTarget(name, e.getEnergy());

		double v3;
		enemy.eDistance = e.getDistance();
		enemy.isAlive = true;
		TassieEnemyInfo eInfo;
		setPolarPoint(eInfo = new TassieEnemyInfo(), getX(), getY(), v3 = (getHeadingRadians() + e.getBearingRadians()), e.getDistance());
		enemy.x = eInfo.x;
		enemy.y = eInfo.y;
		enemy.eVelocity = eInfo.eVelocity = e.getVelocity();
		enemy.eHeading = eInfo.eHeading = e.getHeadingRadians();
		eInfo.segments = enemy.segments;
		eInfo.eName = name;

		try
		{
			broadcastMessage(eInfo);
		}
		catch (Exception e1)
		{}

		doGun(enemy = getMainTarget(), e);

		// System.out.format("[%d] batteState=%d target=%s heat=%3.2f\n", getTime(),battleState,enemy.eName,getGunHeat());

		// 1vs1 || 1vs2 || (2vs1 && heat) || (2vs2 && isLead) - lock radar to myTarget
		if (battleState <= 5 || (battleState == 6 && getGunHeat() < RADAR_GUNLOCK) || (battleState == 7 && isLeader))
		{
			doRadar(Math.atan2(enemy.x - getX(), enemy.y - getY()));
		}
		else if (teamInfo != null && !teamInfo.leadScan.equals(name))  // the null because (2vs1 && gunheat) can be false .. grrr i hate null checks
		{
			doRadar(v3);
		}
	}

	@Override
	public void onMessageReceived(MessageEvent event)
	{
		((ITassieMessage) event.getMessage()).proccedMessage(this);
	}

	@Override
	public void onRobotDeath(RobotDeathEvent event)
	{
		String name;
		if (isTeammate(name = event.getName()))
		{
			teamInfo = null;
			isMateDead = true;
			battleState--;
			return;
		}
		battleState -= 2;
		if (name.equals(leader.eName))
		{
			leader.isAlive = false;
			return;
		}
		minion.isAlive = false;
	}

	public void onPaint(Graphics2D g)
	{}

	private void doGun(TassieTarget target, ScannedRobotEvent e)
	{
		double absBearing = getHeadingRadians() + e.getBearingRadians();
		double bPower = Math.min(Rules.MAX_BULLET_POWER, TARGET_DISTANCE / target.eDistance);
		if (getGunTurnRemaining() == 0) setFire(bPower);

		if (e.getVelocity() != 0)
		{
			target.eDir = (Math.sin(e.getHeadingRadians() - absBearing) * e.getVelocity() < 0) ? -1 : 1;
		}

		int[] cSegments = target.segments[(int) (target.eDistance / 200)][(int) Math.abs(e.getVelocity() / 2)][(int) Math.abs(target.eVelocity / 2)];
		target.eVelocity = e.getVelocity();

		TassieWave wave = new TassieWave();
		wave.myTarget = target;
		wave.eAbsBearing = absBearing;
		wave.eDirection = target.eDir;
		wave.segResult = cSegments;
		wave.rx = getX();
		wave.ry = getY();
		wave.bSpeed = Rules.getBulletSpeed(bPower);
		addCustomEvent(wave);

		int bIndex = 12;
		for (int i = 0; i < 25; i++)
		{
			if (cSegments[bIndex] < cSegments[i]) bIndex = i;
		}
		double gFactor = (double) (bIndex - (cSegments.length - 1) / 2) / ((cSegments.length - 1) / 2);
		double gunDelta = target.eDir * gFactor * Math.asin(8 / wave.bSpeed);
		setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing + gunDelta - getGunHeadingRadians()));
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
		double lRate = leader.eEnergy + leader.eDistance * 0.6;
		double mRate = minion.eEnergy + minion.eDistance * 0.0;
		return ((leader.isAlive && (lRate < mRate || !minion.isAlive)) ? leader : minion);
	}

	public static void setPolarPoint(Point2D resultPoint, double x, double y, double angle, double dist)
	{
		resultPoint.setLocation(x + Math.sin(angle) * dist, y + Math.cos(angle) * dist);
	}

	class TassieWave extends Condition
	{
		TassieTarget	myTarget;
		double			eAbsBearing;
		double			eDirection;
		double			rx;
		double			ry;
		double			bSpeed;
		private double	count;
		int[]			segResult;

		@Override
		public boolean test()
		{
			if (myTarget.distance(rx, ry) < (++count * bSpeed))
			{
				double eGunDelta = Utils.normalRelativeAngle(Math.atan2(myTarget.x - rx, myTarget.y - ry) - eAbsBearing);
				double guessFactor = Math.max(-1, Math.min(1, eGunDelta / Math.asin(8 / bSpeed))) * eDirection;
				int index = (int) Math.round((segResult.length - 1) / 2 * (guessFactor + 1));
				segResult[index]++;
				removeCustomEvent(this);
				return true;
			}
			return false;
		}
	}
}

interface ITassieMessage
{
	public void proccedMessage(TassieDevil bot);
}

class TassieTeamInfo extends Point2D.Double implements ITassieMessage
{
	private static final long	serialVersionUID	= 2L;

	ArrayList<Bullet>			teamBullets;
	String						leadScan;

	Point2D.Double				protectPoint;

	public TassieTeamInfo()
	{
		teamBullets = new ArrayList<Bullet>();
		leadScan = "";
	}

	@Override
	public void proccedMessage(TassieDevil bot)
	{
		TassieDevil.teamInfo = this;
		// /PaintHelper.drawArc(new Point2D.Double(x,y), 40, 0, Math.PI * 2, false, bot.getGraphics(), bot.myColor);
	}
}

class TassieEnemyInfo extends Point2D.Double implements ITassieMessage
{
	private static final long	serialVersionUID	= 4L;

	String						eName;
	double						eEnergy;
	double						eHeading;
	double						eVelocity;
	int[][][][]					segments;

	@Override
	public void proccedMessage(TassieDevil bot)
	{
		TassieTarget target;
		// advance the target by one step
		TassieDevil.setPolarPoint(target = TassieDevil.getTarget(eName, eEnergy), x, y, eHeading, eVelocity);
		target.isAlive = true;
		target.segments = segments;
		target.eDistance = Point2D.distance(x, y, bot.getX(), bot.getY());
	}
}

class TassieTarget extends Point2D.Double
{
	private static final long	serialVersionUID	= 5L;
	boolean						isAlive;
	String						eName;

	private static final int	DISTANCE_INDEXES	= 5;
	private static final int	VELOCITY_INDEXES	= 5;
	private static final int	BINS				= 25;

	double						eDir;
	int[][][][]					segments;

	double						eDistance;
	double						eEnergy;
	double						eVelocity;
	double						eHeading;

	public TassieTarget()
	{
		eName = "";
		segments = new int[DISTANCE_INDEXES][VELOCITY_INDEXES][VELOCITY_INDEXES][BINS];
	}
}
