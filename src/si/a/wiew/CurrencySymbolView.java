package si.a.wiew;

import si.a.util.Constants;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

public class CurrencySymbolView extends Drawable {
	
	private final Paint paint = new Paint();
	private String symbol;
	private float position;
	
	public CurrencySymbolView(final String symbol, final float textSize, 
			final int color, float position) {
		paint.setColor(color);
		paint.setAntiAlias(true);
		paint.setTextSize(textSize);
		
		this.symbol = symbol + Constants.CHAR_HAIR_SPACE;
		this.position = position;
	}

	@Override
	public void draw(Canvas canvas) { canvas.drawText(symbol, 0, position, paint); }
	
	@Override
	public int getIntrinsicWidth() { return (int)paint.measureText(symbol); }

	@Override
	public int getOpacity() { return 0; }

	@Override
	public void setAlpha(int alpha) {}

	@Override
	public void setColorFilter(ColorFilter cf) {}
}