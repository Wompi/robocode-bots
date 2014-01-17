package wompi;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import wompi.paint.PaintHelper;
import wompi.robomath.RobotMath;

/**
 * Code is open source, released under the RoboWiki Public Code License: http://robowiki.net/cgi-bin/robowiki?RWPCL
 * 
 * 
 * What the ... is a Quokka? (See: http://en.wikipedia.org/wiki/Quokka) It's smaller then a wallaby (my other robot) and so it is my nano now.
 * 
 * some specials? (no) - normal head-on gun - endless spinning radar - drives around in circles
 * 
 * Credit? Well, i found out there is another robot in the RoboRumble, who uses the same movement like my robots I dind'nt noticed him until i entered
 * the rumble and he is not open-source. But anyway he did it first and so a little credit goes to "tj.zombie1n 1.0.1" - it is a nano to and it's some
 * sort of WhiteWhale because we move on the same path. And of course RoboWiki.
 * 
 * Competitive? I have no idea how far this one will go. Just after finish writing this header i put him at the rumble
 * 
 * 
 * Size v1.0 250
 * 
 * Well it turned out that a NanoBot has to be less than 250byte and not 250byte... so i have to take out the velo==0 rule for targets This makes me
 * sad because it helped a lot in scoring ... but i can't find the last byte If someone find the byte, it would be nice if he can contact me ... drop
 * a message in RoboWiki to wompi please
 * 
 * Size v1.1: 241 ....lost
 * 
 * Size v1.3: 249 I was in the mood for some nano action, so i grabbed the quokka again and we will see how it works. It took me ages to shuffle the
 * code until i reached the 249 byte barrier. To be honest the movement was some kind of accident and i'm quite happy with this one. It's mainly the
 * moving from wallaby, but stripped of some control variables. I wished i had more room but in the end, i think this will do. I run some tests
 * against all top 10 nano melee bots and it finished somewhere around 8th. I'm unhappy with the radar because i have the feeling there is an easier
 * way to do it, but unfortunately, all i tried - failed. I looked at all open source nano bots but it was'nt what i needed ... or (mostly) i did'nt
 * understand how they work - yes yes these guys are crazy. Main target was, to fit in the linear targeting gun because a simple head-on was not
 * competitive against most of the TopBots. Lib still has this simple gun and he does impressive well. But i think he has the strongest moving so it
 * works somehow. The quokka is fighting until only 3 robots left from this time he waits until he has less energy than the other. It turned out that
 * in 1vs1 i always lost because of shooting me dry. The gun is not working very well an long distance either so it makes no dmg anyway.. i'm not sure
 * if this is a good melee behavior but this is how it works ...
 * 
 * Credit: Definite Lib 1.0 (kawigi) gets a big credit and thanks for making his bot open source. It is unbelievable how much i learned about robocode
 * and java from this one. Well done mate. I had a look on almost every open-source nano MeleeBot and i want to give this bots credit too.
 * 
 * well ..off we go (sorry for the bad English i'm sleepy and will fix it at the next version)
 * 
 * Size v1.4: 247 I was in the mood for some nano stuff and to my surprise i came up with something that looks very nice. Precise linear gun (based on
 * wallaby) and orbiting movement. The radar with the gun slip still fits and this makes quokka very nice at combat. It is of course no match for the
 * top ten bots but i like to have a bot with a precise weapon in nano class. Maybe if i can play with the movement later it will be better but for
 * now it should be enough. I'm not sure if firing 3.0 bullets all the time is a smart idea but there is no code left to make this smooth.
 * 
 * Credit: The radar is something i saw on Lib 1.0 (kawigi) and i want to give this credit because it is very nice done.
 * 
 * well ... its quokka hunting season
 * 
 * Size v1.5: 249 Well to my surprise i could fit in a complete precise circular gun and i hope it is somewhat unique. I saw normal circular guns on
 * nano bots but never with wall handling. The movement is of course way to weak to be competitive but i think it is still a nice bot. Unfortunately
 * didn't fit the radar from Lib 1.0 and it is a little to restrictive with locking the enemy. If someone can find one measly byte please give me a
 * note on RoboWiki to wompi. Its just one byte what i need to fit in the radar, bummer. But anyway i'm quite happy with this and it gave me a lot of
 * ideas for wallaby
 * 
 * Credit: not much to say this time
 * 
 * ruuuuunnnn...
 * 
 * Size v1.6 249 I was a little bit bored and thought it could'nt hurt to have a look at Quokka again. And i think it is a good one now. Nice movement
 * and still the circular gun. I had to shuffle the code for almost a day now and i guess it is enough for a long time - nano code makes me really
 * nuts :)
 * 
 * Credit: Of course credit to Miked0801 and his movement from Infinity. It is by far the smallest code for melee movement that i have seen and still
 * is competitive. Well done mate. I think it is ok if i take it because its open source and i don't took all of it and anyway i don't think it gets
 * smaller in any way. Quokka is complete different to infinity beside of one part of the movement and i hope he is ok with this.
 * 
 * .... quiiik
 * 
 * 
 * 
 * @author Wompi
 * @date 24/06/2012
 * 
 */
public class Quokka extends AdvancedRobot
{

	static double	INF		= Double.POSITIVE_INFINITY;
	static double	PI_360	= Math.PI * 2;
	static double	PI_180	= Math.PI;

	public Quokka()
	{}

	@Override
	public void run()
	{
		// debug
		setAllColors(Color.ORANGE);
		setTurnRadarRightRadians(INF);
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		double hisHeading = e.getHeadingRadians();
		double absBear = e.getBearingRadians() + getHeadingRadians();
		double hisVelo = e.getVelocity();

		if (hisVelo < 0) hisHeading += PI_180;

		Point2D start = new Point2D.Double(getX(), getY());
		Point2D e1 = RobotMath.calculatePolarPoint(hisHeading, 100, start);
		Point2D e2 = RobotMath.calculatePolarPoint(absBear, 100, start);

		Point2D a1 = new Point2D.Double((e1.getX() + e2.getX()) / 2, (e1.getY() + e2.getY()) / 2);

		double adjust = 0;
		if (e.getDistance() < 100) adjust = PI_180;

		double moveAngle = RobotMath.calculateAngle(start, a1) - getHeadingRadians() + adjust;
		Point2D e3 = RobotMath.calculatePolarPoint(moveAngle, 100, start);

		PaintHelper.drawLine(start, e1, getGraphics(), Color.green);
		PaintHelper.drawLine(start, e2, getGraphics(), Color.red);
		PaintHelper.drawLine(start, a1, getGraphics(), Color.yellow);

		setTurnRadarLeftRadians(getRadarTurnRemaining());
		//setTurnRightRadians(Math.tan(moveAngle));
		setAhead(200 * Math.cos(moveAngle));

		setTurnRightRadians(Math.tan(moveAngle));
		setAhead(200 * Math.cos(moveAngle));

		//setAhead(120 - Math.abs(getTurnRemaining()));
	}

	@Override
	public void onPaint(Graphics2D g)
	{

	}
}
