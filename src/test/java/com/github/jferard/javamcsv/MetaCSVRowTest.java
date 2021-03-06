/*
 * java-mcsv - A MetaCSV library for Java
 *     Copyright (C) 2020-2021 J. Férard <https://github.com/jferard>
 *
 * This file is part of java-mcsv.
 *
 * java-mcsv is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * java-mcsv is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses />.
 */

package com.github.jferard.javamcsv;

import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import java.util.Arrays;

public class MetaCSVRowTest {
    @Test
    public void test() {
        MetaCSVRow row = new MetaCSVRow("foo", "bar", "baz");
        Assert.assertEquals("foo", row.getDomain());
        Assert.assertEquals("bar", row.getKey());
        Assert.assertEquals("baz", row.getValue());
    }

    @Test
    public void testFromIterable() throws MetaCSVParseException {
        MetaCSVRow row = MetaCSVRow.fromIterable(Arrays.<String>asList("foo", "bar", "baz"));
        Assert.assertEquals("foo", row.getDomain());
        Assert.assertEquals("bar", row.getKey());
        Assert.assertEquals("baz", row.getValue());
    }

    @Test
    public void testFromIterableZeroElement() {
        Assert.assertThrows(MetaCSVParseException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                MetaCSVRow row = MetaCSVRow.fromIterable(Arrays.<String>asList());
            }
        });
    }

    @Test
    public void testFromIterableOneElement() {
        Assert.assertThrows(MetaCSVParseException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                MetaCSVRow row = MetaCSVRow.fromIterable(Arrays.<String>asList("foo"));
            }
        });
    }

    @Test
    public void testFromIterableTwoElements() {
        Assert.assertThrows(MetaCSVParseException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                MetaCSVRow row = MetaCSVRow.fromIterable(Arrays.<String>asList("foo", "bar"));
            }
        });
    }

    @Test
    public void testFromIterableFourElements() {
        Assert.assertThrows(MetaCSVParseException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                MetaCSVRow row = MetaCSVRow.fromIterable(Arrays.<String>asList("foo", "bar", "baz", "bat"));
            }
        });
    }
}