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
package wompi.echidna.radar;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.Rules;
import robocode.util.Utils;
import wompi.Echidna;
import wompi.echidna.misc.painter.PaintTargetSquare;
import wompi.echidna.target.ATarget;
import wompi.wallaby.PaintHelper;

public class RadarWeighted extends ARadar
{

	ATarget	myScanTarget;

	public RadarWeighted(AdvancedRobot robot)
	{
		super(robot);
	}

	@Override
	public void init()
	{
		myRobot.setAdjustRadarForGunTurn(true);
		startBestAngleScan();
	}

	@Override
	public void onScannedRobot(ATarget target, boolean isMaintarget)
	{
		doRadar();
	}

	@Override
	public void run()
	{}

	@Override
	public void onPaint(Graphics2D g)
	{
		for (ATarget target : Echidna.myTargetHandler.getAllTargets())
		{
			if (target.isAlive())
			{
				Point2D start = new Point2D.Double(target.getAbsX(), target.getAbsY());
				double timeDiff = myRobot.getTime() - target.getTime();
				double dist = timeDiff * Rules.MAX_VELOCITY;
				PaintHelper.drawArc(start, dist, 0, Math.PI * 2.0, true, g, PaintHelper.greenTrans);
				PaintHelper.drawArc(start, dist, 0, Math.PI * 2.0, false, g, PaintHelper.whiteTrans);
			}
		}
		if (myScanTarget != null) PaintTargetSquare.drawTargetSquare(g, myScanTarget.getHeading(), myScanTarget.getAbsX(), myScanTarget.getAbsY(),
				true, Color.BLUE);
	}

	@Override
	public void onRobotDeath(RobotDeathEvent e)
	{}

	private void doRadar()
	{
		if (myRobot.getTime() <= 8 || myRobot.getOthers() == 0) return;

		myScanTarget = Echidna.myTargetHandler.getMainTarget();
		if (myRobot.getGunHeat() >= 1)
		{
			double maxRate = Double.NEGATIVE_INFINITY;
			for (ATarget target : Echidna.myTargetHandler.getAllTargets())
			{
				if (target.isAlive())
				{
					double timeDiff = myRobot.getTime() - target.getTime();
					double rate = Rules.MAX_VELOCITY / (target.getBlindDistance() - timeDiff * Rules.MAX_VELOCITY);

					if (rate > maxRate)
					{
						maxRate = rate;
						myScanTarget = target;
					}
				}
			}
		}
		// System.out.format("[%d] %s is now scan target\n", myRobot.getTime(),scanTarget.getName());
		double eAbsBearing = myScanTarget.getBlindAbsBearing();
		double rDiff = Utils.normalRelativeAngle(eAbsBearing - myRobot.getRadarHeadingRadians());
		myRobot.setTurnRadarRightRadians(Math.signum(rDiff) * Double.POSITIVE_INFINITY);
	}
}
