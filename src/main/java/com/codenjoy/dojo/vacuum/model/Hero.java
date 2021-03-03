package com.codenjoy.dojo.vacuum.model;

/*-
 * #%L
 * Codenjoy - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2018 Codenjoy
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.State;
import com.codenjoy.dojo.services.multiplayer.PlayerHero;
import com.codenjoy.dojo.vacuum.model.items.Roundabout;
import com.codenjoy.dojo.vacuum.services.Event;

import java.util.Optional;

public class Hero extends PlayerHero<Field> implements State<Elements, Player> {

    private static final int RESTART_ACTION = 0;

    private Direction direction;
    private boolean reset = false;
    private boolean win = false;
    private Player player;

    public Hero(Point xy) {
        super(xy);
    }

    public void init(Player player) {
        this.player = player;
    }

    @Override
    public void init(Field field) {
        this.field = field;
    }

    public void tryChange(Direction input) {
        if (direction == null) {
            direction = input;
        }
    }

    @Override
    public void down() {
        tryChange(Direction.DOWN);
    }

    @Override
    public void up() {
        tryChange(Direction.UP);
    }

    @Override
    public void left() {
        tryChange(Direction.LEFT);
    }

    @Override
    public void right() {
        tryChange(Direction.RIGHT);
    }

    @Override
    public void act(int... p) {
        if (p.length > 0 && p[0] == RESTART_ACTION) {
            reset = true;
        }
    }

    @Override
    public void tick() {
        if (direction != null) {
            tryMove(direction.change(this.copy()));
        }

        if (field.isAllClear()) {
            player.event(Event.ALL_CLEAR);
            win = true;
        }
    }

    public void tryMove(Point pt) {
        boolean isNotEntryLimited = field.limiter(pt)
                .map(limiter -> limiter.canEnterFrom(this))
                .orElse(true);

        Optional<Roundabout> roundabout = field.roundabout(pt);

        isNotEntryLimited &= roundabout.map(r -> r.canEnterFrom(this))
                .orElse(true);

        if (field.isBarrier(pt) || !isNotEntryLimited) {
            direction = null;
            return;
        }

        roundabout.ifPresent(r -> direction = r.enterFrom(this));
        move(pt);

        field.switcher(pt)
                .ifPresent(s -> direction = s.direction());

        if (field.isCleanPoint(pt)) {
            player.event(Event.TIME_WASTED);
        }

        if (field.isDust(pt)) {
            field.removeDust(pt);
            player.event(Event.DUST_CLEANED);
        }

        Point next = direction.change(pt);
        if (field.isBarrier(next)) {
            direction = null;
        }
    }

    @Override
    public Elements state(Player player, Object... alsoAtPoint) {
        return Elements.VACUUM;
    }

    public boolean win() {
        return win;
    }

    public boolean reset() {
        return reset;
    }
}
