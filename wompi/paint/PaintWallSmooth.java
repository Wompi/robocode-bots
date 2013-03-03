package wompi.paint;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import robocode.AdvancedRobot;
import robocode.RobotStatus;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import wompi.robomath.RobotMath;

public class PaintWallSmooth
{
	private RobotStatus			myStatus;
	private double				bFieldW;
	private double				bFieldH;
	private double				bFieldBorder;
	private double				centerX;
	private double				centerY;

	private final static double	PI_90	= Math.PI / 2.0;
	private final static double	PI_180	= Math.PI;

	public void onInit(AdvancedRobot bot, double fieldBorder)
	{
		bFieldBorder = fieldBorder;
		bFieldH = bot.getBattleFieldHeight() - bFieldBorder;
		bFieldW = bot.getBattleFieldWidth() - bFieldBorder;
		centerX = bot.getBattleFieldWidth() / 2.0;
		centerY = bot.getBattleFieldHeight() / 2.0;
	}

	public void onStatus(StatusEvent e)
	{
		myStatus = e.getStatus();
	}

	public void onScannedRobot(ScannedRobotEvent e)
	{}

	public void onPaint(Graphics2D g)
	{
		double dy = Math.min(bFieldH - myStatus.getY(), myStatus.getY() - bFieldBorder);
		double dx = Math.min(bFieldW - myStatus.getX(), myStatus.getX() - bFieldBorder);

		double alpha = Math.acos(dy / 120);
		double betha = Math.acos(dx / 120);

		Point2D start = new Point2D.Double(myStatus.getX(), myStatus.getY());

		if (!Double.isNaN(alpha))
		{
			double dist = dy / Math.cos(alpha);
			Point2D ende0;
			Point2D ende1;
			if (myStatus.getY() > centerY)
			{
				ende0 = RobotMath.calculatePolarPoint(-alpha, dist, start);
				ende1 = RobotMath.calculatePolarPoint(+alpha, dist, start);
			}
			else
			{
				ende0 = RobotMath.calculatePolarPoint(PI_180 - alpha, dist, start);
				ende1 = RobotMath.calculatePolarPoint(PI_180 + alpha, dist, start);
			}
			PaintHelper.drawLine(start, ende0, g, Color.green);
			PaintHelper.drawLine(start, ende1, g, Color.green);
		}

		if (!Double.isNaN(betha))
		{
			double dist = dx / Math.cos(betha);
			Point2D ende0;
			Point2D ende1;
			if (myStatus.getX() > centerX)
			{
				ende0 = RobotMath.calculatePolarPoint(PI_90 - betha, dist, start);
				ende1 = RobotMath.calculatePolarPoint(PI_90 + betha, dist, start);
			}
			else
			{
				ende0 = RobotMath.calculatePolarPoint(-PI_90 - betha, dist, start);
				ende1 = RobotMath.calculatePolarPoint(-PI_90 + betha, dist, start);
			}
			PaintHelper.drawLine(start, ende0, g, Color.YELLOW);
			PaintHelper.drawLine(start, ende1, g, Color.YELLOW);

		}
	}
}
