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
import wompi.dingo.paint.DingoBulletPaint;
import wompi.paint.PaintEscapePath;
import wompi.paint.PaintMaxEscapeAngle;
import wompi.paint.PaintWallHeadingDistance;
import wompi.robomath.WallHeadingDistance;

public class Dingo extends AdvancedRobot
{
	public static final double				DIST			= 185;
	public static final double				PI_45			= Math.PI / 4.0;
	public static final double				PI_360			= Math.PI * 2.0;
	public static final double				PI_90			= Math.PI / 2.0;
	public static final double				PI				= Math.PI;
	public final static double				BORDER			= 17.99;
	private final static double				INF				= Double.POSITIVE_INFINITY;

	private DingoStartSearch				mySearch;
	private DingoRadar						myRadar;

	private final DingoFireDetection		myFireDetector;
	private final DingoInactivity			myInactivity;
	private final DingoEnergyDrop			myEnergyDrop;
	private final WallHeadingDistance		myWallDistance;

	// debug visualization
	private final PaintWallHeadingDistance	myPaintWallDistance;
	private DingoPaint						myPaint;
	private DingoBasicPaint					myBasics;
	private final DingoBulletPaint			myBulletPaint;
	private final PaintMaxEscapeAngle		myPaintMaxEscape;
	private final PaintEscapePath			myPaintEscapePath;

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
		myInactivity = new DingoInactivity();
		myEnergyDrop = new DingoEnergyDrop();
		myWallDistance = new WallHeadingDistance();

		myBulletPaint = new DingoBulletPaint();
		myPaintWallDistance = new PaintWallHeadingDistance(myWallDistance);
		myPaintMaxEscape = new PaintMaxEscapeAngle();
		myPaintEscapePath = new PaintEscapePath();
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		if (e.getTime() > 0) myPaint.registerStatus(e.getStatus());
		myEnergyDrop.onStatus(e);
		myFireDetector.onStatus(e);

		myBulletPaint.onStatus(e);
		myPaintMaxEscape.onStatus(e);
		myPaintEscapePath.onStatus(e);
		if (wallHitIndex >= 0) wallHitIndex++;
	}

	@Override
	public void run()
	{
		myFireDetector.onInit(this);
		myEnergyDrop.onInit(this);
		myWallDistance.onInit(this, BORDER);
		mySearch = new DingoStartSearch();
		myRadar = new DingoRadar();

		myPaint = new DingoPaint(this);
		myPaintMaxEscape.onInit(this, BORDER);
		myPaintEscapePath.onInit(this, BORDER);
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

		System.out.format("[%04d] bearing=%3.10f \n", getTime(), e.getBearing());

		double bPower = Math.min(2.99, Math.max(0.1, Math.min(e.getEnergy() / 4.0, 350 / e.getDistance())));

		myPaintMaxEscape.setBulletSpeed(bPower);
		myPaintMaxEscape.onScannedRobot(e);
		myPaintEscapePath.setBulletSpeed(bPower);
		myPaintEscapePath.onScannedRobot(e);

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

	private void onMove0()
	{
		if (getTime() < 40)
			setAhead(Double.MAX_VALUE);
		else
			setBack(Double.MAX_VALUE);
		setTurnRight(90);
	}

	private void onMove1()
	{
		if (Utils.isNear(getDistanceRemaining(), 0.0))
		{
			double heading = getHeadingRadians();
			myWallDistance.setStartPoint(getX(), getY());
			myWallDistance.setHeading(heading);
			double dForward = myWallDistance.getForwardDistance();
			double dBackward = myWallDistance.getBackwardDistance();

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
//		PaintRobotPath.onPaint(g, "", getTime(), getX(), getY(), Color.YELLOW);

//		myPaint.onPaint(g);
//		myBasics.onPaint(g);
//		myBasics.edgeDiagonals(g, getX(), getY(), getHeadingRadians(), Color.GREEN);
//		myBasics.edgeDiagonals(g, xg, yg, eHeading, Color.BLUE);
//

		myPaintWallDistance.onPaint(g);
		myPaintMaxEscape.onPaint(g);
		myPaintEscapePath.onPaint(g);
		myBulletPaint.onPaint(g);
	}
}
