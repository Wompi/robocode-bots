package wompi.paint;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import robocode.AdvancedRobot;

public class PaintRiskFunction
{

	public static Font							myFont	= new Font("Dialog", Font.PLAIN, 16);

	private HashMap<String, HelperRiskFunction>	myRiskFunctions;
	private Rectangle2D							myPaintArea;

	//@formatter:off
	private static Color[]	cField	={ 
										Color.RED, 		Color.BLUE, 	Color.CYAN, 
										Color.GRAY, 	Color.GREEN, 	Color.MAGENTA, 
										Color.ORANGE, 	Color.PINK, 	Color.YELLOW, 
										Color.WHITE, Color.DARK_GRAY 
									};
	//@formatter:on
	private int									cIndex;

	public void onInit(AdvancedRobot myRobot, boolean isReset)
	{
		if (isReset || myRiskFunctions != null) myRiskFunctions = new HashMap<String, HelperRiskFunction>();

		// TODO: put this as parameters 
		double x = myRobot.getBattleFieldWidth() * 0.1;
		double y = myRobot.getBattleFieldHeight() * 0.2;
		myPaintArea = new Rectangle2D.Double(x, y, x * 8, y);
		cIndex = myRiskFunctions.size();

	}

	public void removeRiskFunctionValues(String riskName)
	{
		myRiskFunctions.remove(riskName);
	}

	public void addRiskFunctionValues(String riskName, double[] segmentField)
	{
		HelperRiskFunction eRisk;
		if ((eRisk = myRiskFunctions.get(riskName)) == null)
		{
			eRisk = new HelperRiskFunction();
			eRisk.rColor = cField[cIndex % cField.length];
			myRiskFunctions.put(riskName, eRisk);
			cIndex++;
		}
		GeneralPath aPath = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 2000);
		aPath.moveTo(0, 0);
		double max = Double.MIN_VALUE;
		double min = Double.MAX_VALUE;
		double xmax = 0;
		double xmin = 0;

		int count = 0;
		for (double seg : segmentField)
		{
			if (seg > max)
			{
				max = seg;
				xmax = count / ((double) segmentField.length);
			}

			if (seg < min)
			{
				min = seg;
				xmin = count / ((double) segmentField.length);
			}
			aPath.lineTo(count, seg);
			count++;
		}
		eRisk.size = segmentField.length;
		eRisk.rPath = aPath;
		eRisk.maxvalue = max;
		eRisk.minvalue = min;
		eRisk.xMaxValue = xmax;
		eRisk.xMinValue = xmin;

	}

	public void onPaint(Graphics2D g)
	{
		int textOffset = 3;
		double rMax = Double.MIN_VALUE;
		double rMin = Double.MAX_VALUE;
		double rXMIN = Double.MAX_VALUE;
		double rXMAX = Double.MIN_VALUE;

		Color maxColor = Color.WHITE;
		String maxName = "";
		int maxSize = 0;

		for (Map.Entry<String, HelperRiskFunction> entry : myRiskFunctions.entrySet())
		{
			double max = entry.getValue().maxvalue;
			double min = entry.getValue().minvalue;
			String name = entry.getKey();

			if (max > rMax)
			{
				rMax = max;
				rXMAX = entry.getValue().xMaxValue;
				maxColor = entry.getValue().rColor;
				maxName = name;
				maxSize = entry.getValue().size;
			}

			if (min < rMin)
			{
				rMin = min;
				rXMIN = entry.getValue().xMinValue;
			}
		}

		for (Map.Entry<String, HelperRiskFunction> entry : myRiskFunctions.entrySet())
		{
			GeneralPath aPath = entry.getValue().rPath;
			double max = entry.getValue().maxvalue;
			double min = entry.getValue().minvalue;
			String name = entry.getKey();
			Color rColor = entry.getValue().rColor;

			Rectangle2D pathBound = aPath.getBounds2D();
			AffineTransform transform = new AffineTransform();
			double scaleY = myPaintArea.getHeight() / (rMax + Math.abs(rMin));
			double scaleX = myPaintArea.getWidth() / pathBound.getWidth();

//			System.out.format("Ay=%3.5f scaleY=%3.5f Py=%3.5f %s \n", myPaintArea.getY(), scaleY, pathBound.getY(),
//					name);

			transform.translate(myPaintArea.getX(), myPaintArea.getY() - scaleY * rMin);
			transform.scale(scaleX, scaleY);

			g.setColor(Color.DARK_GRAY);
			double xOffset = myPaintArea.getWidth() * rXMAX;
			Point2D start = new Point2D.Double(myPaintArea.getX() + xOffset, myPaintArea.getY());
			Point2D end = new Point2D.Double(myPaintArea.getX() + xOffset, myPaintArea.getY() + myPaintArea.getHeight());
			g.draw(new Line2D.Double(start, end));

			// test
			g.setColor(Color.LIGHT_GRAY);
			xOffset = myPaintArea.getWidth() * rXMIN;
			start = new Point2D.Double(myPaintArea.getX() + xOffset, myPaintArea.getY());
			end = new Point2D.Double(myPaintArea.getX() + xOffset, myPaintArea.getY() + myPaintArea.getHeight());
			g.draw(new Line2D.Double(start, end));

			g.setColor(rColor);
			g.draw(transform.createTransformedShape(aPath));
			g.setColor(Color.DARK_GRAY);
			g.draw(myPaintArea);

			g.setColor(rColor);
			g.setFont(myFont);
			g.drawString(String.format("max: %3.10f min: %3.10f %s", max, min, name), (int) (myPaintArea.getMinX()),
					(int) (myPaintArea.getMaxY() + textOffset));

			textOffset += myFont.getSize() + 3;

		}
		g.setColor(maxColor);
		g.drawString(String.format("%3.0fdeg %s", maxSize * rXMAX, maxName),
				(int) (myPaintArea.getX() + myPaintArea.getWidth() * rXMAX),
				(int) (myPaintArea.getY() - 3 - myFont.getSize()));

	}

	private class HelperRiskFunction
	{
		GeneralPath	rPath;
		double		maxvalue;
		double		minvalue;

		double		xMaxValue;
		double		xMinValue;
		Color		rColor;
		int			size;
	}
}
