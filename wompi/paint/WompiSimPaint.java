package wompi.paint;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import robocode.AdvancedRobot;
import robocode.Rules;
import wompi.robomath.RobotMath;
import wompi.teststuff.WompiSim;
import wompi.wallaby.PaintHelper;

public class WompiSimPaint
{
	private final static double				DELTA_P		= Math.PI / 90;				// 5 degree
	private final static double				PI_360		= Math.PI * 2;

	private static Rectangle2D				bField;
	private static final List<WSimPoint>	mySimPoints	= new ArrayList<WSimPoint>();

	public static void init(AdvancedRobot bot)
	{
		bField = new Rectangle2D.Double(17d, 17d, bot.getBattleFieldWidth() - 34d, bot.getBattleFieldHeight() - 34d);
	}

	private static void myDebug(double head, Point2D source, Point2D destination, int direction, double v, double maxv,
			double power, double angle)
	{
		WompiSim.h = head;
		WompiSim.v = v;
		WompiSim.x = source.getX();
		WompiSim.y = source.getY();

		int i = 0;
		double dist;
		double delta = (direction < 0) ? Math.PI : 0;

		double bSpeed = Rules.getBulletSpeed(power);

		while ((dist = (++i * bSpeed)) < destination.distance(WompiSim.x, WompiSim.y))
		{

			Point2D pg = RobotMath.calculatePolarPoint(angle, dist, source);
			WompiSim.simulate(Math.atan2(pg.getX() - WompiSim.x, pg.getY() - WompiSim.y) - WompiSim.h + delta,
					direction, maxv);

			WSimPoint simPoint = new WSimPoint();
			simPoint.x = WompiSim.x;
			simPoint.y = WompiSim.y;
			simPoint.direction = direction;
			simPoint.ticks = (int) (simPoint.distance(destination) / bSpeed);

			Rectangle2D rect = new Rectangle2D.Double(WompiSim.x - 18.0, WompiSim.y - 18.0, 36.0, 36.0);

			double tangle = RobotMath.calculateAngle(destination, simPoint);
			Point2D tPoint = RobotMath.calculatePolarPoint(tangle, dist, destination);
			Line2D line = new Line2D.Double(destination, tPoint);

			if (bField.contains(simPoint))
			{
				simPoint.isInField = true;
				WSimPoint.minTick = Math.min(WSimPoint.minTick, simPoint.ticks);
				WSimPoint.maxTick = Math.max(WSimPoint.maxTick, simPoint.ticks);

				if (rect.intersectsLine(line))
				{
					simPoint.isIntersect = true;
				}
			}
			mySimPoints.add(simPoint);
		}
	}

	public static void onPaint(Graphics2D g, WSimData data)
	{
		int ticks = (int) (data.eDistance / Rules.getBulletSpeed(data.bPower));

		if (ticks > 0)
		{
			double angle = 0;
			WSimPoint.minTick = 1000;
			WSimPoint.maxTick = 0;

			while ((angle += DELTA_P) <= PI_360)
			{
//				Point2D pg = RobotMath.calculatePolarPoint(angle, 100, myTarget);
//				PaintHelper.drawPoint(pg, Color.YELLOW, g, 1);
//				if (Utils.isNear(angle, PI_360)) PaintHelper.drawLine(myTarget, pg, g, Color.YELLOW);

				myDebug(data.eHeading, data.tPos, data.bPos, 1, data.eVelocity, data.eMaxVelocity, data.bPower, angle);
				myDebug(data.eHeading, data.tPos, data.bPos, -1, data.eVelocity, data.eMaxVelocity, data.bPower, angle);
//				myDebug(getHeadingRadians(), myPosition, myTarget, 1, getVelocity(), 8.0, myTarget.ePower,
//						angle, myWompiFrontPoints);
//				myDebug(getHeadingRadians(), myPosition, myTarget, -1, getVelocity(), 8.0, myTarget.ePower,
//						angle, myWompiBackPoints);

			}
			System.out.format("minTick=%d tick=%d maxTick=%d speed=%3.2f\n", WSimPoint.minTick, ticks,
					WSimPoint.maxTick, Rules.getBulletSpeed(data.bPower));
		}

		int maxDeltaTick = Math.max(Math.abs(WSimPoint.minTick - ticks), Math.abs(WSimPoint.maxTick - ticks));

		for (WSimPoint point : mySimPoints)
		{
			if (point.isInField)
			{
				if (point.isIntersect)
				{
					if (point.direction > 0)
						PaintHelper.drawPoint(point, Color.RED, g, 1);
					else
						PaintHelper.drawPoint(point, Color.GREEN, g, 1);
				}
				else
				{
					int t = Math.abs(point.ticks - ticks);
					double b = t / maxDeltaTick;

					Color c = new Color(255, 255, 255, (int) (255 * b));

//					if (point.ticks == ticks)
//					{
//						c = Color.CYAN;
//					}
					PaintHelper.drawPoint(point, c, g, 1);
				}
			}
		}
		mySimPoints.clear();
	}

	public class WSimData
	{
		public double	eDistance;
		public double	bPower;
		public double	eHeading;
		public double	eVelocity;
		public double	eMaxVelocity;

		public Point2D	tPos;			// target position
		public Point2D	bPos;			// bot position

	}

}

class WSimPoint extends Point2D.Double
{
	public static int	minTick;
	public static int	maxTick;
	int					direction;
	int					ticks;
	boolean				isInField;
	boolean				isIntersect;
}
