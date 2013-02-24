package wompi;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import robocode.HitWallEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.util.Utils;
import wompi.paint.PaintBotwidthGeometry;
import wompi.paint.PaintBulletHits;
import wompi.paint.PaintHelper;
import wompi.paint.PaintRobotPath;
import wompi.robomath.RobotMath;

public class Nabarlek extends AdvancedRobot
{

	private static double		DIR;
	private static double		eEnergy;
	private static double		bSpeed;
	private static double		speedUp;

	private static final int	BW				= 36;
	private static final int	BW_HALF			= 18;

	// debug
	PaintBotwidthGeometry		myBotwidth		= new PaintBotwidthGeometry();
	PaintBulletHits				myBuletHits		= new PaintBulletHits();

	ArrayList<BotEdge>			myEscapeSquares	= new ArrayList<BotEdge>();

	double						xT;
	double						yT;

	@Override
	public void run()
	{
		setAllColors(Color.LIGHT_GRAY);
		myBotwidth.onInit(this, 18.0);
		myBuletHits.onInit(this, 18.0);
		eEnergy = getEnergy();
		bSpeed = 19.7;
		speedUp = 0;

		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);
		setTurnRadarRightRadians(DIR = Double.POSITIVE_INFINITY);
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		myBotwidth.onStatus(e);
		myBuletHits.onStatus(e);
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		myBotwidth.onScannedRobot(e);
		myBuletHits.onScannedRobot(e);

		double absBearing = getHeadingRadians() + e.getBearingRadians();
		double dist = e.getDistance();

		long xe = Math.round(Math.sin(absBearing) * dist + getX());
		long ye = Math.round(Math.cos(absBearing) * dist + getY());

		// debug
		xT = xe;
		yT = ye;

		int d0 = (int) Math.ceil(Line2D.ptLineDist(xe, ye, getX() - BW_HALF, getY() + BW_HALF, getX(), getY()));
		int d1 = (int) Math.ceil(Line2D.ptLineDist(xe, ye, getX() + BW_HALF, getY() + BW_HALF, getX(), getY()));
		int d2 = (int) Math.ceil(Line2D.ptLineDist(xe, ye, getX() + BW_HALF, getY() - BW_HALF, getX(), getY()));
		int d3 = (int) Math.ceil(Line2D.ptLineDist(xe, ye, getX() - BW_HALF, getY() - BW_HALF, getX(), getY()));

		Point2D ePos = new Point2D.Double(xe, ye);
		Point2D b0 = new Point2D.Double(getX() - BW_HALF, getY() + BW_HALF);
		Point2D b1 = new Point2D.Double(getX() + BW_HALF, getY() + BW_HALF);
		Point2D b2 = new Point2D.Double(getX() + BW_HALF, getY() - BW_HALF);
		Point2D b3 = new Point2D.Double(getX() - BW_HALF, getY() - BW_HALF);

		Point2D bPos = new Point2D.Double(getX(), getY());
		double bAngle = RobotMath.calculateAngle(ePos, bPos);

		ArrayList<BotEdge> edges = new ArrayList<BotEdge>();

		double angle;
		edges.add(new BotEdge(angle = RobotMath.calculateAngle(ePos, b0), angle - bAngle, xe, ye));
		edges.add(new BotEdge(angle = RobotMath.calculateAngle(ePos, b1), angle - bAngle, xe, ye));
		edges.add(new BotEdge(angle = RobotMath.calculateAngle(ePos, b2), angle - bAngle, xe, ye));
		edges.add(new BotEdge(angle = RobotMath.calculateAngle(ePos, b3), angle - bAngle, xe, ye));

		Collections.sort(edges);

		// take just the two max;
		BotEdge e0 = edges.get(0);
		BotEdge e1 = edges.get(1);
		BotEdge e2 = edges.get(2);
		BotEdge e3 = edges.get(3);

		System.out.format("[%04d] a0=%10.5f a1=%10.5f a2=%10.5f a3=%10.5f\n", getTime(), Math.toDegrees(e0.edgeDelta),
				Math.toDegrees(e1.edgeDelta), Math.toDegrees(e2.edgeDelta), Math.toDegrees(e3.edgeDelta));

		Point2D newBPos0 = RobotMath.calculatePolarPoint(e0.edgeAngle + e0.edgeDelta, dist, ePos);
		PaintHelper.drawSquare(newBPos0.getX(), newBPos0.getY(), 0, 36, Color.CYAN, getGraphics());
		Point2D newBPos1 = RobotMath.calculatePolarPoint(e1.edgeAngle + e1.edgeDelta, dist, ePos);
		PaintHelper.drawSquare(newBPos1.getX(), newBPos1.getY(), 0, 36, Color.CYAN, getGraphics());

		// debug
		double a0 = Math.asin(d0 / dist);
		double a1 = Math.asin(d1 / dist);
		double a2 = Math.asin(d2 / dist);
		double a3 = Math.asin(d3 / dist);
//		System.out.format("[%04d] a0=%10.5f a1=%10.5f a2=%10.5f a3=%10.5f\n", getTime(), Math.toDegrees(a0),
//				Math.toDegrees(a1), Math.toDegrees(a2), Math.toDegrees(a3));

		double eDelta = eEnergy - (eEnergy = e.getEnergy());
		if (eDelta > 0 && eDelta <= 3)
		{
			bSpeed = Rules.getBulletSpeed(eDelta);
			System.out.format("[%04d] fire = %3.2f \n", getTime(), eDelta);

			myEscapeSquares.clear();
			myEscapeSquares.add(e0);
			myEscapeSquares.add(e1);

		}

		for (BotEdge edge : myEscapeSquares)
		{
			Point2D pos = new Point2D.Double(edge.bX, edge.bY);
			Point2D _a0 = RobotMath.calculatePolarPoint(edge.edgeAngle + edge.edgeDelta, dist, pos);
			PaintHelper.drawSquare(_a0.getX(), _a0.getY(), 0, 36, Color.RED, getGraphics());
		}

		int tick = (int) Math.ceil(dist / bSpeed);

		double s0 = d0 * 2.0 / tick;
		double s1 = d1 * 2.0 / tick;
		double s2 = d2 * 2.0 / tick;
		double s3 = d3 * 2.0 / tick;

		setTurnRightRadians(Math.cos(e.getBearingRadians()));
		double speed = Math.max(s0, Math.max(s1, Math.max(s2, s3)));
		setMaxVelocity(speed = Math.max(0, speedUp--) + speed + Math.abs(getTurnRemaining() / 10));

		//System.out.format("[%04d] Speed: %3.2f (%3.4f) angle=%3.4f\n", getTime(), speed, bSpeed, getTurnRemaining());

		if (!Double
				.isNaN(speed = (Utils.normalRelativeAngle(absBearing - getRadarHeadingRadians()) * Double.POSITIVE_INFINITY)))
		{
			setTurnRadarRightRadians(speed);
		}
		setAhead(DIR);
	}

	@Override
	public void onHitByBullet(HitByBulletEvent e)
	{
		eEnergy += Rules.getBulletHitBonus(e.getPower());
		myBuletHits.onHitByBullet(e);

	}

	@Override
	public void onBulletHit(BulletHitEvent e)
	{
		eEnergy -= Rules.getBulletDamage(e.getBullet().getPower());
	}

	@Override
	public void onHitWall(HitWallEvent e)
	{
		DIR = -DIR;
		speedUp = 15;
	}

	@Override
	public void onPaint(Graphics2D g)
	{
		PaintRobotPath.onPaint(g, "", getTime(), xT, yT, Color.DARK_GRAY);
		PaintRobotPath.onPaint(g, getName(), getTime(), getX(), getY(), Color.YELLOW.darker().darker().darker());
		myBotwidth.onPaint(g);
		myBuletHits.onPaint(g);
	}
}

class BotEdge implements Comparable<BotEdge>
{
	double	edgeAngle;
	double	edgeDelta;
	double	bX;
	double	bY;

	public BotEdge(double angle, double delta, double x, double y)
	{
		edgeAngle = angle;
		edgeDelta = delta;
		bX = x;
		bY = y;
	}

	@Override
	public int compareTo(BotEdge o)
	{
		double e0 = Math.abs(edgeDelta);
		double e1 = Math.abs(o.edgeDelta);
		double deltaE = (e1 - e0);

		//System.out.format("sort %3.4f %3.4f %3.4f\n", e0, e1, deltaE);

		if (deltaE < 0)
			return -1;
		else if (deltaE > 0) return 1;
		return 0;
	}
}