package wompi.paint;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;

import robocode.AdvancedRobot;
import robocode.RobotStatus;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.util.Utils;
import wompi.robomath.RobotPrecisePrediction;

public class PaintEscapePath
{
	private static final double	PI_360	= Math.PI * 2.0;
	private static final double	PI_180	= Math.PI;
	private static final double	PI_45	= Math.PI / 4.0;

	private static final double	DELTA_P	= Math.PI / 36;	// 36 = 5 degree 

	private RobotStatus			myStatus;
	private RobotStatus			myScanStatus;
	private ScannedRobotEvent	myRobotEvent;
	private Rectangle2D			battleField;

	private double				bSpeed;

	public PaintEscapePath()
	{

	}

	public void onInit(AdvancedRobot bot, double fieldBorder)
	{
		battleField = new Rectangle2D.Double(fieldBorder, fieldBorder, bot.getBattleFieldWidth() - 2.0 * fieldBorder,
				bot.getBattleFieldHeight() - 2.0 * fieldBorder);
	}

	public void onStatus(StatusEvent e)
	{
		myStatus = e.getStatus();
	}

	public void setBulletSpeed(double bPower)
	{
		bSpeed = Rules.getBulletSpeed(bPower);
	}

	public void onScannedRobot(ScannedRobotEvent e)
	{
		myRobotEvent = e;
		myScanStatus = myStatus;
	}

	public void onPaint(Graphics2D g)
	{
		paintForward(g, 1, Color.RED);
		paintForward(g, -1, Color.GREEN);
	}

	private void paintForward(Graphics2D g, int direction, Color color)
	{
		double absBearing = myScanStatus.getHeadingRadians() + myRobotEvent.getBearingRadians();
		double head = myRobotEvent.getHeadingRadians();

		double bx = myScanStatus.getX() + Math.sin(absBearing) * myRobotEvent.getDistance();
		double by = myScanStatus.getY() + Math.cos(absBearing) * myRobotEvent.getDistance();

		RobotPrecisePrediction predictPath = new RobotPrecisePrediction();

		Point2D lastForward = null;
		boolean fCheck = false;

		AffineTransform trans = new AffineTransform();
//	trans.translate(500, 500);
		trans.translate(bx, by);
		trans.rotate(-head);

		Point2D start = trans.transform(new Point2D.Double(), null);
		Polygon pForwardCW = new Polygon();
		pForwardCW.addPoint((int) start.getX(), (int) start.getY());
		Polygon pForwardCCW = new Polygon();
		pForwardCCW.addPoint((int) start.getX(), (int) start.getY());

		double DELTA = 0;
		while (DELTA <= PI_360)
		{
			predictPath.setInitialValues(DELTA, myRobotEvent.getVelocity());
			predictPath.setBulletTurns((int) (myRobotEvent.getDistance() / bSpeed));

			Point2D eForward = null;
			ArrayList<Point2D> lForward = null;
			if (!fCheck)
			{
				predictPath.calculatePath(direction);
				predictPath.adjustPredictPath(trans, battleField);
				eForward = predictPath.getCWEndPoint();
				lForward = predictPath.getCWAdjustedPredictPath();

				if (lastForward != null)
				{
					boolean xfcheck = Utils.isNear(eForward.getX(), lastForward.getX());
					boolean yfcheck = Utils.isNear(eForward.getY(), lastForward.getY());

					fCheck = xfcheck && yfcheck;
					//System.out.format(" fore delta=%3.5f x=%b y=%b\n", Math.toDegrees(DELTA), xfcheck, yfcheck);
				}
				lastForward = eForward;

//			for (Point2D p : lForward)
//			{
//				PaintHelper.drawPoint(p, Color.RED, g, 1);
//			}

			}

			if (eForward != null)
			{
				if (!Utils.isNear(PI_360, DELTA) && !fCheck)
				{
					pForwardCW.addPoint((int) eForward.getX(), (int) eForward.getY());

					Point2D ccwEndpoint = predictPath.getCCWEndPoint();
					pForwardCCW.addPoint((int) ccwEndpoint.getX(), (int) ccwEndpoint.getY());
				}
				else
				{
					Collections.reverse(lForward);
					for (Point2D point : lForward)
					{
						pForwardCW.addPoint((int) point.getX(), (int) point.getY());
					}

					ArrayList<Point2D> depp = predictPath.getCCWAdjustedPredictPath();
					Collections.reverse(depp);
					for (Point2D point : depp)
					{
						pForwardCCW.addPoint((int) point.getX(), (int) point.getY());
					}

				}
			}
			DELTA += DELTA_P;
		}

		//Shape sForward = trans.createTransformedShape(pForward);
		//trans.scale(20.0, 20.0);

		//sForward = RobotMovement.stripOutsidePoints(sForward, battleField);

		Stroke old = g.getStroke();
		g.setStroke(new BasicStroke(1));
		g.setColor(color);
		g.draw(pForwardCW);
		g.draw(pForwardCCW);
		g.setColor(new Color(color.getColorSpace(), color.getColorComponents(null), 0.2f));
		g.fill(pForwardCW);
		g.fill(pForwardCCW);
		g.setStroke(old);

	}

}
