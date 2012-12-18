package wompi.robomath;

import java.awt.geom.Point2D;

import robocode.AdvancedRobot;
import robocode.util.Utils;

public class WallHeadingDistance
{
	private static final double	PI_90	= Math.PI / 2.0;
	private static final double	PI_180	= Math.PI;

	private double				bFieldH;
	private double				bFieldW;
	private double				bBorder;

	private double				ex1;
	private double				ex2;
	private double				ex3;
	private double				ex4;

	private double				ey1;
	private double				ey2;
	private double				ey3;
	private double				ey4;

	private double				startX;
	private double				startY;

	private double				dx1;
	private double				dx2;
	private double				dy1;
	private double				dy2;

	private double				forwardDistance;
	private double				backwardDistance;
	private double				eHeading;

	public WallHeadingDistance()
	{
		eHeading = Double.NaN;
	}

	public void onInit(AdvancedRobot bot, double fieldBorder)
	{
		bFieldH = bot.getBattleFieldHeight();
		bFieldW = bot.getBattleFieldWidth();
		bBorder = fieldBorder;

		ex1 = ex2 = bFieldW - bBorder;
		ex3 = ex4 = bBorder;

		ey1 = ey4 = bFieldH - bBorder;
		ey2 = ey3 = bBorder;
	}

	public void setStartPoint(double startPointX, double startPointY)
	{
		startX = startPointX;
		startY = startPointY;
		dy2 = startY - bBorder;
		dy1 = bFieldH - startY - bBorder;
		dx2 = startX - bBorder;
		dx1 = bFieldW - startX - bBorder;
	}

	public void setHeading(double heading)
	{
		eHeading = heading;
		forwardDistance = getDistance(heading);
		backwardDistance = getDistance(heading + PI_180);
	}

	public double getForwardHeading()
	{
		if (Double.isNaN(eHeading))
		{
			System.out.format("ERROR: no heading set for WallHeadingDistance\n");
			return 0;
		}
		return Utils.normalRelativeAngle(eHeading);
	}

	public double getBackWardHeading()
	{
		if (Double.isNaN(eHeading))
		{
			System.out.format("ERROR: no heading set for WallHeadingDistance\n");
			return 0;
		}
		return Utils.normalRelativeAngle(eHeading + PI_180);
	}

	public double getForwardDistance()
	{
		return forwardDistance;
	}

	public double getBackwardDistance()
	{
		return backwardDistance;
	}

	// mostly for debug and painting so it might be OK with Point2D
	public Point2D getStartPoint()
	{
		return new Point2D.Double(startX, startY);
	}

	private double getDistance(double heading)
	{
		double result = 0;
		double h = Utils.normalRelativeAngle(heading);
		double bAngle = h;

		if (Utils.isNear(h, 0))
		{
			result = dy1;
		}
		else if (Utils.isNear(h, PI_180))
		{
			result = dy2;
		}
		else if (h > 0)
		{
			if (Utils.isNear(h, PI_90))
			{
				result = dx1;
			}
			else if (h < PI_90)
			{
				double a1 = Math.atan2(ex1 - startX, ey1 - startY);
				if (Utils.isNear(h, a1))
				{
					result = Math.hypot(dx1, dy1);
				}
				else if (h > a1)
				{
					bAngle = PI_90 - h;
					result = dx1 / Math.cos(bAngle);
				}
				else if (h < a1)
				{
					// bAngle = h;				
					result = dy1 / Math.cos(bAngle);
				}
			}
			else if (h > PI_90)
			{
				double a2 = Math.atan2(ex2 - startX, ey2 - startY);
				if (Utils.isNear(h, a2))
				{
					result = Math.hypot(dx1, dy2);
				}
				else if (h > a2)
				{
					bAngle = PI_180 - h;
					result = dy2 / Math.cos(bAngle);
				}
				else if (h < a2)
				{
					bAngle = h - PI_90;
					result = dx1 / Math.cos(bAngle);
				}
			}
		}
		else if (h < 0)
		{
			if (Utils.isNear(h, -PI_90))
			{
				result = dx2;
			}
			else if (h > -PI_90)
			{
				double a4 = Math.atan2(ex4 - startX, ey4 - startY);
				if (Utils.isNear(h, a4))
				{
					result = Math.hypot(dx2, dy1);
				}
				else if (h > a4)
				{
					//bAngle = h;
					result = dy1 / Math.cos(bAngle);
				}
				else if (h < a4)
				{
					bAngle = PI_90 + h;
					result = dx2 / Math.cos(bAngle);
				}
			}
			else if (h < -PI_90)
			{
				double a3 = Math.atan2(ex3 - startX, ey3 - startY);

				if (Utils.isNear(h, a3))
				{
					result = Math.hypot(dx2, dy2);
				}
				else if (h > a3)
				{
					bAngle = h + PI_90;
					result = dx2 / Math.cos(bAngle);
				}
				else if (h < a3)
				{
					bAngle = PI_180 + h;
					result = dy2 / Math.cos(bAngle);
				}
			}
		}

		// debug maybe
//		rx = startX + Math.sin(h) * result;
//		ry = startY + Math.cos(h) * result;

		return result;
	}
}
