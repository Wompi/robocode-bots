package wompi;

import java.awt.Color;

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
	private static double	eHeat;
	private static long		eFireTime;

	public Kowari()
	{}

	@Override
	public void run()
	{
		setAllColors(Color.RED);
		dir = 1;
		eEnergy = 100;
		setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		double absBearing;
		setTurnRadarLeftRadians(getRadarTurnRemaining());

		setTurnGunRightRadians(Utils.normalRelativeAngle((absBearing = (getHeadingRadians() + e.getBearingRadians()))
				+ ((e.getVelocity() / 14) * Math.sin(e.getHeadingRadians() - absBearing)) - getGunHeadingRadians()));

		// TODO: the distance 160 should be adjusted to the current bullet power of the enemies gun heating
		// well that means he shoots 3.0 stay 16*11=176 pixel away (16 heat, 11 bullet speed) if he shoots 0.1 stay
		// 11*19.7=216.7 pixel away - this would mean he can always have only one bullet at the time in the air and you 
		// can dodge it way better. My thought so far it would be good to find a divisor where the distance 
		// bullet ticks is a min and you can oscillate between these parameters - this need a lot more research
		//setTurnRightRadians(Math.cos(e.getBearingRadians() - ((e.getDistance() - 160) * dir * 0.0024)));
		//setTurnRightRadians(Math.cos(e.getBearingRadians() - e.getDistance() * getVelocity() / 2500));
		setTurnRightRadians(Math.cos(e.getBearingRadians() - (e.getDistance() - 160) * dir * 0.0004));

		// TODO: change the direction dependent on the fire frequency 
		// that means he shoots 3.0 = 16 ticks so head(sin(time * pi/(16-2))) would do the trick
		// you have to maintain the time variable 0 on every shoot and ++ on every turn this will oscillate the bot
		// every 14 tick - the -2 is just an offset to hit the break if near the time he can next shoot
		// maybe it is possible to use the getTime() and modulo for this kind of stuff
		// i guess this will bring a nice touch to the movement and can avoid a couple of scenarios 

		if (!Utils.isNear((absBearing = (eEnergy - (eEnergy = e.getEnergy()))), 0))
		{
			//if (deathCount > 0)
			{
				eHeat = Math.ceil((Rules.getGunHeat(absBearing) / getGunCoolingRate())) - 1 - 4;
				dir = -dir;
				eFireTime = 0;
				//System.out.format("[%04d] eHeat=%3.4f eFireTime=%d ePower=%3.4f\n", getTime(), eHeat, eFireTime,
				//		absBearing);
			}
			//else
			{
				// Note: before i forget this stuff
				// this is the simplified math from Narbalek and means 
				// speed = 2*d0 / tick
				// with d0 = 25 to 27 (this is the distance to the line that hits the far corner of our bot)
				// tick - is the time the enemy bullet needs to hit the bot at its current position
				// if you transform the formula you get the cryptic monster below
				// the whole idea is to move the bot with the calculated speed just enough to dodge any
				// HoT shooting. He moves barely out of the former position 
				//setMaxVelocity((1080 - 162 * absBearing) / e.getDistance());
			}
		}

		double t = Math.sin(eFireTime++ * dir * Math.PI / eHeat);

		setAhead(Double.POSITIVE_INFINITY * t);
		//System.out.format("[%04d] move=%3.4f v=%3.4f\n", getTime(), t, getVelocity());

		setFire(Math.min(600 / e.getDistance(), eEnergy / 4));
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

		// TODO: the hit time can be used in some kind of standard deviation look at the difference between
		// bullet turns and hits - if it is 1 or 2 of it means the bullet has hit you right after each other
		// if the difference is bigger you can guess the frequency of the hitting and make your decision from there
		// this is very basic tested so far but i have a hunch it could work
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
