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
package wompi.echidna.misc.painter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import wompi.wallaby.PaintHelper;

public class PaintBotLineIntersection
{
	public static void onPaint(Graphics2D g, double ex, double ey, Line2D targetLine, Color myColor)
	{
		Rectangle2D bot = new Rectangle2D.Double(ex - 18, ey - 18, 36, 36);

		g.setColor(myColor);
		g.draw(bot);
		g.setColor(PaintHelper.whiteTrans);
		g.draw(targetLine);

	}
}
