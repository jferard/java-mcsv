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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;

public class MetaCSVParserTest {
    @Test(expected = MetaCSVParseException.class)
    public void testEmptyStream() throws IOException, MetaCSVParseException, MetaCSVDataException {
        ByteArrayInputStream is = TestHelper.utf8InputStream("");
        MetaCSVParser.create(is).parse();
    }

    @Test
    public void testEmptyBody() throws IOException, MetaCSVParseException, MetaCSVDataException {
        ByteArrayInputStream is = TestHelper.utf8InputStream("domain,key,value\r\n");
        MetaCSVData data = MetaCSVParser.create(is).parse();
        Assert.assertEquals(TestHelper.UTF_8_CHARSET, data.getEncoding());
        Assert.assertFalse(data.isUtf8BOM());
        Assert.assertEquals("\r\n", data.getLineTerminator());
        Assert.assertEquals(',', data.getDelimiter());
        Assert.assertTrue(data.isDoubleQuote());
        Assert.assertEquals(0, data.getEscapeChar());
        Assert.assertEquals('"', data.getQuoteChar());
        Assert.assertFalse(data.isSkipInitialSpace());
    }

    @Test(expected = MetaCSVParseException.class)
    public void testBadHeader1() throws IOException, MetaCSVParseException, MetaCSVDataException {
        ByteArrayInputStream is = TestHelper.utf8InputStream("domai,key,value\r\n");
        MetaCSVParser.create(is).parse();
    }

    @Test(expected = MetaCSVParseException.class)
    public void testBadHeader2() throws IOException, MetaCSVParseException, MetaCSVDataException {
        ByteArrayInputStream is = TestHelper.utf8InputStream("domain,ke,value\r\n");
        MetaCSVParser.create(is).parse();
    }

    @Test(expected = MetaCSVParseException.class)
    public void testBadHeader3() throws IOException, MetaCSVParseException, MetaCSVDataException {
        ByteArrayInputStream is = TestHelper.utf8InputStream("domain,key,valu\r\n");
        MetaCSVParser.create(is).parse();
    }

    @Test(expected = MetaCSVParseException.class)
    public void testUnknownDomain() throws IOException, MetaCSVParseException,
            MetaCSVDataException {
        ByteArrayInputStream is = TestHelper.utf8InputStream(
                "domain,key,value\r\n" +
                        "foo,bar,baz\r\n");
        MetaCSVParser.create(is).parse();
    }

    @Test
    public void testFileDomain() throws IOException, MetaCSVParseException, MetaCSVDataException {
        ByteArrayInputStream is = TestHelper.utf8InputStream(
                "domain,key,value\r\n" +
                        "file,encoding,ascii\r\n" +
                        "file,bom,false\r\n" +
                        "file,line_terminator,\\n\r\n");
        MetaCSVData data = MetaCSVParser.create(is).parse();
        Assert.assertEquals(TestHelper.ASCII_CHARSET, data.getEncoding());
        Assert.assertFalse(data.isUtf8BOM());
        Assert.assertEquals("\n", data.getLineTerminator());
    }

    @Test(expected = MetaCSVParseException.class)
    public void testUnknownFileKey() throws IOException, MetaCSVParseException,
            MetaCSVDataException {
        ByteArrayInputStream is = TestHelper.utf8InputStream(
                "domain,key,value\r\n" +
                        "file,bar,baz\r\n");
        MetaCSVParser.create(is).parse();
    }

    @Test
    public void testCSVDomain() throws IOException, MetaCSVParseException, MetaCSVDataException {
        ByteArrayInputStream is = TestHelper.utf8InputStream(
                "domain,key,value\r\n" +
                        "csv,delimiter,\",\"\r\n" +
                        "csv,double_quote,true\r\n" +
                        "csv,escape_char,\\\r\n" +
                        "csv,quote_char,'\r\n" +
                        "csv,skip_initial_space,false\r\n");
        MetaCSVData data = MetaCSVParser.create(is).parse();
        Assert.assertEquals(',', data.getDelimiter());
        Assert.assertFalse(data.isDoubleQuote());
        Assert.assertEquals('\\', data.getEscapeChar());
        Assert.assertEquals('\'', data.getQuoteChar());
        Assert.assertFalse(data.isSkipInitialSpace());
    }

    @Test(expected = MetaCSVParseException.class)
    public void testUnknownCSVKey() throws IOException, MetaCSVParseException,
            MetaCSVDataException {
        ByteArrayInputStream is = TestHelper.utf8InputStream(
                "domain,key,value\r\n" +
                        "csv,bar,baz\r\n");
        MetaCSVParser.create(is).parse();
    }

    @Test(expected = MetaCSVParseException.class)
    public void testUnknownBoolean() throws IOException, MetaCSVParseException,
            MetaCSVDataException {
        ByteArrayInputStream is = TestHelper.utf8InputStream(
                "domain,key,value\r\n" +
                        "csv,double_quote,T\r\n");
        MetaCSVParser.create(is).parse();
    }

    @Test(expected = MetaCSVParseException.class)
    public void testUnknownChar() throws IOException, MetaCSVParseException, MetaCSVDataException {
        ByteArrayInputStream is = TestHelper.utf8InputStream(
                "domain,key,value\r\n" +
                        "csv,delimiter,foo\r\n");
        MetaCSVParser.create(is).parse();
    }

    @Test
    public void testDataDomain() throws IOException, MetaCSVParseException, MetaCSVDataException {
        ByteArrayInputStream is = TestHelper.utf8InputStream(
                "domain,key,value\r\n" +
                        "data,null_value,NULL\r\n" +
                        "data,col/0/type,boolean/T/F\r\n" +
                        "data,col/1/type,\"currency/pre/$/decimal/,/.\"\r\n" +
                        "data,col/2/type,currency/post/€/integer\r\n" +
                        "data,col/3/type,date/dd\\/MM\\/yyyy\r\n" +
                        "data,col/4/type,date/dd\\/MM\\/yyyy/fr_FR\r\n" +
                        "data,col/5/type,datetime/yyyy-MM-dd HH:mm:ss\r\n" +
                        "data,col/6/type,datetime/yyyy-MM-dd HH:mm:ss/en_US\r\n" +
                        "data,col/7/type,\"float/,/.\"\r\n" +
                        "data,col/8/type,\"decimal//.\"\r\n" +
                        "data,col/9/type,\"integer/ \"\r\n" +
                        "data,col/10/type,\"percentage/post/%/float/,/.\"\r\n" +
                        "data,col/11/type,\"percentage/post/%/decimal//.\"\r\n" +
                        "data,col/12/type,text\r\n" +
                        "data,col/13/type,object/foo/bar/baz\r\n"
        );
        MetaCSVData data = MetaCSVParser.create(is).parse();
        Assert.assertEquals("BooleanFieldDescription(T, F)",
                data.getDescription(0).toString());
        Assert.assertEquals("CurrencyFieldDescription(true, $, DecimalFieldDescription(,, .))",
                data.getDescription(1).toString());
        Assert.assertEquals("CurrencyFieldDescription(false, €, IntegerFieldDescription(null))",
                data.getDescription(2).toString());
        Assert.assertEquals("DateFieldDescription(dd/MM/yyyy, null)",
                data.getDescription(3).toString());
        Assert.assertEquals("DateFieldDescription(dd/MM/yyyy, fr_FR)",
                data.getDescription(4).toString());
        Assert.assertEquals("DatetimeDescription(yyyy-MM-dd HH:mm:ss, null)",
                data.getDescription(5).toString());
        Assert.assertEquals("DatetimeDescription(yyyy-MM-dd HH:mm:ss, en_US)",
                data.getDescription(6).toString());
        Assert.assertEquals("FloatFieldDescription(,, .)",
                data.getDescription(7).toString());
        Assert.assertEquals("DecimalFieldDescription(, .)",
                data.getDescription(8).toString());
        Assert.assertEquals("IntegerFieldDescription( )",
                data.getDescription(9).toString());
        Assert.assertEquals("PercentageFieldDescription(false, %, FloatFieldDescription(,, .))",
                data.getDescription(10).toString());
        Assert.assertEquals("PercentageFieldDescription(false, %, DecimalFieldDescription(, .))",
                data.getDescription(11).toString());
        Assert.assertEquals("TextFieldDescription()",
                data.getDescription(12).toString());
        Assert.assertEquals("ObjectFieldDescription([foo, bar, baz])",
                data.getDescription(13).toString());
    }

    @Test(expected = MetaCSVParseException.class)
    public void testBadDataNullKey() throws IOException, MetaCSVParseException,
            MetaCSVDataException {
        ByteArrayInputStream is = TestHelper.utf8InputStream(
                "domain,key,value\r\n" +
                        "data,null_value/foo,NULL\r\n");
        MetaCSVParser.create(is).parse();
    }

    @Test(expected = MetaCSVParseException.class)
    public void testBadDataColKey() throws IOException, MetaCSVParseException,
            MetaCSVDataException {
        ByteArrayInputStream is = TestHelper.utf8InputStream(
                "domain,key,value\r\n" +
                        "data,col/1/type/foo,bar\r\n");
        MetaCSVParser.create(is).parse();
    }

    @Test(expected = MetaCSVParseException.class)
    public void testUnkownDataKey() throws IOException, MetaCSVParseException,
            MetaCSVDataException {
        ByteArrayInputStream is = TestHelper.utf8InputStream(
                "domain,key,value\r\n" +
                        "data,foo,bar\r\n");
        MetaCSVParser.create(is).parse();
    }

    @Test(expected = MetaCSVParseException.class)
    public void testBadColNum() throws IOException, MetaCSVParseException, MetaCSVDataException {
        ByteArrayInputStream is = TestHelper.utf8InputStream(
                "domain,key,value\r\n" +
                        "data,col/foo/type,bar\r\n");
        MetaCSVParser.create(is).parse();
    }

    @Test(expected = MetaCSVParseException.class)
    public void testBadColSubKey() throws IOException, MetaCSVParseException, MetaCSVDataException {
        ByteArrayInputStream is = TestHelper.utf8InputStream(
                "domain,key,value\r\n" +
                        "data,col/3/foo,bar\r\n");
        MetaCSVParser.create(is).parse();
    }

    @Test(expected = MetaCSVParseException.class)
    public void testUnknownColType() throws IOException, MetaCSVParseException,
            MetaCSVDataException {
        ByteArrayInputStream is = TestHelper.utf8InputStream(
                "domain,key,value\r\n" +
                        "data,col/3/type,foo\r\n");
        MetaCSVParser.create(is).parse();
    }

    @Test
    public void testMissingFalse() throws IOException, MetaCSVParseException, MetaCSVDataException {
        ByteArrayInputStream is = TestHelper.utf8InputStream(
                "domain,key,value\r\n" +
                        "data,col/3/type,boolean/X\r\n");
        MetaCSVData data = MetaCSVParser.create(is).parse();
        Assert.assertEquals("BooleanFieldDescription(X, )",
                data.getDescription(3).toString());
    }

    @Test(expected = MetaCSVParseException.class)
    public void testMissingBooleanParameters()
            throws IOException, MetaCSVParseException, MetaCSVDataException {
        ByteArrayInputStream is = TestHelper.utf8InputStream(
                "domain,key,value\r\n" +
                        "data,col/3/type,boolean\r\n");
        MetaCSVParser.create(is).parse();
    }

    @Test(expected = MetaCSVParseException.class)
    public void testTooManyBooleanParameters()
            throws IOException, MetaCSVParseException, MetaCSVDataException {
        ByteArrayInputStream is = TestHelper.utf8InputStream(
                "domain,key,value\r\n" +
                        "data,col/3/type,boolean/true/false/maybe\r\n");
        MetaCSVParser.create(is).parse();
    }

    @Test
    public void testCurrencyInteger()
            throws IOException, MetaCSVParseException, MetaCSVDataException {
        ByteArrayInputStream is = TestHelper.utf8InputStream(
                "domain,key,value\r\n" +
                        "data,col/2/type,currency/pre/$/integer\r\n");
        MetaCSVData data = MetaCSVParser.create(is).parse();
        Assert.assertEquals("CurrencyFieldDescription(true, $, IntegerFieldDescription(null))",
                data.getDescription(2).toString());
    }

    @Test(expected = MetaCSVParseException.class)
    public void testCurrencyOther()
            throws IOException, MetaCSVParseException, MetaCSVDataException {
        ByteArrayInputStream is = TestHelper.utf8InputStream(
                "domain,key,value\r\n" +
                        "data,col/2/type,currency/pre/$/foo/a/b\r\n");
        MetaCSVParser.create(is).parse();
    }

    @Test
    public void testDateLocale() throws IOException, MetaCSVParseException, MetaCSVDataException {
        ByteArrayInputStream is = TestHelper.utf8InputStream(
                "domain,key,value\r\n" +
                        "data,col/1/type,date/YYYY/fr_FR\r\n");
        MetaCSVData data = MetaCSVParser.create(is).parse();
        Assert.assertEquals("DateFieldDescription(YYYY, fr_FR)",
                data.getDescription(1).toString());
    }

    @Test(expected = MetaCSVParseException.class)
    public void testDateNoParameter()
            throws IOException, MetaCSVParseException, MetaCSVDataException {
        ByteArrayInputStream is = TestHelper.utf8InputStream(
                "domain,key,value\r\n" +
                        "data,col/1/type,date\r\n");
        MetaCSVParser.create(is).parse();
    }

    @Test(expected = MetaCSVParseException.class)
    public void testDateTooManyParameters()
            throws IOException, MetaCSVParseException, MetaCSVDataException {
        ByteArrayInputStream is = TestHelper.utf8InputStream(
                "domain,key,value\r\n" +
                        "data,col/1/type,date/YYYY/fr_FR/foo\r\n");
        MetaCSVParser.create(is).parse();
    }

    @Test(expected = MetaCSVParseException.class)
    public void testDatetimeNoParameter()
            throws IOException, MetaCSVParseException, MetaCSVDataException {
        ByteArrayInputStream is = TestHelper.utf8InputStream(
                "domain,key,value\r\n" +
                        "data,col/1/type,datetime\r\n");
        MetaCSVParser.create(is).parse();
    }

    @Test(expected = MetaCSVParseException.class)
    public void testDatetimeTooManyParameters()
            throws IOException, MetaCSVParseException, MetaCSVDataException {
        ByteArrayInputStream is = TestHelper.utf8InputStream(
                "domain,key,value\r\n" +
                        "data,col/1/type,datetime/YYYY/fr_FR/foo\r\n");
        MetaCSVParser.create(is).parse();
    }

    @Test(expected = MetaCSVParseException.class)
    public void testFloatTooManyParameters()
            throws IOException, MetaCSVParseException, MetaCSVDataException {
        ByteArrayInputStream is = TestHelper.utf8InputStream(
                "domain,key,value\r\n" +
                        "data,col/1/type,float//./foo\r\n");
        MetaCSVParser.create(is).parse();
    }

    @Test(expected = MetaCSVParseException.class)
    public void testDecimalTooManyParameters()
            throws IOException, MetaCSVParseException, MetaCSVDataException {
        ByteArrayInputStream is = TestHelper.utf8InputStream(
                "domain,key,value\r\n" +
                        "data,col/1/type,decimal//./foo\r\n");
        MetaCSVParser.create(is).parse();
    }

    @Test(expected = MetaCSVParseException.class)
    public void testIntegerTooManyParameters()
            throws IOException, MetaCSVParseException, MetaCSVDataException {
        ByteArrayInputStream is = TestHelper.utf8InputStream(
                "domain,key,value\r\n" +
                        "data,col/1/type,integer//.\r\n");
        MetaCSVParser.create(is).parse();
    }

    @Test(expected = MetaCSVParseException.class)
    public void testBadPrePost() throws IOException, MetaCSVParseException, MetaCSVDataException {
        ByteArrayInputStream is = TestHelper.utf8InputStream(
                "domain,key,value\r\n" +
                        "data,col/1/type,currency/foo/$/integer\r\n");
        MetaCSVParser.create(is).parse();
    }

    @Test(expected = MetaCSVParseException.class)
    public void testBadPercentageNumber()
            throws IOException, MetaCSVParseException, MetaCSVDataException {
        ByteArrayInputStream is = TestHelper.utf8InputStream(
                "domain,key,value\r\n" +
                        "data,col/1/type,percentage/post/%/foo\r\n");
        MetaCSVParser.create(is).parse();
    }

    @Test
    public void testMetaVersion() throws IOException, MetaCSVParseException,
            MetaCSVDataException {
        Reader r = new StringReader(
                "domain,key,value\r\n" +
                        "meta,version,0.0\r\n");
        MetaCSVParser metaCSVParser = MetaCSVParser.create(r);
        try {
            MetaCSVData data = metaCSVParser.parse();
            Assert.assertEquals("0.0", data.getMetaVersion());
        } finally {
            metaCSVParser.close();
        }
    }

    @Test
    public void testMeta() throws IOException, MetaCSVParseException,
            MetaCSVDataException {
        MetaCSVParser metaCSVParser =
                MetaCSVParser.create(Arrays.asList(Arrays.asList("meta", "foo", "bar")), false);
        try {
            MetaCSVData data = metaCSVParser.parse();
            Assert.assertEquals("bar", data.getMeta("foo"));
        } finally {
            metaCSVParser.close();
        }
    }
}