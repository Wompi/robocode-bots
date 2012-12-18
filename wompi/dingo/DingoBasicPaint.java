package wompi.dingo;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import robocode.AdvancedRobot;
import robocode.Rules;
import wompi.paint.PaintHelper;
import wompi.robomath.RobotMath;

public class DingoBasicPaint
{
	private static final double	PI_90	= Math.PI / 2.0;
	private static final double	PI_180	= Math.PI;

	private final AdvancedRobot	myBot;

	public DingoBasicPaint(AdvancedRobot bot)
	{
		myBot = bot;
	}

	public void onPaint(Graphics2D g)
	{
		infield(g);
		//	borderDistance(g, myBot.getVelocity(), Color.red, +1);
		//	borderDistance(g, myBot.getVelocity(), Color.red, -1);
//		borderDistance(g, 7.0, Color.YELLOW);
//		borderDistance(g, 6.0, Color.BLUE);
		currentHeading(g);
	}

	private void borderDistance(Graphics2D g, double veloc, Color color, int direction)
	{
		Point2D cP = new Point2D.Double(myBot.getBattleFieldWidth() / 2.0, myBot.getBattleFieldHeight() / 2.0);

		double velo = 0;
		int dv = 0;
		while (velo <= 8.0)
		{
			double v = velo;
			double h = Math.toRadians(myBot.getHeading());
			double x = 0;
			double y = 0;

			Color cColor = Color.GRAY;
			if (dv == (int) v)
			{
				cColor = color;
				dv++;
			}

			int i = 0;
			while (i++ < 30)
			{
//				double delta = limit(Rules.getTurnRateRadians(v), Math.max(PI_90 - h, 0));
//				double delta = limit(Rules.getTurnRateRadians(v), PI_90 - h);
				double delta = Rules.getTurnRateRadians(v) * direction;
				h += delta;
				x += Math.sin(h) * v;
				y += Math.cos(h) * v;

//				System.out.format("P: x=%3.4f y=%3.4f d=%3.2f\n", x, y, Math.toDegrees(delta));
				PaintHelper.drawPoint(new Point2D.Double(x + myBot.getX(), y + myBot.getY()), cColor, g, 1);
			}
			velo += 0.1;
		}
	}

	private void currentHeading(Graphics2D g)
	{
		Point2D bP = new Point2D.Double(myBot.getX(), myBot.getY());

		double dy1 = -myBot.getY() - 18.0 + myBot.getBattleFieldHeight();
		double dy2 = myBot.getY() - 18.0;
		double dx1 = -myBot.getX() - 18.0 + myBot.getBattleFieldWidth();
		double dx2 = myBot.getX() - 18.0;

		double f = 0;
		Point2D f1 = RobotMath.calculatePolarPoint(f += 0, dy1, bP);
		Point2D f2 = RobotMath.calculatePolarPoint(f += PI_90, dx1, bP);
		Point2D f3 = RobotMath.calculatePolarPoint(f += PI_90, dy2, bP);
		Point2D f4 = RobotMath.calculatePolarPoint(f += PI_90, dx2, bP);
		PaintHelper.drawLine(bP, f1, g, Color.GRAY);
		PaintHelper.drawLine(bP, f2, g, Color.GRAY);
		PaintHelper.drawLine(bP, f3, g, Color.GRAY);
		PaintHelper.drawLine(bP, f4, g, Color.GRAY);

//		Point2D hP = RobotMath.calculatePolarPoint(myBot.getHeadingRadians(), 200, bP);
//		PaintHelper.drawLine(bP, hP, g, Color.BLUE);
	}

	private void infield(Graphics2D g)
	{
		Rectangle2D r = new Rectangle2D.Double(18, 18, myBot.getBattleFieldWidth() - 2 * 18,
				myBot.getBattleFieldHeight() - 2 * 18);
		g.setColor(Color.DARK_GRAY);
		g.draw(r);
	}
}
