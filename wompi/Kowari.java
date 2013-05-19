package wompi;

import java.awt.Color;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.HitByBulletEvent;
import robocode.HitWallEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.util.Utils;

public class Kowari extends AdvancedRobot
{
	private static final double	ADVANCE_FACTOR	= 1.0 / 2000.0;					// 2500 = 16deg 2000 = 20deg
	private static final double	DISTANCE_FACTOR	= 176;								// 3.0 and 16 tick cooldown = 11*16= 176 = only one bullet in the air
	private static final double	HIT_FACTOR		= Math.PI / 4.0;
	private static final double	BPOWER			= 2.3333;
	private static final double	BSPEED			= Rules.getBulletSpeed(BPOWER);
	private static final double	GUN_TURNS		= Rules.getGunHeat(BPOWER) * 10;

	private static double		eEnergy;
	private static int			dir;
	private static double		dirChange;
	private static long			lastHit;
	private static double		dFactor;

	private static double		eVelo;

	private static double		eShootVelo;
	private static double		eMiddleVelo;

	static double				eGunHeat;

	static double				turnRandom;

	static long					eBulletTicks;

	public Kowari()
	{}

	@Override
	public void run()
	{
		setAllColors(Color.RED);
		setAdjustGunForRobotTurn(true);
		lastHit = dir = 30;
		eGunHeat = 3.0;
		eBulletTicks = 0;
		setTurnRadarRightRadians(Double.POSITIVE_INFINITY);

	}

	@Override
	public void onStatus(StatusEvent e)
	{
		eGunHeat -= 0.1;
		eBulletTicks--;
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		double aBear;
		//@formatter:off
//		setTurnRightRadians(
//				(dFactor  - e.getDistance())
//				* getVelocity() 
//				* ADVANCE_FACTOR
//				+ Math.cos(aBear = e.getBearingRadians())
//				);
		double perpAngle = Math.cos(aBear = e.getBearingRadians());
		//setTurnRightRadians(perpAngle - Rules.getTurnRateRadians(getVelocity()) * Math.signum(getVelocity()));
		double turnOff = Math.toRadians(10);
		//if (eGunHeat <=0.8)
		{
			//if (eGunHeat <=0.2) turnOff= 0;
		 // turnOff = -turnOff;
		}
		setTurnRightRadians(perpAngle - turnOff  * Math.signum(getVelocity()));
		
	
		double v = ((eVelo +=e.getVelocity())/GUN_TURNS);
		if (getGunHeat() < 0.2)
		{
			if (eShootVelo != 0 && 	 eMiddleVelo != eShootVelo)
			{
				//setAllColors(Color.YELLOW);
				v = -v * 1.2;
			}
			else
			{
				//setAllColors(Color.GREEN);
			}
		}		
		setTurnGunRightRadians(
				Utils.normalRelativeAngle(
						(aBear+=getHeadingRadians()) 
						- getGunHeadingRadians() 
						+ 
						(
						   v 
						   * Math.sin(
								e.getHeadingRadians() - aBear
							) 
							/ BSPEED
						)
				)
		);
		//@formatter:on
		setTurnRadarLeftRadians(getRadarTurnRemaining());

		double eDelta = eEnergy - (eEnergy = e.getEnergy());

		//System.out.format("[%04d] gunHeat=%3.4f eDelta=%3.5f \n", getTime(), Rules.getGunHeat(eDelta), eDelta);

		if (eDelta > 0)
		{
			turnRandom = Math.random() * 25;
			onBulletMissed(null);
			if (Math.cos(dirChange) < 0) onHitWall(null); // saves 2 byte compared to dir = - dir
			eGunHeat = Rules.getGunHeat(eDelta); // gunHeat calculates to negative if eDelta is negative
//			if (eBulletTicks <= 0)
			{
				eBulletTicks = (long) (e.getDistance() / Rules.getBulletSpeed(eDelta)) - 1;
				System.out.format("[%04d] eBuletTicks=%d \n", getTime(), eBulletTicks);
			}
//			else
//			{
//				System.out.format("[%04d] REST eBulletTicks=%d \n", getTime(), eBulletTicks);
//			}
		}

		if (getGunHeat() < 0.8 && eMiddleVelo == 0)
		{
			eMiddleVelo = Math.signum(e.getVelocity());
		}

		if (setFireBullet(BPOWER) != null)
		{
			onBulletMissed(null);
			eShootVelo = Math.signum(e.getVelocity());
			eMiddleVelo = 0;
			eVelo = 0;

		}

		if (eBulletTicks <= 3)
		{
			//setMaxVelocity(1500 / e.getDistance());
			setMaxVelocity(0);
		}
		else
		{
			setMaxVelocity(1500 / e.getDistance());
		}
		setAhead(dir);
	}

	@Override
	public void onHitWall(HitWallEvent e)
	{
		dir = -dir;
	}

	@Override
	public void onBulletMissed(BulletMissedEvent e)
	{
		dFactor = 100 + getTime() % 200;
	}

	@Override
	public void onHitByBullet(HitByBulletEvent e)
	{
		if ((lastHit - (lastHit = getTime())) > -24)
		{
			dirChange += HIT_FACTOR; // / delta;
		}
	}

	@Override
	public void onBulletHit(BulletHitEvent e)
	{
		eEnergy = e.getEnergy();
	}
}
