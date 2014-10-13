/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.netty;

import io.netty.util.internal.chmv8.ConcurrentHashMapV8;

import java.util.concurrent.ConcurrentMap;

public class ObjectLocal<T> {

    private final ConcurrentMap<Object, T> map = new ConcurrentHashMapV8<>();

    public T get(Object obj) {
        return map.get(obj);
    }

    public void set(Object obj, T value) {
        if (value == null) {
            remove(obj);
        } else {
            map.put(obj, value);
        }
    }

    public void remove(Object obj) {
        map.remove(obj);
    }
}
