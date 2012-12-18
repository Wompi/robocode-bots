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
import java.util.Arrays;
import java.util.HashMap;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.util.Utils;
import wompi.echidna.target.ATarget;
import wompi.paint.PaintHelper;
import wompi.wallaby.WallabyPainter;

public class RadarCalculatedAngles extends ARadar
{
	private final static double	PI_360		= Math.PI * 2.0;

	HashMap<String, ATarget>	myScans;

	double						widestArea;
	double						startAngle;
	double						endAngle;

	// debug
	boolean						isLog		= true;
	int							logLevel	= 2;

	public RadarCalculatedAngles(AdvancedRobot robot)
	{
		super(robot);
		myScans = new HashMap<String, ATarget>();
	}

	@Override
	public void init()
	{
		myRobot.setAdjustRadarForGunTurn(true);
		myRobot.setAdjustRadarForRobotTurn(true);
		startBestAngleScan();
	}

	@Override
	public void onRobotDeath(RobotDeathEvent e)
	{
		myScans.remove(e.getName());
	}

	@Override
	public void onScannedRobot(ATarget target, boolean isMaintarget)
	{
		myScans.put(target.getName(), target);
	}

	@Override
	public void onPaint(Graphics2D g)
	{
		calcBearingDiff();
		Point2D rP = new Point2D.Double(myRobot.getX(), myRobot.getY());
		for (ATarget target : myScans.values())
		{
			double eAbsBearing = Utils.normalAbsoluteAngle(Math.atan2(target.getAbsX() - myRobot.getX(), target.getAbsY() - myRobot.getY()));
			double eDistance = rP.distance(target.getAbsX(), target.getAbsY());
			WallabyPainter.drawAngleLine(g, rP, eAbsBearing, eDistance, PaintHelper.whiteTrans);
		}

		WallabyPainter.drawAngleLine(g, rP, startAngle, 1200, Color.YELLOW);
		WallabyPainter.drawAngleLine(g, rP, endAngle, 1200, Color.red);
		PaintHelper.drawArc(rP, 1200, endAngle, widestArea, true, g, PaintHelper.whiteTrans);
	}

	@Override
	public void run()
	{
		// System.out.format("\n");
		calcBearingDiff();

		if (myScans.size() == myRobot.getOthers())
		{
			double rHead = myRobot.getRadarHeadingRadians();
			double rRemain = myRobot.getRadarTurnRemainingRadians();

			double turnToStart = Utils.normalRelativeAngle(startAngle - rHead);
			double turnToEnd = Utils.normalRelativeAngle(endAngle - rHead);

			// System.out.format("[%d] remain=%3.2f  \n", myRobot.getTime(),myRobot.getRadarTurnRemaining());

			boolean isInfinity = Double.isInfinite(rRemain);
			if (rRemain == 0 || isInfinity)
			{
				double rTurn;
				if (widestArea > Math.PI)
				{
					rTurn = Double.POSITIVE_INFINITY * rDirection;
					// System.out.format("[%d] infinite turn \n", myRobot.getTime());
				}
				else if (isInfinity)
				{
					if (Math.abs(turnToEnd) < Math.abs(turnToStart))
					{
						rTurn = turnToEnd;
						// System.out.format("[%d] turn to end after infinity\n", myRobot.getTime());
					}
					else
					{
						rTurn = turnToStart;
						// System.out.format("[%d] turn to start after infinity\n", myRobot.getTime());
					}
				}
				else
				{
					if (Math.abs(turnToEnd) > Math.abs(turnToStart))
					{
						rTurn = turnToEnd;
						// System.out.format("[%d] turn to end\n", myRobot.getTime());
					}
					else
					{
						rTurn = turnToStart;
						// System.out.format("[%d] turn to start\n", myRobot.getTime());
					}
				}
				myRobot.setTurnRadarRightRadians(rTurn);
				rDirection = Math.signum(myRobot.getRadarTurnRemainingRadians());
			}
		}

	}

	private void calcBearingDiff()
	{
		if (myScans.isEmpty()) return;

		int len;
		double[] angles = new double[len = myScans.size()];
		int i = 0;
		for (ATarget target : myScans.values())
		{
			angles[i++] = Utils.normalAbsoluteAngle(Math.atan2(target.getAbsX() - myRobot.getX(), target.getAbsY() - myRobot.getY()));
			// System.out.format("[%d] angle=%3.2f %s\n", i-1,Math.toDegrees(angles[i-1]),target.getName());
		}
		Arrays.sort(angles);

		double maxAbs = angles[0] + PI_360 - angles[len - 1];
		int minIndex = len - 1;
		// System.out.format("[%d] abs=%3.2f \n", 0,Math.toDegrees(maxAbs));

		for (i = 1; i < len; i++)
		{
			double absAngle = angles[i] - angles[i - 1];
			// System.out.format("[%d] abs=%3.2f \n", i,Math.toDegrees(absAngle));

			if (absAngle > maxAbs)
			{
				maxAbs = absAngle;
				minIndex = i - 1;
			}
		}

		double delta = Math.toRadians(15);
		widestArea = maxAbs;
		startAngle = Utils.normalAbsoluteAngle(angles[minIndex] + delta);
		endAngle = Utils.normalAbsoluteAngle(startAngle + widestArea - 2 * delta);
		widestArea = PI_360 - widestArea + 2 * delta;
		// widestArea += 2*delta;
		// log(String.format("SKI[%d] start=%3.2f end=%3.2f widest=%3.2f rDirection=%3.2f\n",myRobot.getTime(),Math.toDegrees(startAngle),Math.toDegrees(endAngle),Math.toDegrees(widestArea),rDirection),2);
	}

	private void log(String msg, int level)
	{
		if (isLog && level <= logLevel)
		{
			System.out.format(msg);
		}
	}

}
