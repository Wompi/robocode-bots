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
import robocode.Rules;
import robocode.util.Utils;
import wompi.echidna.target.ATarget;
import wompi.robomath.RobotMath;
import wompi.wallaby.WallabyPainter;

public class MoveOrbiting extends AMovement
{
	// / enemy values
	ATarget				myTarget;

	// robot values

	// controlling values
	int					nextDirChange;
	double				DIR;
	double				WALL_STICK	= 160;
	int					stopMoves;

	// debug
	ArrayList<Point2D>	goodPoints	= new ArrayList<Point2D>();
	ArrayList<Point2D>	badPoints	= new ArrayList<Point2D>();

	public MoveOrbiting(AdvancedRobot robot)
	{
		super(robot);
	}

	public void init()
	{
		DIR = 10000;
	}

	// @Override
	// public void onHitWall(HitWallEvent e)
	// {
	// DIR = -DIR;
	// }
	//
	// public void onHitRobot(HitRobotEvent e)
	// {
	// DIR = -DIR;
	// }

	@Override
	public void onScannedRobot(ATarget target, boolean isMaintarget)
	{
		if (isMaintarget) myTarget = target;

		double eEnergyDelta = myTarget.getLastEnergy() - myTarget.getEnergy();
		if (eEnergyDelta < 3.01 && eEnergyDelta > 0.09)
		{
			// WallabyPainter.drawTargetSquare(myRobot.getGraphics(), target.getHeading(), target.getX(), target.getY());
			long deltaTime = target.getTime() - target.getLastScan();
			stopMoves = (int) (Math.random() * 7);
			System.out.format("FIRE[%d] power=%3.2f lastSeen=%d stop=%d %s\n", myRobot.getTime(), eEnergyDelta, deltaTime, stopMoves,
					target.getName());

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

		WallabyPainter.drawWallStick(g, myRobot.getHeadingRadians(), WALL_STICK * Math.signum(myRobot.getVelocity()), myRobot.getX(), myRobot.getY());
	}

	// --------------------------------------------- working stuff ----------------------------------------------------------
	private void doMove()
	{
		if (myTarget == null) return;

		Point2D eP = new Point2D.Double(myTarget.getX(), myTarget.getY());

		double turn = 0, deltaTurn = 0;
		double WZ = 18;
		int i = 20;
		badPoints.clear();
		goodPoints.clear();
		Point2D fP = new Point2D.Double(myRobot.getX(), myRobot.getY());  // future Point
		double head = myRobot.getHeadingRadians();
		do
		{
			double angle = Utils.normalAbsoluteAngle(RobotMath.calculateAngle(fP, eP));
			double bear = angle - head;
			double cTurn = bear; // = Math.cos(bear); // calculatedTurn

			if (i == 20)
			{
				turn = cTurn;
				// System.out.format("\nSTART=%d turn=%3.5f max=%3.5f\n",i,turn, Rules.getTurnRateRadians(getVelocity()));
			}

			double cos = Math.cos(bear);
			double max = Rules.getTurnRateRadians(myRobot.getVelocity());
			head += Math.min(cos, max);
			// System.out.format("[%d] cos=%3.5f max=%3.5f head=%3.2f\n",i,Math.toDegrees(cos),Math.toDegrees(max),Math.toDegrees(head));

			cTurn = head;
			// cTurn = Math.sin(bear);

			fP = RobotMath.calculatePolarPoint(cTurn, myRobot.getVelocity(), fP);
			if ((!new Rectangle2D.Double(WZ, WZ, myRobot.getBattleFieldWidth() - 2 * WZ, myRobot.getBattleFieldHeight() - 2 * WZ).contains(fP)))
			{

				deltaTurn += max;
				nextDirChange--;
				badPoints.add(fP);
			}
			else
			{
				goodPoints.add(fP);
			}
		}
		while (--i >= 0);

		turn = Math.cos(turn);

		myRobot.setTurnRightRadians(turn - deltaTurn * Math.signum(myRobot.getVelocity()));

		if (stopMoves-- <= 0)
		{
			if (nextDirChange-- <= 0 || myRobot.getDistanceRemaining() == 0)
			{
				// nextDirChange = (int)(myTarget.getDistance()/2);
				nextDirChange = 50;
				myRobot.setAhead(DIR = -DIR);
			}
		}
		else
		{
			myRobot.setAhead(0);
			nextDirChange = 0;
		}
	}
}
