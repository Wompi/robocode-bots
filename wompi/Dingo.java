package wompi;

import java.awt.Color;
import java.awt.Graphics2D;

import robocode.AdvancedRobot;
import robocode.Bullet;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.util.Utils;
import wompi.dingo.DingoBasicPaint;
import wompi.dingo.DingoEnergyDrop;
import wompi.dingo.DingoFireDetection;
import wompi.dingo.DingoInactivity;
import wompi.dingo.DingoPaint;
import wompi.dingo.DingoRadar;
import wompi.dingo.DingoStartSearch;
import wompi.dingo.DingoWallDistance_Heading;
import wompi.dingo.paint.DingoBulletPaint;

public class Dingo extends AdvancedRobot
{
	public static final double				DIST			= 185;
	public static final double				PI_45			= Math.PI / 4.0;
	public static final double				PI_360			= Math.PI * 2.0;
	public static final double				PI_90			= Math.PI / 2.0;
	public static final double				PI				= Math.PI;
	public final static double				BORDER			= 18.0;
	private final static double				INF				= Double.POSITIVE_INFINITY;

	private DingoStartSearch				mySearch;
	private DingoRadar						myRadar;

	private final DingoFireDetection		myFireDetector;
	private final DingoInactivity			myInactivity;
	private final DingoEnergyDrop			myEnergyDrop;
	private final DingoWallDistance_Heading	myWallDistance;

	// debug visualization
	private DingoPaint						myPaint;
	private DingoBasicPaint					myBasics;
	private final DingoBulletPaint			myBulletPaint;

	private long							lastScan;

	private double							xg;
	private double							yg;
	private double							eAbsBearing;
	private double							eHeading;
	private double							eEnergy;
	private double							eDistance;

	// test 
	int										wallHitIndex	= -1;
	boolean									iFire;

	public Dingo()
	{
		myFireDetector = new DingoFireDetection();
		myBulletPaint = new DingoBulletPaint();
		myInactivity = new DingoInactivity();
		myEnergyDrop = new DingoEnergyDrop();
		myWallDistance = new DingoWallDistance_Heading(600.0, 800.0, BORDER);
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		if (e.getTime() > 0) myPaint.registerStatus(e.getStatus());
		myEnergyDrop.onStatus(e);
		myFireDetector.onStatus(e);
		myBulletPaint.onStatus(e);
		if (wallHitIndex >= 0) wallHitIndex++;
	}

	@Override
	public void run()
	{
		System.out.format("[%d] gunheat=%3.2f\n", getTime(), getGunHeat());
		myFireDetector.onInit(this);
		myEnergyDrop.onInit(this);

		mySearch = new DingoStartSearch();
		myRadar = new DingoRadar();
		myPaint = new DingoPaint(this);
		myBasics = new DingoBasicPaint(this);
		setAllColors(Color.ORANGE);

		eEnergy = getEnergy();
		eDistance = 1000;

		mySearch.onInit(this);
		while (!mySearch.isFound())
		{
			myFireDetector.onRun();
			mySearch.onRun();
			execute();
		}

		setAdjustRadarForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

		setTurnRadarRightRadians(INF);
		while (true)
		{
			myEnergyDrop.onRun();
			if (myEnergyDrop.isInactive()) myFireDetector.onInactivity();
			myFireDetector.onRun();
			boolean eHasFired = myFireDetector.hasFired();
			if (eHasFired)
			{
				myBulletPaint.onScannedRobot(myFireDetector.getFiredBulletPower(), xg, yg); // takes the position of the turn before
			}

			myRadar.onRun(this, eAbsBearing);

			onMove1();

			double bPower = Math.min(2.99, Math.max(0.1, Math.min(eEnergy / 4.0, 350 / eDistance)));
			//bPower = 3.0;
			Bullet b = null;
			if (iFire)
			{
				wallHitIndex = -1;
				b = setFireBullet(bPower);
			}
			if (b != null)
			{
				myEnergyDrop.onFire(b.getPower());

				System.out.format("b0=%3.15f b1=%3.15f \n", bPower, b.getPower());
			}
			execute();
		}
	}

	@Override
	public void onHitByBullet(HitByBulletEvent e)
	{
		myEnergyDrop.onHitByBullet(e);
		myFireDetector.onHitByBullet(e);
	}

	@Override
	public void onBulletHit(BulletHitEvent e)
	{
		myEnergyDrop.onBulletHit(e);
		myFireDetector.onBulletHit(e);
	}

	@Override
	public void onHitRobot(HitRobotEvent e)
	{
		myEnergyDrop.onHitRobot(e);
		myFireDetector.onHitRobot(e);
	}

	@Override
	public void onHitWall(HitWallEvent e)
	{
		myEnergyDrop.onHitWall(e);
		wallHitIndex = 0;
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
//		long dScan = e.getTime() - lastScan;
//		if (dScan > 1) System.out.format("[%04d] scan - %d\n", e.getTime(), dScan);
//		lastScan = e.getTime();

		myFireDetector.onScannedRobot(e);
		myRadar.onScannedRobot(e.getTime());
		eAbsBearing = getHeadingRadians() + e.getBearingRadians();
		eHeading = e.getHeadingRadians();
		xg = getX() + Math.sin(eAbsBearing) * e.getDistance();
		yg = getY() + Math.cos(eAbsBearing) * e.getDistance();

		eEnergy = e.getEnergy();
		eDistance = e.getDistance();
		mySearch.onScannedRobot();

		iFire = Utils.isNear(getGunTurnRemainingRadians(), 0.0);
		setTurnGunRightRadians(Utils.normalRelativeAngle(eAbsBearing - getGunHeadingRadians()));
	}

	private void onMove1()
	{
		if (Utils.isNear(getDistanceRemaining(), 0.0))
		{
			myWallDistance.setStartPoint(getX(), getY());
			double heading = getHeadingRadians();
			double dForward = myWallDistance.getDistance(heading);
			double dBackward = myWallDistance.getDistance(heading + Math.PI);

			double dir = 1;
			double dDist = dForward;
			if (dForward > dBackward)
			{
				heading += Math.PI;
				dir = -dir;
				dDist = dBackward;
			}
			double s0 = Math.signum(Utils.normalRelativeAngle(heading));

			double hDiff = 0;

			setMaxVelocity(8.0);
			if (Utils.isNear(getX(), BORDER) || Utils.isNear(getX(), getBattleFieldWidth() - BORDER))
			{
				// edge
				hDiff = Math.sin(heading) * s0;
				if (Utils.isNear(0.0, Math.toDegrees(hDiff) % 90))
				{
					double max = Math.max(dForward, dBackward);
					dir = 1;
					if (max == dBackward) dir = -1;
					setAhead(dir * dDist * Math.random());
				}
			}
			else if (Utils.isNear(getY(), BORDER) || Utils.isNear(getY(), getBattleFieldHeight() - BORDER))
			{
				hDiff = Math.cos(heading) * s0;
				if (Utils.isNear(0.0, Math.toDegrees(hDiff) % 90))
				{
					double max = Math.max(dForward, dBackward);
					dir = 1;
					if (max == dBackward) dir = -1;
					setAhead(dir * dDist * Math.random());
				}
			}
			else
			{
				setAhead(dDist * dir);
			}

			setTurnRightRadians(hDiff);

			System.out.format("F=%3.5f B=%3.5f D=%2.2f hDiff=%3.10f\n", dForward, dBackward, dir,
					Math.toDegrees(hDiff) % 90);
		}
		setMaxVelocity(Math.random() * 4.0 + 5.0);
	}

	@Override
	public void onPaint(Graphics2D g)
	{
		//myPaint.onPaint(g);
		myBasics.onPaint(g);
		myBasics.edgeDiagonals(g, getX(), getY(), getHeadingRadians(), Color.GREEN);
		myBasics.edgeDiagonals(g, xg, yg, eHeading, Color.BLUE);

		myBulletPaint.onPaint(g);
	}
}
