package wompi.paint;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import robocode.AdvancedRobot;
import robocode.HitByBulletEvent;
import robocode.RobotStatus;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import wompi.robomath.RobotMath;

public class PaintBulletHits
{
	private RobotStatus					myStatus;
	private Rectangle2D					battleField;
	private ScannedRobotEvent			myRobotEvent;
	private RobotStatus					myScanStatus;
	private RobotStatus					myLastStatus;

	private static ArrayList<BulletHit>	myBulletHits;

	private static final int			BW		= 36;
	private static final int			BW_HALF	= 18;

	AdvancedRobot						myBot;

	public void onInit(AdvancedRobot bot, double fieldBorder)
	{
		myBot = bot;

		// if you only want round based hits comment this
		if (myBulletHits == null)
		{
			myBulletHits = new ArrayList<BulletHit>();
		}
	}

	public void onStatus(StatusEvent e)
	{
		myLastStatus = myStatus;
		myStatus = e.getStatus();
	}

	public void onScannedRobot(ScannedRobotEvent e)
	{
		myRobotEvent = e;
		myScanStatus = myStatus;
	}

	public void onHitByBullet(HitByBulletEvent e)
	{
		BulletHit bh = new BulletHit();
		bh.e = e;
		bh.hitStatus = myLastStatus;
		myBulletHits.add(bh);
	}

	public void onPaint(Graphics2D g)
	{
		if (myLastStatus == null) return;

		for (BulletHit b : myBulletHits)
		{
			Point2D bPos = new Point2D.Double(b.e.getBullet().getX(), b.e.getBullet().getY());
			Point2D hPos = RobotMath.calculatePolarPoint(b.e.getHeadingRadians() + Math.PI, 100, bPos);

			Point2D myPos = new Point2D.Double(b.hitStatus.getX(), b.hitStatus.getY());

			// all this stuff should be easier to calculate but I constantly get nuts on these trigs :(
			// - if the veeocity is negative the heading is 180deg off
			// - if the angle is less than 45deg or more than 135deg the cos has to be taken   
			double offset = 0;
			double vSig = Math.signum(b.hitStatus.getVelocity());
			if (vSig < 0)
			{
				offset = Math.PI;
			}

			double absAngle = b.hitStatus.getHeadingRadians() + offset;
			double relAngle = Math.sin(absAngle);
			//System.out.format("[%04d] hSin = %3.5f (%3.5f) \n", myBot.getTime(), hCos, b.hitStatus.getHeading());
			if (Math.abs(relAngle) <= Math.PI / 4.0) relAngle = Math.cos(absAngle);
			double dist_to_border = Math.abs(18.0 / relAngle);
			Point2D h1Pos = RobotMath.calculatePolarPoint(absAngle, dist_to_border, myPos);

			PaintHelper.drawLine(myPos, h1Pos, g, Color.LIGHT_GRAY);
			PaintHelper.drawSquare(b.hitStatus.getX(), b.hitStatus.getY(), 0.0, 36, Color.GREEN, g);
			PaintHelper.drawLine(bPos, hPos, g, Color.YELLOW);

			if (!b.isPrinted)
			{
				Line2D bLine = new Line2D.Double(bPos, hPos);
				double bDist = bLine.ptLineDist(myPos); // distance bot center to bullet line
				double c0 = bLine.relativeCCW(myPos); // which side of the bullet line is our point
				double c1 = bLine.relativeCCW(h1Pos); // reference point to see on which side of the line the point sits	
				//System.out.format("[%04d] bDist: %10.5f %3.0f %3.0f v=%3.0f\n", myBot.getTime(), bDist, c0, c1, vSig);

				// whith this adjust the reference point is always +1 and the dist is relative to this side
				// -dist means the enemy shoots ahead +dist means the enemy shoot back
				bDist *= vSig * c0;
				System.out.format("[%04d] bDist: %10.5f \n", myBot.getTime(), bDist);
				b.isPrinted = true;
			}
		}

	}
}

class BulletHit
{
	HitByBulletEvent	e;
	RobotStatus			hitStatus;
	boolean				isPrinted;
}