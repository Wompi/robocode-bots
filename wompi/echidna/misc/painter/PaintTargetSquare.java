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
import java.awt.Polygon;
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
		Polygon poly = new Polygon();
		poly.addPoint(0, 0);
		poly.addPoint(0, 36);
		poly.addPoint(36, 36);
		poly.addPoint(36, 0);
		poly.addPoint(0, 0);

		transform.translate(eX - rDim, eY - rDim);
		transform.rotate(-eHeading, rectangle.getCenterX(), rectangle.getCenterY());

		Shape s1 = transform.createTransformedShape(rectangle);
		Shape s2 = transform.createTransformedShape(poly);

		if (fill)
		{
			g.setColor(Color.GREEN);
			g.fill(s1);
		}
		g.setColor(myColor);
		g.draw(s2);
	}
}
