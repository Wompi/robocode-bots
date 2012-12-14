package wompi.dingo;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import robocode.util.Utils;
import wompi.wallaby.PaintHelper;

public class DingoWallDistance_Heading
{
	private static final double	PI_90	= Math.PI / 2.0;
	private static final double	PI_180	= Math.PI;
	private static final double	PI_360	= Math.PI * 2.0;

	private final double		bFieldH;
	private final double		bFieldW;
	private final double		bBorder;

	private final double		ex1;
	private final double		ex2;
	private final double		ex3;
	private final double		ex4;

	private final double		ey1;
	private final double		ey2;
	private final double		ey3;
	private final double		ey4;

	private double				startX;
	private double				startY;

	private double				dx1;
	private double				dx2;
	private double				dy1;
	private double				dy2;

	// debug
	private double				rx;
	private double				ry;

	public DingoWallDistance_Heading(double battleFieldH, double battleFieldW, double borderDelta)
	{
		bFieldH = battleFieldH;
		bFieldW = battleFieldW;
		bBorder = borderDelta;

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

	public double getDistance(double heading)
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
		rx = startX + Math.sin(h) * result;
		ry = startY + Math.cos(h) * result;

		return result;
	}

	public void onPaint(Graphics2D g, Color color)
	{
		Point2D startPos = new Point2D.Double(startX, startY);
		Point2D borderPos = new Point2D.Double(rx, ry);
		PaintHelper.drawLine(startPos, borderPos, g, color);

//		PaintHelper.drawArc(bP, 100, 0, a1, false, g, Color.YELLOW);
//		PaintHelper.drawArc(bP, 90, 0, a2, false, g, Color.YELLOW);
//		PaintHelper.drawArc(bP, 80, 0, a3, false, g, Color.YELLOW);
//		PaintHelper.drawArc(bP, 70, 0, a4, false, g, Color.YELLOW);
//		PaintHelper.drawArc(bP, 50, 0, h1, false, g, Color.RED);
//
		Point2D e1Pos = new Point2D.Double(ex1, ey1);
		Point2D e2Pos = new Point2D.Double(ex2, ey2);
		Point2D e3Pos = new Point2D.Double(ex3, ey3);
		Point2D e4Pos = new Point2D.Double(ex4, ey4);

		PaintHelper.drawLine(startPos, e1Pos, g, Color.DARK_GRAY.darker());
		PaintHelper.drawLine(startPos, e2Pos, g, Color.DARK_GRAY.darker());
		PaintHelper.drawLine(startPos, e3Pos, g, Color.DARK_GRAY.darker());
		PaintHelper.drawLine(startPos, e4Pos, g, Color.DARK_GRAY.darker());

	}
}
