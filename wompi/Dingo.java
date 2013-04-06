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
import wompi.dingo.DingoEnergyDrop;
import wompi.dingo.DingoFireDetection;
import wompi.dingo.DingoInactivity;
import wompi.paint.PaintEnemyBulletWaves;
import wompi.paint.PaintEscapePath;
import wompi.paint.PaintMaxEscapeAngle;
import wompi.paint.PaintWallHeadingDistance;
import wompi.radar.ARadar;
import wompi.radar.RadarSingleLock;
import wompi.radar.RadarStartSearch;
import wompi.robomath.WallHeadingDistance;

public class Dingo extends AdvancedRobot
{
	public static final double				DIST			= 185;
	public static final double				PI_45			= Math.PI / 4.0;
	public static final double				PI_360			= Math.PI * 2.0;
	public static final double				PI_90			= Math.PI / 2.0;
	public static final double				PI				= Math.PI;
	public final static double				BORDER			= 18.0;
	private final static double				INF				= Double.POSITIVE_INFINITY;

	private final ARadar					myRadar;

	private final DingoFireDetection		myFireDetector;
	private final DingoInactivity			myInactivity;
	private final DingoEnergyDrop			myEnergyDrop;
	private final WallHeadingDistance		myWallDistance;

	// debug visualization
	private final PaintWallHeadingDistance	myPaintWallDistance;
	private final PaintEnemyBulletWaves		myBulletPaint;
	private final PaintMaxEscapeAngle		myPaintMaxEscape;
	private final PaintEscapePath			myPaintEscapePath;

	private long							lastScan;

	private double							xg;
	private double							yg;
	private double							eAbsBearing;
	private double							eBearing;
	private double							eHeading;
	private double							eEnergy;
	private double							eDistance;

	// test 
	int										wallHitIndex	= -1;
	boolean									iFire;

	public Dingo()
	{
		myRadar = new RadarStartSearch(new RadarSingleLock());

		myFireDetector = new DingoFireDetection();
		myInactivity = new DingoInactivity();
		myEnergyDrop = new DingoEnergyDrop();
		myWallDistance = new WallHeadingDistance();

		myBulletPaint = new PaintEnemyBulletWaves();
		myPaintWallDistance = new PaintWallHeadingDistance(myWallDistance);
		myPaintMaxEscape = new PaintMaxEscapeAngle();
		myPaintEscapePath = new PaintEscapePath();
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		myRadar.onStatus(e);

		myEnergyDrop.onStatus(e);
		myFireDetector.onStatus(e);

		myBulletPaint.onStatus(e);
		myPaintMaxEscape.onStatus(e);
		myPaintEscapePath.onStatus(e);
	}

	@Override
	public void run()
	{
		myFireDetector.onInit(this);
		myEnergyDrop.onInit(this);
		myWallDistance.onInit(this, BORDER);

		myPaintMaxEscape.onInit(this, BORDER);
		myPaintEscapePath.onInit(this, BORDER);

		setAllColors(Color.ORANGE);

		eEnergy = getEnergy();
		eDistance = 1000;

		myRadar.onInit(this);

		while (true)
		{

			myRadar.onRun();

			myEnergyDrop.onRun();
			if (myEnergyDrop.isInactive()) myFireDetector.onInactivity();
			myFireDetector.onRun();
			boolean eHasFired = myFireDetector.hasFired();
			if (eHasFired)
			{
				// broken after refactor
				//myBulletPaint.onScannedRobot(,myFireDetector.getFiredBulletPower()); // takes the position of the turn before
			}

			//onMove0();
			//onMove1();

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
		myRadar.onHitRobot(e);
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

		double bPower = Math.min(2.99, Math.max(0.1, Math.min(e.getEnergy() / 4.0, 350 / e.getDistance())));

		myPaintMaxEscape.setBulletSpeed(bPower);
		myPaintMaxEscape.onScannedRobot(e);
		myPaintEscapePath.setBulletSpeed(bPower);
		myPaintEscapePath.onScannedRobot(e);

		myFireDetector.onScannedRobot(e);
		myRadar.onScannedRobot(e);
		eAbsBearing = getHeadingRadians() + e.getBearingRadians();
		eHeading = e.getHeadingRadians();
		xg = getX() + Math.sin(eAbsBearing) * e.getDistance();
		yg = getY() + Math.cos(eAbsBearing) * e.getDistance();

		eBearing = e.getBearingRadians();
		eEnergy = e.getEnergy();
		eDistance = e.getDistance();

		iFire = Utils.isNear(getGunTurnRemainingRadians(), 0.0);
		setTurnGunRightRadians(Utils.normalRelativeAngle(eAbsBearing - getGunHeadingRadians()));
	}

	private void onMove0()
	{
		setTurnRightRadians(Math.sin(eBearing));
		setAhead(1000);
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

//			System.out.format("F=%3.5f B=%3.5f D=%2.2f hDiff=%3.10f\n", dForward, dBackward, dir,
//					Math.toDegrees(hDiff) % 90);
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
