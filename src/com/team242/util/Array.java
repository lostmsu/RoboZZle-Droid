/**
 * 
 */
package com.team242.util;

/**
 * @author lost
 *
 */
public class Array {
	public static void clear(char[][] array){
		for(int i = 0; i < array.length; i++)
			for(int j = 0; j < array[i].length; j++)
				array[i][j] = '\0';
	}
}
