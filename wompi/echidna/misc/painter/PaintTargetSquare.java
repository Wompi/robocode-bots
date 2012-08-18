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
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

public class PaintTargetSquare
{
	public static void drawTargetSquare(Graphics2D g, double eHeading, double eX, double eY, boolean fill, Color myColor)
	{
		double rDim = 18.0; // robot width

		AffineTransform transform = new AffineTransform();
		Rectangle rectangle = new Rectangle(0, 0, 36, 36);

		transform.rotate(-eHeading, eX, eY);
		transform.translate(eX - rDim, eY - rDim);

		Shape s1 = transform.createTransformedShape(rectangle);

		if (fill)
		{
			g.setColor(Color.GREEN);
			g.fill(s1);
		}
		g.setColor(myColor);
		g.draw(s1);
	}

	public static void drawTargetGrid(Graphics2D g, double eHeading, double eX, double eY, boolean fill, Color myColor, double dx, double dy)
	{
		double rDim = 18.0;
		AffineTransform transform = new AffineTransform();
		transform.rotate(-eHeading, eX, eY);
		transform.translate(eX - dx - rDim, eY - dy - rDim);

		Rectangle rectangle = new Rectangle(0, 0, 36, 36);
		Shape s1 = transform.createTransformedShape(rectangle);
		g.setColor(myColor);
		g.draw(s1);

	}
}
