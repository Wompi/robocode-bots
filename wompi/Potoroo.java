package wompi;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import robocode.AdvancedRobot;
import robocode.HitByBulletEvent;
import robocode.HitWallEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;
import wompi.paint.PaintHelper;
import wompi.robomath.RobotMath;

public class Potoroo extends AdvancedRobot
{
	static double						DIR;
	static ArrayList<PotarooWave>		myWaves	= new ArrayList<PotarooWave>();
	static ArrayList<PotarooEnemyWave>	myEnemyWaves;
	static double						eEnergy;

	static double						forceX;
	static double						forceY;

	@Override
	public void run()
	{
		myEnemyWaves = new ArrayList<PotarooEnemyWave>();
		setAllColors(Color.orange);
		DIR = 1;

		eEnergy = getEnergy();
		setAdjustRadarForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setAdjustGunForRobotTurn(true);

		while (true)
		{
			if (getRadarTurnRemaining() == 0.0) setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
			execute();
		}
	}

	@Override
	public void onHitWall(HitWallEvent e)
	{
		DIR = -DIR;
	}

	private double limit(double min, double value, double max)
	{
		return Math.max(min, Math.min(value, max));
	}

	@Override
	public void onHitByBullet(HitByBulletEvent e)
	{
		//DIR = -DIR;
	}

	@Override
	public void onPaint(Graphics2D g)
	{
		PotarooEnemyWave nearestWave = null;
		try
		{
			int i = myEnemyWaves.size();
			while (true)
			{
				PotarooEnemyWave eWave = myEnemyWaves.get(--i);
				if (!eWave.isDead)
				{
					Point2D guessZero = RobotMath.calculatePolarPoint(eWave.eAbsBearing, eWave.eRadius, eWave);

					PaintHelper.drawLine(eWave, guessZero, g, Color.DARK_GRAY);
					PaintHelper.drawArc(eWave, eWave.eRadius, 0, Math.PI * 2.0, false, g, Color.DARK_GRAY);
					nearestWave = eWave;
				}
				else
				{
					break;
				}
			}
		}
		catch (Exception e2)
		{}
		if (nearestWave != null)
			PaintHelper.drawArc(nearestWave, nearestWave.eRadius, 0, Math.PI * 2.0, false, g, Color.GREEN);

	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		PotarooWave w;
		double DELTA = (Math.PI / 9.0) * DIR;

		double fHalf = Math.min(getBattleFieldHeight() / 2.0, getBattleFieldWidth() / 2.0);
		double RADIUS = limit(fHalf - 50.0, e.getDistance() - 40, fHalf - 50.0);
		double absBearing = getHeadingRadians() + e.getBearingRadians();
		double eAbsBearing = absBearing - Math.PI + DELTA;
		double eX = getX() + Math.sin(absBearing) * e.getDistance();
		double eY = getY() + Math.cos(absBearing) * e.getDistance();

		double _eX = limit(RADIUS + 19.0, eX, getBattleFieldWidth() - RADIUS - 19.0);
		double _eY = limit(RADIUS + 19.0, eY, getBattleFieldHeight() - RADIUS - 19.0);

		double mX = _eX + Math.sin(eAbsBearing) * RADIUS;
		double mY = _eY + Math.cos(eAbsBearing) * RADIUS;

		double eAdvVelocity = e.getVelocity() * Math.cos(e.getHeadingRadians() - absBearing);
		double eLatVelocity = e.getVelocity() * Math.sin(e.getHeadingRadians() - absBearing);

		// Collect enemy wave 
		double energy = getEnergy();
		double eDelta = eEnergy - (eEnergy = e.getEnergy());
		if (eDelta >= 0.08 && eDelta <= 3)
		{
			PotarooEnemyWave eWave = new PotarooEnemyWave();
			eWave.x = eX;
			eWave.y = eY;
			eWave.eAbsBearing = eAbsBearing;
			eWave.eBulletSpeed = Rules.getBulletSpeed(eDelta);
			myEnemyWaves.add(eWave);
		}

		PotarooEnemyWave nearest = null;
		try
		{
			int i = myEnemyWaves.size();
			while (true)
			{
				PotarooEnemyWave eWave = myEnemyWaves.get(--i);
				if (eWave.test(getTime(), getX(), getY()))
				{
					break;
				}
				nearest = eWave;
			}
		}
		catch (Exception e2)
		{}

		PaintHelper.drawArc(new Point2D.Double(_eX, _eY), RADIUS, 0.0, Math.PI * 2.0, false, getGraphics(),
				Color.DARK_GRAY);

		Point2D end = new Point2D.Double(mX, mY);
		PaintHelper.drawLine(new Point2D.Double(getX(), getY()), end, getGraphics(), Color.RED);

		double angle = eAbsBearing - getHeadingRadians();
		setTurnRightRadians(Math.cos(angle));
		//setAhead(Math.cos(angle) * 1000);

		setAhead(1000 * DIR);

		double bPower = 0.045 * energy * (1.0 - e.getDistance() / 1400.0);
		//double bPower = Math.min(3.0, Math.min(e.getEnergy() / 4.0, 550 / e.getDistance()));
		if (getGunTurnRemaining() == 0)
		{
			if (getEnergy() + 0.1 > bPower && setFireBullet(bPower) != null)
			{
				w = new PotarooWave();
				w.wTime = getTime();
				w.wLatVelocity = eLatVelocity;
				w.wAdvVelocity = eAdvVelocity;
				w.x = getX();
				w.y = getY();
				w.wBearing = absBearing;
				w.wDistance = e.getDistance();
				w.wBulletSpeed = Rules.getBulletSpeed(bPower);
				myWaves.add(w);
			}
		}

		int i = 0;
		double eDist;
		double aim = 0;
		try
		{
			double maxMatch = Double.MAX_VALUE;
			while (true)
			{
				w = myWaves.get(i++);
				if (!w.isCollected)
				{
					if ((getTime() - w.wTime) * w.wBulletSpeed >= w.distance(eX, eY))
					{
						w.wAngle = Utils.normalRelativeAngle(Math.atan2(eX - w.x, eY - w.y) - w.wBearing);
						w.isCollected = true;
					}
					else
						continue;
				}

				eDist = 2 * Math.pow(eLatVelocity - w.wLatVelocity, 2) /*+ Math.pow(eAdvVelocity - w.wAdvVelocity, 2)*/
						+ Math.pow((w.wDistance - e.getDistance()) / 100, 2);
				if (eDist < maxMatch)
				{
					maxMatch = eDist;
					aim = w.wAngle;
				}

			}
		}
		catch (IndexOutOfBoundsException e0)
		{}
		setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing - getGunHeadingRadians()) + aim);
		setTurnRadarRightRadians(Utils.normalRelativeAngle(absBearing - getRadarHeadingRadians()) * 1.9);
	}

	public class PotarooWave extends Point2D.Double
	{
		private static final long	serialVersionUID	= 1L;
		double						wBearing;
		double						wLatVelocity;
		double						wAdvVelocity;
		double						wAngle;
		double						wDistance;
		double						wTime;
		double						wBulletSpeed;

		boolean						isCollected;

	}

	public class PotarooEnemyWave extends Point2D.Double
	{
		double	eAbsBearing;
		double	eBulletSpeed;
		double	eRadius;
		int		tCount;

		// debug
		boolean	isDead;

		public boolean test(long time, double x, double y)
		{
			eRadius = ++tCount * eBulletSpeed;
			return isDead = (eRadius >= this.distance(x, y));
		}
	}

}
