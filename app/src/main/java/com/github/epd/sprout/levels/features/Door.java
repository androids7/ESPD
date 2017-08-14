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
package com.github.epd.sprout.levels.features;

import com.github.epd.sprout.Assets;
import com.github.epd.sprout.Dungeon;
import com.github.epd.sprout.actors.Char;
import com.github.epd.sprout.levels.Level;
import com.github.epd.sprout.levels.Terrain;
import com.github.epd.sprout.scenes.GameScene;
import com.watabou.noosa.audio.Sample;

public class Door {

	public static void enter( int pos, Char ch ) {
		Level.set( pos, Terrain.OPEN_DOOR );
		GameScene.updateMap( pos );

		if (ch == Dungeon.hero){
			//don't obsserve here as that already happens on hero move
			Sample.INSTANCE.play( Assets.SND_OPEN );
		} else if (Dungeon.visible[pos]) {
			Sample.INSTANCE.play( Assets.SND_OPEN );
			Dungeon.observe();
		}
	}

	public static void leave( int pos, Char ch ) {
		if (Dungeon.level.heaps.get( pos ) == null) {
			Level.set( pos, Terrain.DOOR );
			GameScene.updateMap( pos );

			if (ch != Dungeon.hero && Dungeon.visible[pos])
				Dungeon.observe();
		}
	}
}