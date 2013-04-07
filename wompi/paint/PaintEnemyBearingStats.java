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
import wompi.robomath.RobotMath;

// This class collects all bearing offsets which the target has if i shot at him
// This can be used to gather some statistics and hopefully find a pattern to minimize
// this kind of gun.
//
// Bearing offset can be averaged or segmented - lets see how it works

public class PaintEnemyBearingStats
{
	private RobotStatus					myStatus;

	public static ArrayList<BulletWave>	myBulletBearings	= new ArrayList<BulletWave>();

	public void onStatus(StatusEvent e)
	{
		myStatus = e.getStatus();

		// this will advance the new bullets by 1 tick and should be right 
		for (BulletWave wave : myBulletBearings)
		{
			wave.advanceBullet();
		}

	}

	public void onScannedRobot(ScannedRobotEvent e, Bullet myBullet, double bulletPower)
	{
		double latv = RobotMath.calculateLateralVelocity(myStatus.getHeadingRadians(), e.getBearingRadians(),
				e.getHeadingRadians(), e.getVelocity());

		BulletWave _wave = new BulletWave();
		_wave.shootX = myStatus.getX();
		_wave.shootY = myStatus.getY();
		_wave.shootLatv = latv;
		_wave.shootDistance = e.getDistance();
		_wave.shootAbsBearing = e.getBearingRadians() + myStatus.getHeadingRadians();
		_wave.bPower = bulletPower;
		_wave.isBulletWave = (myBullet != null);
		myBulletBearings.add(_wave);

		for (BulletWave wave : myBulletBearings)
		{
			wave.check(myStatus, e);
		}

	}

	public void onPaint(Graphics2D g)
	{
		for (BulletWave wave : myBulletBearings)
		{
			if (!wave.isClosed && wave.isBulletWave)
			{
				Point2D start = new Point2D.Double(wave.shootX, wave.shootY);
				PaintHelper.drawArc(start, wave.bDist, 0, Math.PI * 2, false, g, Color.DARK_GRAY);
			}
		}
	}
}

class BulletWave
{
	// shoot point related
	public double	shootAbsBearing;
	public double	shootX;
	public double	shootY;
	public boolean	isBulletWave;

	// bullet related
	public double	bDist;
	public double	bPower;

	// result
	public double	enemyBearing;
	public boolean	isClosed;

	// statisics
	public double	shootLatv;
	public double	shootDistance;

	public BulletWave()
	{

	}

	public void advanceBullet()
	{
		if (isClosed) return;
		bDist += Rules.getBulletSpeed(bPower);
	}

	public void check(RobotStatus eStatus, ScannedRobotEvent eScan)
	{
		if (isClosed) return;

		double absBearing = eScan.getBearingRadians() + eStatus.getHeadingRadians();

		double ex = (eStatus.getX() + eScan.getDistance() * Math.sin(absBearing)) - shootX;
		double ey = (eStatus.getY() + eScan.getDistance() * Math.cos(absBearing)) - shootY;

		if (bDist > Math.hypot(ex, ey))
		{
			enemyBearing = Math.atan2(ex, ey) - shootAbsBearing;
			if (isBulletWave)
				System.out.format("[%04d] bearing=%3.5f %s latv(%3d) dist(%3d)\n", eStatus.getTime(),
						Math.toDegrees(Utils.normalRelativeAngle(enemyBearing)), isBulletWave ? "BULLET" : "",
						(int) shootLatv, (int) shootDistance);
			isClosed = true;
		}
	}
}
