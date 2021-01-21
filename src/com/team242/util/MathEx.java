/**
 * 
 */
package com.team242.util;

/**
 * @author lost
 *
 */
public class MathEx {
	public static int rem(int x, int y)
	{
		int r = x % y;
		if (r < 0 && y > 0 || r > 0 && y < 0) r += y;
		return r;
	}
}
