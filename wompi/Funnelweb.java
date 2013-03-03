package wompi;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import wompi.echidna.misc.painter.PaintMinRiskPoints;
import wompi.paint.PaintHelper;
import wompi.paint.PaintWallHeadingDistance;
import wompi.paint.PaintWallSmooth;
import wompi.robomath.RobotMath;
import wompi.robomath.WallHeadingDistance;

public class Funnelweb extends AdvancedRobot
{
	private static final double	WZ					= 18.0;
	private static final double	WZ_W				= 800 - 2 * WZ;
	private static final double	WZ_H				= 600 - 2 * WZ;
	private final static double	DIST				= 160;
	private final static double	DELTA_RISK_ANGLE	= Math.PI / 128.0;
	private final static double	PI_360				= Math.PI * 2.0;
	private final static double	PI_180				= Math.PI;
	private final static double	PI_90				= Math.PI / 2.0;
	private final static double	PI_30				= Math.PI / 6.0;
	private final static double	TARGET_FORCE		= 1000;					// 100000 low dmg high surv - 10000 high dmg low surv

	private static double		CENTER_X;
	private static double		CENTER_Y;

	PaintMinRiskPoints			myPaintMinRiskAll	= new PaintMinRiskPoints();
	WallHeadingDistance			myWallDist;
	PaintWallHeadingDistance	myPaintWall;
	PaintWallSmooth				myWallSmooth		= new PaintWallSmooth();

	public Funnelweb()
	{
		myWallDist = new WallHeadingDistance();
		myPaintWall = new PaintWallHeadingDistance(myWallDist);
	}

	@Override
	public void run()
	{
		setAllColors(Color.CYAN);

		myWallDist.onInit(this, 18);
		myWallSmooth.onInit(this, 18.0);
		CENTER_X = getBattleFieldWidth() / 2.0;
		CENTER_Y = getBattleFieldHeight() / 2.0;

		setAdjustGunForRobotTurn(true);
		setAdjustRadarForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		myWallDist.setStartPoint(getX(), getY());
		myWallSmooth.onStatus(e);
		myWallDist.setHeading(getHeadingRadians());

		// debug
		double dx0 = getX() - 18 - CENTER_X;
		double dy0 = getY() - 18 - CENTER_Y;

		double dy = Math.min(getBattleFieldHeight() - getY() - 18.0, getY() - 18.0);
		double dx = Math.min(getBattleFieldWidth() - getX() - 18, getX() - 18.0);

		System.out.format("[%04d] dx0=%3.5f dy0=%3.5f dx=%3.5f dy=%3.5f\n", getTime(), dx0, dy0, dx, dy);

		double alpha = Math.acos(dy / 120);
		double betha = Math.acos(dx / 120);
		System.out.format("[%04d] apha=%3.5f betha=%3.5f head=%3.5f\n", getTime(), Math.toDegrees(alpha),
				Math.toDegrees(betha), getHeading());

	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{

		double absBearing = e.getBearingRadians() + getHeadingRadians();

		double eDist = e.getDistance();
		double xF = -Math.sin(absBearing) / (eDist * eDist);
		double yF = -Math.cos(absBearing) / (eDist * eDist);

		double v0 = Math.cos(e.getBearingRadians());
		double v1 = Math.atan2(xF, yF);

		Point2D start = new Point2D.Double(getX(), getY());
		Point2D end = RobotMath.calculatePolarPoint(v1, 100, start);
		PaintHelper.drawLine(start, end, getGraphics(), Color.MAGENTA);

		System.out.format("[%04d] perp=%3.5f force=%3.5f \n", getTime(), v0, v1);

		//v1 += v0;

		setTurnRightRadians(Math.tan(v1 -= getHeadingRadians()));
		setAhead(120 * Math.cos(v1));

		//setAhead(DIST - Math.abs(getTurnRemaining()));

		setTurnRadarRightRadians(-getRadarTurnRemainingRadians());
	}

	@Override
	public void onPaint(Graphics2D g)
	{
		myPaintWall.onPaint(g);
		myWallSmooth.onPaint(g);
		//myPaintMinRiskAll.onPaint(g, false);
	}

}
