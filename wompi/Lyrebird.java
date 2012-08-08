/*******************************************************************************
 * Copyright (c)  2012  Wompi 
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the ZLIB
 * which accompanies this distribution, and is available at
 * http://robowiki.net/wiki/ZLIB
 * 
 * Contributors:
 *     Wompi - initial API and implementation
 ******************************************************************************/
package wompi;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.Condition;
import robocode.DeathEvent;
import robocode.HitRobotEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.WinEvent;
import robocode.util.Utils;
import wompi.echidna.misc.DisplacementVector;
import wompi.echidna.misc.EnergySlope;
import wompi.echidna.misc.SegmentationStats;
import wompi.echidna.misc.SimpleAverage;
import wompi.echidna.misc.VelocityContainer;
import wompi.echidna.misc.painter.PaintRobotPath;
import wompi.echidna.misc.painter.PaintSegmentDiagramm;
import wompi.wallaby.PaintHelper;
import wompi.wallaby.WallabyPainter;

public class Lyrebird extends AdvancedRobot
{
	private static final double	FIELD_W				= 800.0;
	private static final double	FIELD_H				= 600.0;

	private static final double	WZ					= 25.0;
	private static final double	WZ_SIZE_W			= FIELD_W - 2 * WZ;
	private static final double	WZ_SIZE_H			= FIELD_H - 2 * WZ;
	private static final double	WZ_G				= 17.0;
	private static final double	WZ_G_SIZE_W			= FIELD_W - 2 * WZ_G;
	private static final double	WZ_G_SIZE_H			= FIELD_H - 2 * WZ_G;

	private final static double	DIST				= 110;
	private final static double	DIST_REMAIN			= 20;

	private final static double	RADAR_GUNLOCK		= 1.0;
	private final static double	RADAR_WIDE			= 3.0;
	private final static double	TARGET_FORCE		= 55000;									// 100000 low dmg high surv - 10000 high dmg low surv

	private final static int	EZERO_TURNS			= 10;
	private final static double	TARGET_DISTANCE		= 700.0;

	private final static double	PI_360				= Math.PI * 2.0;
	private final static double	DELTA_RISK_ANGLE	= Math.PI / 32.0;
	private final static double	MAX_HEAD_DIFF		= 0.161442955809475;						// 9.25 degree

	// debug

	// target
	static double				eHeading;
	static double				vAvg;
	static long					avgCount;
	static double				eDanger;

	static double				eBearing;
	static double				ePower;
	static double				lastVelocity;
	boolean						isMove;

	static double				eDistance;
	static double				ex;
	static double				ey;

	static double[]				zeroField			= new double[3];
	boolean						isZero;

	static SimpleAverage		distAvg				= new SimpleAverage(30, "distance");
	static SimpleAverage		distEnemyAvg		= new SimpleAverage(25, "enemy movedist");
	SegmentationStats			veloSegment			= new SegmentationStats(17);

	static VelocityContainer	veloContainer		= new VelocityContainer();

	LyrebirdFireWatcher			myFireWatcher;
	DisplacementVector			myDisplacer;

	@Override
	public void onStatus(StatusEvent e)
	{
		if (getTime() == 0)
		{
			setAllColors(Color.ORANGE);
			setAdjustGunForRobotTurn(true);
			setAdjustRadarForGunTurn(true);
			myFireWatcher = new LyrebirdFireWatcher(this);
			myDisplacer = new DisplacementVector();
			return;
		}
		if (getRadarTurnRemaining() == 0.0) setTurnRadarRightRadians(Double.MAX_VALUE);
		myFireWatcher.onStatus(e);
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent event)
	{
		double v0;
		double xg;
		double yg;
		double rM;
		double v1;
		double h0;
		double h1;
		long i;
		double bPower;
		double v2;

		myFireWatcher.onScannedRobot(event);
		eBearing = event.getBearingRadians();
		veloSegment.addValue(event.getVelocity());
		veloContainer.addValue(event.getVelocity(), getTime());

		xg = ex = Math.sin((rM = (getHeadingRadians() + event.getBearingRadians()))) * (v0 = event.getDistance());
		yg = ey = Math.cos(rM) * v0;

		myDisplacer.registerPostion(ex + getX(), ey + getY(), getTime());

		double lastHead = eHeading;
		eHeading = h1 = event.getHeadingRadians();
		h0 = Utils.normalRelativeAngle(eHeading - lastHead);
		double maxTurnRate = Rules.getTurnRateRadians(lastVelocity);
		if (!Utils.isNear(Math.abs(h0), maxTurnRate) && Math.abs(h0) > maxTurnRate)
		{
			h0 = 0;
			// System.out.format("[%d] headingdiff=%3.20f max=%3.20f last=%3.5f head=%3.5f %s\n",getTime(),Utils.normalRelativeAngle(eHeading -
			// lastHead),maxTurnRate,lastHead,eHeading,zeroStr);
		}
		lastVelocity = event.getVelocity();

		// bPower = Math.min(Rules.MAX_BULLET_POWER,Math.max(0.1,myFireWatcher.ePower-0.1));

		double zeroTime = EnergySlope.calculateSlope(getTime(), event.getEnergy());
		bPower = EnergySlope.caclulateFirePower(getTime(), getEnergy(), zeroTime);

		bPower = Math.min(3.0, Math.max(bPower, 0.1));

		if (getGunTurnRemaining() == 0)
		{
			setFire(bPower);
		}
		setTurnRadarRightRadians(Utils.normalRelativeAngle(getHeadingRadians() + event.getBearingRadians() - getRadarHeadingRadians()) * RADAR_WIDE);

		rM = Double.MAX_VALUE;
		v2 = (((vAvg += (Math.abs(v1 = event.getVelocity())))) / ++avgCount) * Math.signum(event.getVelocity());

		// double depp = Math.signum(veloSegment.getDirection(event.getVelocity()));
		// if (event.getVelocity() == 0)
		// {
		// v2 = vAvg/avgCount * depp ;
		// }
		// v2 = veloSegment.getDirection(event.getVelocity());
		// v2 = event.getVelocity() *
		// System.out.format("[%d] v2=%3.2f v=%3.2f \n", getTime(), v2,event.getVelocity());

		v1 = v0 = i = 0;

		while ((v0 += DELTA_RISK_ANGLE) <= PI_360)
		{
			double bulletD;
			double hypoD;
			if ((bulletD = (++i * (20.0 - 3.0 * bPower))) < (hypoD = Math.hypot(xg, yg)))
			{
				xg += Math.sin(h1) * v2;
				yg += Math.cos(h1) * v2;
				double relD = Point2D.distance(xg, yg, ex, ey);
				double turnRel = relD / i;
				double turnAvg = distEnemyAvg.avg() * 1.2;
				double diplacerDist = myDisplacer.avgDist(bPower, event.getDistance(), getTime());
				// System.out.format("[%d][%d] bulletD=%3.2f hypo=%3.2f relD=%3.2f turnRel=%3.2f turnAvg=%3.2f \n",
				// getTime(),i,bulletD,hypoD,relD,turnRel,turnAvg);
				System.out.format("[%d][%d] bulletD=%3.2f hypo=%3.2f relD=%3.2f avgDist=%3.2f eAvgD=%3.2f displacer=%3.2f", getTime(), i, bulletD,
						hypoD, relD, distAvg.avg(), turnAvg, diplacerDist);

				// boolean check = (relD > turnAvg);
				boolean check = (relD > diplacerDist);
				if (!new Rectangle2D.Double(WZ_G, WZ_G, WZ_G_SIZE_W, WZ_G_SIZE_H).contains(xg + getX(), yg + getY()) || check)
				{
					v2 = -v2;
					System.out.format("****");
				}
				h1 += h0;
				System.out.format("\n");
			}
		}

		setTurnGunRightRadians(Utils.normalRelativeAngle(Math.atan2(xg, yg) - getGunHeadingRadians()));
		// if (myFireWatcher.waveCount > 0 )
		if (Math.abs(getDistanceRemaining()) < DIST_REMAIN)
		{
			// Math.sin(getTime()/30)/5
			setTurnRightRadians(Math.tan(v1 = (Math.atan2(myFireWatcher.mX - getX(), myFireWatcher.mY - getY()) - getHeadingRadians())));
			setAhead(DIST * Math.cos(v1));
			// setAhead(DIST);
		}
		else if (getTurnRemainingRadians() == 0) setTurnRightRadians(Math.sin(getDistanceRemaining() / 30) / 5);

		// if (myFireWatcher.eGunHeat <= 0.4)
		// {
		// //System.out.format("[%d] FULL STOP %3.2f - myV=%3.2f\n",getTime(),myFireWatcher.eGunHeat,getVelocity());
		// setAhead(0);
		// setBodyColor(Color.YELLOW);
		// }
		// else
		// {
		// setBodyColor(Color.GREEN);
		// }

		LyrebirdVirtualBullet wave = new LyrebirdVirtualBullet();
		wave.myPower = bPower;
		wave.gx = xg + getX();
		wave.gy = yg + getY();

		wave.tx = ex + getX();
		wave.ty = ey + getY();

		wave.rx = getX();
		wave.ry = getY();
		addCustomEvent(wave);
	}

	public void onBulletHit(BulletHitEvent e)
	{
		myFireWatcher.onBulletHit(e);
	}

	public void onHitRobot(HitRobotEvent e)
	{
		myFireWatcher.onHitRobot(e);
	}

	public void onDeath(DeathEvent e)
	{
		eDanger++;
	}

	public void onWin(WinEvent e)
	{
		eDanger--;
	}

	@Override
	public void onPaint(Graphics2D g)
	{
		PaintRobotPath.onPaint(g, getName(), getTime(), getX(), getY(), Color.GREEN);
		myFireWatcher.onPaint(g);
		PaintSegmentDiagramm.onPaint(g, this, veloSegment.segmentField, Color.GREEN);
		PaintHelper.drawLine(new Point2D.Double(getX(), getY()), new Point2D.Double(myFireWatcher.mX, myFireWatcher.mY), g, Color.BLUE);
		// veloSegment.onPaint(g);
		myDisplacer.onPaint(g, Color.lightGray);
	}

	class LyrebirdVirtualBullet extends Condition
	{
		double			rx;
		double			ry;

		double			tx;
		double			ty;

		double			gx;
		double			gy;
		double			myPower;
		private double	count;

		@Override
		public boolean test()
		{
			double distance = (20 - 3.0 * myPower) * count++;
			double enemyDist = (Point2D.distance(rx, ry, ex + getX(), ey + getY()) - 18);

			// if (distance > 0.8 * enemyDist)
			{
				// PaintHelper.drawArc(new Point2D.Double(rx, ry), distance, 0, PI_360, false, getGraphics(), PaintHelper.yellowTrans);
				PaintHelper.drawPoint(new Point2D.Double(gx, gy), Color.RED, getGraphics(), 4);
				PaintHelper.drawPoint(new Point2D.Double(tx, ty), Color.CYAN, getGraphics(), 4);
			}

			if (distance > enemyDist)
			{
				double deltaDist = (Point2D.distance(gx, gy, ex + getX(), ey + getY()));
				double eDeltaDist = (Point2D.distance(tx, ty, ex + getX(), ey + getY()));

				double avgD = distAvg.avg(deltaDist, getTime());
				double eAvgD = distEnemyAvg.avg(eDeltaDist, getTime());
				System.out
						.format("[%d] deltaDist=%3.2f avgDist=%3.2f eDeltaDist=%3.2f eAvgD=%3.2f \n", getTime(), deltaDist, avgD, eDeltaDist, eAvgD);
				removeCustomEvent(this);
			}
			return false;
		}
	}
}

class LyrebirdFireWatcher
{
	AdvancedRobot	myRobot;

	double			eEnergy;
	double			eGunHeat;
	double			ePower;

	// debug
	double			tx;
	double			ty;
	// int bCount;

	double			mX;
	double			mY;

	double[][]		fireWaves	= new double[3][5];
	int				waveCount;
	int				index;

	public LyrebirdFireWatcher(AdvancedRobot robot)
	{
		myRobot = robot;
		eGunHeat = myRobot.getGunHeat();
		eEnergy = myRobot.getEnergy();
	}

	public void onStatus(StatusEvent e)
	{
		eGunHeat -= myRobot.getGunCoolingRate();
		// System.out.format("[%d] decrease gunheat %3.2f me(%3.2f)\n", e.getTime(),eGunHeat,myRobot.getGunHeat());
	}

	public void onBulletHit(BulletHitEvent e)
	{
		eEnergy -= Rules.getBulletDamage(e.getBullet().getPower());
		// System.out.format("[%d] HIT eEnergy=%3.2f dmg=%3.2f \n", myRobot.getTime(),eEnergy,e.getEnergy());
	}

	public void onHitRobot(HitRobotEvent e)
	{
		// System.out.format("[%d] CRASH eEnergy=%3.2f HIT \n", myRobot.getTime(),eEnergy);
		eEnergy -= 0.6;
	}

	public void onScannedRobot(ScannedRobotEvent event)
	{
		double diff = eEnergy - (eEnergy = event.getEnergy());
		// System.out.format("[%d] eEnergy=%3.2f diff=%3.2f eGunHeat=%3.2f", myRobot.getTime(),eEnergy,diff,eGunHeat);
		double absBearing = myRobot.getHeadingRadians() + event.getBearingRadians();
		tx = myRobot.getX() + Math.sin(absBearing) * event.getDistance();
		ty = myRobot.getY() + Math.cos(absBearing) * event.getDistance();
		if (diff >= Rules.MIN_BULLET_POWER && diff <= Rules.MAX_BULLET_POWER && eGunHeat <= 0.0)
		{
			ePower = diff;
			eGunHeat = Rules.getGunHeat(ePower);
			index = waveCount % fireWaves.length;
			fireWaves[index][0] = tx;
			fireWaves[index][1] = ty;
			fireWaves[index][2] = 0;
			fireWaves[index][3] = ePower;
			fireWaves[index][4] = absBearing;
			waveCount++;
		}

		double rM = Double.MAX_VALUE;

		for (int i = 0; i < fireWaves.length; i++)
		{
			if (fireWaves[i][0] > 0)  //
			{
				double distance = Rules.getBulletSpeed(fireWaves[i][3]) * ++fireWaves[i][2];

				double angle = 0;

				while ((angle += (Math.PI / 32.0)) <= Math.PI * 2)
				{
					double _x = fireWaves[i][0] + Math.sin(angle) * distance;
					double _y = fireWaves[i][1] + Math.cos(angle) * distance;
					double r1 = 0;
					if (new Rectangle2D.Double(70, 70, 800 - 2 * 70, 600 - 2 * 70).contains(_x, _y))
					{
						r1 = 55000 / (Point2D.distanceSq(tx, ty, _x, _y));

						// if ( Math.abs(Utils.normalAbsoluteAngle(fireWaves[index][4] - Math.PI)-angle) < Math.PI/18 )
						// {
						// r1 += 2000.0;
						// }

						if ((r1 += Math.abs(Math.cos(Math.atan2(tx - _x, ty - _y) - angle))) < rM)
						// if (r1 < rM)
						{
							rM = r1;
							mX = _x;
							mY = _y;
						}
						// PaintMinRiskPoints.registerRiskPoint(myRobot.getTime(), _x, _y, r1);
					}
				}
				PaintHelper.drawArc(new Point2D.Double(fireWaves[i][0], fireWaves[i][1]), distance, 0, Math.PI * 2, false, myRobot.getGraphics(),
						PaintHelper.whiteTrans);
			}
		}
	}

	public void onPaint(Graphics2D g)
	{
		WallabyPainter.drawGunHeat(g, eGunHeat, myRobot);
		PaintHelper.drawPoint(new Point2D.Double(mX, mY), Color.CYAN, g, 10);
		// PaintMinRiskPoints.onPaint(g);
	}

}
