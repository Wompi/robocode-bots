package wompi.dingo;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import robocode.AdvancedRobot;
import robocode.RobotStatus;
import robocode.Rules;
import robocode.util.Utils;
import wompi.Dingo;
import wompi.robomath.RobotMath;
import wompi.wallaby.PaintHelper;

public class DingoPaint
{
	private static final double	DELTA_P	= Math.PI / 36.0;	// 36 = 5 degree 

	private final AdvancedRobot	myBot;

	private Point2D				mPoint;
	private RobotStatus			myStatus;
	private RobotStatus			moveStartStatus;

	public DingoPaint(AdvancedRobot bot)
	{
		myBot = bot;
	}

	public void onPaint(Graphics2D g)
	{
		if (mPoint != null)
		{
			PaintHelper.drawArc(getMoveStartPoint(), Dingo.DIST, 0, Dingo.PI_360, false, g, Color.GRAY);
			currentMoveSim(g);
			currentMovePoint(g);
		}
	}

	public void registerStatus(RobotStatus status)
	{
		myStatus = status;
	}

	public void registerMovePoint(double angle, double dist)
	{
		moveStartStatus = myStatus;
		mPoint = RobotMath.calculatePolarPoint(angle, dist, getMoveStartPoint());

	}

	private void currentMovePoint(Graphics2D g)
	{
		if (mPoint != null)
		{
			PaintHelper.drawLine(getMoveStartPoint(), mPoint, g, Color.BLUE);
		}
	}

	private void currentMoveSim(Graphics2D g)
	{
		if (mPoint == null) return;
		double a = 0;
		double maxv = Rules.MAX_VELOCITY;
		while ((a += DELTA_P) <= Dingo.PI_360)
		{
			double h = moveStartStatus.getHeadingRadians();
			double v = moveStartStatus.getVelocity();
			double x = getMoveStartPoint().getX();
			double y = getMoveStartPoint().getY();

			Point2D pg = RobotMath.calculatePolarPoint(Utils.normalAbsoluteAngle(a + h), Dingo.DIST,
					getMoveStartPoint());

			double d = 1;
			int i = 0;
			do
			{
				if (v * d < 0) d *= 2;
				if (((d = v + d) * v) < 0) d /= 2.0;

				double att = Math.atan2(pg.getX() - x, pg.getY() - y) - h;
				double cTurn = limit(Rules.getTurnRateRadians(v), Utils.normalRelativeAngle(att));
				h = Utils.normalNearAbsoluteAngle(h + cTurn);
				v = limit(maxv, d);

				x += Math.sin(h) * v;
				y += Math.cos(h) * v;

				if (getMoveStartPoint().distance(x, y) > Dingo.DIST) break;

				PaintHelper.drawPoint(new Point2D.Double(x, y), Color.RED, g, 1);
			}
			while (true);

		}

	}

	// helper methods
	private double limit(double minmax, double value)
	{
		return Math.max(-minmax, Math.min(value, minmax));
	}

	private Point2D getMoveStartPoint()
	{
		return new Point2D.Double(moveStartStatus.getX(), moveStartStatus.getY());
	}
}
