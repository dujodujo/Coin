package si.a.wiew;

import si.a.coin.app.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Style;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.view.View;
import android.view.View.MeasureSpec;

public class ProgressView extends View {
	
	private int width;
	private int height;
	private int progress = 1;
	private int maxProgress = 1;
	private int size = 1;
	private int maxSize = 1;
	
	private Path path = new Path();
	private Paint paint = new Paint();
	private Paint strokePaint = new Paint();
	
	public ProgressView(Context context) {
		super(context);
		
		float density = getResources().getDisplayMetrics().density;
		
		paint.setStyle(Style.FILL);
		paint.setColor(Color.BLUE);
		paint.setAntiAlias(true);
		
		strokePaint.setStyle(Style.STROKE);
		strokePaint.setColor(Color.GREEN);
		strokePaint.setStrokeWidth(density);
		strokePaint.setAntiAlias(true);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		canvas.drawPath(path, paint);
		canvas.drawPath(path, strokePaint);
	}
	
	public void setColors(int color, int strokeColor) {
		paint.setColor(strokeColor);
		strokePaint.setColor(strokeColor);
		postInvalidate();
	}
	
	public void setProgress(int progress) {
		this.progress = progress;
		
		updatePath(getWidth(), getHeight());
		postInvalidate();
	}
	
	public void setMaxProgress(int maxProgress) {
		this.maxProgress = maxProgress;
		
		updatePath(getWidth(), getHeight());
		postInvalidate();
	}
	
	public void setSize(int size) {
		this.size = size;
		
		updatePath(getWidth(), getHeight());
		postInvalidate();
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
		
		updatePath(getWidth(), getHeight());
		postInvalidate();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		updatePath(w, h);
	}
	
	private void updatePath(int width, int height) {
		
		float max = Math.min(width, height)/2.f;
		float radius = size < maxSize ? max * size/maxSize : max;
		path.reset();
		
		if(progress == 0) {
			path.close();
		} else if(progress < maxProgress) {
			float angle = progress * 360/maxProgress;
			float x = width/2.f;
			float y = height/2.f;
			
			path.moveTo(x, y);
			path.arcTo(new RectF(x-radius, y-radius, x+radius, x+radius), 0, angle);
			
			path.close();
		} else {
			path.addCircle(width/2f, height/2f, radius, Direction.CW);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int wmode = MeasureSpec.getMode(widthMeasureSpec);
		int wsize = MeasureSpec.getSize(widthMeasureSpec);
		
		if(wmode == MeasureSpec.EXACTLY)
			width = wsize;
		else if(wmode == MeasureSpec.AT_MOST)
			width = Math.min(width, wsize);
		
		int hmode = MeasureSpec.getMode(heightMeasureSpec);
		int hsize = MeasureSpec.getSize(widthMeasureSpec);
		
		if(hmode == MeasureSpec.EXACTLY)
			height = hsize;
		else if(hmode == MeasureSpec.AT_MOST)
			height = Math.min(height, hsize);
		setMeasuredDimension(width, height);
	}
}