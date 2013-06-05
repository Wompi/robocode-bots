package wompi;

import robocode.AdvancedRobot;
import robocode.HitWallEvent;
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
	private static double	dir;

	@Override
	public void run()
	{
		// NOTE: this would be nice - I guess
		setAdjustGunForRobotTurn(true);
		dir = 1;
		setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		try
		{
			int integer = MAX_MATCH_LENGTH;
			double absoluteBearing;
			double v1;
			int matchPosition;

			char test = (char) (e.getVelocity() * (Math.sin(e.getHeadingRadians()
					- (absoluteBearing = e.getBearingRadians() + getHeadingRadians()))));

			System.out.format("[%04d] test=%d \n", getTime(), (short) test);

			enemyHistory = String.valueOf(test).concat(enemyHistory);

			while ((matchPosition = enemyHistory.indexOf(enemyHistory.substring(0, (--integer)), 64)) < 0)
				;

			setFire(BULLET_POWER + (ANTI_RAMBOT_DISTANCE / (integer = (int) (e.getDistance()))));

			do
			{
				absoluteBearing += ((short) enemyHistory.charAt(--matchPosition)) / e.getDistance();
			}
			while ((integer -= BULLET_VELOCITY) > 0);
			setTurnGunRightRadians(Utils.normalRelativeAngle(absoluteBearing - getGunHeadingRadians()));

			setAhead(Math.cos(getEnergy() * Math.PI / Math.max(5, Math.abs(v1 = (e.getEnergy() - getEnergy())))) * 160
					* dir);
			setTurnRightRadians(Math.cos(e.getBearingRadians() - v1 * getVelocity() * 0.0004));
			//setTurnRightRadians(Math.cos(e.getBearingRadians() - (e.getDistance() - 160) * getVelocity() * 0.0004));
			//setMaxVelocity(2000 / e.getDistance());
			setTurnRadarLeftRadians(getRadarTurnRemainingRadians());
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
	}

	@Override
	public void onHitWall(HitWallEvent e)
	{
		dir = -dir;
	}

	public static final int	ANTI_RAMBOT_DISTANCE	= 127;
	public static final int	BULLET_POWER			= 2;
	public static final int	BULLET_VELOCITY			= (20 - 3 * BULLET_POWER);
	public static final int	MAX_MATCH_LENGTH		= 40;

	static String			enemyHistory			= "" + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
															+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
															+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
															+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
															+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
															+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 1
															+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
															+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
															+ (char) 0 + (char) 0 + (char) 1 + (char) 0 + (char) 0
															+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
															+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
															+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
															+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
															+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
															+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
															+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
															+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
															+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
															+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
															+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
															+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 1
															+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
															+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
															+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
															+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
															+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
															+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
															+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
															+ (char) 0 + (char) 1 + (char) 0 + (char) 0 + (char) 0
															+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
															+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
															+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
															+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
															+ (char) 0 + (char) 0 + (char) 0 + (char) -1 + (char) -2
															+ (char) -3 + (char) -4 + (char) -5 + (char) -6 + (char) -7
															+ (char) -8 + (char) 8 + (char) 7 + (char) 6 + (char) 5
															+ (char) 4 + (char) 3 + (char) 2 + (char) 1 + (char) 0
															+ (char) 0;
}