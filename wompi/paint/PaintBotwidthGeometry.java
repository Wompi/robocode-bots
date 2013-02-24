package wompi.paint;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import robocode.AdvancedRobot;
import robocode.RobotStatus;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;

public class PaintBotwidthGeometry
{
	private RobotStatus			myStatus;
	private Rectangle2D			battleField;
	private ScannedRobotEvent	myRobotEvent;
	private RobotStatus			myScanStatus;

	private static final int	BW		= 36;
	private static final int	BW_HALF	= 18;

	AdvancedRobot				myBot;

	public void onInit(AdvancedRobot bot, double fieldBorder)
	{
		myBot = bot;
	}

	public void onStatus(StatusEvent e)
	{
		myStatus = e.getStatus();
	}

	public void onScannedRobot(ScannedRobotEvent e)
	{
		myRobotEvent = e;
		myScanStatus = myStatus;
	}

	public void onPaint(Graphics2D g)
	{
		if (myScanStatus == null) return;

		long xr = Math.round(myScanStatus.getX());
		long yr = Math.round(myScanStatus.getY());

		Point2D e0 = new Point2D.Double(xr - BW_HALF, yr + BW_HALF);
		Point2D e1 = new Point2D.Double(xr + BW_HALF, yr + BW_HALF);
		Point2D e2 = new Point2D.Double(xr + BW_HALF, yr - BW_HALF);
		Point2D e3 = new Point2D.Double(xr - BW_HALF, yr - BW_HALF);

		double absBearing = myScanStatus.getHeadingRadians() + myRobotEvent.getBearingRadians();

		long xe = Math.round(Math.sin(absBearing) * myRobotEvent.getDistance() + xr);
		long ye = Math.round(Math.cos(absBearing) * myRobotEvent.getDistance() + yr);

		Point2D ePos = new Point2D.Double(xe, ye);

		PaintHelper.drawSquare(xr, yr, 0, BW, Color.GREEN, g);

		Line2D edge0 = new Line2D.Double(ePos, e0);
		Line2D edge1 = new Line2D.Double(ePos, e1);
		Line2D edge2 = new Line2D.Double(ePos, e2);
		Line2D edge3 = new Line2D.Double(ePos, e3);

		PaintHelper.drawLine(edge0, g, Color.RED);
		PaintHelper.drawLine(edge1, g, Color.RED);
		PaintHelper.drawLine(edge2, g, Color.RED);
		PaintHelper.drawLine(edge3, g, Color.RED);

		int d0 = (int) Math.ceil(Line2D.ptLineDist(xe, ye, xr - BW_HALF, yr + BW_HALF, xr, yr));
		int d1 = (int) Math.ceil(Line2D.ptLineDist(xe, ye, xr + BW_HALF, yr + BW_HALF, xr, yr));
		int d2 = (int) Math.ceil(Line2D.ptLineDist(xe, ye, xr + BW_HALF, yr - BW_HALF, xr, yr));
		int d3 = (int) Math.ceil(Line2D.ptLineDist(xe, ye, xr - BW_HALF, yr - BW_HALF, xr, yr));

		double c0 = edge0.relativeCCW(xr, yr);
		double c1 = edge1.relativeCCW(xr, yr);
		double c2 = edge2.relativeCCW(xr, yr);
		double c3 = edge3.relativeCCW(xr, yr);

		double dist = myRobotEvent.getDistance();
		double bSpeed = Rules.getBulletSpeed(2.0);
		int tick = (int) Math.ceil(dist / bSpeed);
		long t = myScanStatus.getTime();

		double s0 = d0 * 2.0 / tick;
		double s1 = d1 * 2.0 / tick;
		double s2 = d2 * 2.0 / tick;
		double s3 = d3 * 2.0 / tick;

//		double speed = Math.max(s0, Math.max(s1, Math.max(s2, s3)));
//		myBot.setMaxVelocity(speed);

//		System.out.format("[%04d] E0: %d %3.0f %10.5f (%d)\n", t, d0, c0, s0, tick);
//		System.out.format("[%04d] E1: %d %3.0f %10.5f (%d)\n", t, d1, c1, s1, tick);
//		System.out.format("[%04d] E2: %d %3.0f %10.5f (%d)\n", t, d2, c2, s2, tick);
//		System.out.format("[%04d] E3: %d %3.0f %10.5f (%d)\n", t, d3, c3, s3, tick);
//		System.out.format("\n");
	}
}
