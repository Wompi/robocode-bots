package wompi;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import robocode.HitWallEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
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

	private static final double	ADVANCE_FACTOR	= 1.0 / 1000.0;					// 2500 = 16deg 2000 = 20deg
	private static final double	DISTANCE_FACTOR	= 300 - 25;						// 3.0 and 16 tick cooldown = 11*16= 176 = only one bullet in the air
	private static final double	HIT_FACTOR		= Math.PI * 14.0 / 4.0;

	private static double		eEnergy;
	private static double		dir;
	private static double		dirChange;
	private static long			lastHit;

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
		//setAdjustGunForRobotTurn(true);
		bMax.onInit(this, 18);
		xxd = getX();
		yyd = getY();
		setTurnGunRightRadians(Double.POSITIVE_INFINITY);
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		bMax.onStatus(e);
		eWave.onStatus(e);
		//setAhead(0);

		double R = 180;
		double x = getX();
		double y = getY();
		if (getX() < R)
		{
			x = R;
		}
		if (getX() > 800 - R)
		{
			x = 800 - R;
		}
		if (getY() < R)
		{
			y = R;
		}
		if (getY() > 600 - R)
		{
			y = 600 - R;
		}

		double relBear = getHeadingRadians() - Math.atan2(x - getX(), y - getY());

//		setTurnRightRadians(Utils.normalRelativeAngle(angle = (Math.atan2(dx, dy) - getHeadingRadians())));
//		setAhead(100 * Math.cos(angle));

		double edist = Point2D.distance(x, y, getX(), getY());

		//@formatter:off
		setTurnRightRadians(
				(R-22 - edist)
				* getVelocity()
				* ADVANCE_FACTOR
				+ Math.cos(relBear) //* getGunHeat() // keep the random in mind looks very promising 
				);
		PaintHelper.drawArc(new Point2D.Double(x, y), R-22, 0, Math.PI * 2, true, getGraphics(), PaintHelper.greenTrans);
		//setAhead(dir);
	}

	static double	xxd;
	static double	yyd;

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		double v0;

		double bPower = e.getEnergy() * 6 / (v0 = e.getDistance());
		setTurnGunLeftRadians(getGunTurnRemaining() * Math.random()); // multiply with Math.random() for better SittingDuck hits = 4 bytes
		if (setFireBullet(bPower) != null)
		{}

//		double x = getX() + Math.sin(e.getBearingRadians() + getHeadingRadians()) * e.getDistance();
//		double y = getY() + Math.cos(e.getBearingRadians() + getHeadingRadians()) * e.getDistance();
//		PaintHelper.drawArc(new Point2D.Double(400, 300), 300-18, 0, Math.PI * 2, true, getGraphics(), PaintHelper.greenTrans);
//
//		xxd = 50;
//		yyd = 50;

//		int xd = (int) (Math.cos((xxd += (Math.random() * 20)) * Math.PI / 800) * 400 + 400);
//		int yd = (int) (Math.cos((yyd += (Math.random() * 20)) * Math.PI / 600) * 300 + 300);
//		xd = Math.min(800 - 36, Math.max(xd, 36));
//		yd = Math.min(600 - 36, Math.max(yd, 36));
//
//		System.out.format("xxd=%3d yyd=%3d \n", xd, yd);
//
//		double kx = PI_180 * (0.5 - xd / W);
//		double ky = PI_180 * (0.5 - yd / H);
//
//		double dx = Math.cos(getX() * PI_180 / W + kx);
//		double dy = Math.cos(getY() * PI_180 / H + ky);
//		double angle;
		//double relBear = e.getBearingRadians();
//		double relBear = getHeadingRadians() - Math.atan2(400 - getX(), 300 - getY());
//
////		setTurnRightRadians(Utils.normalRelativeAngle(angle = (Math.atan2(dx, dy) - getHeadingRadians())));
////		setAhead(100 * Math.cos(angle));
//
//		//@formatter:off
//		setTurnRightRadians(
//				+ Math.cos(relBear) //* getGunHeat() // keep the random in mind looks very promising 
////				+ ((v0 - DISTANCE_FACTOR)
////				* getVelocity()
////				* ADVANCE_FACTOR)
//				);
		//@formatter:on

		setMaxVelocity(1800 / v0);
//		double mlatv = -getVelocity() * Math.cos(getTurnRemainingRadians());
//		System.out.format("[%04d] mlatv=%3.20f\n", getTime(), mlatv);

//		System.out.format("[%04d] turndelta=%3.5f mlatv=%3.5f heading=%3.5f \n", getTime(), getTurnRemaining(), mlatv,
//				getHeading());
//		double nlatv = getVelocity() * Math.sin(getTurnRemainingRadians());
//
//		double round = Math.round(nlatv) - 0.001 * Math.signum(nlatv);
//
//		double adjust = Math.asin(round / getVelocity());
//		double delta = Utils.normalRelativeAngle(getTurnRemainingRadians() + adjust);
//
//		//setTurnRightRadians(delta);
//
//		System.out.format("[%04d] natv=%3.5f round=%3.5f adjust=%3.5f delta=%3.5f\n", getTime(), nlatv, round,
//				Math.toDegrees(adjust), Math.toDegrees(delta));
//
//		double latv = getVelocity() * Math.sin(getHeadingRadians());
//		System.out.format("[%04d] true=%3.5f pattern=%d\n", getTime(), latv, (int) latv);

		double eDelta = eEnergy - (eEnergy = e.getEnergy());
		setAhead(dir *= (1 + ((eDelta) * Math.cos(dirChange) * Double.MAX_VALUE)));

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
	}
}
