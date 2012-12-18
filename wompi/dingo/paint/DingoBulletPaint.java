package wompi.dingo.paint;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import robocode.Bullet;
import robocode.RobotStatus;
import robocode.Rules;
import robocode.StatusEvent;
import wompi.paint.PaintHelper;

public class DingoBulletPaint
{
	private RobotStatus						myStatus;
	private final ArrayList<DingoBullet>	myBullets	= new ArrayList<DingoBullet>();

	public void onStatus(StatusEvent e)
	{
		myStatus = e.getStatus();
	}

	public void onScannedRobot(double power, double x, double y)
	{
		DingoBullet b = new DingoBullet();
		b.myBullet = new Bullet(0, x, y, power, "", "", true, 1);
		b.myStartTime = myStatus.getTime();

		myBullets.add(b);
	}

	public void onPaint(Graphics2D g)
	{
		for (DingoBullet b : myBullets)
		{
			Bullet b1 = b.myBullet;
			// +1 because the enemy fired the bullet 1 turn before we can detect it
			double d = (myStatus.getTime() - b.myStartTime + 1) * Rules.getBulletSpeed(b1.getPower());
			PaintHelper.drawArc(new Point2D.Double(b1.getX(), b1.getY()), d, 0, Math.PI * 2.0, false, g,
					Color.DARK_GRAY);
		}
	}
}

class DingoBullet
{
	public Bullet	myBullet;
	public long		myStartTime;
}
