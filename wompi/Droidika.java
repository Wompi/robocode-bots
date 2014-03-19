package wompi;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import robocode.AdvancedRobot;
import robocode.Droid;
import robocode.HitByBulletEvent;
import robocode.HitWallEvent;
import robocode.StatusEvent;
import wompi.paint.PaintHelper;
import wompi.robomath.RobotMath;

public class Droidika extends AdvancedRobot implements Droid
{
	private int							dir			= 1;

	private final ArrayList<HitPainter>	hitpaint	= new ArrayList<HitPainter>();

	@Override
	public void onStatus(StatusEvent e)
	{

	}

	@Override
	public void onHitByBullet(HitByBulletEvent e)
	{
		System.out.format("[%03d] power=%3.5f heading=%3.5f \n", e.getTime(), e.getBullet().getPower(), e.getHeading());

		setTurnRightRadians(Math.cos(e.getBearingRadians()));
		setAhead(250 * dir);
		double absBear = e.getBearingRadians() + getHeadingRadians();
		HitPainter p = new HitPainter();
		p.start = new Point2D.Double(e.getBullet().getX(), e.getBullet().getY());
		p.end = RobotMath.calculatePolarPoint(absBear, 700, p.start);
		hitpaint.add(p);
	}

	@Override
	public void onHitWall(HitWallEvent event)
	{
		dir = -dir;
		setAhead(dir * 250);

	}

	@Override
	public void onPaint(Graphics2D g)
	{
		for (HitPainter p : hitpaint)
		{
			PaintHelper.drawLine(p.start, p.end, getGraphics(), Color.GREEN);
		}
	}

}

class HitPainter
{
	Point2D	start;
	Point2D	end;
}
