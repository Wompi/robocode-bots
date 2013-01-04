package wompi;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
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
	final static double				PI_135				= PI_90 + PI_45;
	final static double				PI_80				= Math.PI * 8.0 / 18.0;

	final static double				RAM_DISTANCE		= 150;
	final static double				MAX_SHIELD			= 17;
	final static double				SHIELD_REGENERATON	= 0.05;
	final static double				INF					= Double.POSITIVE_INFINITY;
	static double					eEnergy;
	static boolean					isDodge;
	static ArrayList<DunnartWave>	myWaves;

	static double					myMaxVelocity;
	static double					myDirection;
	static double					myShield;

	// debug
	long							speedUp;

	PaintMinRiskPoints				myPaintRisk;
	PaintMinRiskPoints				myPaintCoordRisk;

	public Dunnart()
	{
		myWaves = new ArrayList<DunnartWave>();
		myDirection = 1;
		myPaintRisk = new PaintMinRiskPoints();
		myPaintCoordRisk = new PaintMinRiskPoints();
		myShield = MAX_SHIELD;
	}

	@Override
	public void onPaint(Graphics2D g)
	{
		myPaintRisk.onPaint(g, true);
		myPaintCoordRisk.onPaint(g, true);
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

		if (!isDodge)
			setAllColors(Color.YELLOW);
		else
			setAllColors(Color.GREEN);

		myShield = Math.max(0, Math.min(myShield + SHIELD_REGENERATON, MAX_SHIELD));
//		System.out.format("[%04d] shield=%3.5f \n", getTime(), myShield);
//		if (isDodge && speedUp <= 0)
//		{
//			checkDirChange(true);
//			speedUp = (int) (Math.random() * 40);
//		}

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

		double eAdvVelocity = e.getVelocity() * Math.cos(e.getHeadingRadians() - absBearing);
		double eLatVelocity = e.getVelocity() * Math.sin(e.getHeadingRadians() - absBearing);

		double xe = Math.sin(absBearing) * eDistance;
		double ye = Math.cos(absBearing) * eDistance;
		int i = 0;
		double aim = 0;
		double maxMatch = INF;
		double eDelta = eEnergy - (eEnergy = e.getEnergy());

		if (eDelta >= 0.1 && eDelta <= 3)
		{
			//checkDirChange(true);
		}

		double angle = 0;
		double v0 = 0;
		double rM = Double.MAX_VALUE;
		double r1 = 0;

		double _x = Math.min(getX(), FIELD_W - getX());
		double _y = Math.min(getY(), FIELD_H - getY());

		double c = PI_180;
		if ((_x <= 150 || _y <= 150) && eDistance < 200) c = PI_360;

		double v1 = 0;
		while (v0 <= c)
		{
			// 0 - PI_90? - 180 cause very wired behavior if heading to the enemy at beginning 
			angle = absBearing - v0 * myDirection;

			r1 = Math.abs(Math.cos(absBearing - angle));
			_x = Math.sin(angle) * 100;
			_y = Math.cos(angle) * 100;

			r1 += 55000 / Point2D.distanceSq(_x, _y, xe, ye);

			if (new Rectangle2D.Double(WZ, WZ, WZ_W, WZ_H).contains(_x + getX(), _y + getY()))
			{
				if (r1 < rM)
				{
					rM = r1;
					v1 = angle;
				}
				myPaintRisk.registerRiskPoint(getTime(), _x + getX(), _y + getY(), r1, getX(), getY(), 100);
			}
			v0 += DELTA_RISK_ANGLE;
		}

		setTurnRightRadians(Math.tan(v1 -= getHeadingRadians()));
		setAhead(1000 * Math.cos(v1));

		double vCos = 8.0 * Math.abs(Math.cos(e.getBearingRadians()));
		double vDist = 900 / eDistance;
		System.out.format("[%04d] vDist=%3.5f vCos=%3.5f (%3.5f) (%3.5f) \n", getTime(), vDist, vCos, eDistance,
				e.getBearing());
		setMaxVelocity(vDist + vCos);

		setTurnRadarRightRadians(-getRadarTurnRemaining());

		double bPower = Math.min(3.0, Math.min(e.getEnergy() / 3.0, 450 / eDistance));

//		double wRate = eDistance * e.getEnergy() / 100.0;
		double wRate = eDistance;

		if (Math.abs(getGunTurnRemaining()) <= 1)
		{
			if (setFireBullet(bPower) != null)
			{
				w = new DunnartWave();
				w.wTime = getTime();
				w.wLatVelocity = eLatVelocity;
				w.wAdvVelocity = eAdvVelocity;
				w.x = getX();
				w.y = getY();
				w.wBearing = absBearing;
				w.wDistance = wRate;
				w.wBulletSpeed = Rules.getBulletSpeed(bPower);
				myWaves.add(w);
			}
		}

		try
		{

			while (true)
			{
				w = myWaves.get(i++);
				if (!w.isCollected)
				{
					if ((getTime() - w.wTime) * w.wBulletSpeed >= w.distance(xe + getX(), ye + getY()))
					{
						w.wAngle = Utils.normalRelativeAngle(Math.atan2(xe + getX() - w.x, ye + getY() - w.y)
								- w.wBearing);
						w.isCollected = true;
					}
					else
						continue;
				}

				eDistance = 2 * Math.pow(eLatVelocity - w.wLatVelocity, 2) /*+ Math.pow(eAdvVelocity - w.wAdvVelocity, 2)*/
						+ Math.pow((w.wDistance - wRate) / 100, 2);
				if (eDistance < maxMatch)
				{
					maxMatch = eDistance;
					aim = w.wAngle;
				}

			}
		}
		catch (IndexOutOfBoundsException e0)
		{}
		setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing - getGunHeadingRadians()) + aim);
	}

	@Override
	public void onHitByBullet(HitByBulletEvent e)
	{
		//setAllColors(Color.RED);
		//colorChange = 5;
		eEnergy += Rules.getBulletHitBonus(e.getPower());
		myShield -= Rules.getBulletHitBonus(e.getPower());
		if (myShield < 0)
		{
			//isDodge = !isDodge;
			isDodge = true;
			//myShield = MAX_SHIELD;
		}

		if (!isDodge)
		{
			checkDirChange(true);
			speedUp = 15;
			myMaxVelocity = 8.0;
		}
		else
		{
			checkDirChange(true);
		}

	}

	@Override
	public void onBulletHit(BulletHitEvent e)
	{
		eEnergy -= Rules.getBulletDamage(e.getBullet().getPower());
	}

	private void checkDirChange(boolean force)
	{
		myDirection = -myDirection;
	}

	public static class DunnartWave extends Point2D.Double
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

}
