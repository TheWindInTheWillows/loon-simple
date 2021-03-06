package org.loon.framework.javase.game.core.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.nio.IntBuffer;

import org.loon.framework.javase.game.action.map.shapes.RectBox;
import org.loon.framework.javase.game.action.map.shapes.Triangle;
import org.loon.framework.javase.game.core.LRelease;
import org.loon.framework.javase.game.core.graphics.device.LGraphicsMath;
import org.loon.framework.javase.game.utils.GraphicsUtils;

/**
 * Copyright 2008 - 2011
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
 * @version 0.1
 */
public class LPixmap extends LGraphicsMath implements LRelease {

	public BufferedImage pixelToImage;

	private Color color = Color.white;

	private int background = Color.black.getRGB();

	private int transparent = 0;

	private boolean isClose, isOpaque = true;

	private int[] drawPixels;

	private int x, y, width, height, size;

	private int colorValue;

	private Color xorColor;

	private boolean xorMode;

	private int xorRGB;

	private int translateX, translateY;

	private RectBox defClip;

	private RectBox clip;

	public LPixmap(String fileName) {
		this(fileName, true);
	}

	public LPixmap(String fileName, boolean transparency) {
		Image img = GraphicsUtils.loadNotCacheImage(fileName);
		this.width = img.getWidth(null);
		this.height = img.getHeight(null);
		this.pixelToImage = GraphicsUtils.createImage(width, height,
				transparency);
		Graphics2D g2d = pixelToImage.createGraphics();
		g2d.drawImage(img, 0, 0, null);
		g2d.dispose();
		if (img != null) {
			img.flush();
			img = null;
		}
		this.makeDrawPixels(transparency);
		this.width = pixelToImage.getWidth(null);
		this.height = pixelToImage.getHeight(null);
		this.validateOpaque();
	}

	public LPixmap(int w, int h, boolean transparency) {
		this.width = w;
		this.height = h;
		if (width <= 0) {
			width = 32;
		}
		if (height <= 0) {
			height = 32;
		}
		this.makeDrawPixels(transparency);
		this.size = drawPixels.length;
		this.validateOpaque();

	}

	public LPixmap(int w, int h, Color c) {
		this(w, h, c.getRGB());
	}

	public LPixmap(int w, int h, int c) {
		this.width = w;
		this.height = h;
		if (width <= 0) {
			width = 32;
		}
		if (height <= 0) {
			height = 32;
		}
		this.color = new Color(c);
		this.makeDrawPixels(true);
		for (int i = 0; i < size; i++) {
			drawPixels[i] = c;
		}
	}

	public LPixmap(int w, int h) {
		this.width = w;
		this.height = h;
		if (width <= 0) {
			width = 32;
		}
		if (height <= 0) {
			height = 32;
		}
		this.makeDrawPixels(true);
	}

	/**
	 * 验证当前图像是否不透明
	 * 
	 */
	private void validateOpaque() {
		int size = this.size;
		int pixel;
		for (int i = 0; i < size; i++) {
			pixel = LColor.premultiply(drawPixels[i]);
			if (isOpaque && (pixel >>> 24) < 255) {
				isOpaque = false;
				return;
			}
		}

	}

	/**
	 * 构建图像像素数组及相关信息
	 * 
	 * @param transparency
	 */
	private void makeDrawPixels(boolean transparency) {
		if (pixelToImage == null) {
			this.pixelToImage = GraphicsUtils.createImage(width, height,
					transparency);
		}
		if (transparency) {
			this.transparent = 0;
		} else {
			this.transparent = pixelToImage.getTransparency();
		}
		WritableRaster writableRaster = pixelToImage.getRaster();
		DataBuffer dataBuffer = writableRaster.getDataBuffer();
		this.drawPixels = AWTDataBufferHelper.getDataInt(dataBuffer);
		this.size = drawPixels.length;

		this.defClip = new RectBox(0, 0, width, height);
		this.clip = new RectBox(0, 0, width, height);

	}

	/**
	 * 清空屏幕为指定颜色
	 * 
	 * @param c
	 */
	public void clearDraw(int c) {
		if (isClose) {
			return;
		}
		for (int i = 0; i < this.size; i++) {
			drawPoint(drawPixels, i, c);
		}
	}

	/**
	 * 清空屏幕为指定颜色
	 * 
	 * @param c
	 */
	public void clearDraw(Color c) {
		clearDraw(c.getRGB());
	}

	/**
	 * 清空屏幕
	 * 
	 */
	public void clearDraw() {
		clearDraw(background);
	}

	/**
	 * 
	 *
	 */
	public void clear() {
		for (int i = 0; i < size; i++) {
			drawPixels[i] = 0;
		}
	}

	/**
	 * 清空屏幕
	 * 
	 */
	public void fill() {
		clearDraw(color.getRGB());
	}

	/**
	 * 向指定坐标插入像素
	 * 
	 * @param x
	 * @param y
	 */
	public void putPixel(int x, int y) {
		putPixel(x, y, colorValue);
	}

	/**
	 * 向指定坐标插入像素
	 * 
	 * @param x
	 * @param y
	 */
	public void putPixel(int x, int y, int c) {
		if (isClose) {
			return;
		}
		if (x < 0 || x >= width || y < 0 || y >= height) {
			return;
		} else {
			drawPoint(x, y, c);
		}
	}

	/**
	 * 获得指定区域的RGB色彩
	 * 
	 * @param pixels
	 * @return
	 */
	public int[] getRGB(int[] pixels) {
		getRGB(0, 0, width, height, pixels, 0, width);
		return pixels;
	}

	/**
	 * 获得指定区域的RGB色彩
	 * 
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @return
	 */
	public int[] getRGB(int x, int y, int w, int h) {
		int[] pixels = new int[w * h];
		getRGB(x, y, w, h, pixels, 0, w);
		return pixels;
	}

	/**
	 * 获得指定区域的RGB色彩
	 * 
	 * @param offset
	 * @param stride
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return
	 */
	public int[] getRGB(int offset, int stride, int x, int y, int width,
			int height) {
		int pixels[] = new int[width * height];
		getRGB(x, y, width, height, pixels, offset, stride);
		return pixels;
	}

	/**
	 * 获得指定区域的RGB色彩
	 * 
	 * @param x
	 * @param y
	 * @param arrays
	 * @return
	 */
	public int[] getRGB(int x, int y, int[] pixels) {
		int pixel = getPixel(x, y);
		if (pixels == null) {
			pixels = new int[3];
		}
		pixels[0] = (pixel >> 16) & 0xff;
		pixels[1] = (pixel >> 8) & 0xff;
		pixels[2] = (pixel) & 0xff;
		return pixels;
	}

	/**
	 * 获得指定区域的RGB色彩
	 * 
	 * @param startX
	 * @param startY
	 * @param w
	 * @param h
	 * @param rgbArray
	 * @param offset
	 * @param scansize
	 * @return
	 */
	public int[] getRGB(int startX, int startY, int w, int h, int[] rgbArray,
			int offset, int scansize) {
		int yoff = offset;
		int off;
		if (rgbArray == null) {
			rgbArray = new int[offset + h * scansize];
		}
		for (int y = startY; y < startY + h; y++, yoff += scansize) {
			off = yoff;
			for (int x = startX; x < startX + w; x++) {
				rgbArray[off++] = getPixel(x, y);
			}
		}
		return rgbArray;
	}

	/**
	 * 设定指定区域的RGB色彩
	 * 
	 * @param startX
	 * @param startY
	 * @param w
	 * @param h
	 * @param rgbArray
	 * @param offset
	 * @param scansize
	 */
	public void setRGB(int startX, int startY, int w, int h, int[] rgbArray,
			int offset, int scansize) {
		int yoff = offset;
		int off;
		for (int y = startY; y < startY + h; y++, yoff += scansize) {
			off = yoff;
			for (int x = startX; x < startX + w; x++) {
				int pixel = rgbArray[off++];
				putPixel(x, y, pixel);
			}
		}
	}

	/**
	 * 设定指定区域的RGB色彩
	 * 
	 * @param pixels
	 * @param width
	 * @param height
	 */
	public void setRGB(int[] pixels, int width, int height) {
		setRGB(0, 0, width, height, pixels, 0, width);
	}

	/**
	 * 设定指定区域的RGB色彩
	 * 
	 * @param pixels
	 * @param offset
	 * @param stride
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void setRGB(int[] pixels, int offset, int stride, int x, int y,
			int width, int height) {
		setRGB(x, y, width, height, pixels, offset, stride);
	}

	/**
	 * 设定指定区域的RGB色彩
	 * 
	 * @param pixels
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @return
	 */
	public int[] setRGB(int[] pixels, int x, int y, int w, int h) {
		setRGB(x, y, w, h, pixels, 0, w);
		return pixels;
	}

	/**
	 * 设定指定区域的RGB色彩
	 * 
	 * @param x
	 * @param y
	 * @param pixels
	 */
	public void setRGB(int x, int y, int[] pixels) {
		putPixel(x, y, (255 << 24) | (pixels[0] << 16) | (pixels[1] << 8)
				| pixels[2]);
	}

	/**
	 * 设定指定区域的RGB色彩
	 * 
	 * @param c
	 * @param x
	 * @param y
	 */
	public void setRGB(Color c, int x, int y) {
		putPixel(x, y, c.getRGB());
	}

	/**
	 * 设定指定区域的RGB色彩
	 * 
	 * @param rgb
	 * @param x
	 * @param y
	 */
	public void setRGB(int rgb, int x, int y) {
		putPixel(x, y, rgb);
	}

	/**
	 * 获得指定位置像素
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public int getPixel(int x, int y) {
		if (isClose) {
			return -1;
		}
		if (x < 0 || x >= width || y < 0 || y >= height) {
			return -1;
		} else {
			return drawPixels[y * width + x];
		}
	}

	/**
	 * 镜像翻转当前LPixmap为新图
	 * 
	 * @return
	 */
	public LPixmap mirror() {
		return mirror(true, false);
	}

	/**
	 * 水平翻转当前LPixmap为新图
	 * 
	 * @return
	 */
	public LPixmap flip() {
		return mirror(false, true);
	}

	/**
	 * 翻转当前LPixmap为新图
	 * 
	 * @param flag
	 * @param flag1
	 * @return
	 */
	public LPixmap mirror(boolean mirror, boolean flip) {
		if (isClose) {
			return null;
		}
		LPixmap pixel = new LPixmap(width, height, !isOpaque);
		if (pixel == null) {
			return null;
		}
		int[] pixels = pixel.drawPixels;
		int index = 0;
		int pixelIndex = (mirror ? width - 1 : 0)
				+ (flip ? width * (height - 1) : 0);
		int flag = (mirror ? -1 : 1);
		int offset = (mirror ? width * 2 : 0) + (flip ? -width * 2 : 0);
		pixel.width = width;
		pixel.height = height;
		pixel.transparent = transparent;
		for (int j = 0; j < height;) {
			for (int i = 0; i < width;) {
				pixels[pixelIndex] = drawPixels[index];
				i++;
				index++;
				pixelIndex += flag;
			}
			j++;
			pixelIndex += offset;
		}
		return pixel;
	}

	/**
	 * 将当前LPixmap克隆为新的LPixmap
	 * 
	 * @return
	 */
	public Object clone() {
		return copy(0, 0, width, height);
	}

	/**
	 * 从当前LPixmap中copy指定范围像素为新的LPixmap
	 * 
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @return
	 */
	public LPixmap copy(int x, int y, int w, int h) {
		if (isClose) {
			return null;
		}
		LPixmap pixel = new LPixmap(w, h, !isOpaque);
		if (pixel == null) {
			return null;
		}
		pixel.width = w;
		pixel.height = h;
		pixel.drawPixmap(this, 0, 0, w, h, x, y);
		if (x < 0) {
			w -= x;
			x = 0;
		}
		if (y < 0) {
			h -= y;
			y = 0;
		}
		if (x + w > width) {
			w -= (x + w) - width;
		}
		if (y + h > height) {
			h -= (y + h) - height;
		}
		try {
			for (int size = 0; size < h; size++) {
				System.arraycopy(drawPixels, (y + size) * width + x,
						pixel.drawPixels, size * pixel.width, w);
			}
		} catch (IndexOutOfBoundsException e) {
		}
		return pixel;
	}

	/**
	 * 拆分当前LPixmap为指定数量的瓦片
	 * 
	 * @param row
	 * @param col
	 * @return
	 */
	public LPixmap[] split(int row, int col) {
		if (isClose) {
			return null;
		}
		int count = row * col;
		int w = width / row;
		int h = height / col;

		LPixmap[] pixels = new LPixmap[count];
		for (int i = 0; i < count; i++) {
			int x = (i % row) * w;
			int y = (i / row) * h;
			pixels[i] = copy(x, y, w, h);
		}

		return pixels;
	}

	/**
	 * 修正图像显示位置
	 * 
	 * @param x
	 * @param y
	 */
	public void translate(int x, int y) {
		if (isClose) {
			return;
		}

		translateX = x;
		translateY = y;
		if (defClip != null) {
			defClip.x += translateX;
			defClip.y += translateY;
			clip.x = Math.min(clip.x + translateX, width);
			clip.y = Math.min(clip.y + translateY, height);
			clip.width = Math.min(clip.width + translateX, width - translateX);
			clip.height = Math.min(clip.height + translateY, height
					- translateY);
		}
	}

	/**
	 * 设定当前LPixmap使用的颜色
	 * 
	 * @param color
	 */
	public void setColor(Color color) {
		if (isClose) {
			return;
		}
		this.color = color;
		this.colorValue = color.getRGB() | 0xFF000000;
	}

	/**
	 * 设定当前LPixmap使用的颜色
	 * 
	 * @param red
	 * @param green
	 * @param blue
	 * @param alpha
	 */
	public void setColor(int red, int green, int blue, int alpha) {
		if (isClose) {
			return;
		}
		this.color = new Color(red, green, blue, alpha);
		this.colorValue = LColor.getARGB(red, green, blue, alpha);
	}

	/**
	 * 反回当前LPixmap使用的颜色
	 * 
	 * @return
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * 返回当前LPixmap的RGB数值
	 * 
	 * @return
	 */
	public int getRGB() {
		return colorValue & 0x00FFFFFF;
	}

	/**
	 * 返回当前LPixmap的ARGB数值
	 * 
	 * @return
	 */
	public int getARGB() {
		return colorValue;
	}

	public void setPaintMode() {
		xorColor = null;
		xorMode = false;
		xorRGB = 0;
	}

	public void setXORMode(Color c) {
		xorColor = c;
		xorMode = xorColor != null;
		xorRGB = xorMode ? xorColor.getRGB() : 0;
	}

	public void setXORMode(int red, int green, int blue) {
		this.xorColor = new Color(red, green, blue);
		this.xorMode = true;
		this.xorRGB = ((red & 0xFF) << 16) | ((green & 0xFF) << 8)
				| (blue & 0xFF);
	}

	public RectBox getClipBounds() {
		if (isClose) {
			return null;
		}
		return defClip != null ? new RectBox(defClip.x, defClip.y,
				defClip.width, defClip.height) : new RectBox(0, 0, width,
				height);
	}

	public void clipRect(int x, int y, int width, int height) {
		if (isClose) {
			return;
		}
		if (defClip != null) {
			defClip = defClip.getIntersection(new RectBox(x, y, width, height));
			clip = clip.getIntersection(new RectBox(x + translateX, y
					+ translateY, width, height));
		} else {
			defClip = new RectBox(x, y, width, height);
			clip = new RectBox(x + translateX, y + translateY, width, height);
		}
	}

	public void setClip(int x, int y, int width, int height) {
		if (isClose) {
			return;
		}
		if (defClip == null) {
			defClip = new RectBox(x, y, width, height);
		} else {
			defClip.setBounds(x, y, width, height);
		}
		clip = new RectBox(Math.max(x + translateX, 0), Math.max(
				y + translateY, 0), Math.min(width, width - translateX), Math
				.min(height, height - translateY));
	}

	public RectBox getClip() {
		if (isClose) {
			return null;
		}
		return getClipBounds();
	}

	public void setClip(RectBox clip) {
		setClip(clip.x, clip.y, clip.width, clip.height);
	}

	/**
	 * 绘制五角星
	 */
	public void drawSixStart(Color color, int x, int y, int r) {
		if (isClose) {
			return;
		}
		setColor(color);
		drawTriangle(color, x, y, r);
		drawRTriangle(color, x, y, r);
	}

	/**
	 * 绘制正三角
	 */
	public void drawTriangle(Color color, int x, int y, int r) {
		if (isClose) {
			return;
		}
		int x1 = x;
		int y1 = y - r;
		int x2 = x - (int) (r * Math.cos(Math.PI / 6));
		int y2 = y + (int) (r * Math.sin(Math.PI / 6));
		int x3 = x + (int) (r * Math.cos(Math.PI / 6));
		int y3 = y + (int) (r * Math.sin(Math.PI / 6));
		int[] xpos = new int[3];
		xpos[0] = x1;
		xpos[1] = x2;
		xpos[2] = x3;
		int[] ypos = new int[3];
		ypos[0] = y1;
		ypos[1] = y2;
		ypos[2] = y3;
		setColor(color);
		fillPolygon(xpos, ypos, 3);
	}

	/**
	 * 绘制倒三角
	 */
	public void drawRTriangle(Color color, int x, int y, int r) {
		if (isClose) {
			return;
		}
		int x1 = x;
		int y1 = y + r;
		int x2 = x - (int) (r * Math.cos(Math.PI / 6.0));
		int y2 = y - (int) (r * Math.sin(Math.PI / 6.0));
		int x3 = x + (int) (r * Math.cos(Math.PI / 6.0));
		int y3 = y - (int) (r * Math.sin(Math.PI / 6.0));
		int[] xpos = new int[3];
		xpos[0] = x1;
		xpos[1] = x2;
		xpos[2] = x3;
		int[] ypos = new int[3];
		ypos[0] = y1;
		ypos[1] = y2;
		ypos[2] = y3;
		setColor(color);
		fillPolygon(xpos, ypos, 3);
	}

	/**
	 * 绘制并填充一组三角
	 * 
	 * @param ts
	 */
	public void fillTriangle(Triangle[] ts) {
		fillTriangle(ts, 0, 0);
	}

	/**
	 * 绘制并填充一组三角
	 * 
	 * @param ts
	 * @param x
	 * @param y
	 */
	public void fillTriangle(Triangle[] ts, int x, int y) {
		if (isClose) {
			return;
		}
		if (ts == null) {
			return;
		}
		int size = ts.length;
		for (int i = 0; i < size; i++) {
			fillTriangle(ts[i], x, y);
		}
	}

	/**
	 * 绘制并填充一组三角
	 * 
	 * @param t
	 */
	public void fillTriangle(Triangle t) {
		fillTriangle(t, 0, 0);
	}

	/**
	 * 绘制并填充一组三角
	 * 
	 * @param t
	 * @param x
	 * @param y
	 */
	public void fillTriangle(Triangle t, int x, int y) {
		if (isClose) {
			return;
		}
		if (t == null) {
			return;
		}
		int[] xpos = new int[3];
		int[] ypos = new int[3];
		xpos[0] = x + (int) t.xpoints[0];
		xpos[1] = x + (int) t.xpoints[1];
		xpos[2] = x + (int) t.xpoints[2];
		ypos[0] = y + (int) t.ypoints[0];
		ypos[1] = y + (int) t.ypoints[1];
		ypos[2] = y + (int) t.ypoints[2];
		fillPolygon(xpos, ypos, 3);
	}

	/**
	 * 绘制一组三角
	 * 
	 * @param ts
	 */
	public void drawTriangle(Triangle[] ts) {
		drawTriangle(ts, 0, 0);
	}

	/**
	 * 绘制一组三角
	 * 
	 * @param ts
	 * @param x
	 * @param y
	 */
	public void drawTriangle(Triangle[] ts, int x, int y) {
		if (isClose) {
			return;
		}
		if (ts == null) {
			return;
		}
		int size = ts.length;
		for (int i = 0; i < size; i++) {
			drawTriangle(ts[i], x, y);
		}
	}

	/**
	 * 绘制三角
	 * 
	 * @param t
	 */
	public void drawTriangle(Triangle t) {
		drawTriangle(t, 0, 0);
	}

	/**
	 * 绘制三角
	 * 
	 * @param t
	 * @param x
	 * @param y
	 */
	public void drawTriangle(Triangle t, int x, int y) {
		if (isClose) {
			return;
		}
		if (t == null) {
			return;
		}
		int[] xpos = new int[3];
		int[] ypos = new int[3];
		xpos[0] = x + (int) t.xpoints[0];
		xpos[1] = x + (int) t.xpoints[1];
		xpos[2] = x + (int) t.xpoints[2];
		ypos[0] = y + (int) t.ypoints[0];
		ypos[1] = y + (int) t.ypoints[1];
		ypos[2] = y + (int) t.ypoints[2];
		drawPolygon(xpos, ypos, 3);
	}

	/**
	 * 从指定位置开始截取指定大小像素到指定位置
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param dx
	 * @param dy
	 */
	public void copyArea(int x, int y, int width, int height, int dx, int dy) {
		if (isClose) {
			return;
		}
		x += translateX;
		y += translateY;

		int xStart = x;
		int xEnd = x + width - 1;
		int xStep = 1;
		if (dx < 0) {
			xStart = x + width - 1;
			xEnd = x;
			xStep = -1;
		}
		int yStart = y;
		int yEnd = y + height - 1;
		int yStep = 1;
		if (dy < 0) {
			yStart = y + height - 1;
			yEnd = y;
			yStep = -1;
		}
		for (x = xStart; x <= xEnd; x += xStep) {
			for (y = yStart; y <= yEnd; y += yStep) {
				if (!inside(x + dx, y + dy) && x >= 0 && x < width && y >= 0
						&& y < height) {
					drawPixels[x + dx + (y + dy) * width] = drawPixels[x + y
							* width];
				}
			}
		}
	}

	/**
	 * 绘制一条直线
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	public void drawLine(int x1, int y1, int x2, int y2) {
		if (isClose) {
			return;
		}

		x1 += translateX;
		y1 += translateY;
		x2 += translateX;
		y2 += translateY;

		int dx = x2 - x1;
		int dy = y2 - y1;

		if (dx == 0) {
			if (y1 < y2) {
				drawVerticalLine(x1, y1, y2);
			} else {
				drawVerticalLine(x1, y2, y1);
			}
		} else if (dy == 0) {
			if (x1 < x2) {
				drawLineImpl(x1, x2, y1);
			} else {
				drawLineImpl(x2, x1, y1);
			}
		} else {
			boolean swapXY = false;
			int dxNeg = 1;
			int dyNeg = 1;
			boolean negativeSlope = false;
			if (Math.abs(dy) > Math.abs(dx)) {
				int temp = x1;
				x1 = y1;
				y1 = temp;
				temp = x2;
				x2 = y2;
				y2 = temp;
				dx = x2 - x1;
				dy = y2 - y1;
				swapXY = true;
			}

			if (x1 > x2) {
				int temp = x1;
				x1 = x2;
				x2 = temp;
				temp = y1;
				y1 = y2;
				y2 = temp;
				dx = x2 - x1;
				dy = y2 - y1;
			}

			if (dy * dx < 0) {
				if (dy < 0) {
					dyNeg = -1;
					dxNeg = 1;
				} else {
					dyNeg = 1;
					dxNeg = -1;
				}
				negativeSlope = true;
			}

			int d = 2 * (dy * dyNeg) - (dx * dxNeg);
			int incrH = 2 * dy * dyNeg;
			int incrHV = 2 * ((dy * dyNeg) - (dx * dxNeg));
			int x = x1;
			int y = y1;
			int tempX = x;
			int tempY = y;

			if (swapXY) {
				int temp = x;
				x = y;
				y = temp;
			}

			drawPoint(x, y);
			x = tempX;
			y = tempY;

			while (x < x2) {
				if (d <= 0) {
					x++;
					d += incrH;
				} else {
					d += incrHV;
					x++;
					if (!negativeSlope) {
						y++;
					} else {
						y--;
					}
				}

				tempX = x;
				tempY = y;
				if (swapXY) {
					int temp = x;
					x = y;
					y = temp;
				}
				drawPoint(x, y);
				x = tempX;
				y = tempY;
			}
		}
	}

	/**
	 * 绘制一个矩形
	 * 
	 * @param x1
	 * @param y1
	 * @param w1
	 * @param h1
	 */
	public void drawRect(int x1, int y1, int w1, int h1) {
		if (isClose) {
			return;
		}
		int tempX = x1;
		int tempY = y1;
		int tempWidth = x1 + w1;
		int tempHeight = y1 + h1;
		if (tempX > tempWidth) {
			x1 = tempX;
			tempX = tempWidth;
			tempWidth = x1;
		}
		if (tempY > tempHeight) {
			y1 = tempY;
			tempY = tempHeight;
			tempHeight = y1;
		}
		drawLine(tempX, tempY, tempHeight, tempY);
		drawLine(tempX, tempY + 1, tempX, tempHeight);
		drawLine(tempHeight, tempHeight, tempX + 1, tempHeight);
		drawLine(tempHeight, tempHeight - 1, tempHeight, tempY + 1);
	}

	/**
	 * 绘制一个围绕指定区域旋转的矩形选框
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param arcWidth
	 * @param arcHeight
	 */
	public void drawRoundRect(int x, int y, int width, int height,
			int arcWidth, int arcHeight) {
		if (isClose) {
			return;
		}
		drawLine(x + arcWidth / 2, y, x + width - arcWidth / 2, y);
		drawLine(x, y + arcHeight / 2, x, y + height - arcHeight / 2);
		drawLine(x + arcWidth / 2, y + height, x + width - arcWidth / 2, y
				+ height);
		drawLine(x + width, y + arcHeight / 2, x + width, y + height
				- arcHeight / 2);
		drawArc(x, y, arcWidth, arcHeight, 90, 90);
		drawArc(x + width - arcWidth, y, arcWidth, arcHeight, 0, 90);
		drawArc(x, y + height + -arcHeight, arcWidth, arcHeight, 180, 90);
		drawArc(x + width - arcWidth, y + height + -arcHeight, arcWidth,
				arcHeight, 270, 90);
	}

	/**
	 * 填充一个围绕指定区域旋转的矩形选框
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param arcWidth
	 * @param arcHeight
	 */
	public void fillRoundRect(int x, int y, int width, int height,
			int arcWidth, int arcHeight) {
		if (isClose) {
			return;
		}
		fillRect(x + arcWidth / 2, y, width - arcWidth + 1, height);
		fillRect(x, y + arcHeight / 2 - 1, arcWidth / 2, height - arcHeight);
		fillRect(x + width - arcWidth / 2, y + arcHeight / 2 - 1, arcWidth / 2,
				height - arcHeight);

		fillArc(x, y, arcWidth - 1, arcHeight - 1, 90, 90);
		fillArc(x + width - arcWidth, y, arcWidth - 1, arcHeight - 1, 0, 90);
		fillArc(x, y + height + -arcHeight, arcWidth - 1, arcHeight - 1, 180,
				90);
		fillArc(x + width - arcWidth, y + height + -arcHeight, arcWidth - 1,
				arcHeight - 1, 270, 90);
	}

	/**
	 * 绘制一个3D的矩形选框
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param raised
	 */
	public void draw3DRect(int x, int y, int width, int height, boolean raised) {
		if (isClose) {
			return;
		}
		int currentARGB = colorValue;
		int brighter = color.brighter().getRGB();
		int darker = color.darker().getRGB();
		colorValue = raised ? brighter : darker;
		drawLine(x, y, x, y + height);
		drawLine(x + 1, y, x + width - 1, y);
		colorValue = raised ? darker : brighter;
		drawLine(x + 1, y + height, x + width, y + height);
		drawLine(x + width, y, x + width, y + height - 1);
		colorValue = currentARGB;
	}

	/**
	 * 填充一个3D的矩形选框
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param raised
	 */
	public void fill3DRect(int x, int y, int width, int height, boolean raised) {
		if (isClose) {
			return;
		}
		int currentARGB = colorValue;
		int brighter = color.brighter().getRGB();
		int darker = color.darker().getRGB();
		if (!raised) {
			colorValue = darker;
		}
		fillRect(x + 1, y + 1, width - 2, height - 2);
		colorValue = raised ? brighter : darker;
		drawLine(x, y, x, y + height - 1);
		drawLine(x + 1, y, x + width - 2, y);
		colorValue = raised ? darker : brighter;
		drawLine(x + 1, y + height - 1, x + width - 1, y + height - 1);
		drawLine(x + width - 1, y, x + width - 1, y + height - 2);
		colorValue = currentARGB;
	}

	/**
	 * 将一个指定的LPixmap绘制到当前LPixmap
	 * 
	 * @param pixel
	 * @param x
	 * @param y
	 */
	public void drawPixmap(LPixmap pixel, int x, int y) {
		if (pixel == null) {
			return;
		} else {
			drawPixmap(pixel, x, y, pixel.width, pixel.height, 0, 0);
			return;
		}
	}

	/**
	 * 将一个指定的LPixmap绘制到当前LPixmap
	 * 
	 * @param pixel
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @param offsetX
	 * @param offsetY
	 */
	public void drawPixmap(LPixmap pixel, int x, int y, int w, int h,
			int offsetX, int offsetY) {
		if (isClose) {
			return;
		}

		x += translateX;
		y += translateY;

		int[] currentPixels = pixel.drawPixels;
		int transparent = pixel.transparent;
		if (x < 0) {
			w += x;
			offsetX -= x;
			x = 0;
		}
		if (y < 0) {
			h += y;
			offsetY -= y;
			y = 0;
		}
		if (x + w > width) {
			w = width - x;
		}
		if (y + h > height) {
			h = height - y;
		}
		if (w < 0 || h < 0) {
			return;
		}
		if (transparent < 0) {
			for (int size = 0; size < h; size++) {
				System.arraycopy(currentPixels, (offsetY + size) * pixel.width
						+ offsetX, drawPixels, (y + size) * width + x, w);
			}
		} else {
			int findIndex = y * width + x;
			int drawIndex = offsetY * pixel.width + offsetX;
			int moveFind = width - w;
			int moveDraw = pixel.width - w;
			for (int i = 0; i < h; i++) {
				for (int j = 0; j < w;) {
					if (inside(j, i)) {
						continue;
					}
					if (currentPixels[drawIndex] != transparent) {
						drawPoint(drawPixels, findIndex,
								currentPixels[drawIndex]);
					}
					j++;
					findIndex++;
					drawIndex++;
				}
				findIndex += moveFind;
				drawIndex += moveDraw;
			}
		}
	}

	/**
	 * 将一个指定的LPixmap绘制到当前LPixmap，并扩展为指定大小
	 * 
	 * @param pixel
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	public void drawPixmap(LPixmap pixel, int x, int y, int w, int h) {
		if (pixel == null) {
			return;
		} else {
			drawPixmap(pixel, x, y, w, h, 0, 0, pixel.width, pixel.height);
			return;
		}
	}

	/**
	 * 将一个指定的LPixmap绘制到当前LPixmap，并截取为指定大小
	 * 
	 * @param pixel
	 * @param x1
	 * @param y1
	 * @param w1
	 * @param h1
	 * @param x2
	 * @param y2
	 * @param w2
	 * @param h2
	 */
	public void drawPixmap(LPixmap pixel, int x1, int y1, int w1, int h1,
			int x2, int y2, int w2, int h2) {
		if (isClose) {
			return;
		}

		x1 += translateX;
		y1 += translateY;
		x2 += translateX;
		y2 += translateY;

		if (pixel == null || w1 <= 0 || h1 <= 0 || w2 <= 0 || h2 <= 0) {
			return;
		}
		if (w1 == w2 && h1 == h2) {
			drawPixmap(pixel, x1, y1, w1, h1, x2, y2);
			return;
		}
		int[] currentPixels = pixel.drawPixels;
		int transparent = pixel.transparent;
		int tw = (w2 << 16) / w1;
		int th = (h2 << 16) / h1;
		if (w1 <= 0 || h1 <= 0) {
			return;
		}
		if (x1 < 0) {
			w2 += (x1 * w2) / w1;
			w1 += x1;
			x2 -= (x1 * w2) / w1;
			x1 = 0;
		}
		if (y1 < 0) {
			h2 += (y1 * h2) / h1;
			h1 += y1;
			y2 -= (y1 * h2) / h1;
			y1 = 0;
		}
		if (x1 + w1 > width) {
			w2 -= (((x1 + w1) - width) * w2) / w1;
			w1 = width - x1;
		}
		if (y1 + h1 > height) {
			h2 -= (((y1 + h1) - height) * h2) / h1;
			h1 = height - y1;
		}
		if (w1 <= 0 || h1 <= 0) {
			return;
		}

		int pixelIndex = y1 * width + x1;
		int offset = y2 << 16;

		for (int i = 0; i < h1; i++) {
			int x = (offset >> 16) * pixel.width;
			int y = x2 << 16;
			for (int j = 0; j < w1; j++) {
				if (inside(i, j)) {
					continue;
				}
				int color;
				if ((color = currentPixels[x + (y >> 16)]) != transparent) {
					drawPoint(drawPixels, pixelIndex, color);
				}
				y += tw;
				pixelIndex++;
			}
			offset += th;
			pixelIndex += width - w1;
		}

	}

	public void fillRect(int x, int y, int width, int height) {
		if (isClose) {
			return;
		}
		int maxX = Math
				.min(x + width - 1 + translateX, clip.x + clip.width - 1);
		int maxY = Math.min(y + height - 1 + translateY, clip.y + clip.height
				- 1);
		for (int row = Math.max(y + translateY, clip.y), rowOffset = row
				* width; row <= maxY; row++, rowOffset += width) {
			for (int col = Math.max(x + translateX, clip.x); col <= maxX; col++) {
				drawPoint(drawPixels, col + rowOffset);
			}
		}

	}

	public void clearRect(int x, int y, int width, int height) {
		fillRect(x, y, width, height);
	}

	/**
	 * 绘制一个椭圆形
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void drawOval(int x, int y, int width, int height) {
		if (isClose) {
			return;
		}
		drawCircle(x, y, width, height, false, new CircleUpdate() {
			public void newPoint(int xLeft, int yTop, int xRight, int yBottom) {
				drawPoint(xLeft, yTop);
				drawPoint(xRight, yTop);
				drawPoint(xLeft, yBottom);
				drawPoint(xRight, yBottom);
			}
		});

	}

	/**
	 * 填充一个椭圆形
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void fillOval(int x, int y, int width, int height) {
		if (isClose) {
			return;
		}
		drawCircle(x, y, width, height, true, new CircleUpdate() {
			public void newPoint(int xLeft, int yTop, int xRight, int yBottom) {
				drawLineImpl(xLeft, xRight, yTop);
				if (yTop != yBottom)
					drawLineImpl(xLeft, xRight, yBottom);
			}
		});

	}

	/**
	 * 绘制一个弧线
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param start
	 * @param arcAngle
	 */
	public void drawArc(int x, int y, int width, int height, int start,
			int arcAngle) {
		if (isClose) {
			return;
		}
		if (arcAngle == 0) {
			return;
		}
		start %= 360;
		if (start < 0) {
			start += 360;
		}
		if (arcAngle % 360 == 0) {
			arcAngle = 360;
		} else {
			arcAngle %= 360;
		}
		final int startAngle = arcAngle > 0 ? start
				: (start + arcAngle < 0 ? start + arcAngle + 360 : start
						+ arcAngle);

		final int centerX = x + translateX + width / 2;
		final int centerY = y + translateY + height / 2;
		final int xPoints[] = new int[7];
		final int yPoints[] = new int[7];
		final int nPoints = getBoundingShape(xPoints, yPoints, startAngle, Math
				.abs(arcAngle), centerX, centerY, x + translateX - 1, y
				+ translateY - 1, width + 2, height + 2);
		final RectBox bounds = getBoundingBox(xPoints, yPoints, nPoints)
				.getIntersection(clip);
		this.drawCircle(x, y, width, height, false, new CircleUpdate() {
			public void newPoint(int xLeft, int yTop, int xRight, int yBottom) {
				drawArcPoint(xPoints, yPoints, nPoints, bounds, xLeft, yTop);
				drawArcPoint(xPoints, yPoints, nPoints, bounds, xRight, yTop);
				drawArcPoint(xPoints, yPoints, nPoints, bounds, xLeft, yBottom);
				drawArcPoint(xPoints, yPoints, nPoints, bounds, xRight, yBottom);
			}
		});
	}

	/**
	 * 填充一个弧线
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param start
	 * @param arcAngle
	 */
	public void fillArc(int x, int y, int width, int height, int start,
			int arcAngle) {
		if (arcAngle == 0) {
			return;
		}
		start %= 360;
		if (start < 0) {
			start += 360;
		}
		if (arcAngle % 360 == 0) {
			fillOval(x, y, width, height);
		} else {
			arcAngle %= 360;
		}
		final int startAngle = arcAngle > 0 ? start
				: (start + arcAngle < 0 ? start + arcAngle + 360 : start
						+ arcAngle);
		final int centerX = x + translateX + width / 2;
		final int centerY = y + translateY + height / 2;
		final int xPoints[] = new int[7];
		final int yPoints[] = new int[7];
		final int nPoints = getBoundingShape(xPoints, yPoints, startAngle, Math
				.abs(arcAngle), centerX, centerY, x + translateX - 1, y
				+ translateY - 1, width + 2, height + 2);
		final RectBox bounds = getBoundingBox(xPoints, yPoints, nPoints)
				.getIntersection(clip);
		this.drawCircle(x, y, width, height, true, new CircleUpdate() {
			public void newPoint(int xLeft, int yTop, int xRight, int yBottom) {
				drawArcImpl(xPoints, yPoints, nPoints, bounds, xLeft, xRight,
						yTop);
				if (yTop != yBottom) {
					drawArcImpl(xPoints, yPoints, nPoints, bounds, xLeft,
							xRight, yBottom);
				}
			}
		});
	}

	public void drawPolyline(int xPoints[], int yPoints[], int nPoints) {
		if (isClose) {
			return;
		}
		for (int i = 1; i < nPoints; i++) {
			drawLine(xPoints[i - 1], yPoints[i - 1], xPoints[i], yPoints[i]);
		}
	}

	/**
	 * 绘制一个多边形
	 * 
	 * @param p
	 */
	public void drawPolygon(Polygon p) {
		drawPolygon(p.xpoints, p.ypoints, p.npoints);
	}

	/**
	 * 绘制一个多边形
	 * 
	 * @param xPoints
	 * @param yPoints
	 * @param nPoints
	 */
	public void drawPolygon(int xPoints[], int yPoints[], int nPoints) {
		drawPolyline(xPoints, yPoints, nPoints);
		drawLine(xPoints[nPoints - 1], yPoints[nPoints - 1], xPoints[0],
				yPoints[0]);
	}

	/**
	 * 绘制并填充一个多边形
	 * 
	 * @param p
	 */
	public void fillPolygon(Polygon p) {
		fillPolygon(p.xpoints, p.ypoints, p.npoints);
	}

	/**
	 * 绘制并填充一个多边形
	 * 
	 * @param xPoints
	 * @param yPoints
	 * @param nPoints
	 */
	public void fillPolygon(int xPoints[], int yPoints[], int nPoints) {
		if (isClose) {
			return;
		}
		int xPointsCopy[];
		if (translateX == 0) {
			xPointsCopy = xPoints;
		} else {
			xPointsCopy = (int[]) xPoints.clone();
			for (int i = 0; i < nPoints; i++) {
				xPointsCopy[i] += translateX;
			}
		}
		int yPointsCopy[];
		if (translateY == 0) {
			yPointsCopy = yPoints;
		} else {
			yPointsCopy = (int[]) yPoints.clone();
			for (int i = 0; i < nPoints; i++) {
				yPointsCopy[i] += translateY;
			}
		}
		RectBox bounds = getBoundingBox(xPointsCopy, yPointsCopy, nPoints)
				.getIntersection(clip);
		for (int x = bounds.x; x < bounds.x + bounds.width; x++) {
			for (int y = bounds.y; y < bounds.y + bounds.height; y++) {
				if (contains(xPointsCopy, yPointsCopy, nPoints, bounds, x, y)) {
					drawPoint(x, y);
				}
			}
		}

	}

	private void drawLineImpl(int x1, int x2, int y) {
		if (isClose) {
			return;
		}
		if (y >= clip.y && y < clip.y + clip.height) {
			y *= width;
			int maxX = Math.min(x2, clip.x + clip.width - 1);
			if (drawPixels != null)
				for (int x = Math.max(x1, clip.x); x <= maxX; x++)
					drawPoint(drawPixels, x + y);
		}
	}

	private void drawVerticalLine(int x, int y1, int y2) {
		if (x >= clip.x && x < clip.x + clip.width) {
			int maxY = Math.min(y2, clip.y + clip.height - 1) * width;
			if (drawPixels != null)
				for (int y = Math.max(y1, clip.y) * width; y <= maxY; y += width)
					drawPoint(drawPixels, x + y);
		}
	}

	private void drawPoint(int x, int y) {
		if (!inside(x, y)) {
			drawPoint(drawPixels, x + y * width);
		}
	}

	private void drawPoint(int x, int y, int c) {
		if (!inside(x, y)) {
			int pixelIndex = x + y * width;
			drawPixels[pixelIndex] = xorMode ? 0xFF000000 | ((drawPixels[pixelIndex] ^ c) ^ xorRGB)
					: c;
		}
	}

	private void drawPoint(int[] pixels, int pixelIndex) {
		pixels[pixelIndex] = xorMode ? 0xFF000000 | ((pixels[pixelIndex] ^ colorValue) ^ xorRGB)
				: colorValue;
	}

	private void drawPoint(int[] pixels, int pixelIndex, int c) {
		pixels[pixelIndex] = xorMode ? 0xFF000000 | ((pixels[pixelIndex] ^ c) ^ xorRGB)
				: c;
	}

	private void drawArcPoint(int xPoints[], int yPoints[], int nPoints,
			RectBox bounds, int x, int y) {
		if (contains(xPoints, yPoints, nPoints, bounds, x, y)) {
			drawPoint(x, y);
		}
	}

	private void drawArcImpl(int xPoints[], int yPoints[], int nPoints,
			RectBox bounds, int xLeft, int xRight, int y) {
		if (y >= clip.y && y < clip.y + clip.height) {
			for (int x = Math.max(xLeft, clip.x); x <= xRight; x++) {
				if (contains(xPoints, yPoints, nPoints, bounds, x, y)) {
					drawPoint(x, y);
				}
			}
		}
	}

	private interface CircleUpdate {
		public void newPoint(int xLeft, int yTop, int xRight, int yBottom);
	}

	private void drawCircle(int x, int y, int width, int height, boolean fill,
			CircleUpdate listener) {
		int a = width / 2;
		int b = height / 2;
		long squareA = width * width / 4;
		long squareB = height * height / 4;
		long squareAB = round((long) width * width * height * height, 16L);

		x += translateX;
		y += translateY;
		int centerX = x + a;
		int centerY = y + b;

		int deltaX = (width % 2 == 0) ? 0 : 1;
		int deltaY = (height % 2 == 0) ? 0 : 1;

		int currentY = b;
		int currentX = 0;

		int lastx1 = centerX - currentX;
		int lastx2 = centerX + currentX + deltaX;
		int lasty1 = centerY - currentY;
		int lasty2 = centerY + currentY + deltaY;
		while (currentX <= a && currentY >= 0) {
			long deltaA = (currentX + 1) * (currentX + 1) * squareB + currentY
					* currentY * squareA - squareAB;
			long deltaB = (currentX + 1) * (currentX + 1) * squareB
					+ (currentY - 1) * (currentY - 1) * squareA - squareAB;
			long deltaC = currentX * currentX * squareB + (currentY - 1)
					* (currentY - 1) * squareA - squareAB;
			if (deltaA <= 0) {
				currentX++;
			} else if (deltaC >= 0) {
				currentY--;
			} else {
				long min = Math.min(Math.abs(deltaA), Math.min(
						Math.abs(deltaB), Math.abs(deltaC)));
				if (min == Math.abs(deltaA)) {
					currentX++;
				} else if (min == Math.abs(deltaC)) {
					currentY--;
				} else {
					currentX++;
					currentY--;
				}
			}

			int x1 = centerX - currentX;
			int x2 = centerX + currentX + deltaX;
			int y1 = centerY - currentY;
			int y2 = centerY + currentY + deltaY;
			if (!fill || lasty1 != y1) {
				listener.newPoint(lastx1, lasty1, lastx2, lasty2);
				lasty1 = y1;
				lasty2 = y2;
			}
			lastx1 = x1;
			lastx2 = x2;
		}
		if (lasty1 < lasty2) {
			for (; lasty1 <= lasty2; lasty1++, lasty2--) {
				listener.newPoint(centerX - a, lasty1, centerX + a + deltaX,
						lasty2);
			}
		}
	}

	private boolean inside(int x, int y) {
		return (x < clip.x || x >= clip.x + clip.width || y < clip.y || y >= clip.y
				+ clip.height);
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	/**
	 * 获得当前图像是否不透明
	 * 
	 * @return
	 */
	public boolean isOpaque() {
		return isOpaque;
	}

	/**
	 * 获得图像高
	 * 
	 * @return
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * 获得图像宽
	 * 
	 * @return
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * 获得指定区域是否透明
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isTransparent(int x, int y) {
		if (x < 0 || y < 0 || x >= width || y >= height) {
			return true;
		} else if (isOpaque) {
			return false;
		} else {
			int pixel = drawPixels[x + y * width];
			return (pixel >>> 24) == 0;
		}
	}

	/**
	 * 获得指定区域的ARGB颜色
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public int getARGB(int x, int y) {
		if (x < 0 || y < 0 || x >= width || y >= height) {
			return -1;
		} else {
			return LColor.unpremultiply(drawPixels[x + y * width]);
		}
	}

	public int getTransparent() {
		return transparent;
	}

	public void setTransparent(int transparent) {
		this.transparent = transparent;
	}

	public IntBuffer getPixels() {
		IntBuffer intBuffer = IntBuffer.allocate(this.size);
		intBuffer.put(drawPixels);
		intBuffer.rewind();
		return intBuffer;
	}

	public boolean isClose() {
		return isClose;
	}

	public int getSize() {
		return size;
	}

	public int[] getData() {
		return drawPixels;
	}

	public void dispose() {
		isClose = true;
		if (pixelToImage != null) {
			pixelToImage.flush();
			pixelToImage = null;
		}
		drawPixels = null;
	}

}
