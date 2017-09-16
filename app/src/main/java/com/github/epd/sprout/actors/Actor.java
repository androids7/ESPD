/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.github.epd.sprout.actors;

import android.util.SparseArray;

import com.github.epd.sprout.Dungeon;
import com.github.epd.sprout.Statistics;
import com.github.epd.sprout.actors.blobs.Blob;
import com.github.epd.sprout.actors.buffs.Buff;
import com.github.epd.sprout.actors.hero.Hero;
import com.github.epd.sprout.actors.mobs.Mob;
import com.github.epd.sprout.levels.Level;
import com.github.epd.sprout.scenes.GameScene;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

import java.util.Arrays;
import java.util.HashSet;

public abstract class Actor implements Bundlable {

	public static final float TICK = 1f;

	private float time;

	private int id = 0;

	protected int actPriority = Integer.MAX_VALUE;

	protected abstract boolean act();

	protected void spend(float time) {
		this.time += time;
	}

	protected void postpone(float time) {
		if (this.time < now + time) {
			this.time = now + time;
		}
	}

	protected float cooldown() {
		return time - now;
	}

	protected void diactivate() {
		time = Float.MAX_VALUE;
	}

	protected void onAdd() {
	}

	protected void onRemove() {
	}

	private static final String TIME = "time";
	private static final String ID = "id";

	@Override
	public void storeInBundle(Bundle bundle) {
		bundle.put(TIME, time);
		bundle.put(ID, id);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		time = bundle.getFloat(TIME);
		id = bundle.getInt(ID);
	}

	private static int nextID = 1;

	public int id() {
		if (id > 0) {
			return id;
		} else {
			return (id = nextID++);
		}
	}

	// **********************
	// *** Static members ***

	private static HashSet<Actor> all = new HashSet<Actor>();
	private static volatile Actor current;

	private static SparseArray<Actor> ids = new SparseArray<Actor>();

	private static float now = 0;

	private static Char[] chars = new Char[Level.getLength()];

	public static synchronized void clear() {

		now = 0;

		Arrays.fill(chars, null);
		all.clear();

		ids.clear();
	}

	public static synchronized void fixTime() {

		if (Dungeon.hero != null && all.contains(Dungeon.hero)) {
			Statistics.duration += now;
		}

		float min = Float.MAX_VALUE;
		for (Actor a : all) {
			if (a.time < min) {
				min = a.time;
			}
		}
		for (Actor a : all) {
			a.time -= min;
		}
		now = 0;
	}

	public static void init() {

		addDelayed(Dungeon.hero, -Float.MIN_VALUE);

		for (Mob mob : Dungeon.level.mobs) {
			add(mob);
		}

		for (Blob blob : Dungeon.level.blobs.values()) {
			add(blob);
		}

		current = null;
	}

	private static final String NEXTID = "nextid";

	public static void storeNextID(Bundle bundle) {
		bundle.put(NEXTID, nextID);
	}

	public static void restoreNextID(Bundle bundle) {
		nextID = bundle.getInt(NEXTID);
	}

	public static void resetNextID() {
		nextID = 1;
	}

	public static void occupyCell(Char ch) {
		chars[ch.pos] = ch;
	}

	public static void freeCell(int pos) {
		chars[pos] = null;
	}

	/* protected */
	public void next() {
		if (current == this) {
			current = null;
		}
	}

	public static boolean processing() {
		return current != null;
	}

	public static void process() {

		boolean doNext;
		boolean interrupted = false;

		do {

			current = null;

			Arrays.fill(chars, null);

			if (!interrupted) {
				now = Float.MAX_VALUE;
				for (Actor actor : all) {
					//Order of actions when time is equal:
					//1. Hero
					//2. Other Chars
					//3. Other Actors (e.g. blobs)
					if (actor.time < now || (actor instanceof Hero && actor.time == now)
							|| (actor instanceof Char && actor.time == now && !(current instanceof Hero))) {
						now = actor.time;
						current = actor;
					}

					if (actor instanceof Char) {
						Char ch = (Char) actor;
						chars[ch.pos] = ch;
					}
				}
			}

			if (current != null) {

				Actor acting = current;

				if (acting instanceof Char && ((Char) acting).sprite != null) {
					// If it's character's turn to act, but its sprite
					// is moving, wait till the movement is over
					try {
						synchronized (((Char) acting).sprite) {
							if (((Char) acting).sprite.isMoving) {
								((Char) acting).sprite.wait();
							}
						}
					} catch (InterruptedException e) {
						interrupted = true;
					}
				}

				interrupted = interrupted || Thread.interrupted();

				if (interrupted) {
					doNext = false;
					current = null;
				} else {
					doNext = acting.act();
					if (doNext && !Dungeon.hero.isAlive()) {
						doNext = false;
						current = null;
					}
				}
			} else {
				doNext = false;
			}

			if (!doNext) {
				synchronized (Thread.currentThread()) {

					interrupted = interrupted || Thread.interrupted();

					if (interrupted) {
						current = null;
						interrupted = false;
					}

					synchronized (GameScene.class) {
						//signals to the gamescene that actor processing is finished for now
						GameScene.class.notify();
					}

					try {
						Thread.currentThread().wait();
					} catch (InterruptedException e) {
						interrupted = true;
					}
				}
			}

		} while (true);
	}

	public static void add(Actor actor) {
		add(actor, now);
	}

	public static void addDelayed(Actor actor, float delay) {
		add(actor, now + delay);
	}

	private static synchronized void add(Actor actor, float time) {

		if (all.contains(actor)) {
			return;
		}

		ids.put(actor.id(), actor);

		all.add(actor);
		actor.time += time;
		actor.onAdd();

		if (actor instanceof Char) {
			Char ch = (Char) actor;
			chars[ch.pos] = ch;
			for (Buff buff : ch.buffs()) {
				all.add(buff);
				buff.onAdd();
			}
		}
	}

	public static synchronized void remove(Actor actor) {

		if (actor != null) {
			all.remove(actor);
			actor.onRemove();

			if (actor.id > 0) {
				ids.remove(actor.id);
			}
		}
	}

	public static synchronized Char findChar(int pos) {
		return chars[pos];
	}

	public static synchronized Actor findById(int id) {
		return ids.get(id);
	}

	public static synchronized HashSet<Actor> all() {
		return new HashSet<Actor>(all);
	}
}
