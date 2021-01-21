/**
 * 
 */
package com.team242.android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;

/**
 * @author lost
 *
 */
public class RoundRect extends ShapeView {

	public RoundRect(Context context) {
		super(context);
	}
	
	RectF rect = new RectF();
	float rx = 3, ry = 3;
	// TODO dp
	public void setRadius(float x, float y){
		rx = x;
		ry = y;
		
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		rect.bottom = getHeight();
		rect.right = getWidth();
		canvas.drawRoundRect(rect, rx, ry, paint);
	}
}
