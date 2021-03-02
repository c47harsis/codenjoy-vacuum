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
import com.codenjoy.dojo.vacuum.services.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Это реализация героя. Обрати внимание, что он имплементит {@see Joystick}, а значит может быть управляем фреймворком
 * Так же он имплементит {@see Tickable}, что значит - есть возможность его оповещать о каждом тике игры.
 * Ну и конечно же он имплементит {@see State}, а значит может быть отрисован на поле.
 * Часть этих интерфейсов объявлены в {@see PlayerHero}, а часть явно тут.
 */
public class Hero extends PlayerHero<Field> implements State<Element, Player> {
    private static final int RESTART_ACTION = 0;

    private Direction direction;
    private List<Event> events;
    private boolean restartRequested = false;

    public Hero(Point xy) {
        super(xy);
    }

    @Override
    public void init(Field field) {
        this.field = field;
    }

    @Override
    public void down() {
        if (direction == null)
            direction = Direction.DOWN;
    }

    @Override
    public void up() {
        if (direction == null)
            direction = Direction.UP;
    }

    @Override
    public void left() {
        if (direction == null)
            direction = Direction.LEFT;
    }

    @Override
    public void right() {
        if (direction == null)
            direction = Direction.RIGHT;
    }

    @Override
    public void act(int... codes) {
        if (codes.length > 0 && codes[0] == RESTART_ACTION) {
            restartRequested = true;
        }
    }

    @Override
    public void tick() {
        events = new ArrayList<>();

        if (restartRequested) {
            events.add(Event.RESTART);
            return;
        }

        if (direction != null) {
            Point destination = direction.change(this.copy());
            goTo(destination);
        }

        if (field.isAllClear()) {
            events.add(Event.ALL_CLEAR);
        }
    }

    private void goTo(Point point) {
        if (field.isBarrier(point)) {
            direction = null;
            return;
        }
        move(point);

        field.getDirectionSwitcher(point)
                .ifPresent(s -> this.direction = s.getDirection());

        if (field.isCleanPoint(point)) {
            events.add(Event.TIME_WASTED);
        }

        if (field.isDust(point))  {
            field.removeDust(point);
            events.add(Event.DUST_CLEANED);
        }

        Point next = direction.change(point);
        if (field.isBarrier(next)) {
            direction = null;
        }

    }

    public List<Event> getEvents() {
        return events;
    }

    @Override
    public Element state(Player player, Object... alsoAtPoint) {
        return Element.VACUUM;
    }
}
