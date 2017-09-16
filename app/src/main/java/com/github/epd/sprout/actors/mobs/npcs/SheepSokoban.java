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
package com.github.epd.sprout.actors.mobs.npcs;

import com.github.epd.sprout.Dungeon;
import com.github.epd.sprout.actors.Actor;
import com.github.epd.sprout.actors.buffs.Buff;
import com.github.epd.sprout.levels.Level;
import com.github.epd.sprout.messages.Messages;
import com.github.epd.sprout.sprites.SokobanSheepSprite;
import com.github.epd.sprout.utils.GLog;
import com.watabou.utils.Random;

public class SheepSokoban extends NPC {

	private static final String[] QUOTES = {Messages.get(SheepSokoban.class, "one"), Messages.get(SheepSokoban.class, "two"), Messages.get(SheepSokoban.class, "three"),
			Messages.get(SheepSokoban.class, "four")};

	{
		name = Messages.get(SheepSokoban.class, "name");
		spriteClass = SokobanSheepSprite.class;

		properties.add(Property.IMMOVABLE);
	}


	@Override
	protected boolean act() {
		throwItem();
		return super.act();
	}

	@Override
	public void add(Buff buff) {
	}

	@Override
	public void damage(int dmg, Object src) {
	}

	@Override
	public String description() {
		return Messages.get(SheepSokoban.class, "desc");
	}

/*  -W-1 -W  -W+1
 *  -1    P  +1
 *  W-1   W  W+1
 * 
 */

	@Override
	public boolean interact() {
		int curPos = pos;
		int movPos = pos;
		int width = Level.getWidth();
		boolean moved = false;
		int posDif = Dungeon.hero.pos - curPos;

		if (posDif == 1) {
			movPos = curPos - 1;
		} else if (posDif == -1) {
			movPos = curPos + 1;
		} else if (posDif == width) {
			movPos = curPos - width;
		} else if (posDif == -width) {
			movPos = curPos + width;
		}

		if (movPos != pos && (Level.passable[movPos] || Level.avoid[movPos]) && Actor.findChar(movPos) == null) {

			moveSprite(curPos, movPos);
			move(movPos);
			moved = true;

		}

		if (moved) {
			Dungeon.hero.sprite.move(Dungeon.hero.pos, curPos);
			Dungeon.hero.move(curPos);
		}

		GLog.n(Messages.get(SheepSokoban.class, "sheepname") + Random.element(QUOTES));

		return true;
	}

}
