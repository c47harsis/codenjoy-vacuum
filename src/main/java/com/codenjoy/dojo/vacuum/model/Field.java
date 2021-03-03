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


import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.multiplayer.GameField;
import com.codenjoy.dojo.vacuum.model.items.DirectionSwitcherItem;
import com.codenjoy.dojo.vacuum.model.items.EntryLimiterItem;
import com.codenjoy.dojo.vacuum.model.items.RoundaboutItem;

import java.util.Optional;

public interface Field extends GameField<Player> {

    Point getStart();

    boolean isBarrier(Point pt);

    boolean isAllClear();

    boolean isDust(Point pt);

    void removeDust(Point pt);

    boolean isCleanPoint(Point pt);

    Optional<DirectionSwitcherItem> switcher(Point pt);

    Optional<EntryLimiterItem> limiter(Point pt);

    Optional<RoundaboutItem> roundabout(Point destination);
}