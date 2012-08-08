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
package wompi.echidna.move;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import robocode.AdvancedRobot;
import robocode.DeathEvent;
import robocode.HitRobotEvent;
import robocode.WinEvent;
import wompi.Echidna;
import wompi.echidna.misc.painter.PaintMinRiskPoints;
import wompi.echidna.stats.StatsRisk;
import wompi.echidna.target.ATarget;

public class MoveLikeWalaby extends AMovement
{
	private static final double	WZ					= 20.0;

	private final static double	DIST_REMAIN			= 20;
	private final static double	TARGET_FORCE		= 55000;					// 100000 low dmg high surv - 10000 high dmg low surv

	private final static double	PI_360				= Math.PI * 2.0;
	private final static double	DELTA_RISK_ANGLE	= Math.PI / 20.0;

	private static double		riskDist			= 185;
	private static int			ramEscape;

	// / enemy values
	ATarget						myTarget;

	// robot values

	// controlling values

	// debug
	PaintMinRiskPoints			debugRiskPoints		= new PaintMinRiskPoints();

	public MoveLikeWalaby(AdvancedRobot robot)
	{
		super(robot);
	}

	public void init()
	{}

	@Override
	public void onScannedRobot(ATarget target, boolean isMaintarget)
	{
		if (isMaintarget) myTarget = target;
		doMove();
	}

	@Override
	public void run()
	{}

	// --------------------------------------------- working stuff ----------------------------------------------------------
	private void doMove()
	{
		if (myTarget == null) return;

		double riskAngle = 0;
		double x, y;
		double maxRisk = Double.MAX_VALUE;
		double resultAngle = 0;

		double combatDist = 185;
		if (ramEscape-- > 0) combatDist = 30;  // adjust this

		while ((riskAngle += DELTA_RISK_ANGLE) <= PI_360)
		{
			x = (combatDist * Math.sin(riskAngle));
			y = (combatDist * Math.cos(riskAngle));

			if (new Rectangle2D.Double(WZ, WZ, myRobot.getBattleFieldWidth() - 2 * WZ, myRobot.getBattleFieldHeight() - 2 * WZ).contains(
					x + myRobot.getX(), y + myRobot.getY()))
			{
				double risk = 0;
				for (ATarget target : Echidna.myTargetHandler.getAllTargets())
				{
					if (target.isAlive())
					{
						risk += TARGET_FORCE / (Point2D.distanceSq(target.getX(), target.getY(), x, y));
					}
				}
				if ((risk += Math.abs(Math.cos(Math.atan2(myTarget.getX() - x, myTarget.getY() - y) - riskAngle))) < maxRisk)
				{
					maxRisk = risk;
					resultAngle = riskAngle;
				}
				debugRiskPoints.registerRiskPoint(myRobot.getTime(), x + myRobot.getX(), y + myRobot.getY(), risk, myRobot.getX(), myRobot.getY(),
						combatDist);
			}
		}

		// debug
		StatsRisk.registerRisk(maxRisk, myRobot.getOthers(), myRobot.getTime());

		if (maxRisk > 2.0) myRobot.setAllColors(Color.RED);
		else myRobot.setAllColors(Color.ORANGE);

		if (Math.abs(myRobot.getDistanceRemaining()) <= DIST_REMAIN || maxRisk > 9.0)
		{

			myRobot.setTurnRightRadians(Math.tan(resultAngle -= myRobot.getHeadingRadians()));
			myRobot.setAhead(combatDist * Math.cos(resultAngle));
		}
	}

	@Override
	public void onHitRobot(HitRobotEvent e)
	{
		ramEscape = 20;
		StatsRisk.registerRiskRamhits(myRobot.getOthers());
		System.out.format("[%d] rambock ahead\n", myRobot.getTime());
	}

	@Override
	public void onPaint(Graphics2D g)
	{
		debugRiskPoints.onPaint(g, true);
	}

	@Override
	public void onDeath(DeathEvent e)
	{
		onWin(null);
	}

	@Override
	public void onWin(WinEvent e)
	{
		StatsRisk.printStats();
	}
}
