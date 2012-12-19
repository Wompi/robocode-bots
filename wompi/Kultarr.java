package wompi;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.HashMap;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.util.Utils;
import wompi.paint.PaintEnemyMaxEscapeAngle;
import wompi.paint.PaintHelper;
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

	double						dir		= 1;
	double						bPower;

	HashMap<String, double[]>	map		= new HashMap<String, double[]>();

	public Kultarr()
	{
		myPaintEscAngle = new PaintMaxEscapeAngle();
		myPaintEnemyEscAngle = new PaintEnemyMaxEscapeAngle();
	}

	@Override
	public void run()
	{
		setAllColors(Color.orange);
		setTurnRadarRight(eDistance = INF);
		myPaintEscAngle.onInit(this, 18.0);
		myPaintEnemyEscAngle.onInit(this, 18.0);
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		myPaintEscAngle.onStatus(e);
		myPaintEnemyEscAngle.onStatus(e);

//		double test;
//		setTurnRightRadians(Math.tan(test = RobotMath.calculateAngle(new Point2D.Double(getX(), getY()),
//				new Point2D.Double(x / count, y / count))) - getHeadingRadians());
//		setAhead(1000 * Math.cos(test));

	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		double absBearing = getHeadingRadians() + e.getBearingRadians();

		map.put(e.getName(), new double[]
		{ Math.sin(absBearing) * e.getDistance(), Math.cos(absBearing) * e.getDistance() });

		if (e.getDistance() < eDistance || eName == e.getName())
		{
			eDistance = e.getDistance();
			eName = e.getName();

			if (getGunHeat() < 1.0) setTurnRadarLeft(getRadarTurnRemaining());

			double eDiff = eEnergy - e.getEnergy();

			myPaintEscAngle.onScannedRobot(e);
			myPaintEscAngle.setBulletSpeed(bPower);
			myPaintEscAngle.onPaint(getGraphics());

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

			if (Math.abs(getGunTurnRemaining()) < 1.0) setFire(bPower);
			bPower = Math.min(2.99, 350 / e.getDistance());

			double delta = Math.signum(e.getVelocity()) * Math.asin(6.0 / Rules.getBulletSpeed(bPower));
			setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing - delta - getGunHeadingRadians()));

//			setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing - getGunHeadingRadians()
//					+ (e.getVelocity() * Math.sin(e.getHeadingRadians() - absBearing) / Rules.getBulletSpeed(bPower))));

//			setTurnRightRadians(Math.tan(absBearing = Utils.normalRelativeAngle(absBearing + r * dir)
//					- getHeadingRadians()));
//			setAhead(1000 * Math.cos(absBearing));

			double _x = 0;
			double _y = 0;

			double a;
			for (double[] b : map.values())
			{
				_x += b[0];
				_y += b[1];
				System.out.format("map %d  x=%3.5f y=%3.5f \n", map.size(), b[0], b[1]);
			}
			_x = (_x) / map.size();
			_y = (_y) / map.size();
			System.out.format(" \n");

			double _xa = Math.abs(_x + getX() - getBattleFieldWidth());
			double _ya = Math.abs(_y + getY() - getBattleFieldHeight());

//			if (_xa < getBattleFieldWidth() / 2.0)
//				_xa *= 0.3;
//			else
//				_xa = _xa + (getBattleFieldWidth() - _xa) * 0.7;
//
//			if (_ya < getBattleFieldHeight() / 2.0)
//				_ya *= 0.3;
//			else
//				_ya = _ya + (getBattleFieldHeight() - _ya) * 0.7;

			PaintHelper.drawPoint(new Point2D.Double(_x + getX(), _y + getY()), Color.YELLOW, getGraphics(), 10);
			PaintHelper.drawPoint(new Point2D.Double(_xa, _ya), Color.RED, getGraphics(), 10);

			setTurnRightRadians(Math.tan(a = Math.atan2(_xa - getX(), _ya - getY()) - getHeadingRadians()));
			setAhead(Math.hypot(_xa - getX(), _ya - getY()) * Math.cos(a) * 1000);

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
		map.remove(e.getName());
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
