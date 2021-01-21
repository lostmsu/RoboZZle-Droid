/**
 * 
 */
package com.team242.robozzle;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author lost
 * 
 */
public class WidthImageView extends View {

	private Drawable logo;

	public WidthImageView(Context context) {
		super(context);
	}

	public WidthImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public WidthImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void setDrawable(Drawable drawable){
		logo = drawable;
		setBackgroundDrawable(logo);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int maxHeight = MeasureSpec.getSize(heightMeasureSpec);
		int height = width * logo.getIntrinsicHeight()
				/ logo.getIntrinsicWidth();
		setMeasuredDimension(width, Math.min(height, maxHeight));
	}

}
