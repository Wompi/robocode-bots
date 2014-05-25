package wompi.robomath;

import java.awt.geom.Rectangle2D;

import robocode.AdvancedRobot;

public class RobotConst
{
	public static final double	PI_45	= Math.PI / 4.0;
	public static final double	PI_90	= Math.PI / 2.0;
	public static final double	PI_180	= Math.PI;
	public static final double	PI_360	= Math.PI * 2.0;

	public static final double	BORDER	= 18.0;
	public static final double	INF		= Double.POSITIVE_INFINITY;

	public static final Rectangle2D getBattleField(AdvancedRobot bot)
	{
		double botW = bot.getWidth() / 2.0;
		double botH = bot.getHeight() / 2.0;
		double fieldW = bot.getBattleFieldWidth();
		double fieldH = bot.getBattleFieldHeight();
		return new Rectangle2D.Double(botW, botH, fieldW - 2 * botW, fieldH - 2 * botH);
	}
}
