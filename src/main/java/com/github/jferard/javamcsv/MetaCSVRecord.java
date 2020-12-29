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

package com.github.jferard.javamcsv;

import org.apache.commons.csv.CSVRecord;

import java.util.Iterator;
import java.util.List;

public class MetaCSVRecord implements Iterable<Object> {
    private CSVRecord record;
    private List<Object> values;

    public MetaCSVRecord(CSVRecord record, List<Object> values) {
        this.record = record;
        this.values = values;
    }

    public Boolean getBoolean(int i) throws MetaCSVCastException {
        Object value = this.values.get(i);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else {
            throw new MetaCSVCastException("Not a boolean: " + value);
        }
    }

    public double getCurrency(int i) {
        Object value = this.values.get(i);
        if (value instanceof Number) {
            return (Double) value;
        } else {
            throw new MetaCSVCastException("Not a currency: " + value);
        }
    }

    public CharSequence getText(int i) {
        Object value = this.values.get(i);
        if (value instanceof CharSequence) {
            return (CharSequence) value;
        } else {
            throw new MetaCSVCastException("Not a text: " + value);
        }
    }

    public Object getAny(int i) {
        return this.values.get(i);
    }

    @Override
    public Iterator<Object> iterator() {
        return this.values.iterator();
    }

    public int size() {
        return this.record.size();
    }

    @Override
    public String toString() {
        return "MetaCSVRecord(" + record + " ," + values + ")";
    }
}
