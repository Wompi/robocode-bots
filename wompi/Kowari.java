package wompi;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.DeathEvent;
import robocode.HitByBulletEvent;
import robocode.HitWallEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class Kowari extends AdvancedRobot
{
	private static double	eEnergy;
	private static double	dir;
	private static int		deathCount;

	public Kowari()
	{}

	@Override
	public void run()
	{
		//setAllColors(Color.RED);
		setTurnRadarRightRadians(dir = Double.POSITIVE_INFINITY);
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		double absBearing;
		setTurnRadarLeftRadians(getRadarTurnRemaining());

		setTurnGunRightRadians(Utils.normalRelativeAngle((absBearing = (getHeadingRadians() + e.getBearingRadians()))
				+ ((e.getVelocity() / 14) * Math.sin(e.getHeadingRadians() - absBearing)) - getGunHeadingRadians()));

		setTurnRightRadians(Math.cos(e.getBearingRadians() - (e.getDistance() - 160) * (getVelocity() / 2500)));

		// TODO: change the direction dependent on the fire frequency 
		// that means he shoots 3.0 = 16 ticks so head(sin(time * pi/(16-2))) would do the trick
		// you have to maintain the time variable 0 on every shoot and ++ on every turn this will oscillate the bot
		// every 14 tick - the -2 is just an offset to hit the break if near the time he can next shoot
		// maybe it is possible to use the getTime() and modulo for this kind of stuff
		// i guess this will bring a nice touch to the movement and can avoid a couple of scenarios 

		setAhead(dir);

		if (!Utils.isNear((absBearing = (eEnergy - (eEnergy = e.getEnergy()))), 0))
		{
			if (deathCount > 0)
				dir = -dir;
			else
				setMaxVelocity((1080 - 162 * absBearing) / e.getDistance());
		}

		setFire(Math.min(1100 / e.getDistance(), eEnergy / 4));
	}

	@Override
	public void onHitWall(HitWallEvent e)
	{
		dir = -dir;
	}

	@Override
	public void onHitByBullet(HitByBulletEvent e)
	{
		eEnergy += Rules.getBulletHitBonus(e.getPower());
		// debug
//		double bHeat = Rules.getGunHeat(e.getPower()) / getGunCoolingRate();
//		System.out.format("[%04d] HIT - %d cool=%3.5f\n", getTime(), getTime() - lastHitTime, bHeat);
//		lastHitTime = getTime();
	}

	@Override
	public void onBulletHit(BulletHitEvent e)
	{
		eEnergy -= Rules.getBulletDamage(e.getBullet().getPower());
	}

	@Override
	public void onDeath(DeathEvent e)
	{
		deathCount++;
	}
}
