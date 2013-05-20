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

	@Override
	public void run()
	{
		// NOTE: this would be nice - I guess
		//setAdjustGunForRobotTurn(true);
		setTurnRadarRightRadians(dir = Double.POSITIVE_INFINITY);
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		double v0;
		double v1;
		double v2;

		// TODO: use this as base for future enhancements - works quite well but could use a little tweaking
		// the myDeath is not a good rule to deal with make it something different
		dir *= (1 + ((eEnergy - (eEnergy = e.getEnergy())) * ((myDeath % 2) - 1) * Double.MAX_VALUE));

		// TODO: make it 0.999 if you find the byte
		setFire(v2 = Math.min(1 + (int) (120 / e.getDistance()), eEnergy / 5.0));

		//@formatter:off
		setTurnGunRightRadians(Utils.normalRelativeAngle(
				  (
				    v0 = (
				    	   getHeadingRadians() + (v1 = e.getBearingRadians())
				    	 )
				  )
				+ (
					(
					  v2 = (
							 e.getVelocity() * Math.sin(e.getHeadingRadians() - v0) / Rules.getBulletSpeed(v2)
						   )
				    ) 
					
				  )
				- getGunHeadingRadians()));
		//@formatter:on

		// TODO: try this some times
		//absoluteBearing = Math.cos(bearing) - Math.toRadians(10) * Math.signum(getVelocity());
		v0 = Math.cos(v1 - (e.getDistance() - 148) * getVelocity() * 0.00033);

		if (myDeath > 4)
		{
			v0 = Math.tan(v1 += v2);
			dir = Math.cos(v1) * Double.MAX_VALUE;
		}
		setAhead(dir);
		setTurnRightRadians(v0);
		setMaxVelocity(1800 / e.getDistance());
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