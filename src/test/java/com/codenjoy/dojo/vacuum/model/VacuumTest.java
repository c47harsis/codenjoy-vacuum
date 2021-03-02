package com.codenjoy.dojo.vacuum.model;

import com.codenjoy.dojo.vacuum.services.Event;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Objects;

import static com.codenjoy.dojo.vacuum.services.Event.*;
import static com.codenjoy.dojo.vacuum.services.Event.DUST_CLEANED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class VacuumTest extends AbstractGameTest {

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
