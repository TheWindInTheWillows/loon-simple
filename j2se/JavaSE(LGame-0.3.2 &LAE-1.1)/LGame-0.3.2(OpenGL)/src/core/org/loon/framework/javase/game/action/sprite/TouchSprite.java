package org.loon.framework.javase.game.action.sprite;

import java.util.HashMap;
import java.util.LinkedList;

import org.loon.framework.javase.game.action.map.AStarFinder;
import org.loon.framework.javase.game.action.map.Field2D;
import org.loon.framework.javase.game.core.LObject;
import org.loon.framework.javase.game.core.LSystem;
import org.loon.framework.javase.game.core.geom.RectBox;
import org.loon.framework.javase.game.core.geom.Vector2f;
import org.loon.framework.javase.game.core.graphics.opengl.GLColor;
import org.loon.framework.javase.game.core.graphics.opengl.GLEx;
import org.loon.framework.javase.game.core.graphics.opengl.LTexture;
import org.loon.framework.javase.game.core.graphics.opengl.LTextures;
import org.loon.framework.javase.game.core.input.LTouch;
import org.loon.framework.javase.game.core.timer.LTimer;
import org.loon.framework.javase.game.utils.CollectionUtils;

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
// 自0.3.2起新增的精灵类，用以让精灵响应触屏操作自动行走。与Actor中MoveTo最大的区别是，该类不缓存行走路径，
// 也不在单独线程中运行，同时却提供了更多的单项操作。
public class TouchSprite extends LObject implements ISprite {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static HashMap<Integer, int[][]> lazyMaps;

	private Field2D field2D;

	private boolean allDirection;

	private LinkedList<Vector2f> findPath = new LinkedList<Vector2f>();

	private int startX, startY, endX, endY, moveX, moveY;

	private int direction, speed, touchX, touchY, mapID;

	private int width, height, tileWidth, tileHeight;

	private int readerWidth, readerHeight;

	protected final static int BLOCK_SIZE = 32;

	private LTexture texture;

	private boolean isComplete, isVisible, isColse;

	private float alpha, rotation;

	private GLColor filterColor;

	private ISprite sprite;

	private Bind bind;

	private LTimer timer;

	public TouchSprite() {
		this(true);
	}

	public TouchSprite(boolean all) {
		this((LTexture) null, BLOCK_SIZE, BLOCK_SIZE, all);
	}

	public TouchSprite(String path) {
		this(path, true);
	}

	/**
	 * all为true时八方向行走，否则为四方向行走。
	 * 
	 * @param path
	 * @param all
	 */
	public TouchSprite(String path, boolean all) {
		this(LTextures.loadTexture(path).get(), 0, 0, all);
	}

	public TouchSprite(int tileWidth, int tileHeight) {
		this(tileWidth, tileHeight, true);
	}

	public TouchSprite(int tileWidth, int tileHeight, boolean all) {
		this((LTexture) null, tileWidth, tileHeight, all);
	}

	public TouchSprite(String path, int tileWidth, int tileHeight, boolean all) {
		this(LTextures.loadTexture(path).get(), tileWidth, tileHeight, all);
	}

	public TouchSprite(LTexture tex2d, int tileWidth, int tileHeight,
			boolean all) {
		this(null, tex2d, tileWidth, tileHeight, LSystem.screenRect.width,
				LSystem.screenRect.height, all);
	}

	public TouchSprite(Field2D field, LTexture tex2d, int tileWidth,
			int tileHeight, int maxWidth, int maxHeight, boolean all) {
		if (lazyMaps == null) {
			lazyMaps = new HashMap<Integer, int[][]>();
		}
		this.readerWidth = maxWidth;
		this.readerHeight = maxHeight;
		this.timer = new LTimer(0);
		this.isVisible = true;
		this.isColse = false;
		this.isComplete = false;
		this.allDirection = all;
		this.setTexture(tex2d);
		if (field == null) {
			if (tileWidth > 0) {
				this.tileWidth = tileWidth;
			} else {
				this.tileWidth = this.width;
			}
			if (tileHeight > 0) {
				this.tileHeight = tileHeight;
			} else {
				this.tileHeight = this.height;
			}
			int w = maxWidth / this.tileWidth;
			int h = maxHeight / this.tileHeight;
			this.mapID = LSystem.unite(mapID, w);
			this.mapID = LSystem.unite(mapID, h);
			int[][] maps = lazyMaps.get(mapID);
			if (maps == null) {
				maps = new int[h][w];
			}
			this.field2D = new Field2D(maps, this.tileWidth, this.tileHeight);
		} else {
			this.tileWidth = field.getTileWidth();
			this.tileHeight = field.getTileHeight();
			this.field2D = field;
		}
		this.speed = 4;
	}

	public void updateMove() {
		synchronized (TouchSprite.class) {
			if (!getCollisionBox().contains(touchX, touchY)) {
				if (findPath != null) {
					findPath.clear();
				}
				findPath = AStarFinder.find(field2D, field2D
						.pixelsToTilesWidth(x()), field2D
						.pixelsToTilesHeight(y()), field2D
						.pixelsToTilesWidth(touchX), field2D
						.pixelsToTilesHeight(touchY), allDirection);
			} else if (findPath != null) {
				findPath.clear();
			}
		}
	}

	public int hashCode() {
		if (field2D == null) {
			return super.hashCode();
		}
		int hashCode = 1;
		hashCode = LSystem.unite(hashCode, allDirection);
		hashCode = LSystem.unite(hashCode, field2D.pixelsToTilesWidth(x()));
		hashCode = LSystem.unite(hashCode, field2D.pixelsToTilesHeight(y()));
		hashCode = LSystem.unite(hashCode, field2D.pixelsToTilesWidth(touchX));
		hashCode = LSystem.unite(hashCode, field2D.pixelsToTilesHeight(touchY));
		hashCode = LSystem.unite(hashCode, field2D.getWidth());
		hashCode = LSystem.unite(hashCode, field2D.getHeight());
		hashCode = LSystem.unite(hashCode, field2D.getTileWidth());
		hashCode = LSystem.unite(hashCode, field2D.getTileHeight());
		hashCode = LSystem.unite(hashCode, CollectionUtils.hashCode(field2D
				.getMap()));
		return hashCode;
	}

	public void setTexture(String path) {
		this.setTexture(LTextures.loadTexture(path).get());
	}

	public void setTexture(LTexture tex2d) {
		if (tex2d != null) {
			this.texture = tex2d;
			this.width = texture.getWidth() > LSystem.screenRect.width ? BLOCK_SIZE
					: texture.getWidth();
			this.height = texture.getHeight() > LSystem.screenRect.height ? BLOCK_SIZE
					: texture.getHeight();
		} else {
			this.width = BLOCK_SIZE;
			this.height = BLOCK_SIZE;
		}
	}

	public Field2D getField2D() {
		return new Field2D(field2D);
	}

	public void onTouch(LTouch e) {
		this.onTouch(e.x(), e.y());
	}

	public void onTouch(int x, int y) {
		this.touchX = x;
		this.touchY = y;
		this.updateMove();
	}

	public int getTouchX() {
		return touchX;
	}

	public int getTouchY() {
		return touchY;
	}

	public void onPosition(LTouch e) {
		this.onPosition(e.getX(), e.getY());
	}

	public void onPosition(float x, float y) {
		if (findPath == null) {
			return;
		}
		synchronized (findPath) {
			if (findPath != null) {
				findPath.clear();
			}
		}
		this.setLocation(x, y);
	}

	public void update(long elapsedTime) {
		if (field2D == null || findPath == null) {
			return;
		}
		if (isComplete()) {
			return;
		}
		if (timer.action(elapsedTime)) {
			synchronized (findPath) {
				if (endX == startX && endY == startY) {
					if (findPath != null) {
						if (findPath.size() > 1) {
							Vector2f moveStart = (Vector2f) findPath.get(0);
							Vector2f moveEnd = (Vector2f) findPath.get(1);
							startX = field2D.tilesToWidthPixels(moveStart.x());
							startY = field2D.tilesToHeightPixels(moveStart.y());
							endX = moveEnd.x() * field2D.getTileWidth();
							endY = moveEnd.y() * field2D.getTileHeight();
							moveX = moveEnd.x() - moveStart.x();
							moveY = moveEnd.y() - moveStart.y();
							direction = Field2D.getDirection(moveX, moveY);
							findPath.remove(0);
						} else {
							findPath.clear();
						}
					}
				}
				switch (direction) {
				case Field2D.TUP:
					startY -= speed;
					if (startY < endY) {
						startY = endY;
					}
					break;
				case Field2D.TDOWN:
					startY += speed;
					if (startY > endY) {
						startY = endY;
					}
					break;
				case Field2D.TLEFT:
					startX -= speed;
					if (startX < endX) {
						startX = endX;
					}
					break;
				case Field2D.TRIGHT:
					startX += speed;
					if (startX > endX) {
						startX = endX;
					}
					break;
				case Field2D.UP:
					startX += speed;
					startY -= speed;
					if (startX > endX) {
						startX = endX;
					}
					if (startY < endY) {
						startY = endY;
					}
					break;
				case Field2D.DOWN:
					startX -= speed;
					startY += speed;
					if (startX < endX) {
						startX = endX;
					}
					if (startY > endY) {
						startY = endY;
					}
					break;
				case Field2D.LEFT:
					startX -= speed;
					startY -= speed;
					if (startX < endX) {
						startX = endX;
					}
					if (startY < endY) {
						startY = endY;
					}
					break;
				case Field2D.RIGHT:
					startX += speed;
					startY += speed;
					if (startX > endX) {
						startX = endX;
					}
					if (startY > endY) {
						startY = endY;
					}
					break;
				}
				setLocation(startX, startY);
				if (sprite != null) {
					try {
						if (bind != null && bind.isBindPos()) {
							synchronized (sprite) {
								bind.callPos(getX(), getY());
							}
						}
					} catch (Exception e) {
					}
					sprite.update(elapsedTime);
				}
			}
		}
	}

	public long getDelay() {
		return timer.getDelay();
	}

	public void setDelay(long d) {
		timer.setDelay(d);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getDirection() {
		return direction;
	}

	public float getRotation() {
		return rotation;
	}

	public void setRotation(float r) {
		this.rotation = r;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public boolean isComplete() {
		return findPath == null || findPath.size() == 0 || isComplete;
	}

	public void setComplete(boolean c) {
		this.isComplete = true;
	}

	public void createUI(GLEx g) {
		if (!isVisible || isColse) {
			return;
		}
		synchronized (findPath) {
			if (sprite != null) {
				sprite.createUI(g);
				return;
			}
			if (alpha > 0 && alpha < 1) {
				g.setAlpha(alpha);
			}
			if (texture != null) {
				if (filterColor != null) {
					g.drawTexture(texture, getX(), getY(), filterColor,
							rotation);
				} else {
					g.drawTexture(texture, getX(), getY(), rotation);
				}
			}
			if (alpha > 0 && alpha < 1) {
				g.setAlpha(1);
			}
		}
	}

	public void setFilterColor(GLColor f) {
		this.filterColor = f;
	}

	public GLColor getFilterColor() {
		return this.filterColor;
	}

	public ISprite getBindSprite() {
		return sprite;
	}

	public void bind(ISprite sprite) {
		if (sprite == null) {
			this.sprite = null;
			this.bind = null;
		} else {
			this.sprite = sprite;
			this.bind = new Bind(sprite);
		}
	}

	public float getAlpha() {
		return alpha;
	}

	public void setAlpha(float a) {
		this.alpha = a;
	}

	public LTexture getBitmap() {
		return texture;
	}

	public float getRotationTo(float x, float y) {
		float r = (float) Math.atan2(x - x(), y - y());
		return RotateHelper.getAngleDiff(rotation, r);
	}

	public RectBox getCollisionBox() {
		return getRect(x(), y(), width, height);
	}

	public boolean isVisible() {
		return isVisible;
	}

	public void setVisible(boolean v) {
		this.isVisible = v;
	}

	public void dispose() {
		this.isColse = true;
		if (texture != null) {
			texture.destroy();
			texture = null;
		}
		if (findPath != null) {
			findPath.clear();
			findPath = null;
		}
		if (lazyMaps != null) {
			lazyMaps.remove(mapID);
		}
	}

	public int getReaderHeight() {
		return readerHeight;
	}

	public int getReaderWidth() {
		return readerWidth;
	}

	public void disposeAll() {
		if (lazyMaps != null) {
			lazyMaps.clear();
			lazyMaps = null;
			LSystem.gc();
		}
	}

}
