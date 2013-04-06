package wompi;

import java.awt.Color;
import java.awt.geom.Point2D;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import robocode.HitWallEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.util.Utils;
import wompi.paint.PaintHelper;
import wompi.robomath.RobotMath;

// TODO: use the DIR variable as lastHitTime and make it changed for a needs
// initialize the variable with 30 - just a thought to prevent the first hit issu  

// TODO: rethink about the new movement
// the movement should work with facing to the enemy and if he shoots move to him and turn perpendicuar i
// if he shoots again move back to the facing position with some luck it can dodge 
// linear and headon in one catch a well thought sin period should do the trick

// TODO: have a closer look at the gun - sounds promising to alternate between HoT and linear
// maybe adjust the factor in onHit and onMiss
// otherwise adjust the overall factor like the speed variable dependent where he stays and what the 
// hit bounds is

// TODO: the distance 160 should be adjusted to the current bullet power of the enemies gun heating
// well that means he shoots 3.0 stay 16*11=176 pixel away (16 heat, 11 bullet speed) if he shoots 0.1 stay
// 11*19.7=216.7 pixel away - this would mean he can always have only one bullet at the time in the air and you 
// can dodge it way better. My thought so far it would be good to find a divisor where the distance 
// bullet ticks is a min and you can oscillate between these parameters - this need a lot more research
//setTurnRightRadians(Math.cos(e.getBearingRadians() - ((e.getDistance() - 160) * dir * 0.0024)));
//setTurnRightRadians(Math.cos(e.getBearingRadians() - e.getDistance() * getVelocity() / 2500));
//double turn = Math.cos(e.getBearingRadians());
//double offset = - (e.getDistance() - /*Rules.getBulletSpeed(ePower) * Rules.getGunHeat(ePower) * 10*/250)
//* getVelocity() * ADVANCE_FACTOR

// TODO: change the direction dependent on the fire frequency 
// that means he shoots 3.0 = 16 ticks so head(sin(time * pi/(16-2))) would do the trick
// you have to maintain the time variable 0 on every shoot and ++ on every turn this will oscillate the bot
// every 14 tick - the -2 is just an offset to hit the break if near the time he can next shoot
// maybe it is possible to use the getTime() and modulo for this kind of stuff
// i guess this will bring a nice touch to the movement and can avoid a couple of scenarios 

// TODO: this concept should go to a micro bot - periodic direction change dependent on enemy gun heat  
//eHeat = Math.ceil((Rules.getGunHeat(absBearing) / getGunCoolingRate())) - 1 - 4;

// Note: before i forget this stuff
// this is the simplified math from Narbalek and means 
// speed = 2*d0 / tick
// tick = eDist / (20-3*bPower)
// with d0 = 25 to 27 (this is the distance to the line that hits the far corner of our bot)
// tick - is the time the enemy bullet needs to hit the bot at its current position
// if you transform the formula you get the cryptic monster below
// the whole idea is to move the bot with the calculated speed just enough to dodge any
// HoT shooting. He moves barely out of the former position where the speed is LOW if far away 
// and HIGH if near

// TODO: the bounce angle for wall hits is significant for HoT hits... if you drive perenticular to the bot
// you got least hits but can stuck in corners

//setMaxVelocity((1080 - 162 * ePower) / e.getDistance() + SPEEDUP_FACTOR / ++speedUp);
//setMaxVelocity(54 * e.getDistance() / Rules.getBulletSpeed(ePower) + SPEEDUP_FACTOR / ++speedUp); // grr 1 byte shorter
//setAhead(Math.sin(getTime() * Math.PI / 16) * Double.POSITIVE_INFINITY);

// TODO: the hit time can be used in some kind of standard deviation look at the difference between
// bullet turns and hits - if it is 1 or 2 of it means the bullet has hit you right after each other
// if the difference is bigger you can guess the frequency of the hitting and make your decision from there
// this is very basic tested so far but i have a hunch it could work
//if ((lastHitTime - (lastHitTime = getTime()) / (Rules.getGunHeat(e.getPower()) * 10)) < 2) toggle = !toggle;
//System.out.format("[%04d] HIT - %d cool=%3.5f hitFreq=%3.5f (%3.5f)\n", getTime(), getTime() - lastHitTime,
//		bHeat, hitFrequence, dTime / bHeat);

//eEnergy -= Rules.getBulletDamage(e.getBullet().getPower());
// Note: this is a very nice trick to get rid of the above byte hungry call
// 6 byte less
// be careful the hitevent is also removed but this should be ok
//eEnergy = e.getEnergy();
//clearAllEvents();

public class Kowari extends AdvancedRobot
{
	private static final double	ADVANCE_FACTOR	= 1.0 / 2000.0;		// 2500 = 16deg 2000 = 20deg
	private static final double	DISTANCE_FACTOR	= 200;					// 3.0 and 16 tick cooldown = 11*16= 176 = only one bullet in the air
	private static final double	HIT_FACTOR		= Math.PI * 2 / 20.0;

	private static double		eEnergy;
	private static double		dir;
	private static double		dirChange;
	private static long			lastHit;

	private static double		ePower;

	public Kowari()
	{
		// 158
		dir = 1;
	}

	@Override
	public void run()
	{
		setAdjustRadarForRobotTurn(true);
		setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		setAllColors(Color.RED);

	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		double v0;
		double absBear = e.getBearingRadians() + getHeadingRadians();
		double bPower;

		double off = 0;
		if (dir < 0) off = Math.PI;

		double hisAbsBear = getHeadingRadians() + e.getBearingRadians() - Math.PI;

		double ex = getX() + Math.sin(absBear) * e.getDistance();
		double ey = getY() + Math.cos(absBear) * e.getDistance();

		Point2D start = new Point2D.Double(ex, ey);
		Point2D end = RobotMath.calculatePolarPoint(hisAbsBear, e.getDistance(), start);
		PaintHelper.drawLine(start, end, getGraphics(), Color.GRAY);
		double escapeAngle = hisAbsBear - 8 / Rules.getBulletSpeed(ePower);
		end = RobotMath.calculatePolarPoint(escapeAngle, 200, start);
		PaintHelper.drawLine(start, end, getGraphics(), Color.LIGHT_GRAY);

		double me = Math.PI - Math.PI / 2 - 8 / Rules.getBulletSpeed(ePower);
		double angle = Math.max(300 / e.getDistance(), me);
		//if (e.getDistance() < 150) absBear += Math.PI;
		double angleToEscape = absBear + angle * dir;
//		double angleToEscape = absBear;
		start = new Point2D.Double(getX(), getY());
		end = RobotMath.calculatePolarPoint(angleToEscape, 200, start);
		PaintHelper.drawLine(start, end, getGraphics(), Color.green);

		double px = getX() + Math.sin(angleToEscape) * 8 / Rules.getBulletSpeed(ePower) * e.getDistance();
		double py = getY() + Math.cos(angleToEscape) * 8 / Rules.getBulletSpeed(ePower) * e.getDistance();
		end = new Point2D.Double(px, py);

		PaintHelper.drawLine(start, end, getGraphics(), Color.YELLOW);

		setTurnRightRadians(Utils.normalRelativeAngle(angleToEscape - getHeadingRadians() + off));

		if (Math.abs(getDistanceRemaining()) < 20)
		{
			onHitWall(null);
		}

		setTurnGunRightRadians(Utils.normalRelativeAngle(e.getBearingRadians() + getHeadingRadians()
				- getGunHeadingRadians()));
		setTurnRadarLeftRadians(getRadarTurnRemainingRadians());
		setFire(bPower = (e.getEnergy() * 6 / (v0 = e.getDistance())));
		//@formatter:off
		
		//@formatter:on

		//setMaxVelocity(1800 / v0);

		double eDelta = eEnergy - (eEnergy = e.getEnergy());
		if (eDelta > 0)
		{
			ePower = eDelta;
			dir = -dir;
		}
		setAhead(96 * dir);
		System.out.format("[%04d] distance=%3.4f angle=%3.5f me=%3.5f \n", getTime(), e.getDistance(),
				Math.toDegrees(angle), Math.toDegrees(me));
	}

	@Override
	public void onHitWall(HitWallEvent e)
	{
		dir = -dir; // keep in mind that this is not necessary - for the trade of some hits (very bad against samples)
		//setAhead(Math.random() * 250 * dir);
	}

	@Override
	public void onHitByBullet(HitByBulletEvent e)
	{
//		System.out.format("[%04d] ---------------hisHit=%3.5f \n", getTime(), e.getPower());

		dirChange += HIT_FACTOR / (lastHit - (lastHit = getTime()));
		eEnergy += Rules.getBulletHitBonus(e.getPower());
	}

	@Override
	public void onBulletHit(BulletHitEvent e)
	{
//		System.out.format("[%04d] ---------------myHit=%3.5f \n", getTime(), e.getBullet().getPower());
		eEnergy = e.getEnergy();
	}
}
