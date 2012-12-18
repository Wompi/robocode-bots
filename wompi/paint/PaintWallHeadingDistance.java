package wompi.paint;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import wompi.robomath.RobotMath;
import wompi.robomath.WallHeadingDistance;

public class PaintWallHeadingDistance
{
	private final WallHeadingDistance	myWallDistance;

	public PaintWallHeadingDistance(WallHeadingDistance wallDistance)
	{
		myWallDistance = wallDistance;
	}

	public void onPaint(Graphics2D g)
	{
		Point2D myStartPoint = myWallDistance.getStartPoint();
		if (myStartPoint == null)
		{
			System.out.format("ERROR: no startpoint set for PaintWallHeadingDistance\n");
			return;
		}

		Point2D forwardPoint = RobotMath.calculatePolarPoint(myWallDistance.getForwardHeading(),
				myWallDistance.getForwardDistance(), myStartPoint);
		Point2D backWardPoint = RobotMath.calculatePolarPoint(myWallDistance.getBackWardHeading(),
				myWallDistance.getBackwardDistance(), myStartPoint);

		PaintHelper.drawLine(myStartPoint, forwardPoint, g, Color.GREEN);
		PaintHelper.drawLine(myStartPoint, backWardPoint, g, Color.GREEN.darker().darker());

//				PaintHelper.drawArc(bP, 100, 0, a1, false, g, Color.YELLOW);
//				PaintHelper.drawArc(bP, 90, 0, a2, false, g, Color.YELLOW);
//				PaintHelper.drawArc(bP, 80, 0, a3, false, g, Color.YELLOW);
//				PaintHelper.drawArc(bP, 70, 0, a4, false, g, Color.YELLOW);
//				PaintHelper.drawArc(bP, 50, 0, h1, false, g, Color.RED);
//			Point2D e1Pos = new Point2D.Double(ex1, ey1);
//			Point2D e2Pos = new Point2D.Double(ex2, ey2);
//			Point2D e3Pos = new Point2D.Double(ex3, ey3);
//			Point2D e4Pos = new Point2D.Double(ex4, ey4);
//		
//			PaintHelper.drawLine(startPos, e1Pos, g, Color.DARK_GRAY.darker());
//			PaintHelper.drawLine(startPos, e2Pos, g, Color.DARK_GRAY.darker());
//			PaintHelper.drawLine(startPos, e3Pos, g, Color.DARK_GRAY.darker());
//			PaintHelper.drawLine(startPos, e4Pos, g, Color.DARK_GRAY.darker());
//		
	}
}
