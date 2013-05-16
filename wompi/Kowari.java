package wompi;

import robocode.AdvancedRobot;
import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.DeathEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.WinEvent;
import robocode.util.Utils;
import wompi.stats.StatsWriter;

public class Kowari extends AdvancedRobot
{
	private static final double	ADVANCE_FACTOR	= 1.0 / 2000.0;					// 2500 = 16deg 2000 = 20deg
	private static final double	DISTANCE_FACTOR	= 176;								// 3.0 and 16 tick cooldown = 11*16= 176 = only one bullet in the air
	private static final double	HIT_FACTOR		= Math.PI / 8.0;
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

	static int					bVelo;
	static StatsWriter			myStats			= new StatsWriter();

	public Kowari()
	{}

	@Override
	public void run()
	{
		//setAllColors(Color.RED);
		myStats.onInit(this, 18);
		setAdjustGunForRobotTurn(true);
		lastHit = dir = 30;
		eGunHeat = 3.0;
		bVelo = 9;
		setTurnRadarRightRadians(Double.POSITIVE_INFINITY);

		while (true)
		{
			myStats.onRun();
			execute();
		}
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		eGunHeat -= 0.1;
		myStats.onStatus(e);

	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		myStats.onScannedRobot(e);
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
		if (eGunHeat <=0.8)
		{
			//if (eGunHeat <=0.2) turnOff= 0;
		  turnOff = -turnOff;
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
			bVelo++;
			turnRandom = Math.random() * 25;
			onBulletMissed(null);
			if (Math.cos(dirChange) < 0) onHitWall(null); // saves 2 byte compared to dir = - dir
			eGunHeat = Rules.getGunHeat(eDelta); // gunHeat calculates to negative if eDelta is negative
		}

		if (getGunHeat() < 0.8 && eMiddleVelo == 0)
		{
			eMiddleVelo = Math.signum(e.getVelocity());
		}

		if (setFireBullet(BPOWER) != null)
		{
			onBulletMissed(null);
			eShootVelo = Math.signum(e.getVelocity());
			bVelo++;
			eMiddleVelo = 0;
			eVelo = 0;

		}

		if (eGunHeat <= 0.3)
		{
			setMaxVelocity(1500 / e.getDistance());
			//setMaxVelocity(0);
		}
//		else
		//setMaxVelocity((bVelo % 10.0) + 3);
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
		if (e != null) myStats.onBulletMissed(e);
	}

	@Override
	public void onBulletHitBullet(BulletHitBulletEvent e)
	{
		myStats.onBulletHitBullet(e);
	}

	@Override
	public void onHitByBullet(HitByBulletEvent e)
	{
		if ((lastHit - (lastHit = getTime())) > -24)
		{
			dirChange += HIT_FACTOR; // / delta;
		}
		myStats.onHitByBullet(e);
	}

	@Override
	public void onBulletHit(BulletHitEvent e)
	{
		eEnergy = e.getEnergy();
		myStats.onBulletHit(e);
	}

	@Override
	public void onHitRobot(HitRobotEvent e)
	{
		myStats.onHitRobot(e);
	}

	@Override
	public void onDeath(DeathEvent e)
	{
		myStats.onDeath(e);
	}

	@Override
	public void onWin(WinEvent e)
	{
		myStats.onWin(e);
	}

}
