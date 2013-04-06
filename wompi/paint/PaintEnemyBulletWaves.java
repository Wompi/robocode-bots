package wompi.paint;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import robocode.Bullet;
import robocode.RobotStatus;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.util.Utils;

public class PaintEnemyBulletWaves
{
	private RobotStatus						myStatus;
	private final ArrayList<_BulletWave>	myBullets	= new ArrayList<_BulletWave>();

	public void onStatus(StatusEvent e)
	{
		myStatus = e.getStatus();
	}

	public void onScannedRobot(ScannedRobotEvent e, double power)
	{
		if (Utils.isNear(0.0, power)) return;

		double x = myStatus.getX() + Math.sin(myStatus.getHeadingRadians() + e.getBearingRadians()) * e.getDistance();
		double y = myStatus.getY() + Math.cos(myStatus.getHeadingRadians() + e.getBearingRadians()) * e.getDistance();

		_BulletWave b = new _BulletWave();
		b.myBullet = new Bullet(0, x, y, power, "", "", true, 1);
		b.myStartTime = myStatus.getTime();

		myBullets.add(b);
	}

	public void onPaint(Graphics2D g)
	{
		for (_BulletWave b : myBullets)
		{
			Bullet b1 = b.myBullet;
			// +1 because the enemy fired the bullet 1 turn before we can detect it
			double d = (myStatus.getTime() - b.myStartTime + 1) * Rules.getBulletSpeed(b1.getPower());
			PaintHelper.drawArc(new Point2D.Double(b1.getX(), b1.getY()), d, 0, Math.PI * 2.0, false, g,
					Color.DARK_GRAY);
		}
	}
}

class _BulletWave
{
	public Bullet	myBullet;
	public long		myStartTime;
}
