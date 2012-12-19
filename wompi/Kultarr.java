package wompi;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class Kultarr extends AdvancedRobot
{
	private static final double	INF		= Double.POSITIVE_INFINITY;
	private static final double	PI_90	= Math.PI / 2.0;
	private static final double	PI_180	= Math.PI;

	static double				eEnergy;

	static double				eEscapeAngle;

	static double				eDistance;
	static String				eName;

//	PaintMaxEscapeAngle			myPaintEscAngle;
//	PaintEnemyMaxEscapeAngle	myPaintEnemyEscAngle;

	double						dir		= 1;
	double						bPower;

//	double						decay	= 0.5;

	public Kultarr()
	{
//		myPaintEscAngle = new PaintMaxEscapeAngle();
//		myPaintEnemyEscAngle = new PaintEnemyMaxEscapeAngle();
	}

	@Override
	public void run()
	{
//		setAllColors(Color.orange);
		setTurnRadarRight(eDistance = INF);
//		myPaintEscAngle.onInit(this, 18.0);
//		myPaintEnemyEscAngle.onInit(this, 18.0);
	}

//	@Override
//	public void onStatus(StatusEvent e)
//	{
//		myPaintEscAngle.onStatus(e);
//		myPaintEnemyEscAngle.onStatus(e);
//	}
//
	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		if (e.getDistance() < eDistance || eName == e.getName())
		{
			eDistance = e.getDistance();
			eName = e.getName();

			//if (e.getDistance() < 300) 
			setTurnRadarLeft(getRadarTurnRemaining());

			double eDiff = eEnergy - e.getEnergy();

//			myPaintEscAngle.onScannedRobot(e);
//			myPaintEscAngle.setBulletSpeed(bPower);
//			myPaintEscAngle.onPaint(getGraphics());

			double absBearing = getHeadingRadians() + e.getBearingRadians();

			//double a1 = Math.sin(eEscapeAngle) * e.getDistance();

			// 180 = 90 + eA + x;

			// 200/100 = 500/x;
			double adjust = 200 / e.getDistance();

			// 90 - r = x;

			double r = PI_90 - eEscapeAngle;

			r = PI_90 * adjust;

//			myPaintEnemyEscAngle.onScannedRobot(e);

			if (eDiff != 0)
			{
//				myPaintEnemyEscAngle.setBulletSpeed(eDiff);
				//eEscapeAngle = Math.asin(Rules.MAX_VELOCITY / Rules.getBulletSpeed(eDiff));
				if (Math.abs(getVelocity()) == 8)
				{
					dir = -dir;
				}
			}
//			myPaintEnemyEscAngle.onPaint(getGraphics());

			bPower = Math.min(2.99, adjust);

			setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing
					- getGunHeadingRadians()
					+ (e.getVelocity() * Math.sin(e.getHeadingRadians() - absBearing) / Rules
							.getBulletSpeed(bPower * 0.7))));
			setFire(bPower);

			setTurnRightRadians(Math.tan(absBearing = Utils.normalRelativeAngle(absBearing + r * dir)
					- getHeadingRadians()));
			setAhead(1000 * Math.cos(absBearing));

			eEnergy = e.getEnergy();

		}
		clearAllEvents();
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
