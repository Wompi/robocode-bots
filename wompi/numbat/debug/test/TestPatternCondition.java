/*******************************************************************************
 * Copyright (c)  2012  Wompi 
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the ZLIB
 * which accompanies this distribution, and is available at
 * http://robowiki.net/wiki/ZLIB
 * 
 * Contributors:
 *     Wompi - initial API and implementation
 ******************************************************************************/
package wompi.numbat.debug.test;

import java.util.ArrayList;

import robocode.AdvancedRobot;
import robocode.Condition;
import wompi.numbat.target.NumbatTarget;

public class TestPatternCondition extends Condition
{
	public static int							ID;
	public ArrayList<PatternConditionHelper>	myPattern;
	public AdvancedRobot						myBot;
	public NumbatTarget							myTarget;
	private int									count	= 0;
	private int									myID;

	public TestPatternCondition()
	{
		myID = ID++;
	}

	@Override
	public boolean test()
	{
		try
		{
			PatternConditionHelper helper = myPattern.get(count++);
			System.out.format("[%d][%d] Target: x=%3.2f y%3.2f dH=%3.4f v=%3.2f (scan=%d)\n", myBot.getTime(), myID, myTarget.x, myTarget.y,
					Math.toDegrees(myTarget.getHeadingDifference()), myTarget.eVelocity, myTarget.eScan);
			System.out.format("[%d][%d] Helper: x=%3.2f y%3.2f dH=%3.4f v=%3.2f (time=%d)\n", myBot.getTime(), myID, helper.x, helper.y,
					Math.toDegrees(helper.dHeading), helper.velocity, helper.time);
		}
		catch (Exception e)
		{
			System.out.format("[%d][%d] remove pattern\n", myBot.getTime(), myID);
			myBot.removeCustomEvent(this);
		}

		return false;
	}

}
