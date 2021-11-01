package ru.job4j;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class StartTest {

    @Test
    public void testSum() {
        int x = 1;
        int y = 2;
        assertThat(Start.sum(x, y), is(3));
    }
}