/**
 * 
 */
package com.team242.android.widget;

import android.content.Context;
import android.graphics.Paint;
import android.view.View;

/**
 * @author lost
 *
 */
public class ShapeView extends View {

	public ShapeView(Context context) {
		super(context);
		
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setColor(0xFFFFFFFF);
	}
	
	protected final Paint paint = new Paint();
	
	public void setShapeColor(int color){
		paint.setColor(color);
	}
	
	public int getShapeColor(){
		return paint.getColor();
	}
}
