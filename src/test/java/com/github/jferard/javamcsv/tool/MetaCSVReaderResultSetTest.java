/*
 * java-mcsv - A MetaCSV library for Java
 *     Copyright (C) 2020 J. Férard <https://github.com/jferard>
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

package com.github.jferard.javamcsv.tool;

import com.github.jferard.javamcsv.MetaCSVDataException;
import com.github.jferard.javamcsv.MetaCSVParseException;
import com.github.jferard.javamcsv.MetaCSVReadException;
import com.github.jferard.javamcsv.MetaCSVReader;
import com.github.jferard.javamcsv.TestHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class MetaCSVReaderResultSetTest {
    private ResultSet rs;
    private Calendar c;

    @Before
    public void setUp()
            throws MetaCSVReadException, MetaCSVDataException, MetaCSVParseException, IOException {
        c = GregorianCalendar.getInstance(Locale.US);
        c.setTimeInMillis(0);
        c.set(2020, Calendar.DECEMBER, 1, 0, 0, 0);
        ByteArrayInputStream is = TestHelper.utf8InputStream(
                "boolean,currency,date,datetime,float,integer,percentage,text\r\n" +
                        "T,$15,01/12/2020,NULL,\"10,000.5\",12 354,56.5%,Foo\r\n" +
                        "F,\"$-1,900.5\",NULL,2020-12-01 09:30:55,-520.8,-1 000,-12.8%,Bar\r\n");
        ByteArrayInputStream metaIs = TestHelper.utf8InputStream(
                "domain,key,value\r\n" +
                        "data,null_value,NULL\r\n" +
                        "data,col/0/type,boolean/T/F\r\n" +
                        "data,col/1/type,\"currency/pre/$/decimal/,/.\"\r\n" +
                        "data,col/2/type,date/dd\\/MM\\/yyyy\r\n" +
                        "data,col/3/type,datetime/yyyy-MM-dd HH:mm:ss\r\n" +
                        "data,col/4/type,\"float/,/.\"\r\n" +
                        "data,col/5/type,\"integer/ \"\r\n" +
                        "data,col/6/type,\"percentage/post/%/float/,/.\"\r\n");
        MetaCSVReader reader = MetaCSVReader.create(is, metaIs);
        rs = Tool.readerToResultSet(reader);
    }

    @Test
    public void testFirstColObj() throws SQLException {
        Assert.assertTrue(rs.next());
        List<Object> firstCol =
                Arrays.<Object>asList(null, true, new BigDecimal("15"), c.getTime(), null, 10000.5,
                        12354L, 0.565, "Foo");
        for (int i = 1; i <= 8; i++) {
            Assert.assertEquals(firstCol.get(i), rs.getObject(i));
        }
    }

    @Test
    public void testFirstColByIndex() throws SQLException {
        Assert.assertTrue(rs.next());
        Assert.assertTrue(rs.getBoolean(1));
        Assert.assertEquals(BigDecimal.valueOf(15L), rs.getBigDecimal(2));
        Assert.assertEquals(c.getTime(), rs.getDate(3));
        Assert.assertEquals(null, rs.getDate(4));
        Assert.assertEquals(10000.5, rs.getDouble(5), 0.001);
        Assert.assertEquals(12354L, rs.getLong(6));
        Assert.assertEquals(0.565, rs.getDouble(7), 0.001);
    }

    @Test
    public void testFirstColByName() throws SQLException {
        Assert.assertTrue(rs.next());
        Assert.assertTrue(rs.getBoolean("boolean"));
        Assert.assertEquals(BigDecimal.valueOf(15L), rs.getBigDecimal("currency"));
        Assert.assertEquals(c.getTime(), rs.getDate("date"));
        Assert.assertEquals(null, rs.getDate("datetime"));
        Assert.assertEquals(10000.5, rs.getDouble("float"), 0.001);
        Assert.assertEquals(12354L, rs.getLong("integer"));
        Assert.assertEquals(0.565, rs.getDouble("percentage"), 0.001);
    }

    @Test
    public void testSecondCol() throws SQLException {
        Assert.assertTrue(rs.next());
        Assert.assertTrue(rs.next());
        Calendar c = GregorianCalendar.getInstance(Locale.US);
        c.setTimeInMillis(0);
        c.set(2020, Calendar.DECEMBER, 1, 9, 30, 55);
        List<Object> secondCol =
                Arrays.<Object>asList(null, false, new BigDecimal("-1900.5"), null, c.getTime(),
                        -520.8, -1000L,
                        -0.128, "Bar");
        for (int i = 1; i <= 8; i++) {
            Assert.assertEquals(secondCol.get(i), rs.getObject(i));
        }
        Assert.assertFalse(rs.next());
    }
}