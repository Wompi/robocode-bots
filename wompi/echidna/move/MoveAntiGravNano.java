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
import java.awt.geom.Point2D;

import robocode.AdvancedRobot;
import robocode.util.Utils;
import wompi.echidna.target.ATarget;
import wompi.wallaby.WallabyPainter;

public class MoveAntiGravNano extends AMovement
{
	// / enemy values
	ATarget	myTarget;

	// robot values

	// controlling values
	double	xForce;
	double	yForce;

	// debug

	public MoveAntiGravNano(AdvancedRobot robot)
	{
		super(robot);
	}

	public void init()
	{}

	@Override
	public void onScannedRobot(ATarget target, boolean isMaintarget)
	{
		if (isMaintarget) myTarget = target;

		double rHead = myRobot.getHeadingRadians();
		double tAbsBearing = target.getBearing() + rHead;
		// xForce = xForce * 0.9 - (((Math.sin(tAbsBearing)-Math.sin(rHead))/myRobot.getOthers()) / myTarget.getDistance());
		// yForce = yForce * 0.9 - (((Math.cos(tAbsBearing)-Math.cos(rHead))/myRobot.getOthers()) / myTarget.getDistance());

		// xForce = xForce *.9 - Math.sin(tAbsBearing) / myTarget.getDistance();
		// yForce = yForce *.9 - Math.cos(tAbsBearing) / myTarget.getDistance();

		xForce = xForce * 0.92 - Math.sin(tAbsBearing) / target.getDistance();
		yForce = yForce * 0.92 - Math.cos(tAbsBearing) / target.getDistance();

		// xForce = Math.cos(myTarget.getBearing());
		// yForce = Math.sin(myTarget.getBearing());
		//
	}

	@Override
	public void run()
	{
		doMove();
	}

	// --------------------------------------------- working stuff ----------------------------------------------------------
	private void doMove()
	{
		if (myTarget == null) return;

		// if (Math.abs(myRobot.getDistanceRemaining()) <= 20)
		{
			// double angleForce = Math.atan2(xForce,yForce);
			// double anglePerp = Math.cos(myTarget.getBearing());

			// System.out.format("[%d] forceAngle=%3.2f perpAngle=%3.2f \n", myRobot.getTime(),Math.toDegrees(angleForce),Math.toDegrees(anglePerp));
			double angle = Utils.normalRelativeAngle(Math.atan2(xForce + 1 / myRobot.getX() - 1 / (myRobot.getBattleFieldWidth() - myRobot.getX()),
					yForce + 1 / myRobot.getY() - 1 / (myRobot.getBattleFieldHeight() - myRobot.getY())) - myRobot.getHeadingRadians());

			double perp = myTarget.getBearing() + myRobot.getHeadingRadians() - Math.PI / 2;

			WallabyPainter.drawAngleLine(myRobot.getGraphics(), new Point2D.Double(myRobot.getX(), myRobot.getY()), angle, -200, Color.YELLOW);
			WallabyPainter.drawAngleLine(myRobot.getGraphics(), new Point2D.Double(myRobot.getX(), myRobot.getY()), perp,
					200 * Math.signum(myRobot.getDistanceRemaining()), Color.CYAN);

			myRobot.setTurnLeftRadians(angle);
			// myRobot.setAhead(185 * Math.cos(myRobot.getTurnRemainingRadians()));
			myRobot.setAhead(120 - Math.abs(myRobot.getTurnRemaining()));
		}
	}

}
