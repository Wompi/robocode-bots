package wompi;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.DeathEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.util.Utils;
import wompi.echidna.misc.painter.PaintMinRiskPoints;

/**
 * original: 5 + 742 v1.0: 670
 * 
 * 
 * @author rschott
 * 
 */
public class Dunnart extends AdvancedRobot
{
	final static int				DODGE_HOT			= 0;
	final static int				DODGE_LINEAR		= 1;
	final static int				DODGE_RANDOM		= 2;
	final static int				DODGE_RAMBOT		= 3;

	private static final double		FIELD_W				= 800.0;
	private static final double		FIELD_H				= 600.0;

	private static final double		WZ					= 18.0;
	private static final double		WZ_W				= FIELD_W - 2 * WZ;
	private static final double		WZ_H				= FIELD_H - 2 * WZ;
	final static double				DELTA_RISK_ANGLE	= Math.PI / 32.0;
	final static double				PI_360				= Math.PI * 2.0;
	final static double				PI_180				= Math.PI;
	final static double				PI_90				= Math.PI / 2.0;
	final static double				PI_45				= Math.PI / 4.0;
	final static double				PI_80				= Math.PI * 8.0 / 18.0;

	final static double				RAM_DISTANCE		= 150;
	final static double				INF					= Double.POSITIVE_INFINITY;
	final static double				BULLET_POWER		= 2;
	final static double				BULLET_SPEED		= 20 - 3 * BULLET_POWER;
	final static double				DIR_CHANCE			= 0.375;
	final static double				APPROACH			= 600;
	static double					eEnergy;
	static int						dodgeMode;
	static ArrayList<DunnartWave>	myWaves;

	static double					myCombatDist;
	static double					myMaxVelocity;
	static int						myDirection;

	// debug
	long							colorChange;
	long							speedUp;
	PaintMinRiskPoints				myPaintRisk;

	public Dunnart()
	{
		myWaves = new ArrayList<DunnartWave>();
		myDirection = 1;
		myPaintRisk = new PaintMinRiskPoints();
		dodgeMode = DODGE_HOT;
	}

	@Override
	public void onPaint(Graphics2D g)
	{
		myPaintRisk.onPaint(g, true);
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		setAdjustGunForRobotTurn(true);
		//setAdjustRadarForGunTurn(true);
		if (colorChange-- <= 0) setAllColors(Color.GREEN);

		speedUp--;
	}

	@Override
	public void run()
	{
		setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		DunnartWave w;
		double absBearing = getHeadingRadians() + e.getBearingRadians();
		double eDistance = e.getDistance();

		double adSeg = e.getVelocity() * Math.cos(e.getHeadingRadians() - absBearing);
		double velSeg = e.getVelocity() * Math.sin(e.getHeadingRadians() - absBearing);

		double xe = getX() + Math.sin(absBearing) * eDistance;
		double ye = getY() + Math.cos(absBearing) * eDistance;
		double xf = 0;
		double yf = 0;

		int i = 0;
		double aim = 0;
		double maxMatch = INF;

		if (eDistance < RAM_DISTANCE)
		{
			dodgeMode = DODGE_RAMBOT;
		}

		switch (dodgeMode)
		{
			case DODGE_LINEAR:
				myCombatDist = 200;
				if ((adSeg = eEnergy - (eEnergy = e.getEnergy())) >= 0.1 && adSeg <= 3)
				{
					checkDirChange();
				}
				break;
			case DODGE_RANDOM:
				// not implemented
				break;
			case DODGE_RAMBOT:

				// default is DODGE_HOT
			default:
				if (speedUp <= 0) myMaxVelocity = 900 / e.getDistance();
				myCombatDist = 400;
				break;
		}
//		double delta = myDirection * (myCombatDist - eDistance) / APPROACH;
//		setTurnRightRadians(Math.cos(e.getBearingRadians()) + delta);

		double angle = 0;
		double debug = 0;
		double v0 = -PI_90;
		double rM = Double.MAX_VALUE;
		double rMax = Double.MIN_VALUE;
		double r1 = 0;
		double _x;
		double _y;
		double xRef = getX();
		double yRef = getY();
		double riskDist = 100;

		if (dodgeMode == DODGE_RAMBOT)
		{
			xRef = getX();
			yRef = getY();
			riskDist = Math.min(eDistance, 200);
		}

		while (v0 <= PI_90)
		{
			if (dodgeMode == DODGE_RAMBOT)
			{
				angle = absBearing - v0 - PI_180;
				r1 = Math.abs(Math.sin(absBearing - angle));
			}
			else
			{
				angle = absBearing - v0 * myDirection;
				r1 = Math.abs(Math.cos(absBearing - angle));
			}
			_x = Math.sin(angle) * riskDist;
			_y = Math.cos(angle) * riskDist;

			//r1 += 55000 / Point2D.distanceSq(_x, _y, xe - getX(), ye - getY());

			if (new Rectangle2D.Double(WZ, WZ, WZ_W, WZ_H).contains(_x + xRef, _y + yRef))
			{
//				checkDirChange();
//				speedUp = 15;
//				myMaxVelocity = 8.0;
				if (r1 < rM)
				{
					rM = r1;
					xf = _x;
					yf = _y;
					debug = v0;
				}
				myPaintRisk.registerRiskPoint(getTime(), _x + xRef, _y + yRef, r1, xRef, yRef, riskDist);
			}
			rMax = Math.max(r1, rMax);
			v0 += DELTA_RISK_ANGLE;
		}
		//System.out.format("[%04d] angle=%3.5f \n", getTime(), Math.toDegrees(debug));
		if (rM > rMax * 0.8 && dodgeMode != DODGE_RAMBOT)
		{
			checkDirChange();
			myMaxVelocity = 8.0;
		}
		double a;
		double t = Utils.normalRelativeAngle(Math.tan(a = (Math.atan2(xf, yf) - getHeadingRadians())));
		System.out.format("[%04d] a=%3.5f \n", getTime(), Math.toDegrees(t));
		setTurnRightRadians(t);
		if (dodgeMode == DODGE_RAMBOT)
		{
			setAhead(100 * Math.cos(a));
			//setMaxVelocity(Math.abs(getTurnRemaining()) > 30 ? 0 : myMaxVelocity);
		}
		else
		{
			setAhead(100 * Math.cos(a));
			setMaxVelocity(myMaxVelocity);

		}

		//double m = (Utils.normalRelativeAngle(absBearing - getRadarHeadingRadians()) * INF);
		//System.out.format("[%04d] m=%3.5f \n", getTime(), m);
		//if (!Double.isNaN(m)) 
		setTurnRadarRightRadians(-getRadarTurnRemaining());

		double bPower = Math.min(3.0, Math.min(e.getEnergy() / 4.0, 450 / e.getDistance()));

		w = new DunnartWave();

		if (getGunHeat() == 0)
		{
			w.startTime = getTime();
			w.velSeg = velSeg;
			w.x = getX();
			w.y = getY();
			w.startBearing = absBearing;
			w.distSeg = eDistance;
			w.adSeg = adSeg;
			w.bSpeed = Rules.getBulletSpeed(bPower);
			myWaves.add(w);
		}

		try
		{

			while (true)
			{
				w = myWaves.get(i++);
				if (!w.isCollected)
				{
					if ((getTime() - w.startTime) * w.bSpeed >= w.distance(xe, ye))
					{
						w.angle = Utils.normalRelativeAngle(Math.atan2(xe - w.x, ye - w.y) - w.startBearing);
						w.isCollected = true;
					}
					else
						continue;
				}

				eDistance = 2 * Math.pow(velSeg - w.velSeg, 2) + Math.pow(adSeg - w.adSeg, 2)
						+ Math.pow((w.distSeg - e.getDistance()) / 200, 2);
				if (eDistance < maxMatch)
				{
					maxMatch = eDistance;
					aim = w.angle;
				}

			}
		}
		catch (IndexOutOfBoundsException e0)
		{}

		if (Math.abs(getGunTurnRemaining()) <= 1) setFire(bPower);
		setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing - getGunHeadingRadians()) + aim);
	}

	@Override
	public void onDeath(DeathEvent e)
	{
		//dodgeMode = DODGE_LINEAR;
		//myMaxVelocity = 8;
	}

	@Override
	public void onHitRobot(HitRobotEvent event)
	{
		//checkDirChange();
		//speedUp = 15;
	}

	@Override
	public void onHitByBullet(HitByBulletEvent e)
	{
		setAllColors(Color.RED);
		colorChange = 5;
		eEnergy += Rules.getBulletHitBonus(e.getPower());
	}

	@Override
	public void onBulletHit(BulletHitEvent e)
	{
		eEnergy = e.getEnergy();
	}

	private void checkDirChange()
	{
		if (speedUp <= 0)
		{
			myDirection = -myDirection;
			speedUp = 15;
		}
	}

	public static class DunnartWave extends Point2D.Double
	{
		private static final long	serialVersionUID	= 1L;
		double						startBearing;
		double						velSeg;
		double						angle;
		double						distSeg;
		double						startTime;
		double						adSeg;
		double						bSpeed;

		boolean						isCollected;

	}

}
