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
package wompi.numbat.move;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import robocode.AdvancedRobot;
import robocode.HitRobotEvent;
import robocode.RobotStatus;
import wompi.numbat.misc.NumbatBattleField;
import wompi.numbat.target.ITargetManager;
import wompi.numbat.target.NumbatTarget;

public class NumbatMinRiskMove extends ANumbatMove
{
	private final static double	WZ					= 20.0;
	private final static double	PI_360				= Math.PI * 2.0;
	private final static double	DELTA_RISK_ANGLE	= Math.PI / 32.0;
	private final static double	DIST				= 185;
	private final static double	DIST_REMAIN			= 20;
	private final static double	TARGET_FORCE		= 45000;			// 100000 low dmg high surv - 10000 high dmg low surv
	private final static double	MAX_RANDOM_PERP		= 0.5;

	private static Rectangle2D	B_FIELD_MOVE;
	private boolean				isMoveing;
	private double				moveTurn;
	private HitRobotEvent		ramBot;
	private double				moveDist;

	// debug
	// public boolean isDebug = false;
	// PaintMinRiskPoints debugRiskPerp = new PaintMinRiskPoints();
	// PaintMinRiskPoints debugRiskForce = new PaintMinRiskPoints();
	// PaintMinRiskPoints debugRiskAll = new PaintMinRiskPoints();
	// PaintMinRiskPoints debugRiskPerpBullet = new PaintMinRiskPoints();
	// PaintMinRiskPoints debugRiskForceBullet = new PaintMinRiskPoints();
	double						maxRate;

	@Override
	public void init(RobotStatus status)
	{
		B_FIELD_MOVE = new Rectangle2D.Double(WZ, WZ, NumbatBattleField.BATTLE_FIELD_W - 2 * WZ, NumbatBattleField.BATTLE_FIELD_H - 2 * WZ);
		ramBot = null;
	}

	@Override
	public void setMove(RobotStatus status, ITargetManager targetMan)
	{
		maxRate = Double.MAX_VALUE;
		double riskAngle;
		double mx;
		double my;
		riskAngle = moveTurn = 0;
		isMoveing = false;

		NumbatTarget target = targetMan.getMoveTarget();

		boolean isFireRule = (Math.random() <= 0.33) ? target.isTargetFireing() : false; // use the fire rule on 1/3 of times

		boolean isClose = false;

		while ((riskAngle += DELTA_RISK_ANGLE) <= PI_360)
		{
			mx = DIST * Math.sin(riskAngle) + status.getX();
			my = DIST * Math.cos(riskAngle) + status.getY();

			if (B_FIELD_MOVE.contains(mx, my))
			{
				double riskRate = 0;
				double riskForce = 0;
				for (NumbatTarget enemy : targetMan.getAllTargets())
				{
					if (enemy.isAlive)
					{
						double force = TARGET_FORCE; // + 2000*enemy.getScanDifference(status);
						if (ramBot != null && ramBot.getName() == enemy.eName)
						{
							// what a mess :(
							long ramTime = status.getTime() - ramBot.getTime();
							if (ramTime <= 5)
							{
								force = 2000000; // well maybe to much :)
								isClose = true;
							}
							else
							{
								ramBot = null;
							}
						}
						else if (enemy.getDistance(status) <= DIST) isClose = true;
						riskForce += force / enemy.distanceSq(mx, my);
					}
				}
				riskRate += riskForce;

				if (target != null)
				{

					double perpRate = Math.abs(Math.cos(Math.atan2(target.x - mx, target.y - my) - riskAngle));

					if ((status.getOthers() <= 5 && perpRate < MAX_RANDOM_PERP) || isFireRule)
					{
						riskRate += (perpRate = (MAX_RANDOM_PERP * Math.random()));
					}
					else
					{
						riskRate += perpRate;
					}

					// debug
					// if (isDebug)
					// {
					// debugRiskPerp.registerRiskPoint(status.getTime(), mx, my, perpRate, status.getX(), status.getY(), DIST-20);
					// //debugRiskPerpBullet.registerRiskPoint(status.getTime(),mx, my, bulletPerpRate, status.getX(), status.getY(), DIST-20);
					// //debugRiskForceBullet.registerRiskPoint(status.getTime(),mx, my, bulletForceRate, status.getX(), status.getY(), DIST-30);
					// }
				}

				if (riskRate < maxRate)
				{
					maxRate = riskRate;
					moveTurn = riskAngle;
				}
				// if (isDebug)
				// {
				// debugRiskAll.registerRiskPoint(status.getTime(), mx, my, riskRate, status.getX(), status.getY(), DIST);
				// debugRiskForce.registerRiskPoint(status.getTime(), mx, my, riskForce, status.getX(), status.getY(), DIST-10);
				// }

			}
		}

		if (Math.abs(status.getDistanceRemaining()) <= DIST_REMAIN || isFreeMove(status, isClose, isFireRule))
		{
			isMoveing = true;
			moveDist = target.getDistance(status) * 8.0 / 15.7;

		}

		moveTurn -= status.getHeadingRadians();
	}

	double	headJiggle;

	@Override
	public void excecute(AdvancedRobot myBot)
	{
		if (isMoveing)
		{
			myBot.setTurnRightRadians(Math.tan(moveTurn));
			myBot.setAhead(moveDist * Math.cos(moveTurn));
		}
		//		if (Math.random() > 0.8)
		//		{
		//			//			if (Utils.isNear(0, myBot.getTurnRemainingRadians()))
		//			//			{
		//			//				double maxRand = Rules.getTurnRateRadians(myBot.getVelocity()) * Math.random();
		//			//				double odd = Math.signum(Math.random() - 0.5);
		//			//				myBot.setTurnRightRadians(headJiggle += (odd * maxRand));
		//			//			}
		//			myBot.setMaxVelocity((Math.abs(myBot.getVelocity()) - 1));
		//			myBot.setAllColors(Color.YELLOW);
		//		}
		//		else
		//		{
		//			//			if (headJiggle != 0) myBot.setTurnRightRadians(-headJiggle);
		//			myBot.setMaxVelocity(8.0);
		//			myBot.setAllColors(Color.RED);
		//			headJiggle = 0;
		//		}
	}

	@Override
	public void onHitRobot(HitRobotEvent e, RobotStatus myBotStatus)
	{
		ramBot = e;
	}

	private boolean isFreeMove(RobotStatus status, boolean isClose, boolean isFire)
	{
		if (isClose || isFire) return true;
		if (status.getTime() < 32 && status.getOthers() > 1) return true; // in 1vs1 battles it is not a good idea to start with free move because of
																			// local max
		return false;
	}

	@Override
	public void onPaint(Graphics2D g, RobotStatus status)
	{
		// if (isDebug)
		// {
		// debugRiskPerp.onPaint(g, false);
		// debugRiskAll.onPaint(g, false);
		// debugRiskForce.onPaint(g, false);
		// //debugRiskPerpBullet.onPaint(g, false);
		// //debugRiskForceBullet.onPaint(g, false);
		// //PaintHelper.drawArc(new Point2D.Double(status.getX(), status.getY()), 400,0, PI_360, true, g, PaintHelper.whiteTrans);
		// PaintHelper.drawString(g, String.format("%3.2f", maxRate), status.getX(), status.getY()+40, Color.YELLOW);
		// }
	}

	@Override
	String getName()
	{
		return "MinRisk Antigrav Oscillation";
	}

	@Override
	boolean checkActivateRule(RobotStatus status, ITargetManager targetMan)
	{
		boolean r1 = status.getOthers() > 0;
		boolean r2 = targetMan.getGunTarget() != null;
		// boolean r3 = targetMan.getCloseBots() != 0;

		return r1 && r2;
	}
}
