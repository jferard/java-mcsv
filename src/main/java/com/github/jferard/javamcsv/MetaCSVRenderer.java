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
import com.github.jferard.javamcsv.description.TextFieldDescription;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

public class MetaCSVRenderer {
    public static MetaCSVRenderer create(OutputStream os) throws IOException {
        return MetaCSVRenderer.create(os, true);
    }

    public static MetaCSVRenderer create(OutputStream os, boolean minimal) throws IOException {
        OutputStreamWriter outWriter = new OutputStreamWriter(os, Util.UTF_8_CHARSET);
        final CSVPrinter printer = new CSVPrinter(outWriter, CSVFormat.DEFAULT);
        return create(printer, minimal);
    }

    public static MetaCSVRenderer create(final CSVPrinter printer, boolean minimal) {
        return new MetaCSVRenderer(new MetaCSVPrinter() {
            @Override
            public void printRecord(String domain, String key, Object value) throws IOException {
                printer.printRecord(domain, key, value);
            }

            @Override
            public void flush() throws IOException {
                printer.flush();
            }
        }, minimal);
    }

    private final MetaCSVPrinter printer;
    private final boolean minimal;

    public MetaCSVRenderer(MetaCSVPrinter printer, boolean minimal) {
        this.printer = printer;
        this.minimal = minimal;
    }

    public void render(MetaCSVData data) throws IOException {
        this.printer.printRecord("domain", "key", "value");
        if (minimal) {
            this.renderMinimal(data);
        } else {
            this.renderVerbose(data);
        }
        this.printer.flush();
    }

    private void renderMinimal(MetaCSVData data) throws IOException {
        // file
        Charset encoding = data.getEncoding();
        if (encoding != Util.UTF_8_CHARSET) {
            this.printer.printRecord("file", "encoding", encoding.toString());
        }
        if (data.isUtf8BOM()) {
            this.printer.printRecord("file", "bom", true);
        }
        String lineTerminator = data.getLineTerminator();
        if (!lineTerminator.equals("\r\n")) {
            this.printer.printRecord("file", "line_terminator", Util.escapeLineTerminator(lineTerminator));
        }
        // csv
        char delimiter = data.getDelimiter();
        if (delimiter != ',') {
            this.printer.printRecord("csv", "delimiter", delimiter);
        }
        if (!data.isDoubleQuote()) {
            this.printer.printRecord("csv", "double_quote", "false");
            this.printer.printRecord("csv", "escape_char", data.getEscapeChar());
        }
        char quoteChar = data.getQuoteChar();
        if (quoteChar != '"') {
            this.printer.printRecord("csv", "quote_char", quoteChar);
        }
        if (data.isSkipInitialSpace()) {
            this.printer.printRecord("csv", "skip_initial_space", "true");
        }
        // data
        String nullValue = data.getNullValue();
        if (!(nullValue == null || nullValue.isEmpty())) {
            this.printer.printRecord("data", "null_value", nullValue);
        }
        for (int i : data.getSortedColIndices()) {
            FieldDescription<?> description = data.getDescription(i);
            if (!(description instanceof TextFieldDescription)) {
                StringBuilder out = new StringBuilder();
                description.render(out);
                this.printer.printRecord("data", "col/" + i + "/type", out.toString());
            }
        }
    }

    private void renderVerbose(MetaCSVData data) throws IOException {
        // file
        this.printer.printRecord("file", "encoding", data.getEncoding().toString());
        this.printer.printRecord("file", "bom", data.isUtf8BOM());
        this.printer.printRecord("file", "line_terminator", Util.escapeLineTerminator(data.getLineTerminator()));
        // csv
        this.printer.printRecord("csv", "delimiter", data.getDelimiter());
        boolean doubleQuote = data.isDoubleQuote();
        this.printer.printRecord("csv", "double_quote", doubleQuote);
        if (!doubleQuote) {
            this.printer.printRecord("csv", "escape_char", data.getEscapeChar());
        }
        this.printer.printRecord("csv", "quote_char", data.getQuoteChar());
        this.printer.printRecord("csv", "skip_initial_space", data.isSkipInitialSpace());
        // data
        this.printer.printRecord("data", "null_value", data.getNullValue());
        for (int i : data.getSortedColIndices()) {
            FieldDescription<?> description = data.getDescription(i);
            StringBuilder out = new StringBuilder();
            description.render(out);
            this.printer.printRecord("data", "col/" + i + "/type", out.toString());
        }
    }
}
