/*
 * $Id$
 * Copyright (C) 2006 Klaus Reimer <k@ailis.de>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package de.ailis.pherialize;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.ailis.pherialize.exceptions.SerializeException;


/**
 * Serializes Java objects in a PHP serializer format string.
 * 
 * @author Klaus Reimer (k.reimer@iplabs.de)
 * @version $Revision$
 */

public class Serializer
{
    /** The object history for resolving references */
    private List history;


    /**
     * Constructor
     */

    public Serializer()
    {
        super();
        this.history = new ArrayList();
    }


    /**
     * Serializes the specified object.
     * 
     * @param object
     *            The object
     * @return The serialized data
     */

    public String serialize(Object object)
    {
        StringBuffer buffer;

        buffer = new StringBuffer();
        serializeObject(object, buffer);
        return buffer.toString();
    }


    /**
     * This method is used internally for recursively scanning the object while
     * serializing.
     * 
     * @param object
     *            The object to serialize
     * @param buffer
     *            The string buffer to append serialized data to
     */

    private void serializeObject(Object object, StringBuffer buffer)
    {
        if (object == null)
        {
            serializeNull(buffer);
        }
        else if (serializeReference(object, buffer))
        {
            return;
        }
        else if (object instanceof String)
        {
            serializeString((String) object, buffer);
        }
        else if (object instanceof Character)
        {
            serializeCharacter((Character) object, buffer);
        }
        else if (object instanceof Integer)
        {
            serializeInteger(((Integer) object).intValue(), buffer);
        }
        else if (object instanceof Short)
        {
            serializeInteger(((Short) object).intValue(), buffer);
        }
        else if (object instanceof Byte)
        {
            serializeInteger(((Byte) object).intValue(), buffer);
        }
        else if (object instanceof Long)
        {
            serializeLong(((Long) object).longValue(), buffer);
        }
        else if (object instanceof Double)
        {
            serializeDouble(((Double) object).doubleValue(), buffer);
        }
        else if (object instanceof Float)
        {
            serializeDouble(((Float) object).doubleValue(), buffer);
        }
        else if (object instanceof Boolean)
        {
            serializeBoolean((Boolean) object, buffer);
        }
        else if (object instanceof Collection)
        {
            serializeCollection((Collection) object, buffer);
            return;
        }
        else if (object instanceof Map)
        {
            serializeMap((Map) object, buffer);
            return;
        }
        else if (object instanceof Serializable)
        {
            serializeSerializable((Serializable) object, buffer);
            return;
        }
        else
        {
            throw new SerializeException("Unable to serialize "
                + object.getClass().getName());
        }

        this.history.add(object);
    }


    /**
     * Tries to serialize a reference if the specified object was already
     * serialized. It returns true in this case. If the object was not
     * serialized before then false is returned.
     * 
     * @param object
     *            The object to serialize
     * @param buffer
     *            The string buffer to append serialized data to
     * @return If a reference was serialized or not
     */

    private boolean serializeReference(Object object, StringBuffer buffer)
    {
        Iterator iterator;
        int index;
        boolean isReference;

        iterator = this.history.iterator();
        index = 0;
        isReference = false;
        while (iterator.hasNext())
        {
            if (iterator.next() == object)
            {
                buffer.append("R:");
                buffer.append(index + 1);
                buffer.append(';');
                isReference = true;
                break;
            }
            index++;
        }
        return isReference;
    }


    /**
     * Serializes the specified string and appends it to the serialization
     * buffer.
     * 
     * @param string
     *            The string to serialize
     * @param buffer
     *            The string buffer to append serialized data to
     */

    private void serializeString(String string, StringBuffer buffer)
    {
        buffer.append("s:");
        buffer.append(string.length());
        buffer.append(":\"");
        buffer.append(string);
        buffer.append("\";");
    }


    /**
     * Serializes the specified character and appends it to the serialization
     * buffer.
     * 
     * @param value
     *            The value to serialize
     * @param buffer
     *            The string buffer to append serialized data to
     */

    private void serializeCharacter(Character value, StringBuffer buffer)
    {
        buffer.append("s:1:\"");
        buffer.append(value);
        buffer.append("\";");
    }


    /**
     * Adds a serialized NULL to the serialization buffer.
     * 
     * @param buffer
     *            The string buffer to append serialized data to
     */

    private void serializeNull(StringBuffer buffer)
    {
        buffer.append("N;");
    }


    /**
     * Serializes the specified integer number and appends it to the
     * serialization buffer.
     * 
     * @param number
     *            The integer number to serialize
     * @param buffer
     *            The string buffer to append serialized data to
     */

    private void serializeInteger(int number, StringBuffer buffer)
    {
        buffer.append("i:");
        buffer.append(number);
        buffer.append(';');
    }


    /**
     * Serializes the specified lonf number and appends it to the serialization
     * buffer.
     * 
     * @param number
     *            The lonf number to serialize
     * @param buffer
     *            The string buffer to append serialized data to
     */

    private void serializeLong(long number, StringBuffer buffer)
    {
        if ((number >= Integer.MIN_VALUE) && (number <= Integer.MAX_VALUE))
        {
            buffer.append("i:");
        }
        else
        {
            buffer.append("d:");
        }
        buffer.append(number);
        buffer.append(';');
    }


    /**
     * Serializes the specfied double number and appends it to the serialization
     * buffer.
     * 
     * @param number
     *            The number to serialize
     * @param buffer
     *            The string buffer to append serialized data to
     */

    private void serializeDouble(double number, StringBuffer buffer)
    {
        buffer.append("d:");
        buffer.append(number);
        buffer.append(';');
    }


    /**
     * Serializes the specfied boolean and appends it to the serialization
     * buffer.
     * 
     * @param value
     *            The value to serialize
     * @param buffer
     *            The string buffer to append serialized data to
     */

    private void serializeBoolean(Boolean value, StringBuffer buffer)
    {
        buffer.append("b:");
        buffer.append(value.booleanValue() ? 1 : 0);
        buffer.append(';');
    }


    /**
     * Serializes the specfied collection and appends it to the serialization
     * buffer.
     * 
     * @param collection
     *            The collection to serialize
     * @param buffer
     *            The string buffer to append serialized data to
     */

    private void serializeCollection(Collection collection, StringBuffer buffer)
    {
        Iterator iterator;
        int index;

        this.history.add(collection);
        buffer.append("a:");
        buffer.append(collection.size());
        buffer.append(":{");
        iterator = collection.iterator();
        index = 0;
        while (iterator.hasNext())
        {
            serializeObject(Integer.valueOf(index), buffer);
            this.history.remove(this.history.size() - 1);
            serializeObject(iterator.next(), buffer);
            index++;
        }
        buffer.append('}');
    }


    /**
     * Serializes the specfied map and appends it to the serialization buffer.
     * 
     * @param map
     *            The map to serialize
     * @param buffer
     *            The string buffer to append serialized data to
     */

    private void serializeMap(Map map, StringBuffer buffer)
    {
        Iterator iterator;
        Object key;

        this.history.add(map);
        buffer.append("a:");
        buffer.append(map.size());
        buffer.append(":{");
        iterator = map.keySet().iterator();
        while (iterator.hasNext())
        {
            key = iterator.next();
            serializeObject(key, buffer);
            this.history.remove(this.history.size() - 1);
            serializeObject(map.get(key), buffer);
        }
        buffer.append('}');
    }


    /**
     * Serializes a serializable object
     * 
     * @param object
     *            The serializable object
     * @param buffer
     *            The string buffer to append serialized data to
     */

    private void serializeSerializable(Serializable object, StringBuffer buffer)
    {
        String className;
        Class c;
        Field[] fields;
        int i, max;
        Field field;
        String key;
        Object value;
        StringBuffer fieldBuffer;
        int fieldCount;

        this.history.add(object);
        c = object.getClass();
        className = c.getSimpleName().toLowerCase();
        buffer.append("O:");
        buffer.append(className.length());
        buffer.append(":\"");
        buffer.append(className);
        buffer.append("\":");

        fieldBuffer = new StringBuffer();
        fieldCount = 0;
        while (c != null)
        {
            fields = c.getDeclaredFields();
            for (i = 0, max = fields.length; i < max; i++)
            {
                field = fields[i];
                if (Modifier.isStatic(field.getModifiers())) continue;
                if (Modifier.isVolatile(field.getModifiers())) continue;
                
                try
                {
                    field.setAccessible(true);
                    key = field.getName();
                    value = field.get(object);
                    serializeObject(key, fieldBuffer);
                    this.history.remove(this.history.size() - 1);
                    serializeObject(value, fieldBuffer);
                    fieldCount++;
                }
                catch (SecurityException e)
                {
                //  Field is just ignored when this exception is thrown
                }
                catch (IllegalArgumentException e)
                {
                //  Field is just ignored when this exception is thrown
                }
                catch (IllegalAccessException e)
                {
                //  Field is just ignored when this exception is thrown
                }
            }
            c = c.getSuperclass();
        }
        buffer.append(fieldCount);
        buffer.append(":{");
        buffer.append(fieldBuffer);
        buffer.append("}");
    }
}
