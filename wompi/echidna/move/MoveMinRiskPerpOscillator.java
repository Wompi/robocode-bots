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

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.Rules;
import robocode.StatusEvent;
import wompi.echidna.target.ATarget;

public class MoveMinRiskPerpOscillator extends AMovement
{
	private static final double	WZ					= 20.0;
	private final static double	PI_360				= Math.PI * 2.0;
	private final static double	DELTA_RISK_ANGLE	= Math.PI / 32.0;
	private final static double	DIST				= 185;
	private final static double	TARGET_FORCE		= 100000;
	private final static double	DIST_REMAIN			= 20;

	static Rectangle2D			B_FIELD_M;

	ArrayList<ATarget>			myTargets			= new ArrayList<ATarget>();
	ATarget						myTarget;

	double						lastEnergy;

	public MoveMinRiskPerpOscillator(AdvancedRobot robot)
	{
		super(robot);
	}

	public void init()
	{
		B_FIELD_M = new Rectangle2D.Double(WZ, WZ, myRobot.getBattleFieldWidth() - 2 * WZ, myRobot.getBattleFieldHeight() - 2 * WZ);
	}

	public void onStatus(StatusEvent event)
	{
		System.out.format("STATUS MOVE[%d] \n", myRobot.getTime());
	}

	@Override
	public void onScannedRobot(ATarget target, boolean isMaintarget)
	{
		if (isMaintarget) myTarget = target;

		if (!myTargets.contains(target))
		{
			myTargets.add(target);
		}
	}

	public void onRobotDeath(RobotDeathEvent e)
	{
		for (ATarget target : myTargets)
		{
			if (target.getName() == e.getName())
			{
				myTargets.remove(target);
				break;
			}
		}
	}

	@Override
	public void run()
	{
		doMove();
	}

	@Override
	public void onPaint(Graphics2D g)
	{
		// PaintMinRiskPoints.onPaint(g);

	}

	// --------------------------------------------- working stuff ----------------------------------------------------------
	private void doMove()
	{
		if (myTarget == null) return;

		double angle = 0;
		double mRate = Double.MAX_VALUE;
		double riskAngle = 0;

		while ((angle += DELTA_RISK_ANGLE) <= PI_360)
		{
			double x;
			double y;
			if (B_FIELD_M.contains((x = (DIST * Math.sin(angle))) + myRobot.getX(), (y = (DIST * Math.cos(angle))) + myRobot.getY()))
			{
				double r1 = 0;
				for (ATarget target : myTargets)
				{
					r1 += TARGET_FORCE / Point2D.distanceSq(target.getX(), target.getY(), x, y);
				}
				if ((r1 += Math.abs(Math.cos(Math.atan2(myTarget.getX() - x, myTarget.getY() - y) - angle))) < mRate)
				{
					mRate = r1;
					riskAngle = angle;
				}

				// debug
				// PaintMinRiskPoints.registerRiskPoint(myRobot.getTime(), x+myRobot.getX(), y+myRobot.getY(), r1);
			}
		}

		myRobot.setMaxVelocity(Rules.MAX_VELOCITY);
		if (Math.abs(myRobot.getDistanceRemaining()) <= DIST_REMAIN)
		{
			myRobot.setTurnRightRadians(Math.tan(riskAngle -= myRobot.getHeadingRadians()));
			if (myRobot.getOthers() < 3)
			{
				double eDiff = lastEnergy - (lastEnergy = myTarget.getEnergy());
				if (0 < eDiff && eDiff < 3.0)
				{
					myRobot.setAhead(80 * Math.cos(riskAngle));
				}
			}
			else
			{
				myRobot.setAhead(DIST * Math.cos(riskAngle));
			}
		}
	}

}
