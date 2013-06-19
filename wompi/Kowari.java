package wompi;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.HitWallEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
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
	private static double	eEnergy;

	private static int		bTurn;

	@Override
	public void run()
	{
		// NOTE: this would be nice - I guess
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForRobotTurn(true);

		setTurnRadarRightRadians(dir = Double.POSITIVE_INFINITY);
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		bTurn--;
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{

		//double aBear = e.getBearingRadians() + getHeadingRadians();
		double eDelta = eEnergy - (eEnergy = e.getEnergy());

		if (eDelta > 0)
		{
			double move = (Math.random() - 0.5) * 42; // try 42
			//double move = 0; // try 42
			int depp;
			if (getDistanceRemaining() == 0
					&& Math.min(bTurn, depp = (int) (e.getDistance() / Rules.getBulletSpeed(eDelta))) < 14)
			{
				bTurn = depp;
				System.out.format("[%04d] bTurn=%3d \n", getTime(), bTurn);
				move = (dir = Math.signum(Math.random() - 0.3) * dir);
				//move = (dir = -dir);
				//move = dir;
			}
			//System.out.format("[%04d] move=%3.5f \n", getTime(), move);
			setAhead(move);
		}
		//if (eDelta != 0) System.out.format("[%04d] eDelta=%3.5f \n", getTime(), eDelta);
		//setFire(350 / e.getDistance());

		//@formatter:off
		setTurnRightRadians(
				(220 - e.getDistance()) * getVelocity() / 3000
				+ Math.cos(e.getBearingRadians())
	    );
		//@formatter:on

		setTurnLeftRadians(Math.cos(e.getBearingRadians()));
		setTurnRadarLeftRadians(getRadarTurnRemaining());

		// --------------------- YATAGAN GUN ------------------------------------------------------
		// TODO: just to see if the movement can do better - get rid of this gun and make a GF one bullet gun
		// Note: gun size 138 byte includes enemyHistory. This is hard to beat i guess
		int integer = 40;
		double absoluteBearing;
		int matchPosition;
		enemyHistory = String.valueOf(
				(char) (e.getVelocity() * (Math.sin(e.getHeadingRadians()
						- (absoluteBearing = e.getBearingRadians() + getHeadingRadians()))))).concat(enemyHistory);
		while ((matchPosition = enemyHistory.indexOf(enemyHistory.substring(0, (--integer)), 64)) < 0)
			;
		setFire(2 + (127 / (integer = (int) (e.getDistance()))));
		do
		{
			absoluteBearing += ((short) enemyHistory.charAt(--matchPosition)) / e.getDistance();
		}
		while ((integer -= 11) > 0);
		setTurnGunRightRadians(Utils.normalRelativeAngle(absoluteBearing - getGunHeadingRadians()));
		// -------------- GUN ----------------------------------------------------------------------
	}

	@Override
	public void onBulletHit(BulletHitEvent e)
	{
		//eEnergy = e.getEnergy();
		eEnergy -= Rules.getBulletDamage(e.getBullet().getPower());
	}

	@Override
	public void onHitWall(HitWallEvent event)
	{
		setAhead(dir = -dir);
	}

	static String	enemyHistory	= "" + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
											+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
											+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
											+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
											+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
											+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
											+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
											+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
											+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
											+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
											+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
											+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
											+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
											+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
											+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
											+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
											+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
											+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
											+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
											+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
											+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
											+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
											+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
											+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
											+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
											+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
											+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0
											+ (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) 0 + (char) -1
											+ (char) -2 + (char) -3 + (char) -4 + (char) -5 + (char) -6 + (char) -7
											+ (char) -8 + (char) 8 + (char) 7 + (char) 6 + (char) 5 + (char) 4
											+ (char) 3 + (char) 2 + (char) 1 + (char) 0 + (char) 0;

}