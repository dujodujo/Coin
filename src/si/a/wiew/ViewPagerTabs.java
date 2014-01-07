package si.a.wiew;

import java.util.ArrayList;
import java.util.List;

import si.a.coin.app.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.View;

public class ViewPagerTabs extends View implements OnPageChangeListener {
	public static final String TAG = "ViewPagerTabs";
	
	private List<String> labels = new ArrayList<String>();
	private Paint paint = new Paint();
	private Path path = new Path();
	
	private int currentPagePosition = 0;
	private int previousPagePosition = currentPagePosition;
	private float pageOffset = 0.f;
	private int maxWidth = 0;
	
	public ViewPagerTabs(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		
		setSaveEnabled(true);
		paint.setTextSize(getResources().getDimension(R.dimen.font_size_small));
		paint.setColor(Color.BLACK);
		paint.setAntiAlias(true);
		paint.setShadowLayer(2, 0, 0, Color.WHITE);
	}
	
	public void addTabHeaders(final int... labelResId) {
		final Context context = getContext();
		paint.setTypeface(Typeface.DEFAULT_BOLD);
		for(final int id : labelResId) { 
			String label = context.getString(id);
			int width = (int) paint.measureText(label);
			if(width > maxWidth)
				maxWidth = width;
			labels.add(label);
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		final int width = getWidth();
		final int halfwidth = width / 2;
		final int bottom = getHeight();
		
		float density = getResources().getDisplayMetrics().density;
		float spacing = 32 * density;
		int offset = 5;
		
		path.reset();
		path.moveTo(halfwidth, bottom - offset*density);
		path.lineTo(halfwidth + offset*density, bottom);
		path.lineTo(halfwidth - offset*density, bottom);
		path.close();
		
		paint.setColor(Color.WHITE);
		canvas.drawPath(path, paint);
		paint.setTypeface(Typeface.DEFAULT_BOLD);
		
		float y = getPaddingTop() - paint.getFontMetrics().top;
		
		for(int i = 0; i < labels.size(); i++) {
			String label = labels.get(i);
			paint.setTypeface(i == currentPagePosition ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
			paint.setColor(i == currentPagePosition ? Color.BLACK : Color.BLUE);
			
			float x = halfwidth + (maxWidth + spacing) * (i - pageOffset);
			float labelWidth = paint.measureText(label);
			float labelHalfWidth = labelWidth/2;
			
			float labelLeft = x - labelHalfWidth;
			float labelVisibleLeft = labelLeft >= 0 ? 1f : 1f - (labelLeft/labelWidth);

			float labelRight = x + labelHalfWidth;
			float labelVisibleRight = labelRight < width ? 1f : 1f - ((labelRight - width) / labelWidth);

			float labelVisible = Math.min(labelVisibleLeft, labelVisibleRight);
			paint.setAlpha((int) labelVisible * 255);
			canvas.drawText(label, labelLeft, y, paint);
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		
		final int width;
		if(widthMode == MeasureSpec.EXACTLY) {
			width = widthSize;
		} else if(widthMode == MeasureSpec.AT_MOST) {
			width = Math.min(getMeasuredWidth(), widthSize);
		} else {
			width = 0;
		}
		
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		
		final int height;
		if(heightMode == MeasureSpec.EXACTLY) {
			height = heightSize;
		} else if(heightMode == MeasureSpec.AT_MOST) {
			height = Math.min(getMeasuredWidth(), widthSize);
		} else {
			height = 0;
		}
		
		setMeasuredDimension(width, height);
	}

	@Override
	protected int getSuggestedMinimumHeight() {
		paint.setTypeface(Typeface.DEFAULT_BOLD);
		return (int)(-paint.getFontMetrics().top + paint.getFontMetrics().bottom) +
			getPaddingTop() + getPaddingBottom();
	}
	
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		pageOffset = position + positionOffset;
		invalidate();
	}
	
	public void onPageSelected(int position) {
		currentPagePosition = position;
		previousPagePosition = 
			currentPagePosition == 0 ? currentPagePosition : currentPagePosition-1;
		invalidate();
	}

	public void onPageScrollStateChanged(int state) {}
	
	@Override
	protected Parcelable onSaveInstanceState() {
		final Bundle state = new Bundle();
		state.putParcelable("super_state", super.onSaveInstanceState());
		state.putInt("page_position", currentPagePosition);
		state.putFloat("page_offset", pageOffset);
		return state;
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if(state instanceof Bundle) {
			Bundle bundle = (Bundle) state;
			currentPagePosition = bundle.getInt("page_position");
			pageOffset = bundle.getFloat("page_offset");
			super.onRestoreInstanceState(bundle.getParcelable("super_state"));
			return;
		}
		super.onRestoreInstanceState(state);
	}
}