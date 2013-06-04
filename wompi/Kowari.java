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
 * v1.7: 249
 * 		This version is a complete steal and I just want to see how it works. So Sheldor, if you read this, I apologize
 * 		if I offend you with this version. I'm still trying to get a glimpse of 1vs1 dynamics and the enhancement to
 * 		Sabreur looked very promising. If it by chance is working how it should I probably remove the bot from the 
 * 		rumble later. 
 * 		Basically I could shrink the code of Sabreur by 20 bytes and fitted an average to the enemy velocity value
 * 		This makes the gun capable to hit StopAndGo bots as well. Everything else should work as in Sabreur
 * 
 *      Tiny Quiiiiikkk ... beware of the Kowaris - they steal your cheese
 *      
 *      Credit: CrazyBassonist for his Caligula and Sheldor for his enhancement to this bot
 * 
 * @author Wompi
 * @date 20/05/2013
 */

public class Kowari extends AdvancedRobot
{
	private static int		myDeath;
	private static double	eEnergy;
	private static double	dir;
	private static double	eVelo;

	private static int		eCount;

	@Override
	public void run()
	{
		// NOTE: this would be nice - I guess
		//setAdjustGunForRobotTurn(true);
		dir = 1;
		setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		double v0;
		double v1;

		// TODO: use this as base for future enhancements - works quite well but could use a little tweaking
		// the myDeath is not a good rule to deal with make it something different
		//dir *= (1 + ((eEnergy - (eEnergy = e.getEnergy())) * (((myDeath + 1) % 2) - 1) * Double.MAX_VALUE));

		// TODO: make it 0.999 if you find the byte
		if (setFireBullet(v1 = (350 / e.getDistance())) != null)
		{
			eVelo = eCount = 0;
			//eVelo = 0;
		}

		//@formatter:off
		setTurnGunRightRadians(Utils.normalRelativeAngle(
				  (
				    v0 = (
				    	   getHeadingRadians() + e.getBearingRadians()
				    	 )
				  )
				+ (
					(
					//TODO: this will be wrong if I go in RAM-mode - find something other	
					  (
					    (eVelo += e.getVelocity())/++eCount
					    //(eVelo = (eVelo * 14 + e.getVelocity())/(14 + 1))
					  ) 
					  * Math.sin(e.getHeadingRadians() - v0) 
				    ) 
				    / Rules.getBulletSpeed(v1)
				  )
				- getGunHeadingRadians()));
		//@formatter:on

		// TODO: try this some times
		//v0 = Math.cos(e.getBearingRadians()) - Math.toRadians(10) * Math.signum(getVelocity());
		v0 = Math.cos(e.getBearingRadians() - (getEnergy() - e.getEnergy()) * getVelocity() * 0.0004);
		//v0 = Math.cos(v1 = (e.getBearingRadians() + getGunTurnRemainingRadians()));

		//System.out.format("[%04d] gunremain=%3.5f dist=%3.5f \n", getTime(), getGunTurnRemaining(), e.getDistance());

		if (myDeath > 3)
		{
			// TODO: find out if + or - is the right way - so far both seems to work but which one is the right?
			//dir = Math.cos(v0 = (e.getBearingRadians() + getGunTurnRemainingRadians())) * Double.MAX_VALUE;
			//dir = v0 * Double.MAX_VALUE;
			//v0 = Math.tan(v1);
		}
		//setAhead(dir);
		//setAhead(Math.cos(getEnergy() * Math.PI / 5) * 160 * dir);
		setAhead(Math.tan(getEnergy() * Math.PI / (e.getEnergy() - getEnergy())) * 160 * dir * 500 / e.getDistance());
		//System.out.format("[%04d] ahead=%3.5f energy=%3.5f \n", getTime(), getDistanceRemaining(), e.getEnergy());
		setTurnRightRadians(v0);
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
		if (getRoundNum() < 12) myDeath++;
	}

}