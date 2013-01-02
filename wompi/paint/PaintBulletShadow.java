package wompi.paint;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import robocode.AdvancedRobot;
import robocode.Bullet;
import robocode.RobotStatus;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import wompi.paint.helper.EnemyStatus;
import wompi.robomath.RobotMath;
import wompi.robomath.WallHeadingDistance;

public class PaintBulletShadow
{
	private RobotStatus					myStatus;
	private final HashMap<Long, Bullet>	myBullets;
	private EnemyStatus					myRobotStatus;
	private final WallHeadingDistance	wallDistance;

	public PaintBulletShadow()
	{
		myBullets = new HashMap<Long, Bullet>();
		wallDistance = new WallHeadingDistance();
	}

	public void onInit(AdvancedRobot bot, double fieldBorder)
	{
		wallDistance.onInit(bot, fieldBorder);
	}

	public void setBullet(Bullet b)
	{
		myBullets.put(PaintFunctions.getAbsoutTime(myStatus), b);
	}

	public void onStatus(StatusEvent e)
	{
		myStatus = e.getStatus();
	}

	public void onScannedRobot(ScannedRobotEvent e)
	{
		myRobotStatus = new EnemyStatus(e, myStatus);
	}

	public void onPaint(Graphics2D g)
	{
		Iterator<Entry<Long, Bullet>> ite = myBullets.entrySet().iterator();
		while (ite.hasNext())
		{
			Entry<Long, Bullet> entry = ite.next();
			Bullet b = entry.getValue();
			if (!b.isActive())
			{
				ite.remove();
				continue;
			}

			double bSpeed = b.getVelocity();

			wallDistance.setStartPoint(myRobotStatus.getX(), myRobotStatus.getY());

			Point2D bulletPos = new Point2D.Double(b.getX(), b.getY());
			Point2D bulletEnd = RobotMath.calculatePolarPoint(b.getHeadingRadians() + Math.PI, bSpeed, bulletPos);
			PaintHelper.drawLine(bulletPos, bulletEnd, g, Color.DARK_GRAY);

			double angle0 = RobotMath.calculateAngle(wallDistance.getStartPoint(), bulletPos);
			double angle1 = RobotMath.calculateAngle(wallDistance.getStartPoint(), bulletEnd);
			wallDistance.setHeading(angle0);
			Point2D w0 = RobotMath.calculatePolarPoint(angle0, wallDistance.getForwardDistance(),
					wallDistance.getStartPoint());
			wallDistance.setHeading(angle1);

			Point2D w1 = RobotMath.calculatePolarPoint(angle1, wallDistance.getForwardDistance(),
					wallDistance.getStartPoint());

			PaintHelper.drawLine(wallDistance.getStartPoint(), w0, g, Color.DARK_GRAY);
			PaintHelper.drawLine(wallDistance.getStartPoint(), w1, g, Color.DARK_GRAY);
		}

	}
}
