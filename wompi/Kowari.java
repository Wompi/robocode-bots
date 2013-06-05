package wompi;

import java.awt.Color;
import java.awt.geom.Point2D;

import robocode.AdvancedRobot;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;
import wompi.paint.PaintHelper;

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
	private static double	eVelo;
	private static int		eCount;

	static double			eBearing;
	static double			eDistance;
	static double			eY;
	static double			eX;
	static double			myX;
	static double			myY;
	static double			bDist;
	static Point2D			eLoc;

	@Override
	public void run()
	{
		// NOTE: this would be nice - I guess
		setAdjustGunForRobotTurn(true);
		dir = 1;
		setTurnRadarRightRadians(Double.MAX_VALUE);
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		double v0;
		double v1;

		double relBear = e.getBearingRadians();
		double absBear = relBear + getHeadingRadians();

		double _x = (getX() + e.getDistance() * Math.sin(absBear));
		double _y = (getY() + e.getDistance() * Math.cos(absBear));

		double hypo = Math.hypot(_x, _y) - 18;
		if ((bDist += 11) > hypo)
		{
//			System.out.format("[%04d] bDist=%3.5f bDistTics=%3.5f hypo=%3.5f\n", getTime(), bDist, hypo / 11, hypo);
			//@formatter:off
			double _xx = getX() + (e.getDistance() * Math.sin(absBear));
			double _yy = getY() +(e.getDistance() * Math.cos(absBear));
			
			double dist = Math.hypot(
					eX - _xx, 
					eY - _yy
					);
			eX = _xx;
			eY = _yy;
			//@formatter:on
			eBearing = absBear;
			eDistance = e.getDistance();
			bDist = 0;
			myX = getX();
			myY = getY();
			System.out.format("[%04d] dist=%3.5f \n", getTime(), dist);
			setFire(3.0);
		}
		PaintHelper.drawArc(new Point2D.Double(getX(), getY()), bDist, 0, Math.PI * 2, false, getGraphics(),
				Color.green);

		setTurnGunRightRadians(Utils.normalRelativeAngle(absBear - getGunHeadingRadians()));

		setAhead(Math.cos(getEnergy() * Math.PI / 5) * 160 * dir);
		//setTurnRightRadians(Math.cos(e.getBearingRadians() - v1 * getVelocity() * 0.0004));
		setTurnRightRadians(Math.cos(e.getBearingRadians() - (e.getDistance() - 160) * getVelocity() * 0.0004));
		//setMaxVelocity(2000 / e.getDistance());
		setTurnRadarLeftRadians(getRadarTurnRemainingRadians());
	}

	@Override
	public void onHitWall(HitWallEvent e)
	{
		dir = -dir;
	}
}