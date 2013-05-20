package wompi;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.DeathEvent;
import robocode.HitWallEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class Kowari extends AdvancedRobot
{
	static double	direction;
	static double	enemyEnergy;
	static int		deathCount	= 1;

	@Override
	public void run()
	{
		setTurnRadarRightRadians(direction = Double.POSITIVE_INFINITY);
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		double absoluteBearing;
		double bearing;
		double lead;

		setFire(lead = (e.getEnergy() * 15 / e.getDistance()));

		//@formatter:off
		setTurnGunRightRadians(Utils.normalRelativeAngle((absoluteBearing = getHeadingRadians()
				+ (bearing = e.getBearingRadians()))
				+ (lead = (e.getVelocity() / Rules.getBulletSpeed(lead)) * Math.sin(e.getHeadingRadians() - absoluteBearing))
				- getGunHeadingRadians()));
		//@formatter:on

		direction *= (1 + ((enemyEnergy - (enemyEnergy = e.getEnergy())) * ((deathCount % 2) - 1) * Double.MAX_VALUE));

		absoluteBearing = Math.cos(bearing) - Math.toRadians(10) * Math.signum(getVelocity());
		//absoluteBearing = Math.cos(bearing - (e.getDistance() - 120) * (getVelocity() / 2500));

		if (deathCount > 3)
		{
			absoluteBearing = Math.tan(bearing += lead);
			direction = Math.cos(bearing) * 100;
		}
		setAhead(direction);
		setTurnRightRadians(absoluteBearing);
		setMaxVelocity(1500 / e.getDistance());
		setTurnRadarLeftRadians(getRadarTurnRemainingRadians());
	}

	@Override
	public void onBulletHit(BulletHitEvent e)
	{
		enemyEnergy -= 10;
	}

	@Override
	public void onDeath(DeathEvent e)
	{
		//if (getRoundNum() < 12)
		{
			deathCount++;
		}
	}

	@Override
	public void onHitWall(HitWallEvent e)
	{
		direction = -direction;
	}
}