package wompi;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import robocode.HitWallEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

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
	private static final double	DISTANCE_FACTOR	= 176;					// 3.0 and 16 tick cooldown = 11*16= 176 = only one bullet in the air
	private static final double	HIT_FACTOR		= Math.PI * 14.0 / 4.0; // 14 = average bulletheat and pi/4 shift - needs a little tewak i guess 
	private static final double	SPEED_FACTOR	= 1080;				// TODO: find the best value

	private static double		eEnergy;
	private static double		dir;
	private static boolean		isLocked;
	private static double		dirChange;
	private static long			lastHit;

	// gun check
//	static double				avgVeloCount;
//	static double				avgVelo;
	static double				rollVelo;
	static double				dist;
	static double				lastDist;

	//static long					lastShoot;

	// debug 
//	PaintBulletHits				myHits			= new PaintBulletHits();
//	PaintEnemyBulletWaves		myWaves			= new PaintEnemyBulletWaves();

	public Kowari()
	{}

	@Override
	public void run()
	{
		lastDist = 1;

//		setAdjustRadarForGunTurn(true);
//		setAdjustGunForRobotTurn(true);
		// debug
//		myHits.onInit(this, 18);
		//lastHit = 30; // TODO: not really necessary
		dir = 1;
		setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
	}

	// debug
//	@Override
//	public void onStatus(StatusEvent e)
//	{
//		myHits.onStatus(e);
//		myWaves.onStatus(e);
//	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		/// debug
//		myHits.onScannedRobot(e);
		//double bPower = (e.getEnergy() * 15 / e.getDistance());
		double bPower = (750 / e.getDistance());
		double absBearing;

		//@formatter:off
		setTurnRightRadians(
				Math.cos(
						(absBearing = e.getBearingRadians()) 
						- ((e.getDistance()) - (DISTANCE_FACTOR))
						* getVelocity() 
						* ADVANCE_FACTOR
						/** lastShoot--/5*/) 
			    );
		//@formatter:on

		setTurnRadarLeftRadians(getRadarTurnRemaining());

		// dir is a function of locked/delta_energy and dirChange - now i only have to find it 
		//
		double eDelta;
		if ((eDelta = (eEnergy - (eEnergy = (e.getEnergy())))) > 0 && !isLocked /*&& Math.cos(dirChange) < 0*/)
		{
			//lastShoot = 10;
			onHitWall(null); // saves 2 byte compared to dir = - dir
//			/// debug
////			double xe = getX() + Math.sin(getHeadingRadians() + e.getBearingRadians()) * e.getDistance();
////			double ye = getY() + Math.cos(getHeadingRadians() + e.getBearingRadians()) * e.getDistance();
////			myWaves.onScannedRobot(eDelta, xe, ye);
			//setMaxVelocity();
			double d = (SPEED_FACTOR / Rules.getBulletSpeed(eDelta)) * dir;
			setAhead(d);
			System.out.format("[%04d] d=%3.5f e=%3.15f\n", getTime(), d, eDelta);
		}

		double latv = e.getVelocity() * Math.sin(e.getHeadingRadians() - (absBearing += getHeadingRadians()));
		dist += latv;
		double buffy = dist / Rules.getBulletSpeed(bPower);

		//@formatter:off
		setTurnGunRightRadians(
				Utils.normalRelativeAngle(
						absBearing
						- getGunHeadingRadians()
						+ (buffy)
							/ (1.1 * Rules.getBulletSpeed(bPower))));
		//@formatter:on
//		System.out.format("[%04d] dist=%3.2f ev=%3.5f avgv=%3.5f latv=%3.5f \n", getTime(), dist, e.getVelocity(),
//				buffy, latv);

		if (setFireBullet(bPower) != null)
		//if (setFireBullet(bPower = (750 / e.getDistance())) != null) // TODO: check this out in the rumble
		{
//			System.out.format("[%04d] fire (%3.5f) evelo (%3.5f) speed (%3.5f)! dist (%3.5f) last (%3.5f)\n",
//					getTime(), bPower, e.getVelocity(), Rules.getBulletSpeed(bPower), dist, lastDist);
			lastDist = dist;
			dist = 0;
		}

		isLocked = false;

//		setMaxVelocity(SPEED_FACTOR / e.getDistance());
//		setAhead(dir);
	}

	@Override
	public void onHitWall(HitWallEvent e)
	{
		dir = -dir;
	}

	@Override
	public void onHitByBullet(HitByBulletEvent e)
	{
		isLocked = true;

		// (PI/4) / hDelta/14 
		dirChange += HIT_FACTOR / (lastHit - (lastHit = getTime()));
//		System.out.format("[%04d] lastHit=%3.4f 14(%3.4f) dirchange=%3.4f (%3.4f) \n", getTime(), delta, delta / 14,
//				dirChange, off);

//		// debug
//		myHits.onHitByBullet(e);
//
	}

	@Override
	public void onBulletHit(BulletHitEvent e)
	{
		isLocked = true;
//		missed = 0;
	}

//	static int	missed;
//
//	@Override
//	public void onBulletMissed(BulletMissedEvent e)
//	{
//		System.out.format("[%04d] missed (%d)\n", getTime(), ++missed);
//	}
	// debug
//	@Override
//	public void onPaint(Graphics2D g)
//	{
//		setAllColors(Color.RED);
//		myHits.onPaint(g);
//		myWaves.onPaint(g);
//	}

}
