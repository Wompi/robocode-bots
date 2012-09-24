package wompi.paint;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import wompi.robomath.RobotMath;
import wompi.wallaby.PaintHelper;

public class WompiPaint
{
	public static void paintWallDistance(double x, double y, double angle, Graphics2D g, double w, double h)
	{
		Point2D start = new Point2D.Double(x, y);

		Point2D a = new Point2D.Double(18.0, y);
		Point2D b = new Point2D.Double(x, 18.0);

		Point2D c = new Point2D.Double(w - 18.0, y);
		Point2D d = new Point2D.Double(x, h - 18);

		Color cMin = Color.DARK_GRAY;
		Color cMax = Color.LIGHT_GRAY;

		if (start.distance(a) < start.distance(c))
		{
			PaintHelper.drawLine(start, a, g, cMin);
			PaintHelper.drawLine(start, c, g, cMax);
		}
		else
		{
			PaintHelper.drawLine(start, a, g, cMax);
			PaintHelper.drawLine(start, c, g, cMin);
		}

		if (start.distance(b) < start.distance(d))
		{
			PaintHelper.drawLine(start, b, g, cMin);
			PaintHelper.drawLine(start, d, g, cMax);
		}
		else
		{
			PaintHelper.drawLine(start, b, g, cMax);
			PaintHelper.drawLine(start, d, g, cMin);
		}
	}

	public static void paintFourLines(double x, double y, double head, double angle, double distance, Graphics2D g)
	{
		Point2D start = new Point2D.Double(x, y);
		head += angle;
		for (int i = 0; i < 4; i++)
		{
			Point2D end = RobotMath.calculatePolarPoint(head, distance, start);
			PaintHelper.drawLine(start, end, g, Color.YELLOW);
			head += Math.PI / 2.0;
		}
	}
}
