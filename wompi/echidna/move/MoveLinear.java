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
import java.awt.geom.Rectangle2D;

import robocode.AdvancedRobot;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.util.Utils;
import wompi.echidna.target.ATarget;

public class MoveLinear extends AMovement
{
	private static final double	WZ	= 20.0;

	// / enemy values
	ATarget						myTarget;

	// robot values
	static double				DIR;
	static Rectangle2D			bField;
	static double				breakTurns;

	// controlling values

	// debug
	public MoveLinear(AdvancedRobot robot)
	{
		super(robot);
	}

	public void init()
	{
		DIR = 1;
		bField = new Rectangle2D.Double(WZ, WZ, myRobot.getBattleFieldWidth() - 2 * WZ, myRobot.getBattleFieldHeight() - 2 * WZ);
		myRobot.setTurnRightRadians(Utils.normalRelativeAngle(Math.PI - myRobot.getHeadingRadians()));
	}

	@Override
	public void onHitWall(HitWallEvent e)
	{
		// DIR = -DIR;
		System.out.format("[%d] hit wall\n", myRobot.getTime());
	}

	public void onHitRobot(HitRobotEvent e)
	{
		DIR = -DIR;
	}

	@Override
	public void onScannedRobot(ATarget target, boolean isMaintarget)
	{
		if (isMaintarget) myTarget = target;
	}

	@Override
	public void run()
	{
		doMove();
	}

	@Override
	public void onPaint(Graphics2D g)
	{}

	// --------------------------------------------- working stuff ----------------------------------------------------------
	private void doMove()
	{
		if (myTarget == null) return;

		if (myRobot.getTurnRemainingRadians() == 0)
		{
			if (myRobot.getDistanceRemaining() == 0)
			{
				double dist = Math.max(myRobot.getY() - 18, myRobot.getBattleFieldHeight() - myRobot.getY() - 18);
				myRobot.setAhead(dist * (DIR = -DIR));
			}
		}
	}
}
