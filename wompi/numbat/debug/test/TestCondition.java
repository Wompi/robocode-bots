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

import robocode.AdvancedRobot;
import robocode.Condition;

public class TestCondition extends Condition
{
	public AdvancedRobot	myBot;
	private long			time;

	@Override
	public boolean test()
	{
		if (myBot.getTime() - time > 1) System.out.format("[%d] wave break - %d - %d\n", myBot.getTime(), time, myBot.getTime());
		time = myBot.getTime();
		return false;
	}

}
