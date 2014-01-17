package wompi;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import robocode.AdvancedRobot;
import robocode.RobotStatus;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import wompi.echidna.misc.painter.PaintDiagramm;
import wompi.echidna.misc.painter.PaintMinRiskPoints;
import wompi.echidna.misc.painter.PaintSegmentDiagramm;
import wompi.paint.PaintHelper;
import wompi.robomath.RobotMath;

public class Funnelweb extends AdvancedRobot
{
	final static double	INF			= Double.POSITIVE_INFINITY;

	final static double	PI_360		= Math.PI * 2;
	final static double	PI_90		= Math.PI / 2;
	final static double	PI_180		= Math.PI;
	final static double	PI_270		= Math.PI * 3 / 2;

	double[]			danger;
	double[]			wDanger;

	static final double	GUN_FACTOR	= 30;
	static final int	AIM_START	= 10;
	static final double	AIM_FACTOR	= 1.008;
	static final int	FIRE_FACTOR	= 7;

	static double		lastDistance;

	PaintDiagramm		diagram		= new PaintDiagramm();
	RobotStatus			myStatus;
	double				escapeAngle;
	int					scanCount;

	PaintMinRiskPoints	dangerDebug	= new PaintMinRiskPoints();
	PaintMinRiskPoints	wallDebug	= new PaintMinRiskPoints();
	PaintMinRiskPoints	allDebug	= new PaintMinRiskPoints();

	@Override
	public void run()
	{
		danger = new double[360];
		wDanger = new double[360];
		setAllColors(Color.GREEN);
		setTurnRadarRightRadians(INF);
		setAdjustGunForRobotTurn(true);

	}

	@Override
	public void onStatus(StatusEvent e)
	{
		myStatus = e.getStatus();
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		// Get Abs bearing for aiming routines (and A-Grav)
		// and distance for just about everything else :)
		double absBear = e.getBearingRadians() + getHeadingRadians();
		double dist = e.getDistance();

		scanCount++;
		try
		{
			double myEscape = Double.NEGATIVE_INFINITY;
			int i = -1;
			Point2D start = new Point2D.Double(getX(), getY());
			double myX = getX();
			double myY = getY();
			long time = getTime();

			while (true)
			{
				i += 1;
				double eAngle = Math.toRadians(i);

				double wNORTH = Math.cos(eAngle - 0) / (1000 - myY);
				double wEAST = Math.cos(eAngle - PI_90) / (1000 - myX);
				double wSOUTH = Math.cos(eAngle - PI_180) / (myY);
				double wWEST = Math.cos(eAngle - PI_270) / (myX);

				double wallRisk = (-wNORTH - wEAST - wSOUTH - wWEST) / 15;
				wDanger[i] = wallRisk;
				danger[i] = -Math.cos(eAngle - absBear) / dist + wallRisk;

				Point2D risk = RobotMath.calculatePolarPoint(eAngle, 100, start);
				Point2D wrisk = RobotMath.calculatePolarPoint(eAngle, 110, start);
				Point2D arisk = RobotMath.calculatePolarPoint(eAngle, 120, start);

				dangerDebug.registerRiskPoint(time, risk.getX(), risk.getY(), danger[i], myX, myY, 400);
				wallDebug.registerRiskPoint(time, wrisk.getX(), wrisk.getY(), wallRisk, myX, myY, 300);
				allDebug.registerRiskPoint(time, arisk.getX(), arisk.getY(), danger[i] + wallRisk, myX, myY, 300);

				double wallDanger = danger[i];

				if (wallDanger > myEscape)
				{
					//	System.out.format("break at %d (%3.5f)\n", i, danger[i]);

					setTurnRightRadians(Math.tan(eAngle - getHeadingRadians()));
					setAhead(INF * Math.cos(eAngle - getHeadingRadians()));
					escapeAngle = eAngle;
					myEscape = wallDanger;
				}
			}
		}
		catch (Exception e1)
		{}

		// If we're at 0 and pointed at a target, fire.
		if (setFireBullet(Math.min(2.49999, getEnergy() * GUN_FACTOR / dist)) != null)
//		if (setFireBullet(450 / dist) != null)
		{
			lastDistance = Double.POSITIVE_INFINITY;
		}

		// Lock onto closest bots																
		if (lastDistance + 100 > dist)
		{
			lastDistance = dist;

			// and only the closest bot
			// Radar lock as we approach shooting time
			// Lowering this value causes us to turn more often do to better data.
			if (getGunHeat() < 1)
			{
				// Let this var be equal the the absolute bearing now...
				// and set the radar.
				setTurnRadarLeft(getRadarTurnRemaining());
				// This would be nice to prevent the occasional long shot.				
				//clearAllEvents();
			}
			// Infinity style gun fits!!!!  
			setTurnGunRightRadians(robocode.util.Utils.normalRelativeAngle(absBear
					- getGunHeadingRadians()
					+
//				Math.random() *

//				Math.max(1 - distance / (400), 0) * 
					(e.getVelocity() / (AIM_START + Math.pow(AIM_FACTOR, dist)))
					* Math.sin(e.getHeadingRadians() - absBear)));

		}
		//setTurnRadarLeftRadians(getRadarTurnRemaining());

		//System.out.format("[%03d] angle=%3.5f dist=%d  %s\n", getTime(), absBear, (int) dist, e.getName());

	}

	@Override
	public void onPaint(Graphics2D g)
	{
		PaintSegmentDiagramm.onPaint(g, this, danger, Color.RED, 3);
		PaintSegmentDiagramm.onPaint(g, this, wDanger, Color.LIGHT_GRAY, 21);
		//diagram.onPaint(g, myStatus, getVelocity(), Color.green, getName());
		dangerDebug.onPaint(g, false);
		wallDebug.onPaint(g, false);
		allDebug.onPaint(g, false);

		Point2D start = new Point2D.Double(getX(), getY());
		Point2D ende = RobotMath.calculatePolarPoint(escapeAngle, 400, start);

		PaintHelper.drawLine(start, ende, g, Color.LIGHT_GRAY);

	}
}