package wompi.numbat.radar;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Collection;

import robocode.AdvancedRobot;
import robocode.RobotStatus;
import robocode.util.Utils;
import wompi.numbat.target.ITargetManager;
import wompi.numbat.target.NumbatTarget;
import wompi.wallaby.PaintHelper;
import wompi.wallaby.WallabyPainter;

public class NumbatRadarMeleeField extends ANumbatRadar
{
	private final static double	PI_360		= Math.PI * 2.0;

	double						widestArea;
	double						startAngle;
	double						endAngle;

	double						rDirection;
	boolean						isRadar;

	// debug
	boolean						isLog		= true;
	int							logLevel	= 2;

	@Override
	void setRadar(RobotStatus status, ITargetManager targetMan)
	{
		// System.out.format("\n");
		calcBearingDiff(status, targetMan);

		double rHead = status.getRadarHeadingRadians();
		double rRemain = status.getRadarTurnRemainingRadians();

		double turnToStart = Utils.normalRelativeAngle(startAngle - rHead);
		double turnToEnd = Utils.normalRelativeAngle(endAngle - rHead);

		// System.out.format("[%d] remain=%3.2f  \n", myRobot.getTime(),myRobot.getRadarTurnRemaining());

		boolean isInfinity = Double.isInfinite(rRemain);
		if (rRemain == 0 || isInfinity)
		{
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
			isRadar = true;
		}
		if (isStartSearch(status)) isRadar = true;
	}

	private void calcBearingDiff(RobotStatus status, ITargetManager targetMan)
	{
		int len;
		Collection<NumbatTarget> allTargets = targetMan.getAllTargets();

		int aliveBots = 0;
		for (NumbatTarget target : allTargets)
		{
			if (target.isAlive) aliveBots++;
		}
		double[] angles = new double[len = aliveBots];

		int i = 0;
		for (NumbatTarget target : allTargets)
		{
			if (target.isAlive)
			{
				angles[i++] = target.getAbsoluteBearing(status);
			}
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

	@Override
	protected void excecute(AdvancedRobot myBot)
	{
		if (isRadar || isStartSearch(myBot.getOthers(), myBot.getTime()))
		{
			super.excecute(myBot);
			rDirection = Math.signum(myBot.getRadarTurnRemainingRadians());
			isRadar = false;
		}
	}

	@Override
	String getName()
	{
		return "Melee Field";
	}

	@Override
	boolean checkActivateRule(RobotStatus status)
	{
		return status.getOthers() > 1;
	}

	@Override
	protected void onPaint(Graphics2D g, RobotStatus status, ITargetManager targetMan)
	{
		calcBearingDiff(status, targetMan);
		Point2D rP = new Point2D.Double(status.getX(), status.getY());
		for (NumbatTarget target : targetMan.getAllTargets())
		{
			double eAbsBearing = Utils.normalAbsoluteAngle(target.getAbsoluteBearing(status));
			double eDistance = rP.distance(target.x, target.y);
			WallabyPainter.drawAngleLine(g, rP, eAbsBearing, eDistance, PaintHelper.whiteTrans);
		}

		WallabyPainter.drawAngleLine(g, rP, startAngle, 1200, Color.YELLOW);
		WallabyPainter.drawAngleLine(g, rP, endAngle, 1200, Color.red);
		PaintHelper.drawArc(rP, 1200, endAngle, widestArea, true, g, PaintHelper.whiteTrans);
	}

}
