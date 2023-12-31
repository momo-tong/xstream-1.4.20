/*
 * Copyright (C) 2004, 2005, 2006 Joe Walnes.
 * Copyright (C) 2006, 2007, 2009, 2011, 2021 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 15. March 2004 by Joe Walnes
 */
package com.thoughtworks.xstream.core;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.converters.DataHolder;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.core.util.ObjectIdDictionary;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.ExtendedHierarchicalStreamWriterHelper;
import com.thoughtworks.xstream.mapper.Mapper;

import java.util.Collections;
import java.util.Iterator;


public class TreeMarshaller implements MarshallingContext {

    protected HierarchicalStreamWriter writer;
    protected ConverterLookup converterLookup;
    private Mapper mapper;
    private ObjectIdDictionary parentObjects = new ObjectIdDictionary();
    private DataHolder dataHolder;

    public TreeMarshaller(
        HierarchicalStreamWriter writer, ConverterLookup converterLookup, Mapper mapper) {
        this.writer = writer;
        this.converterLookup = converterLookup;
        this.mapper = mapper;
    }

    public void convertAnother(Object item) {
        convertAnother(item, null);
    }

    public void convertAnother(Object item, Converter converter) {
        if (converter == null) {
            converter = converterLookup.lookupConverterForType(item.getClass());
        } else {
            if (!converter.canConvert(item.getClass())) {
                ConversionException e = new ConversionException(
                    "Explicit selected converter cannot handle item");
                e.add("item-type", item.getClass().getName());
                e.add("converter-type", converter.getClass().getName());
                throw e;
            }
        }
        convert(item, converter);
    }

    protected void convert(Object item, Converter converter) {
        if (parentObjects.containsId(item)) {
            ConversionException e = new CircularReferenceException(
                "Recursive reference to parent object");
            e.add("item-type", item.getClass().getName());
            e.add("converter-type", converter.getClass().getName());
            throw e;
        }
        parentObjects.associateId(item, "");
        converter.marshal(item, writer, this);
        parentObjects.removeId(item);
    }

    public void start(Object item, DataHolder dataHolder) {
        this.dataHolder = dataHolder;
        if (item == null) {
            writer.startNode(mapper.serializedClass(null));
            writer.endNode();
        } else {
            ExtendedHierarchicalStreamWriterHelper.startNode(writer, mapper
                .serializedClass(item.getClass()), item.getClass());
            convertAnother(item);
            writer.endNode();
        }
    }

    public Object get(Object key) {
        return dataHolder != null ? dataHolder.get(key) : null;
    }

    public void put(Object key, Object value) {
        lazilyCreateDataHolder();
        dataHolder.put(key, value);
    }

    public Iterator keys() {
        return dataHolder != null ? dataHolder.keys() : Collections.EMPTY_MAP.keySet().iterator();
    }

    private void lazilyCreateDataHolder() {
        if (dataHolder == null) {
            dataHolder = new MapBackedDataHolder();
        }
    }

    protected Mapper getMapper() {
        return this.mapper;
    }

    public static class CircularReferenceException extends ConversionException {

        public CircularReferenceException(String msg) {
            super(msg);
        }
    }
}
