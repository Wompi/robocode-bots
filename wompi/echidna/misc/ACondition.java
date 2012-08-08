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
package wompi.echidna.misc;

import robocode.AdvancedRobot;
import robocode.Condition;

public abstract class ACondition extends Condition
{
	protected AdvancedRobot	myRobot;
	public ACondition		followRule;

	public ACondition(AdvancedRobot robot)
	{
		myRobot = robot;
	}

	public abstract ConditionType getConditionType();

	public void processCommand()
	{};

	public final void registerCondition()
	{
		myRobot.addCustomEvent(this);
	}

	public final void unregisterCondition()
	{
		myRobot.removeCustomEvent(this);
	}

	public final String getConditionName()
	{
		return this.getClass().toString();
	}

}
