package wompi.move;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

import robocode.AdvancedRobot;
import robocode.RobotStatus;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import wompi.robomath.RobotConst;
import wompi.target.helper.StatsVelocity;

public class MoveArea
{
	// calculation for cached area object at beginning
	// 75 = 1200 / 16 (for 0.1 bullets max turns)  
	// 75 x 9 x 9 (maxTurns x currentSpeed[0-8] x maxSpeed[0-8])
	// 6075 area objects forward only

	private double				myStartPointX;
	private double				myStartPointY;

	private double				myHeading;

	private double				myHeadPointX;
	private double				myHeadPointY;

	// this needs to be reversed when used for painting
	private double[]			myTurnPointsX;
	private double[]			myTurnPointsY;

	private double[]			myEndPointsX;
	private double[]			myEndPointsY;

	private static Rectangle2D	BATTLE_FIELD;

	private Shape				myArea;
	private RobotStatus			myStatus;

	public void onInit(AdvancedRobot bot)
	{
		if (BATTLE_FIELD != null) { return; }
		BATTLE_FIELD = RobotConst.getBattleField(bot);
	}

	public void onStatus(StatusEvent e)
	{
		myStatus = e.getStatus();

		// TODO: needs some enhancement at the turn side
		//calculateMoveArea(myStatus.getHeadingRadians(), myStatus.getX(), myStatus.getY(), myStatus.getVelocity(), 30);
	}

	public void onScannedRobot(ScannedRobotEvent e, StatsVelocity maxVelocity)
	{
		double absBear = e.getBearingRadians() + myStatus.getHeadingRadians();
		myStartPointX = myStatus.getX() + Math.sin(absBear) * e.getDistance();
		myStartPointY = myStatus.getY() + Math.cos(absBear) * e.getDistance();
		myHeading = e.getHeadingRadians();

		if (e.getVelocity() < 0) myHeading += RobotConst.PI_180;

		// TODO: needs some enhancement at the turn side
		//double bPower = Math.max(0.1, Math.min(Rules.MAX_BULLET_POWER, 450.0 / e.getDistance()));
		double bPower = 0.1;
		double bSpeed = Rules.getBulletSpeed(bPower);
		int turns = (int) (e.getDistance() / bSpeed);

		calculateMoveArea(e.getVelocity(), maxVelocity.getMaxVelocity(), turns);
	}

	private void calculateMoveArea(double velocity, double maxVelocity, int turns)
	{
		myArea = null;

		maxVelocity = Math.abs(maxVelocity);

		double velocitySign = Math.signum(velocity);
		velocity = Math.abs(velocity);

		myTurnPointsX = new double[turns];
		myTurnPointsY = new double[turns];

		myEndPointsX = new double[turns];
		myEndPointsY = new double[turns];

		myTurnPointsX[0] = 0;
		myTurnPointsY[0] = 0;

		int hDeltaTurn = turns - MoveMath.getAccelerationTurns(velocity, maxVelocity);
		double headDist = MoveMath.getAccelerationDist(velocity, maxVelocity) + maxVelocity * hDeltaTurn;

		myEndPointsX[0] = myHeadPointX = 0;
		myEndPointsY[0] = myHeadPointY = headDist;// * velocitySign;

		int counter = 1;
		double heading = 0;

		while (counter < turns)
		{
			velocity = Math.min(maxVelocity, Rules.ACCELERATION + velocity);
			heading = Rules.getTurnRateRadians(velocity) + heading;

			myTurnPointsX[counter] = myTurnPointsX[counter - 1] + Math.sin(heading) * velocity;// * velocitySign;
			myTurnPointsY[counter] = myTurnPointsY[counter - 1] + Math.cos(heading) * velocity;// * velocitySign;

			int deltaTurn = turns - MoveMath.getAccelerationTurns(velocity, maxVelocity) - counter;
			double turnDist = MoveMath.getAccelerationDist(velocity, maxVelocity) + maxVelocity * deltaTurn;
			myEndPointsX[counter] = myTurnPointsX[counter] + Math.sin(heading) * turnDist; // * velocitySign;
			myEndPointsY[counter] = myTurnPointsY[counter] + Math.cos(heading) * turnDist; // * velocitySign;
			counter++;
		}

	}

	public Shape getArea()
	{
		if (myArea == null)
		{
			AffineTransform transform = new AffineTransform();
			transform.translate(myStartPointX, myStartPointY);
			//transform.translate(500, 500);
			transform.rotate(RobotConst.PI_360 - myHeading);
			//transform.rotate(myHeading);

			Area af1 = new Area(getForwardArea());
			Area af2 = new Area(af1);
			af2.transform(new AffineTransform(new double[]
			{ -1.0, 0.0, 0.0, 1.0 }));

			af1.add(af2);

			myArea = transform.createTransformedShape(af1);

			Area fullArea = new Area(myArea);
			Area battleArea = new Area(BATTLE_FIELD);
			battleArea.intersect(fullArea);
			myArea = battleArea;
		}

		return myArea;
	}

	private Polygon getForwardArea()
	{
		Polygon p = new Polygon();

		for (int i = 0; i < myTurnPointsX.length - 1; i++)
		{
			p.addPoint((int) myTurnPointsX[i], (int) myTurnPointsY[i]);
		}

		for (int i = myEndPointsX.length - 1; i > 0; i--)
		{
			p.addPoint((int) myEndPointsX[i], (int) myEndPointsY[i]);
		}
		p.addPoint((int) myHeadPointX, (int) myHeadPointY);

//		for (int i = 0; i < myEndPointsX.length - 1; i++)
//		{
//			p.addPoint((int) -myEndPointsX[i], (int) myEndPointsY[i]);
//		}
//
//		for (int i = myTurnPointsX.length - 1; i > 0; i--)
//		{
//			p.addPoint((int) -myTurnPointsX[i], (int) myTurnPointsY[i]);
//		}
//
		p.addPoint(0, 0);
		return p;

	}
}
