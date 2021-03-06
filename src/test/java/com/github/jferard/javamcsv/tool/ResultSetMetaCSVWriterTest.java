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

package com.github.jferard.javamcsv.tool;

import com.github.jferard.javamcsv.MetaCSVData;
import com.github.jferard.javamcsv.MetaCSVDataBuilder;
import com.github.jferard.javamcsv.MetaCSVDataException;
import com.github.jferard.javamcsv.MetaCSVRenderer;
import com.github.jferard.javamcsv.MetaCSVWriter;
import com.github.jferard.javamcsv.TestHelper;
import com.github.jferard.javamcsv.description.BooleanFieldDescription;
import com.github.jferard.javamcsv.description.TextFieldDescription;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Pattern;

public class ResultSetMetaCSVWriterTest {
    private static ResultSet rs;

    @Before
    public void setUp() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:h2:mem:test");
        Statement statement = connection.createStatement();
        statement.executeUpdate("DROP TABLE IF EXISTS test");
        statement.executeUpdate("CREATE TABLE test (\n" +
                "Int INTEGER,\n" +
                "BigD DECIMAL,\n" +
                "Float DOUBLE,\n" +
                "Text VARCHAR,\n" +
                "Object ARRAY,\n" +
                "Date DATE,\n" +
                "Bool BOOLEAN,\n" +
                "Dt DATETIME\n" +
                ")");
        statement.executeUpdate(
                "INSERT INTO test VALUES (1, 1, 0.456, 'foo', (1, 2), '2021-01-01', TRUE, '2021-01-01T23:21:56')");
        rs = statement.executeQuery("SELECT * FROM test");
    }

    @Test
    public void testMeta() throws SQLException, MetaCSVDataException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MetaCSVRenderer renderer = MetaCSVRenderer.create(out, false);
        new ResultSetMetaCSVWriter(rs).writeMetaCSV(renderer);
        Assert.assertEquals("domain,key,value\r\n" +
                        "file,encoding,UTF-8\r\n" +
                        "file,bom,false\r\n" +
                        "file,line_terminator,\\r\\n\r\n" +
                        "csv,delimiter,\",\"\r\n" +
                        "csv,double_quote,true\r\n" +
                        "csv,quote_char,\"\"\"\"\r\n" +
                        "csv,skip_initial_space,false\r\n" +
                        "data,null_value,\r\n" +
                        "data,col/0/type,integer\r\n" +
                        "data,col/1/type,decimal//.\r\n" +
                        "data,col/2/type,float//.\r\n" +
                        "data,col/3/type,text\r\n" +
                        "data,col/4/type,object\r\n" +
                        "data,col/5/type,date/yyyy-MM-dd\r\n" +
                        "data,col/6/type,boolean/true/false\r\n" +
                        "data,col/7/type,datetime/yyyy-MM-dd'T'HH:mm:ss\r\n"
                , out.toString());
    }

    @Test
    public void testCSV() throws SQLException, MetaCSVDataException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ResultSetMetaCSVWriter rsWriter = new ResultSetMetaCSVWriter(rs);
        MetaCSVData data = rsWriter.getMetaCSVData();
        MetaCSVWriter writer = MetaCSVWriter.create(out, data);
        rsWriter.writeCSV(writer);
        Assert.assertTrue(out.toString(TestHelper.UTF_8_CHARSET_NAME)
                .startsWith("INT,BIGD,FLOAT,TEXT,OBJECT,DATE,BOOL,DT\r\n" +
                        "1,1.0,0.456,foo,[Ljava.lang.Object;"));
        Assert.assertTrue(out.toString(TestHelper.UTF_8_CHARSET_NAME)
                .endsWith(",2021-01-01,true,2021-01-01T23:21:56\r\n"));
    }

    @Test
    public void testMetaOut() throws SQLException, MetaCSVDataException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream mout = new ByteArrayOutputStream();

        ResultSetMetaCSVWriter rsWriter = new ResultSetMetaCSVWriter(rs);
        MetaCSVData data = rsWriter.getMetaCSVData();
        MetaCSVWriter writer = MetaCSVWriter.create(out, data);
        rsWriter.write(writer, MetaCSVRenderer.create(mout));

        Pattern pattern = Pattern.compile("^INT,BIGD,FLOAT,TEXT,OBJECT,DATE,BOOL,DT\r\n" +
                        "1,1\\.0,0\\.456,foo,\\[Ljava\\.lang\\.Object;.+?,2021-01-01,true,2021-01-01T23:21:56\r\n$",
                Pattern.MULTILINE);
        Assert.assertTrue(pattern.matcher(out.toString(TestHelper.UTF_8_CHARSET_NAME)).matches());

        Assert.assertEquals("domain,key,value\r\n" +
                        "data,col/0/type,integer\r\n" +
                        "data,col/1/type,decimal//.\r\n" +
                        "data,col/2/type,float//.\r\n" +
                        "data,col/4/type,object\r\n" +
                        "data,col/5/type,date/yyyy-MM-dd\r\n" +
                        "data,col/6/type,boolean/true/false\r\n" +
                        "data,col/7/type,datetime/yyyy-MM-dd'T'HH:mm:ss\r\n",
                mout.toString(TestHelper.UTF_8_CHARSET_NAME));
    }

    @Test
    public void testManualColTypes() throws SQLException, MetaCSVDataException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream mout = new ByteArrayOutputStream();
        MetaCSVDataBuilder builder = new MetaCSVDataBuilder().delimiter(';')
                .colType(0, BooleanFieldDescription.INSTANCE);
        ResultSetMetaCSVWriter rsWriter = new ResultSetMetaCSVWriter(rs, builder);
        MetaCSVData data = rsWriter.getMetaCSVData();
        MetaCSVWriter writer = MetaCSVWriter.create(out, data);
        rsWriter.write(writer, MetaCSVRenderer.create(mout));
        Pattern pattern = Pattern.compile("^INT;BIGD;FLOAT;TEXT;OBJECT;DATE;BOOL;DT\r\n" +
                        "true;1\\.0;0\\.456;foo;\"\\[Ljava\\.lang\\.Object;.+?\";2021-01-01;true;2021-01-01T23:21:56\r\n$",
                Pattern.MULTILINE);
        Assert.assertTrue(pattern.matcher(out.toString(TestHelper.UTF_8_CHARSET_NAME)).matches());
        Assert.assertEquals("domain,key,value\r\n" +
                        "csv,delimiter,;\r\n" +
                        "data,col/0/type,boolean/true/false\r\n" +
                        "data,col/1/type,decimal//.\r\n" +
                        "data,col/2/type,float//.\r\n" +
                        "data,col/4/type,object\r\n" +
                        "data,col/5/type,date/yyyy-MM-dd\r\n" +
                        "data,col/6/type,boolean/true/false\r\n" +
                        "data,col/7/type,datetime/yyyy-MM-dd'T'HH:mm:ss\r\n",
                mout.toString(TestHelper.UTF_8_CHARSET_NAME));
    }
}