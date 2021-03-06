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

import com.github.jferard.javamcsv.description.FieldDescription;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class MetaCSVDataBuilder {
    private String encoding;
    private String lineTerminator;
    private char delimiter;
    private boolean doubleQuote;
    private char escapeChar;
    private char quoteChar;
    private boolean skipInitialSpace;
    private Map<Integer, FieldDescription<?>> descriptionByColIndex;
    private String nullValue;
    private boolean bom;
    private String metaVersion;
    private Map<String, String> meta;

    public MetaCSVDataBuilder() {
        this.encoding = Util.UTF_8_CHARSET_NAME;
        this.lineTerminator = Util.CRLF;
        this.descriptionByColIndex = new HashMap<Integer, FieldDescription<?>>();
        this.delimiter = ',';
        this.doubleQuote = true;
        this.quoteChar = '"';
        this.nullValue = "";
        this.metaVersion = "draft0";
        this.meta = new HashMap<String, String>();
        this.bom = false;
    }

    public MetaCSVData build() throws MetaCSVDataException {
        Charset charset;
        if (this.encoding.equals("UTF-8-SIG")) {
            charset = Util.UTF_8_CHARSET;
            this.bom = true;
        } else {
            charset = Charset.forName(this.encoding);
        }
        if (!charset.equals(Util.UTF_8_CHARSET) && bom) {
            throw new MetaCSVDataException("Can't have a bom with charset " + charset);
        }
        return new MetaCSVData(this.metaVersion, this.meta, charset, this.bom,
                Util.unescapeLineTerminator(lineTerminator),
                this.delimiter, this.quoteChar, this.doubleQuote, this.escapeChar,
                this.skipInitialSpace, this.nullValue, this.descriptionByColIndex);
    }

    public MetaCSVDataBuilder encoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    public MetaCSVDataBuilder lineTerminator(String lineTerminator) {
        this.lineTerminator = lineTerminator;
        return this;
    }

    public MetaCSVDataBuilder delimiter(char delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    public MetaCSVDataBuilder doubleQuote(boolean doubleQuote) {
        this.doubleQuote = doubleQuote;
        return this;
    }

    public MetaCSVDataBuilder escapeChar(char escapeChar) {
        this.doubleQuote = false;
        this.escapeChar = escapeChar;
        return this;
    }

    public MetaCSVDataBuilder quoteChar(char quoteChar) {
        this.quoteChar = quoteChar;
        return this;
    }

    public MetaCSVDataBuilder skipInitialSpace(boolean skipInitialSpace) {
        this.skipInitialSpace = skipInitialSpace;
        return this;
    }

    public MetaCSVDataBuilder colType(int c, FieldDescription<?> fieldDescription) {
        this.descriptionByColIndex.put(c, fieldDescription);
        return this;
    }

    public MetaCSVDataBuilder nullValue(String nullValue) {
        this.nullValue = nullValue;
        return this;
    }

    public MetaCSVDataBuilder bom(boolean value) {
        this.bom = value;
        return this;
    }

    public MetaCSVDataBuilder metaVersion(String metaVersion) {
        this.metaVersion = metaVersion;
        return this;
    }

    public MetaCSVDataBuilder meta(String key, String value) {
        this.meta.put(key, value);
        return this;
    }
}
