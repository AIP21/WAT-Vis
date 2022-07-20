package com.anipgames.WAT_Vis.util;

import java.util.HashMap;
import java.util.Iterator;

// IDK how to do string methods in Java, so I just made my own version of HashMap
// - Jay
public class Dict<K, V> extends HashMap<K, V> {
    public Dict() {
        super();
    }

    @Override
    public String toString() {
        Iterator<Entry<K, V>> i = entrySet().iterator();
        if (!i.hasNext()) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (;;) {
            Entry<K, V> e = i.next();
            K key = e.getKey();
            V value = e.getValue();
            sb.append('"');
            sb.append(key == this ? "(this Map)" : key);
            sb.append('"');
            sb.append(':').append(' ');
            sb.append('"');
            sb.append(value == this ? "(this Map)" : value);
            sb.append('"');
            if (i.hasNext()) {
                sb.append(',').append(' ');
            }
            if (!i.hasNext())
                return sb.append('}').toString();
        }
    }
}
