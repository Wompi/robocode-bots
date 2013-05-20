package wompi;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.DeathEvent;
import robocode.HitWallEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

/**
 * What the ... is a Kowari? (See: http://en.wikipedia.org/wiki/Kowari)
 * 
 * (if you are keen to read this ... be prepared for very bad English (i'm German) 
 * To keep track of what i have done, i update a little development diary at: 
 * 							https://github.com/Wompi/robocode-bots/wiki/Kowari
 * 
 * The official version history can be found at: http://robowiki.net/wiki/Kowar
 * 
 * If you want to talk about it - you find me at: http://robowiki.net/wiki/User:Wompi
 * 
 * Credit: As usual credit to CrazyBassonist and his BOT Caligula. I guess Sheldor came up with the little
 * gun hack (it use the gun calculation for the ram movement). And of course to all the veterans who have invented
 * all the little code shrinking goodies which by now state of the art.
 * 
 * v1.6: 248
 * 		This version is still a prove of concept it contains for my taste to much stuff from other bots. But I have to 
 * 		start somewhere and it is a good way to check out some tactics. Which are complete new to me because everything 
 * 		is 1vs1 related.
 * 		I guess this one will have some serious trouble with ram and advanced bots.
 * 		This version contains some new features like a direction formula and the distance dependent velocity movement
 * 		everything else is mostly reusing of well known formulas
 * 
 *      Quiiiik .....
 * 
 * 
 * @author Wompi
 * @date 20/05/2013
 */

public class Kowari extends AdvancedRobot
{
	private static int		myDeath	= 1;
	private static double	eEnergy;
	private static double	dir;

	// bearing gun
	static double			eBearing;
	static double			myx;
	static double			myy;
	static double			bDist;

	@Override
	public void run()
	{
		// NOTE: this would be nice - I guess
		setAdjustGunForRobotTurn(true);
		setTurnRadarRightRadians(dir = bDist = Double.POSITIVE_INFINITY);
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		double v0;
		double v1;
		double v2;

		setAhead(dir *= (1 + ((eEnergy - (eEnergy = e.getEnergy())) * ((myDeath % 2) - 1) * Double.MAX_VALUE)));
		//setTurnRightRadians(Math.cos((v1 = e.getBearingRadians()) - (e.getDistance() - 160) * getVelocity() * 0.00033));
		setTurnRightRadians(Math.cos(v1 = e.getBearingRadians()) - Math.toRadians(10) * Math.signum(getVelocity()));

		setFire(v2 = Math.min(1 + (int) (190 / e.getDistance()), eEnergy / 3.0));
		double _x = (getX() + e.getDistance() * Math.sin(v1 += getHeadingRadians())) - myx;
		double _y = (getY() + e.getDistance() * Math.cos(v1)) - myy;

		if ((bDist += Rules.getBulletSpeed(v2)) > Math.hypot(_x, _y))
		{
			eBearing = v1;
			bDist = 0; //Rules.getBulletSpeed(v2);
			myx = getX();
			myy = getY();
		}
		setTurnGunRightRadians(Utils.normalRelativeAngle(v1 - getGunHeadingRadians() + Math.atan2(_x, _y) - eBearing));

		//setMaxVelocity(1800 / e.getDistance());
		setTurnRadarLeftRadians(getRadarTurnRemainingRadians());
	}

	@Override
	public void onHitWall(HitWallEvent e)
	{
		dir = -dir;
	}

	@Override
	public void onBulletHit(BulletHitEvent e)
	{
		// TODO: check out other implementations - right now it sounds good to just drop a turn if I hit the enemy.  
		eEnergy = e.getEnergy();
	}

	@Override
	public void onDeath(DeathEvent e)
	{
		myDeath++;
	}

}