package wompi.robomath.prediction;

import java.awt.geom.Rectangle2D;

public class PredictPath
{
	public final static int	ANGLE_SEGMENTS	= 100;	// adjust to current segmentation 180/90 or something
	public final static int	MAX_TICKS		= 60;	// 60 might be to much for 1vs1 but for melee it is enough

	double[]				xBase;
	double[]				yBase;

	int						endIndex;
	double[]				xTrans;
	double[]				yTrans;

	double					startHeading;
	double					endHeading;

	public PredictPath()
	{
		endIndex = MAX_TICKS;
		xBase = new double[MAX_TICKS];
		yBase = new double[MAX_TICKS];
		xTrans = new double[MAX_TICKS];
		yTrans = new double[MAX_TICKS];
	}

	public void caclulateFieldPoints(double heading, Rectangle2D bField)
	{
		for (int i = MAX_TICKS - 1; i > 0; i--)
		{

		}
	}
}
