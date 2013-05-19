package wompi;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;

public class Kowari extends AdvancedRobot
{
	private static final double	FIELD_W				= 800.0;
	private static final double	FIELD_H				= 600.0;

	private static final double	WZ					= 18.0;
	private static final double	WZ_W				= FIELD_W - 2 * WZ;
	private static final double	WZ_H				= FIELD_H - 2 * WZ;

	private static final double	PI_360				= Math.PI * 2;
	private static final double	DIST				= 120;
	private static final double	DIST_REMAIN			= 16;
	private static final double	DELTA_RISK_ANGLE	= Math.PI / 32.0;
	private final static double	TARGET_FORCE		= 200000;

	public Kowari()
	{}

	@Override
	public void run()
	{
		setAllColors(Color.RED);
		setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
	}

	@Override
	public void onStatus(StatusEvent e)
	{}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		double aBear = getHeadingRadians() + e.getBearingRadians();
		double xg = Math.sin(aBear) * e.getDistance();
		double yg = Math.cos(aBear) * e.getDistance();

		//double angle = 0;
		double riskAngle = 0;
		double risk;
		double maxRisk = Double.MAX_VALUE;

		int i = 0;
		try
		{
			while (true)
			{
//				int xd = (int) (DIST * Math.sin(angle));
//				int yd = (int) (DIST * Math.cos(angle));
//				System.out.format("+ (char) %d + (char) %d \n", (int) x, (int) y);
				char x = RISK_CIRCLE_X.charAt(i++);
				char y = RISK_CIRCLE_Y.charAt(i);
				//System.out.format("x=%d y=%d ( %d,%d)\n", (int) x, (int) y, xd, yd);

				if (new Rectangle2D.Double(WZ, WZ, WZ_W, WZ_H).contains(x + getX(), y + getY()))
				{
					risk = Math.abs(Math.cos(Math.atan2(xg - x, yg - y) - (i * DELTA_RISK_ANGLE)));
					risk += TARGET_FORCE / Point2D.distanceSq(xg, yg, x, y);

					if (Math.random() < 0.8 && risk < maxRisk)
					{
						maxRisk = risk;
						riskAngle = i * DELTA_RISK_ANGLE;
					}
				}
			}
		}
		catch (Exception e0)
		{
//			e0.printStackTrace();
//			System.out.format("[%d] catch \n", i);
		}
		setTurnRightRadians(Math.tan(riskAngle -= getHeadingRadians()));
		if (Math.abs(getDistanceRemaining()) <= DIST_REMAIN || e.getDistance() < DIST)
		{
			setAhead(DIST * Math.cos(riskAngle));
		}
		setMaxVelocity(2500 / e.getDistance());

		// RADAR
		setTurnRadarLeftRadians(getRadarTurnRemainingRadians());

	}









	//@formatter:off
	private final static String RISK_CIRCLE_X = ""
			+ (char) 11 
			+ (char) 23 
			+ (char) 34 
			+ (char) 45 
			+ (char) 56 
			+ (char) 66 
			+ (char) 76 
			+ (char) 84 
			+ (char) 92 
			+ (char) 99 
			+ (char) 105 
			+ (char) 110 
			+ (char) 114 
			+ (char) 117 
			+ (char) 119 
			+ (char) 120 
			+ (char) 119 
			+ (char) 117 
			+ (char) 114 
			+ (char) 110 
			+ (char) 105 
			+ (char) 99  
			+ (char) 92  
			+ (char) 84  
			+ (char) 76  
			+ (char) 66  
			+ (char) 56  
			+ (char) 45  
			+ (char) 34  
			+ (char) 23  
			+ (char) 11  
			+ (char) 0 
			+ (char) -11 
			+ (char) -23 
			+ (char) -34 
			+ (char) -45 
			+ (char) -56 
			+ (char) -66 
			+ (char) -76 
			+ (char) -84 
			+ (char) -92 
			+ (char) -99 
			+ (char) -105 
			+ (char) -110 
			+ (char) -114 
			+ (char) -117 
			+ (char) -119 
			+ (char) -120 
			+ (char) -119 
			+ (char) -117 
			+ (char) -114 
			+ (char) -110 
			+ (char) -105 
			+ (char) -99  
			+ (char) -92  
			+ (char) -84  
			+ (char) -76  
			+ (char) -66 
			+ (char) -56 
			+ (char) -45 
			+ (char) -34 
			+ (char) -23 
			+ (char) -11 
			+ (char) 0 
			;
	private final static String RISK_CIRCLE_Y = ""

	+ (char) 11 + (char) 119 
	+ (char) 23 + (char) 117 
	+ (char) 34 + (char) 114 
	+ (char) 45 + (char) 110 
	+ (char) 56 + (char) 105 
	+ (char) 66 + (char) 99 
	+ (char) 76 + (char) 92 
	+ (char) 84 + (char) 84 
	+ (char) 92 + (char) 76 
	+ (char) 99 + (char) 66 
	+ (char) 105 + (char) 56 
	+ (char) 110 + (char) 45 
	+ (char) 114 + (char) 34 
	+ (char) 117 + (char) 23 
	+ (char) 119 + (char) 11 
	+ (char) 120 + (char) 0 
	+ (char) 119 + (char) -11 
	+ (char) 117 + (char) -23 
	+ (char) 114 + (char) -34 
	+ (char) 110 + (char) -45 
	+ (char) 105 + (char) -56 
	+ (char) 99 + (char) -66 
	+ (char) 92 + (char) -76 
	+ (char) 84 + (char) -84 
	+ (char) 76 + (char) -92 
	+ (char) 66 + (char) -99 
	+ (char) 56 + (char) -105 
	+ (char) 45 + (char) -110 
	+ (char) 34 + (char) -114 
	+ (char) 23 + (char) -117 
	+ (char) 11 + (char) -119 
	+ (char) 0 + (char) -120 
	+ (char) -11 + (char) -119 
	+ (char) -23 + (char) -117 
	+ (char) -34 + (char) -114 
	+ (char) -45 + (char) -110 
	+ (char) -56 + (char) -105 
	+ (char) -66 + (char) -99 
	+ (char) -76 + (char) -92 
	+ (char) -84 + (char) -84 
	+ (char) -92 + (char) -76 
	+ (char) -99 + (char) -66 
	+ (char) -105 + (char) -56 
	+ (char) -110 + (char) -45 
	+ (char) -114 + (char) -34 
	+ (char) -117 + (char) -23 
	+ (char) -119 + (char) -11 
	+ (char) -120 + (char) 0 
	+ (char) -119 + (char) 11 
	+ (char) -117 + (char) 23 
	+ (char) -114 + (char) 34 
	+ (char) -110 + (char) 45 
	+ (char) -105 + (char) 56 
	+ (char) -99 + (char) 66 
	+ (char) -92 + (char) 76 
	+ (char) -84 + (char) 84 
	+ (char) -76 + (char) 92 
	+ (char) -66 + (char) 99 
	+ (char) -56 + (char) 105 
	+ (char) -45 + (char) 110 
	+ (char) -34 + (char) 114 
	+ (char) -23 + (char) 117 
	+ (char) -11 + (char) 119 
	+ (char) 0 + (char) 120 
	;
	private final static int LENGTH_X = RISK_CIRCLE_X.length();
	//@formatter:on

}
