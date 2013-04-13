package wompi;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.HitByBulletEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;

public class Kowari extends AdvancedRobot
{
	private static final double	ADVANCE_FACTOR	= 1.0 / 2000.0;	// 2500 = 16deg 2000 = 20deg
	private static final double	DISTANCE_FACTOR	= 176;				// 3.0 and 16 tick cooldown = 11*16= 176 = only one bullet in the air
	private static final double	HIT_FACTOR		= Math.PI / 8.0;

	private static double		eEnergy;
	private static int			dir;
	private static double		dirChange;
	private static long			lastHit;
	private static int			missed;
	private static double		dFactor;

	public Kowari()
	{}

	@Override
	public void run()
	{
		setAdjustGunForRobotTurn(true);
		dir = 100;
		lastHit = 0;
		setTurnGunRightRadians(Double.POSITIVE_INFINITY);
	}

	@Override
	public void onStatus(StatusEvent e)
	{}

	@Override
	public void onScannedRobot(final ScannedRobotEvent e)
	{
		//@formatter:off
		setTurnRightRadians(
				(dFactor  - e.getDistance())
				* getVelocity() 
				* ADVANCE_FACTOR
				+ Math.cos(e.getBearingRadians())
				);
		//@formatter:on

		setTurnGunLeftRadians(getGunTurnRemaining());

		if (((eEnergy - (eEnergy = e.getEnergy()))) > 0)
		{
			if (Math.cos(dirChange) < 0) onHitWall(null); // saves 2 byte compared to dir = - dir
		}

		setFire(e.getEnergy() * 15 / e.getDistance());
		setMaxVelocity(1800 / e.getDistance());
		setAhead(dir);
	}

	@Override
	public void onHitWall(HitWallEvent e)
	{
		dir = -dir;
	}

	@Override
	public void onBulletMissed(BulletMissedEvent event)
	{
		if (missed++ % 2 == 0)
		{
			dFactor = Math.random() * 200 + 100;
		}
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
