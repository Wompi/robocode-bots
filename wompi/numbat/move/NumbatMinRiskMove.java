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
import robocode.Rules;
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
	private final static double	TARGET_FORCE		= 55000;			// 100000 low dmg high surv - 10000 high dmg low surv

	private static Rectangle2D	B_FIELD_MOVE;
	private boolean				isMoveing;
	private double				moveTurn;

	// debug
	//	public boolean				isDebug					= true;
	//	PaintMinRiskPoints			debugRiskPerp			= new PaintMinRiskPoints();
	//	PaintMinRiskPoints			debugRiskForce			= new PaintMinRiskPoints();
	//	PaintMinRiskPoints			debugRiskAll			= new PaintMinRiskPoints();
	//	PaintMinRiskPoints			debugRiskPerpBullet		= new PaintMinRiskPoints();
	//	PaintMinRiskPoints			debugRiskForceBullet	= new PaintMinRiskPoints();
	double						maxRate;
	double						moveDist;

	@Override
	public void init(RobotStatus status)
	{
		B_FIELD_MOVE = new Rectangle2D.Double(WZ, WZ, NumbatBattleField.BATTLE_FIELD_W - 2 * WZ, NumbatBattleField.BATTLE_FIELD_H - 2 * WZ);
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

		boolean adjustRule = status.getOthers() <= 2 && Math.random() < 0.33;
		boolean isFireRule = target.myBulletTracker.hasFired(target.getLastScanDifference()) && targetMan.isNearest(target)
				&& (status.getOthers() > 2 || adjustRule);

		// debug
		//		if (isFireRule)
		//		{
		//			System.out.format("[%d] fire rule for %s\n", status.getTime(), target.eName);
		//		}

		boolean isClose = false;

		moveDist = Math.min(DIST, moveDist += 5);

		double power = 1.5;
		if (target.myBulletTracker.myLastFirePower > 0)
		{
			power = target.myBulletTracker.myLastFirePower;
		}

		if (status.getOthers() == 1) moveDist = Math.max(20, (target.getDistance(status) - 50) * 8.0 / Rules.getBulletSpeed(power));

		while ((riskAngle += DELTA_RISK_ANGLE) <= PI_360)
		{
			mx = moveDist * Math.sin(riskAngle) + status.getX();
			my = moveDist * Math.cos(riskAngle) + status.getY();

			if (B_FIELD_MOVE.contains(mx, my))
			{
				double riskRate = 0;
				double riskForce = 0;
				for (NumbatTarget enemy : targetMan.getAllTargets())
				{
					if (enemy.isAlive)
					{
						isClose |= (enemy.getDistance(status) <= moveDist);
						riskForce += TARGET_FORCE / enemy.distanceSq(mx, my);
					}
				}
				riskRate += riskForce;

				if (target != null)
				{

					double perpRate = Math.abs(Math.cos(Math.atan2(target.x - mx, target.y - my) - riskAngle));
					riskRate += perpRate;

					// debug
					//					if (isDebug)
					//					{
					//						debugRiskPerp.registerRiskPoint(status.getTime(), mx, my, perpRate, status.getX(), status.getY(), DIST - 20);
					//						//						debugRiskPerpBullet.registerRiskPoint(status.getTime(), mx, my, bulletPerpRate, status.getX(), status.getY(), DIST - 20);
					//						//						debugRiskForceBullet.registerRiskPoint(status.getTime(), mx, my, bulletForceRate, status.getX(), status.getY(), DIST - 30);
					//					}
				}

				boolean isGood = Math.random() < 0.6;
				if (isGood && riskRate < maxRate)
				{
					maxRate = riskRate;
					moveTurn = riskAngle;
				}
				//				if (isDebug && isGood)
				//				{
				//
				//					debugRiskAll.registerRiskPoint(status.getTime(), mx, my, riskRate, status.getX(), status.getY(), DIST);
				//					debugRiskForce.registerRiskPoint(status.getTime(), mx, my, riskForce, status.getX(), status.getY(), DIST - 10);
				//				}

			}
		}

		if (Math.abs(status.getDistanceRemaining()) <= DIST_REMAIN || isFreeMove(status, isClose, isFireRule))
		{
			isMoveing = true;
		}

		moveTurn -= status.getHeadingRadians();
	}

	@Override
	public void excecute(AdvancedRobot myBot)
	{
		if (isMoveing)
		{
			myBot.setTurnRightRadians(Math.tan(moveTurn));
			myBot.setAhead(moveDist * Math.cos(moveTurn));
		}
	}

	@Override
	public void onHitRobot(HitRobotEvent e, RobotStatus myBotStatus)
	{
		moveDist = 50;
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
		//		if (isDebug)
		//		{
		//			debugRiskPerp.onPaint(g, false);
		//			debugRiskAll.onPaint(g, false);
		//			debugRiskForce.onPaint(g, false);
		//			//debugRiskPerpBullet.onPaint(g, false);
		//			//debugRiskForceBullet.onPaint(g, false);
		//			//PaintHelper.drawArc(new Point2D.Double(status.getX(), status.getY()), 400,0, PI_360, true, g, PaintHelper.whiteTrans);
		//			PaintHelper.drawString(g, String.format("%3.2f", maxRate), status.getX(), status.getY() + 40, Color.YELLOW);
		//		}
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
