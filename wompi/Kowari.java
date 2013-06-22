package wompi;

import java.awt.Color;
import java.awt.geom.Point2D;

import robocode.AdvancedRobot;
import robocode.Bullet;
import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.HitWallEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.util.Utils;
import wompi.paint.PaintHelper;
import wompi.robomath.RobotMath;

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

	// -------- BEARING GUN wompi ---------------------------
	static double			eBearing;
	static double			myx;
	static double			myy;
	static double			bDist;

	static Bullet			myBullet;

	// ---------BEARIING GUN --------------------------------

	@Override
	public void run()
	{
		// NOTE: this would be nice - I guess
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForRobotTurn(true);

		setTurnRadarRightRadians(bDist = dir = Double.POSITIVE_INFINITY);
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		bDist += 11;
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{

		//double aBear = e.getBearingRadians() + getHeadingRadians();
		double eDelta = eEnergy - (eEnergy = e.getEnergy());

		if (eDelta > 0)
		{
			//double move = (Math.random() - 0.5) * 42; // try 42
			double move = 0; // try 42
			setAllColors(Color.yellow);
			if (getDistanceRemaining() == 0)
			{
				//move = (dir = Math.signum(Math.random() - 0.3) * dir);
				//move = (dir = -dir);
				move = dir;
				setAllColors(Color.red);
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

		// ---------------------- BEARING GUN wompi---------------------------------------------------------
		// 244 / 398 -- 385
		// 154 gunsize no codesize shrinking
		// 141 first code shrink
		// 136 better shrink hard bullet speed and fire rule
		//@formatter:off
		double absBear;
		double _x; 
		double _y; 

				
		if ((bDist) > Math.hypot(
								_x = (getX() + e.getDistance() * Math.sin(absBear = (e.getBearingRadians() + getHeadingRadians()))) - myx
								, 
								_y = (getY() + e.getDistance() * Math.cos(absBear)) - myy)
				&& getGunHeat() == 0				
				)
		{
			eBearing = absBear;
			bDist = 0;
			myx = getX();
			myy = getY();
			Bullet b;
			if ((b =setFireBullet(3.0)) != null)
			{
				myBullet = b;
			}
		}
				
		
		Point2D myPos = new Point2D.Double(getX(), getY());
		Point2D firePos = new Point2D.Double(myx, myy);
		
		PaintHelper.drawArc(firePos, bDist, 0, Math.PI *2,false, getGraphics(), Color.green);
		PaintHelper.drawPoint(firePos, Color.green, getGraphics(), 4);

		if (myBullet != null)
		{
			Point2D bulletPos = new Point2D.Double(myBullet.getX(), myBullet.getY());
			PaintHelper.drawLine(firePos, bulletPos, getGraphics(), Color.green);
			
			double ex = getX() + e.getDistance() * Math.sin(e.getBearingRadians() + getHeadingRadians());
			double ey = getY() + e.getDistance() * Math.cos(e.getBearingRadians() + getHeadingRadians());
			double eDist = firePos.distance(ex,ey);
			PaintHelper.drawLine(firePos, new Point2D.Double(ex, ey), getGraphics(), Color.DARK_GRAY);
			PaintHelper.drawLine(firePos, RobotMath.calculatePolarPoint(eBearing, eDist, firePos), getGraphics(), Color.LIGHT_GRAY);
			PaintHelper.drawArc(firePos,eDist, eBearing, Utils.normalRelativeAngle(Math.atan2(ex - myx, ey - myy) - (eBearing)) , false, getGraphics(), Color.RED);

		}

		
		
		setTurnGunRightRadians(
				Utils.normalRelativeAngle(
						absBear - getGunHeadingRadians() 
						+ 
						Math.atan2(_x, _y) - (eBearing)
				)
		);
		//@formatter:on

		// -----------------------BEARING GUN --------------------------------------------------------------

	}

	@Override
	public void onBulletHitBullet(BulletHitBulletEvent event)
	{
		bDist = Double.POSITIVE_INFINITY;
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
}