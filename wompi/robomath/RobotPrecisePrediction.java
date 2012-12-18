package wompi.robomath;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;

import robocode.Rules;

public class RobotPrecisePrediction
{
	private final ArrayList<Point2D>	cwMovePath;
	private final ArrayList<Point2D>	ccwMovePath;

	private ArrayList<Point2D>			cwAdjustedMovePath;	// clockwise
	private ArrayList<Point2D>			ccwAdjustedMovePath;	//  counter clockwise

	private double						myMoveHeading;
	private double						myMoveVelocity;

	private int							myBulletTurns	= 30;

	public RobotPrecisePrediction()
	{
		cwMovePath = new ArrayList<Point2D>();
		ccwMovePath = new ArrayList<Point2D>();
	}

	public void setInitialValues(double maxMoveHeading, double startVelocity)
	{
		myMoveHeading = maxMoveHeading;
		myMoveVelocity = startVelocity;
	}

	public void setBulletTurns(int turns)
	{
		myBulletTurns = turns;
	}

	public void calculatePath(int direction)
	{
		double v = myMoveVelocity;

		double bTurn = 0;
		int i = 0;
		cwMovePath.clear();
		ccwMovePath.clear();

		double x = 0;
		double y = 0;

		cwMovePath.add(new Point2D.Double());
		ccwMovePath.add(new Point2D.Double());

		while (i <= myBulletTurns)
		{
			bTurn += Rules.getTurnRateRadians(v);
			bTurn = Math.min(myMoveHeading, bTurn);

			v = RobotMovement.getNextVelocity(v, 100 * direction, Rules.MAX_VELOCITY);
			x += Math.sin(bTurn) * v;
			y += Math.cos(bTurn) * v;
			cwMovePath.add(new Point2D.Double(x, y));
			ccwMovePath.add(new Point2D.Double(-x, y));
			i++;
		}
	}

	public void adjustPredictPath(AffineTransform trans, Rectangle2D bField)
	{
		cwAdjustPath(trans, bField);
		ccwAdjustPath(trans, bField);
	}

	public ArrayList<Point2D> getCWAdjustedPredictPath()
	{
		return cwAdjustedMovePath;
	}

	public ArrayList<Point2D> getCCWAdjustedPredictPath()
	{
		return ccwAdjustedMovePath;
	}

	public Point2D getCWEndPoint()
	{
		return cwAdjustedMovePath.get(cwAdjustedMovePath.size() - 1);
	}

	public Point2D getCCWEndPoint()
	{
		return ccwAdjustedMovePath.get(ccwAdjustedMovePath.size() - 1);
	}

	public void cwAdjustPath(AffineTransform trans, Rectangle2D bField)
	{
		Point2D[] pointArr = cwMovePath.toArray(new Point2D[cwMovePath.size()]);
		if (trans != null)
		{
			trans.transform(pointArr, 0, pointArr, 0, cwMovePath.size());
		}
		Point2D[] cwField = getFieldPointList(bField, pointArr);
		cwAdjustedMovePath = new ArrayList<Point2D>(Arrays.asList(cwField));
	}

	public void ccwAdjustPath(AffineTransform trans, Rectangle2D bField)
	{
		Point2D[] pointArr = ccwMovePath.toArray(new Point2D[ccwMovePath.size()]);
		if (trans != null)
		{
			trans.transform(pointArr, 0, pointArr, 0, ccwMovePath.size());
		}
		Point2D[] ccwField = getFieldPointList(bField, pointArr);
		ccwAdjustedMovePath = new ArrayList<Point2D>(Arrays.asList(ccwField));
	}

	private Point2D[] getFieldPointList(Rectangle2D bField, Point2D[] pArray)
	{
		if (bField != null)
		{
			ArrayList<Point2D> fieldPoints = new ArrayList<Point2D>();

			for (Point2D p : pArray)
			{
				if (!bField.contains(p))
				{
					// TODO: this is wrong but maybe it is close enough
					// - wrong because it projects the point on the smallest border with the current x|y value
					double _x = Math.max(bField.getMinX(), Math.min(p.getX(), bField.getMaxX()));
					double _y = Math.max(bField.getMinY(), Math.min(p.getY(), bField.getMaxY()));
					p.setLocation(_x, _y);
					fieldPoints.add(p);
					break;
				}

				fieldPoints.add(p);
			}
			return fieldPoints.toArray(new Point2D[fieldPoints.size()]);
		}
		else
			return pArray;
	}
}
