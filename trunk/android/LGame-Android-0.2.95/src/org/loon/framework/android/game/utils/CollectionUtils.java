package org.loon.framework.android.game.utils;

import java.lang.reflect.Array;
import java.util.NoSuchElementException;




/**
 * 
 * Copyright 2008 - 2009
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * @project loonframework
 * @author chenpeng
 * @email：ceponline@yahoo.com.cn
 * @version 0.1.1
 */

final public class CollectionUtils {

	final static public int INITIAL_CAPACITY = 10;

	protected CollectionUtils() {
		super();
	}

	/**
	 * 判定指定对象是否存在于指定对象数组中
	 * 
	 * @param array
	 * @param obj
	 * @return
	 */
	public static int indexOf(Object[] array, Object obj) {
		for (int i = 0; i < array.length; ++i) {
			if (obj == array[i]) {
				return i;
			}
		}
		throw new NoSuchElementException("" + obj);
	}
	
	/**
	 * 扩充指定数组
	 * 
	 * @param obj
	 * @param i
	 * @param flag
	 * @return
	 */
	public static Object expand(Object obj, int i, boolean flag) {
		int j = Array.getLength(obj);
		Object obj1 = Array.newInstance(obj.getClass().getComponentType(), j
				+ i);
		System.arraycopy(obj, 0, obj1, flag ? 0 : i, j);
		return obj1;
	}

	/**
	 * 扩充指定数组
	 * 
	 * @param obj
	 * @param size
	 * @return
	 */
	public static Object expand(Object obj, int size) {
		return expand(obj, size, true);
	}

	/**
	 * 扩充指定数组
	 * 
	 * @param obj
	 * @param size
	 * @param flag
	 * @param class1
	 * @return
	 */
	public static Object expand(Object obj, int size, boolean flag, Class<?> class1) {
		if (obj == null) {
			return Array.newInstance(class1, 1);
		} else {
			return expand(obj, size, flag);
		}
	}

	/**
	 * 剪切出指定长度的数组
	 * 
	 * @param obj
	 * @param size
	 * @return
	 */
	public static Object cut(Object obj, int size) {
		int j;
		if ((j = Array.getLength(obj)) == 1) {
			return Array.newInstance(obj.getClass().getComponentType(), 0);
		}
		int k;
		if ((k = j - size - 1) > 0) {
			System.arraycopy(obj, size + 1, obj, size, k);
		}
		j--;
		Object obj1 = Array.newInstance(obj.getClass().getComponentType(), j);
		System.arraycopy(obj, 0, obj1, 0, j);
		return obj1;
	}

	/**
	 * copy指定长度的数组数据
	 * 
	 * @param obj
	 * @return
	 */
	public static Object[] copyOf(Object[] obj) {
		return copyOf(obj, obj.length);
	}

	/**
	 * copy指定长度的数组数据
	 * 
	 * @param obj
	 * @param newSize
	 * @return
	 */
	public static Object[] copyOf(Object[] obj, int newSize) {
		return (Object[]) copyOf(obj, newSize, ((Object) (obj)).getClass());
	}

	/**
	 * copy指定长度的数组数据
	 * 
	 * @param obj
	 * @param newSize
	 * @param newType
	 * @return
	 */
	private static Object[] copyOf(Object[] obj, int newSize, Class<? extends Object> newType) {
		Object[] copy = ((Object) newType == (Object) Object[].class) ? (Object[]) new Object[newSize]
				: (Object[]) Array.newInstance(newType.getComponentType(),
						newSize);
		return copy;
	}

	/**
	 * copy指定长度的数组数据
	 * 
	 * @param obj
	 * @return
	 */
	public static int[] copyOf(int[] obj) {
		return copyOf(obj, obj.length);
	}

	/**
	 * copy指定长度的数组数据
	 * 
	 * @param obj
	 * @param newSize
	 * @return
	 */
	public static int[] copyOf(int[] obj, int newSize) {
		int tempArr[] = new int[newSize];
		System.arraycopy(obj, 0, tempArr, 0, Math.min(obj.length, newSize));
		return tempArr;
	}

	/**
	 * copy指定长度的数组数据
	 * 
	 * @param obj
	 * @return
	 */
	public static double[] copyOf(double[] obj) {
		return copyOf(obj, obj.length);
	}

	/**
	 * copy指定长度的数组数据
	 * 
	 * @param obj
	 * @param newSize
	 * @return
	 */
	public static double[] copyOf(double[] obj, int newSize) {
		double tempArr[] = new double[newSize];
		System.arraycopy(obj, 0, tempArr, 0, Math.min(obj.length, newSize));
		return tempArr;
	}

	/**
	 * copy指定长度的数组数据
	 * 
	 * @param obj
	 * @return
	 */
	public static float[] copyOf(float[] obj) {
		return copyOf(obj, obj.length);
	}

	/**
	 * copy指定长度的数组数据
	 * 
	 * @param obj
	 * @param newSize
	 * @return
	 */
	public static float[] copyOf(float[] obj, int newSize) {
		float tempArr[] = new float[newSize];
		System.arraycopy(obj, 0, tempArr, 0, Math.min(obj.length, newSize));
		return tempArr;
	}

	/**
	 * copy指定长度的数组数据
	 * 
	 * @param obj
	 * @return
	 */
	public static byte[] copyOf(byte[] obj) {
		return copyOf(obj, obj.length);
	}

	/**
	 * copy指定长度的数组数据
	 * 
	 * @param obj
	 * @param newSize
	 * @return
	 */
	public static byte[] copyOf(byte[] obj, int newSize) {
		byte tempArr[] = new byte[newSize];
		System.arraycopy(obj, 0, tempArr, 0, Math.min(obj.length, newSize));
		return tempArr;
	}

	/**
	 * copy指定长度的数组数据
	 * 
	 * @param obj
	 * @return
	 */
	public static char[] copyOf(char[] obj) {
		return copyOf(obj, obj.length);
	}

	/**
	 * copy指定长度的数组数据
	 * 
	 * @param obj
	 * @param newSize
	 * @return
	 */
	public static char[] copyOf(char[] obj, int newSize) {
		char tempArr[] = new char[newSize];
		System.arraycopy(obj, 0, tempArr, 0, Math.min(obj.length, newSize));
		return tempArr;
	}

	/**
	 * copy指定长度的数组数据
	 * 
	 * @param obj
	 * @return
	 */
	public static long[] copyOf(long[] obj) {
		return copyOf(obj, obj.length);
	}

	/**
	 * copy指定长度的数组数据
	 * 
	 * @param obj
	 * @param newSize
	 * @return
	 */
	public static long[] copyOf(long[] obj, int newSize) {
		long tempArr[] = new long[newSize];
		System.arraycopy(obj, 0, tempArr, 0, Math.min(obj.length, newSize));
		return tempArr;
	}

	/**
	 * copy指定长度的数组数据
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean[] copyOf(boolean[] obj) {
		return copyOf(obj, obj.length);
	}

	/**
	 * copy指定长度的数组数据
	 * 
	 * @param obj
	 * @param newSize
	 * @return
	 */
	public static boolean[] copyOf(boolean[] obj, int newSize) {
		boolean tempArr[] = new boolean[newSize];
		System.arraycopy(obj, 0, tempArr, 0, Math.min(obj.length, newSize));
		return tempArr;
	}

	


}
