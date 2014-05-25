package wompi.radar.misc;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import robocode.AdvancedRobot;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import wompi.paint.PaintHelper;
import wompi.robomath.RobotConst;

public class BlindRadarArea
{
	private static double				RADAR_SCAN_DIAMETER;
	private static double				RADAR_SCAN_RADIUS;
	private static Rectangle2D			BATTLE_FIELD;

	private Area						myBlindRadarArea;

	private final ArrayList<Point2D>	border	= new ArrayList<Point2D>();

	public void onInit(AdvancedRobot bot)
	{
		double botW = bot.getWidth() / 2.0;
		double botH = bot.getHeight() / 2.0;
		double bHypo = Math.hypot(botW, botH);
		RADAR_SCAN_DIAMETER = Rules.RADAR_SCAN_RADIUS * 2 + 2 * bHypo;
		RADAR_SCAN_RADIUS = RADAR_SCAN_DIAMETER / 2.0;

		if (BATTLE_FIELD == null)
		{
			BATTLE_FIELD = RobotConst.getBattleField(bot);
		}
	}

	public void onScannedRobot(ScannedRobotEvent e, double x, double y)
	{
		double centerX = x - RADAR_SCAN_RADIUS;
		double centerY = y - RADAR_SCAN_RADIUS;
		Ellipse2D circle = new Ellipse2D.Double(centerX, centerY, RADAR_SCAN_DIAMETER, RADAR_SCAN_DIAMETER);

		myBlindRadarArea = null;
		if (circle.contains(BATTLE_FIELD)) { return; }

		Area area_circle = new Area(circle);
		myBlindRadarArea = new Area(BATTLE_FIELD);
		myBlindRadarArea.subtract(area_circle);
	}

	public void onPaint(Graphics2D g)
	{
		if (myBlindRadarArea == null) { return; }

		g.setColor(Color.red);
		g.draw(myBlindRadarArea);
		g.setColor(PaintHelper.yellowTrans);
		g.fill(myBlindRadarArea);
	}

	private void testPathIterator(Graphics2D g)
	{
//		Rectangle2D bound = rect.getBounds2D();
//		Random rand = new Random();
//		double x = 0;
//		double y = 0;
//		int count = 0;
//		do
//		{
//			x = bound.getX() + rand.nextInt((int) bound.getWidth());
//			y = bound.getY() + rand.nextInt((int) bound.getHeight());
//			count++;
//		}
//		while (!rect.contains(x, y) && count < 100);
//
//		PaintHelper.drawSquare(x, y, 0, 36, Color.BLUE, g);

//		FlatteningPathIterator p = new FlatteningPathIterator(rect.getPathIterator(null), 0.01);
//		double[] point = new double[6];
//		g.setColor(Color.RED);
//		int count = 0;
//
//		double lastX = 0;
//		double lastY = 0;
//		while (!p.isDone())
//		{
//			p.currentSegment(point);
//			//if (bField.contains(point[0], point[1]))
//			{
//				Stroke old = g.getStroke();
//				g.setStroke(new BasicStroke(1));
//				g.setColor(Color.RED);
//				g.drawLine((int) point[0], (int) point[1], (int) point[0], (int) point[1]);
//				g.setStroke(old);
//
//				double dist = Point2D.distance(lastX, lastY, point[0], point[1]);
//
//				System.out.format("Points[%d]: %3.5f %3.5f  - %3.5f\n", count, point[0], point[1], dist);
//				lastX = point[0];
//				lastY = point[1];
//			}
//			p.next();
//			count++;
//		}
//		System.out.format("Points: %d\n", count);
	}
}
