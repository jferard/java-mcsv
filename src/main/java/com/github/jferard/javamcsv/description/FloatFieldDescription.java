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

package com.github.jferard.javamcsv.description;

import com.github.jferard.javamcsv.DataType;
import com.github.jferard.javamcsv.processor.FieldProcessor;
import com.github.jferard.javamcsv.processor.FloatFieldProcessor;

import java.io.IOException;

public class FloatFieldDescription implements FieldDescription<Double> {
    public static final FieldDescription<Double> INSTANCE = new FloatFieldDescription("", ".");

    private String thousandsSeparator;
    private String decimalSeparator;
    private String nullValue;

    public FloatFieldDescription(String thousandsSeparator, String decimalSeparator) {
        this.thousandsSeparator = thousandsSeparator;
        this.decimalSeparator = decimalSeparator;
        this.nullValue = "";
    }

    @Override
    public void render(Appendable out) throws IOException {
        if (this.thousandsSeparator.isEmpty()) {
            out.append("float//");
        } else {
            out.append("float/").append(this.thousandsSeparator).append('/');
        }
        out.append(this.decimalSeparator);
    }

    @Override
    public FieldProcessor<Double> toFieldProcessor(String nullValue) {
        return new FloatFieldProcessor(this.thousandsSeparator, this.decimalSeparator, nullValue);
    }

    @Override
    public Class<Double> getJavaType() {
        return Double.class;
    }

    @Override
    public DataType getDataType() {
        return DataType.FLOAT;
    }

    @Override
    public String toString() {
        return String.format("FloatFieldDescription(%s, %s)",
                this.thousandsSeparator, this.decimalSeparator);
    }
}
