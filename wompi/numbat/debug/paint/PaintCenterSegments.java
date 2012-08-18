package wompi.numbat.debug.paint;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import wompi.numbat.misc.NumbatBattleField;
import wompi.wallaby.PaintHelper;

public class PaintCenterSegments
{
	private final static int	BOT_WIDTH	= 36;

	public void onPaint(Graphics2D g)
	{
		double cx = NumbatBattleField.getCenterX();
		double cy = NumbatBattleField.getCenterY();

		Point2D center = new Point2D.Double(cx, cy);

		double hypo = Math.hypot(cx, cy);

		for (int i = BOT_WIDTH; i <= hypo; i += BOT_WIDTH)
		{
			PaintHelper.drawArc(center, i, 0.0, Math.PI * 2, false, g, PaintHelper.whiteTrans);
		}
	}

	public void drawFieldGrid(Graphics2D g, Color gridColor, double fieldW, double fieldH)
	{
		if (gridColor == null) gridColor = new Color(0xFF, 0xFF, 0xFF, 0x20);
		for (int i = 0; i <= (fieldW - 36); i += 36)
		{
			for (int j = 0; j <= (fieldH - 36); j += 36)
			{
				g.setColor(gridColor);
				g.drawRect(i, j, 36, 36);
			}
		}
	}

}
