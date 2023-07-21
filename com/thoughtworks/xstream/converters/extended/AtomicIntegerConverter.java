/*
 * Copyright (C) 2022 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 *
 * Created on 28. November 2022 by Joerg Schaible
 */
package com.thoughtworks.xstream.converters.extended;

import java.util.concurrent.atomic.AtomicInteger;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.basic.IntConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;


/**
 * Converts an AtomicInteger type.
 *
 * @author J&ouml;rg Schaible
 */
public class AtomicIntegerConverter extends IntConverter implements Converter {

    /**
     * Constructs an AtomicIntegerConverter.
     */
    public AtomicIntegerConverter() {
        super();
    }

    public boolean canConvert(final Class type) {
        return type != null && type == AtomicInteger.class;
    }

    public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        writer.setValue(toString(source));
    }

    public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
        final String data = reader.getValue(); // needs to be called before hasMoreChildren.
        if (!reader.hasMoreChildren()) {
            return fromString(data);
        } else {
            // backwards compatibility ... unmarshal nested element
            reader.moveDown();
            final Integer integer = (Integer)super.fromString(reader.getValue());
            reader.moveUp();
            return new AtomicInteger(integer.intValue());
        }
    }

    public String toString(final Object obj) {
        return super.toString(new Integer(((AtomicInteger)obj).get()));
    }

    public Object fromString(final String str) {
        return new AtomicInteger(((Integer)super.fromString(str)).intValue());
    }
}
