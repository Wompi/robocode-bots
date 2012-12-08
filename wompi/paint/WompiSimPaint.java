package wompi.paint;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import robocode.AdvancedRobot;
import robocode.Bullet;
import robocode.Rules;
import robocode.util.Utils;
import wompi.robomath.RobotMath;
import wompi.teststuff.WompiSim;
import wompi.wallaby.PaintHelper;

public class WompiSimPaint
{
	private final static double				DELTA_P		= Math.PI / 36;				// 36 = 5 degree, 90 is cool
	private final static double				PI_360		= Math.PI * 2;
	private final static double				PI			= Math.PI;

	private static Rectangle2D				bField;
	private static final List<WSimPoint>	mySimPoints	= new ArrayList<WSimPoint>();

	public static void init(AdvancedRobot bot)
	{
		bField = new Rectangle2D.Double(17d, 17d, bot.getBattleFieldWidth() - 34d, bot.getBattleFieldHeight() - 34d);
	}

	private static void myDebug(double head, Point2D source, Point2D destination, int direction, double v, double maxv,
			double power, double angle, List<Bullet> bList)
	{
		WompiSim.h = head;
		WompiSim.v = v;
		WompiSim.x = source.getX();
		WompiSim.y = source.getY();

		int i = 0;
		double dist;
		double delta = (direction < 0) ? Math.PI : 0;

		double bSpeed = Rules.getBulletSpeed(power);

		double d1 = Double.MAX_VALUE;
		Point2D pg = RobotMath.calculatePolarPoint(Utils.normalAbsoluteAngle(angle + WompiSim.h), d1, source);

		while ((dist = (++i * bSpeed)) < destination.distance(WompiSim.x, WompiSim.y))
		//while (i++ < 60)
		{
			dist = i * bSpeed;
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

			if (bField.contains(simPoint) && dist < destination.distance(simPoint))
			{
				simPoint.isInField = true;
				WSimPoint.minTick = Math.min(WSimPoint.minTick, simPoint.ticks);
				WSimPoint.maxTick = Math.max(WSimPoint.maxTick, simPoint.ticks);

				if (rect.intersectsLine(line))
				{
					simPoint.isIntersect = true;
				}

				for (Bullet b : bList)
				{
					if (!b.isActive()) continue;
					int j = 0;
					double d2;
					double bulletSpeed = Rules.getBulletSpeed(b.getPower());
					Point2D bulletPos = new Point2D.Double(b.getX(), b.getY());
					while ((d2 = (++j * bulletSpeed)) < bulletPos.distance(simPoint))
					{
						Point2D bP = RobotMath.calculatePolarPoint(b.getHeadingRadians(), d2, bulletPos);
						Line2D bulletLine = new Line2D.Double(bulletPos, bP);
						if (rect.intersectsLine(bulletLine))
						{
							simPoint.isHitByBullet = true;
							break;
						}
					}
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

				myDebug(data.eHeading, data.tPos, data.bPos, 1, data.eVelocity, data.eMaxVelocity, data.bPower, angle,
						data.bBullets);
				myDebug(data.eHeading, data.tPos, data.bPos, -1, data.eVelocity, data.eMaxVelocity, data.bPower, angle,
						data.bBullets);
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
//					int t = Math.abs(point.ticks - ticks);
//					double b = t / maxDeltaTick;

//					Color c = new Color(255, 255, 255, (int) (255 * b));
					Color c = new Color(255, 255, 255, 127);

					if (point.ticks == ticks)
					{
						c = Color.CYAN;
					}
					PaintHelper.drawPoint(point, c, g, 1);
				}

				if (point.isHitByBullet)
				{
					PaintHelper.drawPoint(point, Color.ORANGE, g, 1);
				}
			}
			else
			{
				//	PaintHelper.drawPoint(point, new Color(255, 255, 0, (int) (255 * (1 - Math.min(data.bHeat, 1)))), g, 1);
			}
		}
		mySimPoints.clear();
	}

	public class WSimData
	{
		public double		eDistance;
		public double		bPower;
		public double		eHeading;
		public double		eVelocity;
		public double		eMaxVelocity;
		public double		bHeat;
		public List<Bullet>	bBullets;

		public Point2D		tPos;			// target position
		public Point2D		bPos;			// bot position

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
	boolean				isHitByBullet;
}
