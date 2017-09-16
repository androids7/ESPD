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
package com.github.epd.sprout.items;

import com.github.epd.sprout.Assets;
import com.github.epd.sprout.Dungeon;
import com.github.epd.sprout.actors.hero.Hero;
import com.github.epd.sprout.actors.hero.HeroClass;
import com.github.epd.sprout.effects.Speck;
import com.github.epd.sprout.messages.Messages;
import com.github.epd.sprout.sprites.CharSprite;
import com.github.epd.sprout.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;

public class VioletDewdrop extends Item {

	private static final String TXT_VALUE = Messages.get(RedDewdrop.class, "value");

	{
		name = Messages.get(this, "name");
		image = ItemSpriteSheet.VIOLETDEWDROP;

		stackable = true;
	}

	@Override
	public boolean doPickUp(Hero hero) {

		DewVial vial = hero.belongings.getItem(DewVial.class);

		if (vial == null || vial.isFull()) {

			int value = 2 + (Dungeon.depth - 1) / 5;
			if (hero.heroClass == HeroClass.HUNTRESS) {
				value++;
			}

			int effect = Math.min(hero.HT - hero.HP, value * quantity);
			if (effect > 0) {
				hero.HP += effect;
				hero.sprite.emitter().burst(Speck.factory(Speck.HEALING), 1);
				hero.sprite.showStatus(CharSprite.POSITIVE, TXT_VALUE, effect);
			}

		} else if (vial != null) {

			vial.collectDew(this);

		}

		Sample.INSTANCE.play(Assets.SND_DEWDROP);
		hero.spendAndNext(TIME_TO_PICK_UP);

		return true;
	}

	@Override
	public String info() {
		return Messages.get(RedDewdrop.class, "desc");
	}
}
