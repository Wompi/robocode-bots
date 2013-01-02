package wompi;

import java.awt.Color;
import java.awt.Graphics2D;

import robocode.AdvancedRobot;
import robocode.Bullet;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.util.Utils;
import wompi.paint.PaintBulletShadow;
import wompi.paint.PaintEnemyMaxEscapeAngle;
import wompi.paint.PaintMaxEscapeAngle;

public class Kultarr extends AdvancedRobot
{
	private static final double	INF		= Double.POSITIVE_INFINITY;
	private static final double	PI_90	= Math.PI / 2.0;
	private static final double	PI_180	= Math.PI;

	static double				eEnergy;

	static double				eEscapeAngle;

	static double				eDistance;
	static String				eName;

	PaintMaxEscapeAngle			myPaintEscAngle;
	PaintEnemyMaxEscapeAngle	myPaintEnemyEscAngle;
	PaintBulletShadow			myPaintBulletShadow;

	double						dir		= 1;
	double						bPower;

	public Kultarr()
	{
		myPaintEscAngle = new PaintMaxEscapeAngle();
		myPaintEnemyEscAngle = new PaintEnemyMaxEscapeAngle();
		myPaintBulletShadow = new PaintBulletShadow();
	}

	@Override
	public void run()
	{
		setAllColors(Color.orange);
		setTurnRadarRight(eDistance = INF);
		myPaintEscAngle.onInit(this, 18.0);
		myPaintEnemyEscAngle.onInit(this, 18.0);
		myPaintBulletShadow.onInit(this, 18.0);
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		myPaintEscAngle.onStatus(e);
		myPaintEnemyEscAngle.onStatus(e);
		myPaintBulletShadow.onStatus(e);
	}

	@Override
	public void onPaint(Graphics2D g)
	{
		myPaintBulletShadow.onPaint(g);
		myPaintEscAngle.onPaint(getGraphics());

	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		double absBearing = getHeadingRadians() + e.getBearingRadians();

		if (e.getDistance() < eDistance || eName == e.getName())
		{
			eDistance = e.getDistance();
			eName = e.getName();

			//if (getGunHeat() < 1.0) 
			setTurnRadarLeft(getRadarTurnRemaining());

			double eDiff = eEnergy - e.getEnergy();

			myPaintBulletShadow.onScannedRobot(e);
			myPaintEscAngle.onScannedRobot(e);
			myPaintEscAngle.setBulletSpeed(bPower);

			//double a1 = Math.sin(eEscapeAngle) * e.getDistance();

			// 180 = 90 + eA + x;

			// 200/100 = 500/x;
			double adjust = 200 / e.getDistance();

			// 90 - r = x;

			double r = PI_90 - eEscapeAngle;

			r = r + (PI_90 - r) * adjust;

			myPaintEnemyEscAngle.onScannedRobot(e);

			if (eDiff < 0)
			{
				myPaintEnemyEscAngle.setBulletSpeed(eDiff);
				eEscapeAngle = Math.asin(Rules.MAX_VELOCITY / Rules.getBulletSpeed(eDiff));

				if (getTurnRemaining() == 0.0 || e.getDistance() < 300)
				{
					dir = -dir;

				}
			}

			myPaintEnemyEscAngle.onPaint(getGraphics());

			if (Math.abs(getGunTurnRemaining()) < 1.0)
			{
				Bullet b = setFireBullet(bPower);
				if (b != null) myPaintBulletShadow.setBullet(b);
			}

			bPower = Math.min(2.99, 350 / e.getDistance());

			double delta = Math.signum(e.getVelocity()) * Math.asin(6.0 / Rules.getBulletSpeed(bPower));
			setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing - delta - getGunHeadingRadians()));

//			setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing - getGunHeadingRadians()
//					+ (e.getVelocity() * Math.sin(e.getHeadingRadians() - absBearing) / Rules.getBulletSpeed(bPower))));

			setTurnRightRadians(Math.tan(absBearing = Utils.normalRelativeAngle(absBearing + r * dir)
					- getHeadingRadians()));
			setAhead(1000 * Math.cos(absBearing));

			eEnergy = e.getEnergy();
		}
		//clearAllEvents();
	}

	@Override
	public void onHitWall(HitWallEvent event)
	{
		dir = -dir;
	}

	@Override
	public void onRobotDeath(RobotDeathEvent e)
	{
		eDistance = INF;
	}

	@Override
	public void onBulletHit(BulletHitEvent e)
	{
		eEnergy -= Rules.getBulletDamage(e.getBullet().getPower());
	}

	@Override
	public void onHitByBullet(HitByBulletEvent e)
	{
		eEnergy += Rules.getBulletHitBonus(e.getPower());
	}
}
