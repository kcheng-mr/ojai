/**
 * Copyright (c) 2015 MapR, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ojai.json.impl;

import static org.ojai.util.Constants.MILLISECONDSPERDAY;
import static org.ojai.util.Constants.TIMEZONE_OFFSET;

import java.io.UnsupportedEncodingException;
import java.sql.Date;

import org.ojai.DocumentBuilder;
import org.ojai.DocumentReader;
import org.ojai.DocumentReader.EventType;
import org.ojai.annotation.API;
import org.ojai.exceptions.DecodingException;

@API.Internal
public class JsonUtils {

  public static byte[] getBytes(String string) {
    try {
      return string.getBytes("UTF-8");
    } catch (UnsupportedEncodingException e) {}
    return null;
  }

  @SuppressWarnings("deprecation")
  public static long dateToNumDays(Date date) {
    /* just take the portion of time which has completed days */
    return (date.getTime() - date.getTimezoneOffset()*60*1000) / MILLISECONDSPERDAY;
  }

  public static Date numDaysToDate(long days) {
    /*
     * We store the date value as number of days since Unix epoch
     * but the returned Date object must be the original date in
     * the current timezone, hence subtract the timezone offset.
     */
    return new Date((days * MILLISECONDSPERDAY) - TIMEZONE_OFFSET);
  }

  public static void addToMap(DocumentReader r, DocumentBuilder w) {
    EventType e;
    String currentFieldName = null;
    while((e = r.next()) != null) {
      switch (e) {
      case FIELD_NAME:
        currentFieldName = r.getFieldName();
        break;
      case NULL:
        w.putNull(currentFieldName);
        break;
      case BOOLEAN:
        w.put(currentFieldName, r.getBoolean());
        break;
      case STRING:
        w.put(currentFieldName, r.getString());
        break;
      case BYTE:
        w.put(currentFieldName, r.getByte());
        break;
      case SHORT:
        w.put(currentFieldName, r.getShort());
        break;
      case INT:
        w.put(currentFieldName, r.getInt());
        break;
      case LONG:
        w.put(currentFieldName, r.getLong());
        break;
      case FLOAT:
        w.put(currentFieldName, r.getFloat());
        break;
      case DOUBLE:
        w.put(currentFieldName, r.getDouble());
        break;
      case DECIMAL:
        w.put(currentFieldName, r.getDecimal());
        break;
      case DATE:
        w.put(currentFieldName, r.getDate());
        break;
      case TIME:
        w.put(currentFieldName, r.getTime());
        break;
      case TIMESTAMP:
        w.put(currentFieldName, r.getTimestamp());
        break;
      case INTERVAL:
        w.put(currentFieldName, r.getInterval());
        break;
      case BINARY:
        w.put(currentFieldName, r.getBinary());
        break;
      case START_ARRAY:
        w.putNewArray(currentFieldName);
        addToArray(r, w);
        break;
      case END_ARRAY:
        w.endArray();
        break;
      case START_MAP:
        if (currentFieldName != null) {
          w.putNewMap(currentFieldName);
        } else {
          //start of the document
          w.addNewMap();
        }
        addToMap(r, w);
        break;
      case END_MAP:
        w.endMap();
        return;
      default:
        throw new DecodingException("Unknown event type: " + e);
      }
    }
  }

  private static void addToArray(DocumentReader r, DocumentBuilder w) {
    EventType e;
    while((e = r.next()) != null) {
      switch (e) {
      case NULL:
        w.addNull();
        break;
      case BOOLEAN:
        w.add(r.getBoolean());
        break;
      case STRING:
        w.add(r.getString());
        break;
      case BYTE:
        w.add(r.getByte());
        break;
      case SHORT:
        w.add(r.getShort());
        break;
      case INT:
        w.add(r.getInt());
        break;
      case LONG:
        w.add(r.getLong());
        break;
      case FLOAT:
        w.add(r.getFloat());
        break;
      case DOUBLE:
        w.add(r.getDouble());
        break;
      case DECIMAL:
        w.add(r.getDecimal());
        break;
      case DATE:
        w.add(r.getDate());
        break;
      case TIME:
        w.add(r.getTime());
        break;
      case TIMESTAMP:
        w.add(r.getTimestamp());
        break;
      case INTERVAL:
        w.add(r.getInterval());
        break;
      case BINARY:
        w.add(r.getBinary());
        break;
      case START_MAP:
        w.addNewMap();
        addToMap(r, w);
        break;
      case END_MAP:
        w.endMap();
        break;
      case START_ARRAY:
        w.addNewArray();
        addToArray(r, w);
        break;
      case END_ARRAY:
        w.endArray();
        return;
      case FIELD_NAME:
        throw new DecodingException("FIELD_NAME event encountered while appending to an array");
      default:
        throw new DecodingException("Unknown event type: " + e);
      }
    }
  }

}
