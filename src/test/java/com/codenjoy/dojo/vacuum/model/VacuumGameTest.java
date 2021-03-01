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


import com.codenjoy.dojo.services.Dice;
import com.codenjoy.dojo.services.EventListener;
import com.codenjoy.dojo.services.printer.PrinterFactory;
import com.codenjoy.dojo.services.printer.PrinterFactoryImpl;
import com.codenjoy.dojo.utils.TestUtils;
import com.codenjoy.dojo.vacuum.model.level.Level;
import com.codenjoy.dojo.vacuum.services.Event;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.OngoingStubbing;

import java.util.List;
import java.util.Objects;

import static com.codenjoy.dojo.vacuum.services.Event.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class VacuumGameTest {

    private VacuumGame game;
    private Hero hero;
    private Dice dice;
    private EventListener listener;
    private Player player;
    private PrinterFactory printer = new PrinterFactoryImpl();

    @Before
    public void setup() {
        dice = mock(Dice.class);
    }

    private void dice(int... ints) {
        OngoingStubbing<Integer> when = when(dice.next(anyInt()));
        for (int i : ints) {
            when = when.thenReturn(i);
        }
    }

    private void givenFl(String board) {
        Level level = Level.generate(board);

        game = new VacuumGame(level);
        listener = mock(EventListener.class);
        player = new Player(listener);
        game.newGame(player);
        player.newHero(game);
        player.hero.init(game);
        this.hero = game.getHeroes().get(0);
    }

    private void assertE(String expected) {
        assertEquals(TestUtils.injectN(expected),
                printer.getPrinter(game.reader(), player).print());
    }

    @Test
    public void shouldSpawnOnStartPoint() {
        givenFl("#####" +
                "#   #" +
                "# S #" +
                "#   #" +
                "#####");

        assertE("#####" +
                "#   #" +
                "# O #" +
                "#   #" +
                "#####");
    }

    @Test
    public void shouldGoUntilBarrier() {
        givenFl("#####" +
                "# S #" +
                "#   #" +
                "#   #" +
                "#####");

        hero.down();
        game.tick();

        assertE("#####" +
                "# S #" +
                "# O #" +
                "#   #" +
                "#####");

        hero.right(); // Should not affect
        game.tick();

        assertE("#####" +
                "# S #" +
                "#   #" +
                "# O #" +
                "#####");

        game.tick();

        assertE("#####" +
                "# S #" +
                "#   #" +
                "# O #" +
                "#####");

        hero.right();
        game.tick();

        assertE("#####" +
                "# S #" +
                "#   #" +
                "#  O#" +
                "#####");

        game.tick();

        assertE("#####" +
                "# S #" +
                "#   #" +
                "#  O#" +
                "#####");
    }

    @Test
    public void shouldBeAbleToChangeDirection_whenStopped() {
        givenFl("#####" +
                "# S #" +
                "#   #" +
                "#   #" +
                "#####");

        hero.right();
        game.tick();

        assertE("#####" +
                "# SO#" +
                "#   #" +
                "#   #" +
                "#####");

        hero.down();
        game.tick();

        assertE("#####" +
                "# S #" +
                "#  O#" +
                "#   #" +
                "#####");
    }

    @Test
    public void shouldCleanDust() {
        givenFl("#####" +
                "# S #" +
                "#***#" +
                "#***#" +
                "#####");

        hero.down();
        game.tick();


        assertE("#####" +
                "# S #" +
                "#*O*#" +
                "#***#" +
                "#####");

        game.tick();

        assertE("#####" +
                "# S #" +
                "#* *#" +
                "#*O*#" +
                "#####");

        verify(listener, times(2)).event(DUST_CLEANED);
    }

    @Test
    public void shouldBeenFined_whenWastingTime() {
        givenFl("#####" +
                "# S #" +
                "#* *#" +
                "#* *#" +
                "#####");

        hero.down();
        game.tick();
        game.tick();

        assertE("#####" +
                "# S #" +
                "#* *#" +
                "#*O*#" +
                "#####");

        verify(listener, times(2)).event(TIME_WASTED);
    }

    @Test
    public void shouldWin_whenAllClear() {
        givenFl("#####" +
                "#S**#" +
                "#* *#" +
                "#***#" +
                "#####");

        // Clean all of the dust
        hero.right();
        game.tick();
        game.tick();
        game.tick();
        hero.down();
        game.tick();
        game.tick();
        hero.left();
        game.tick();
        game.tick();
        hero.up();
        game.tick();

        assertE("#####" +
                "#S  #" +
                "#O  #" +
                "#   #" +
                "#####");

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(listener, times(8)).event(eventCaptor.capture());
        assertEquals(ALL_CLEAR, eventCaptor.getAllValues().get(7));
        assertTrue(containsOnly(eventCaptor.getAllValues().subList(0, 6), DUST_CLEANED));
    }

    // TODO: Discuss, should it be correct behaviour or not (At the time not)
//    @Test
//    public void wastingTime_whenDoNothing() {
//        givenFl("#####" +
//                "#S**#" +
//                "#* *#" +
//                "#***#" +
//                "#####");
//
//        game.tick();
//        game.tick();
//        game.tick();
//
//        verify(listener, times(3)).event(TIME_WASTED);
//    }

    private boolean containsOnly(List<?> list, Object value) {
        for (Object o : list) {
            if (!Objects.equals(o, value)) {
                return false;
            }
        }
        return true;
    }
}
