package wompi;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import robocode.AdvancedRobot;
import robocode.Bullet;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import robocode.HitWallEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.util.Utils;
import wompi.paint.PaintEnemyBulletWaves;
import wompi.paint.PaintEnemyMaxEscapeAngle;
import wompi.paint.PaintHelper;
import wompi.paint.PaintMaxEscapeAngle;

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
	private static final double	PI_180			= Math.PI;
	private static final double	W				= 800;
	private static final double	H				= 600;

	private static final double	ADVANCE_FACTOR	= 1.0 / 3000.0;					// 2500 = 16deg 2000 = 20deg
	private static final double	DISTANCE_FACTOR	= 350;								// 3.0 and 16 tick cooldown = 11*16= 176 = only one bullet in the air
	private static final double	HIT_FACTOR		= Math.PI * 14.0 / 4.0;

	private static double		eEnergy;
	private static double		dir;
	private static double		dirChange;
	private static long			lastHit;

	// gun
	static double				eBearing;
	static double				myx;
	static double				myy;
	static double				bDist;
	static double				bPower;

	// debug
	PaintEnemyBulletWaves		eWave			= new PaintEnemyBulletWaves();
	PaintMaxEscapeAngle			bMax			= new PaintEnemyMaxEscapeAngle();

	public Kowari()
	{
		// 158
		dir = 1;
	}

	@Override
	public void run()
	{
		// debug
		bMax.onInit(this, 18);
		xxd = getX();
		yyd = getY();

		setAllColors(Color.red);
		setTurnRadarRightRadians(bDist = Double.POSITIVE_INFINITY);
		setAdjustGunForRobotTurn(true);
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		bMax.onStatus(e);
		eWave.onStatus(e);
	}

	static double	xxd;
	static double	yyd;

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		double relBear = e.getBearingRadians();
		double absBear = relBear + getHeadingRadians();

		double _x = (getX() + e.getDistance() * Math.sin(absBear)) - myx;
		double _y = (getY() + e.getDistance() * Math.cos(absBear)) - myy;

		boolean test = false;
		double bearing = Math.atan2(_x, _y) - (eBearing);
		bPower = e.getEnergy() * 15 / e.getDistance();

		if ((bDist += Rules.getBulletSpeed(bPower)) > Math.hypot(_x, _y))
		{
			eBearing = absBear;
			bDist = 0;// Rules.getBulletSpeed(bPower);
			myx = getX();
			myy = getY();
			test = true;
		}

		System.out.format("[%04d] bearing=%3.4f %s\n", getTime(), Math.toDegrees(bearing),
				test ? String.format("FIRE: %3.5f (%3.5f)", bPower, Rules.getBulletSpeed(bPower)) : "");
		Bullet b;
		if ((b = setFireBullet(bPower)) != null)
			System.out.format("[%04d] bullet=%3.4f %3.4f [%3.4f %3.4f]\n", getTime(), b.getX(), b.getY(), getX(),
					getY());

		setTurnGunRightRadians(Utils.normalRelativeAngle(absBear - getGunHeadingRadians() + bearing));

		//@formatter:off
		setTurnRightRadians(
				+ Math.cos(relBear) //* getGunHeat() // keep the random in mind looks very promising 
				+ ((DISTANCE_FACTOR -e.getDistance())
				* getVelocity()
				* ADVANCE_FACTOR)
				);
		//@formatter:on

		setMaxVelocity(1800 / e.getDistance());
		double eDelta = eEnergy - (eEnergy = e.getEnergy());
		setAhead(dir *= (1 + ((eDelta) * Math.cos(dirChange) * Double.MAX_VALUE)));
		setTurnRadarLeftRadians(getRadarTurnRemainingRadians());

		// debug
		if (eDelta != 0) System.out.format("[%04d] eDelta=%3.20f\n", getTime(), eDelta);
		bMax.setBulletSpeed(bPower);
		bMax.onScannedRobot(e);
		eWave.onScannedRobot(e, eDelta);

	}

	@Override
	public void onHitWall(HitWallEvent e)
	{
		dir = -dir; // keep in mind that this is not necessary - for the trade of some hits (very bad against samples)
		System.out.format("[%04d] Boooooiiiiing!\n", getTime());
	}

	@Override
	public void onHitByBullet(HitByBulletEvent e)
	{
//		System.out.format("[%04d] ---------------hisHit=%3.5f \n", getTime(), e.getPower());
		dirChange += HIT_FACTOR / (lastHit - (lastHit = getTime()));
		//eEnergy += Rules.getBulletHitBonus(e.getPower());
	}

	@Override
	public void onBulletHit(BulletHitEvent e)
	{
//		System.out.format("[%04d] ---------------myHit=%3.5f \n", getTime(), e.getBullet().getPower());
		//eEnergy = e.getEnergy();
		eEnergy -= Rules.getBulletDamage(e.getBullet().getPower());
	}

	@Override
	public void onPaint(Graphics2D g)
	{
		setAllColors(Color.red);
		eWave.onPaint(g);
		bMax.onPaint(g);

		Point2D start = new Point2D.Double(myx, myy);
		PaintHelper.drawArc(start, bDist, 0, Math.PI * 2, false, getGraphics(), Color.DARK_GRAY);
		PaintHelper.drawPoint(start, Color.RED, getGraphics(), 3);

	}
}
