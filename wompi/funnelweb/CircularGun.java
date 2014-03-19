package wompi.funnelweb;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.LinkedList;

import robocode.AdvancedRobot;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.util.Utils;

public class CircularGun
{
	private static Rectangle2D							battleField;
	private static HashMap<String, CircularGunTarget>	allGunTargets	= new HashMap<String, CircularGunTarget>();

	private static double[]								gunStats		= new double[9];

	LinkedList<ScannedRobotEvent>						myScans			= new LinkedList<ScannedRobotEvent>();

	public void onInit(AdvancedRobot bot, int border)
	{
		double h = bot.getBattleFieldHeight();
		double w = bot.getBattleFieldWidth();
		battleField = new Rectangle2D.Double(border, border, w - 2 * border, h - 2 * border);
	}

	public void onStatus(StatusEvent e)
	{

	}

	public void onScannedRobot(ScannedRobotEvent e)
	{
		// others
		//		velocity (sorted) or (map) 
		//      heading  (sorted) or (map)

		// accelerationand deceleration cause very strange gun pattern because it does not stop right away but 
		// tursn in the oposite direction - this should be considered 
	}

	public void setFireAngle(AdvancedRobot bot)
	{
		if (((gunTurnCounter += 0.9) * Rules.getBulletSpeed(bPower) < Math.hypot(xGunRel, yGunRel)))
		{
			gunHeading += dHeading;
			xGunRel += Math.sin(gunHeading) * vGun;
			yGunRel += Math.cos(gunHeading) * vGun;

			if (!battleField.contains(xGunRel + getX(), yGunRel + getY()))
			{
				vGun = -vGun;
			}
		}
		setTurnGunRightRadians(Utils.normalRelativeAngle(Math.atan2(xGunRel, yGunRel) - getGunHeadingRadians()));

	}

	public void onPaint(Graphics2D g)
	{

	}
}

class CircularGunTarget
{

}
