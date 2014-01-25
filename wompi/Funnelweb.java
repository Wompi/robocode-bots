package wompi;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.RobotStatus;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import wompi.echidna.misc.painter.PaintMinRiskPoints;
import wompi.paint.PaintRiskFunction;

public class Funnelweb extends AdvancedRobot
{
	final static double					INF					= Double.POSITIVE_INFINITY;

	final static double					PI_360				= Math.PI * 2;
	final static double					PI_90				= Math.PI / 2;
	final static double					PI_180				= Math.PI;
	final static double					PI_270				= Math.PI * 3 / 2;

	static HashMap<String, double[]>	allTargets;

	static final double					GUN_FACTOR			= 30;
	static final int					AIM_START			= 10;
	static final double					AIM_FACTOR			= 1.008;
	static final int					FIRE_FACTOR			= 7;

	static double						lastDistance;

	RobotStatus							myStatus;
	double								escapeAngle;
	int									scanCount;

	PaintMinRiskPoints					dangerDebug			= new PaintMinRiskPoints();
	PaintMinRiskPoints					wallDebug			= new PaintMinRiskPoints();
	PaintMinRiskPoints					allDebug			= new PaintMinRiskPoints();

	PaintRiskFunction					debugRiskFunction	= new PaintRiskFunction();

	@Override
	public void run()
	{
		debugRiskFunction.onInit(this, true);
		allTargets = new HashMap<String, double[]>();

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
		double[] enemy;
		String name;
		if ((enemy = allTargets.get(name = e.getName())) == null)
		{
			allTargets.put(name, enemy = new double[360]);
		}

		double absBear = e.getBearingRadians() + getHeadingRadians();
		double dist = e.getDistance();

		scanCount++;
		try
		{
			int i = -1;

			while (true)
			{
				i += 1;
				double eAngle = Math.toRadians(i);
				enemy[i] = (-Math.cos(eAngle - absBear) / dist);
			}
		}
		catch (Exception e1)
		{
			debugRiskFunction.addRiskFunctionValues(e.getName(), enemy);

			double[] allDanger = new double[360];
			for (Map.Entry<String, double[]> eDanger : allTargets.entrySet())
			{
				try
				{
					int i = 0;
					while (true)
					{
						allDanger[i] += eDanger.getValue()[i];
						i++;
					}
				}
				catch (Exception e2)
				{

				}
			}

			double maxValue = Double.MIN_VALUE;
			double[] wallDanger = new double[360];
			int i = 0;
			double myX = getX();
			double myY = getY();

			try
			{
				while (true)
				{
					double eAngle = Math.toRadians(i);
					double wNORTH = Math.cos(eAngle - 0) / (1000 - myY);
					double wEAST = Math.cos(eAngle - PI_90) / (1000 - myX);
					double wSOUTH = Math.cos(eAngle - PI_180) / (myY);
					double wWEST = Math.cos(eAngle - PI_270) / (myX);
					double wallRisk = (-wNORTH - wEAST - wSOUTH - wWEST);
					wallDanger[i] = wallRisk;
					allDanger[i] += wallRisk;
					if (allDanger[i] > maxValue)
					{

						maxValue = allDanger[i];
						setTurnRightRadians(Math.tan(eAngle - getHeadingRadians()));
						setAhead(INF * Math.cos(eAngle - getHeadingRadians()));
					}
					i++;
				}
			}
			catch (Exception e3)
			{
				debugRiskFunction.addRiskFunctionValues("Danger for all", allDanger);
				debugRiskFunction.addRiskFunctionValues("Walldanger", wallDanger);
			}

		}

		if (setFireBullet(450 / dist) != null)
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
	}

	private double[] fillDanger(double absBearing, double dist, double[] field)
	{
		return field;
	}

	@Override
	public void onPaint(Graphics2D g)
	{
//		PaintSegmentDiagramm.onPaint(g, this, danger, Color.RED, 3);
//		PaintSegmentDiagramm.onPaint(g, this, wDanger, Color.LIGHT_GRAY, 21);
		//diagram.onPaint(g, myStatus, getVelocity(), Color.green, getName());
//		dangerDebug.onPaint(g, false);
//		wallDebug.onPaint(g, false);
//		allDebug.onPaint(g, false);

//		Point2D start = new Point2D.Double(getX(), getY());
//		Point2D ende = RobotMath.calculatePolarPoint(escapeAngle, 400, start);
//
//		PaintHelper.drawLine(start, ende, g, Color.LIGHT_GRAY);

		debugRiskFunction.onPaint(g);

	}

	@Override
	public void onRobotDeath(RobotDeathEvent e)
	{
		debugRiskFunction.removeRiskFunctionValues(e.getName());
	}
}