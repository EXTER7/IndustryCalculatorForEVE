package com.exter.eveindcalc.data.basecost;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.exter.cache.Cache;
import com.exter.cache.InfiniteCache;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.math.BigDecimal;

import exter.eveindustry.data.item.Item;

public class BaseCostDA
{
  private class CacheMissListener implements Cache.IMissListener<Integer, BigDecimal>
  {
    @Override
    public BigDecimal onCacheMiss(Integer key)
    {
      Cursor c = db.rawQuery("SELECT cost FROM base_cost WHERE id=" +String.valueOf(key),null);
      if(c.getCount() != 1)
      {
        c.close();
        return BigDecimal.ZERO;
      }
      c.moveToNext();
      BigDecimal bc = new BigDecimal(c.getString(0));
      c.close();
      return bc;
    }
  }

  private long expire = -1;

  private final Cache<Integer, BigDecimal> cache = new InfiniteCache<>(new CacheMissListener());

  private SQLiteDatabase db;
  private Context context;

  public BaseCostDA(SQLiteDatabase db,Context context)
  {
    this.db = db;
    this.context = context;
  }

  public boolean isExpired()
  {
    synchronized(cache)
    {
      if(expire < 0)
      {
        SharedPreferences sp = context.getSharedPreferences("EIC", Context.MODE_PRIVATE);
        expire = sp.getLong("basecost.exipire", 0);
      }

      long time = (System.currentTimeMillis() / 1000);
      
      Log.i("BaseCostDA", "Time: " + time + " Expire: " + expire);
      
      return time > expire;
    }
  }

  private void setExpire(long exp)
  {
    synchronized(cache)
    {
      expire = exp;
      SharedPreferences sp = context.getSharedPreferences("EIC", Context.MODE_PRIVATE);
      SharedPreferences.Editor ed = sp.edit();
      ed.putLong("basecost.exipire", exp);
      ed.apply();
    }
  }
  
  public void update(JsonReader reader)
  {
    try
    {
      reader.beginObject();
      while(reader.hasNext())
      {
        String name = reader.nextName();
        if(name.equals("items"))
        {
          reader.beginArray();
          while(reader.hasNext())
          {
            int id = -1;
            BigDecimal cost = null;
            reader.beginObject();
            while(reader.hasNext())
            {
              String val = reader.nextName();
              switch (val)
              {
                case "type":
                  reader.beginObject();
                  while (reader.hasNext())
                  {
                    String tval = reader.nextName();
                    if (tval.equals("id"))
                    {
                      id = reader.nextInt();
                    } else
                    {
                      reader.skipValue();
                    }
                  }
                  reader.endObject();
                  break;
                case "adjustedPrice":
                  cost = new BigDecimal(reader.nextString());
                  break;
                default:
                  reader.skipValue();
                  break;
              }
            }
            reader.endObject();
            if(id > 0 && cost != null && cost.signum() > 0)
            {
              db.execSQL("insert or replace into base_cost (id,cost) values ("
                  + String.valueOf(id) + ","
                  + String.valueOf(cost) + ");");
              synchronized(cache)
              {
                cache.flush(id);
              }
            }
          }
          reader.endArray();
        } else
        {
          reader.skipValue();
        }
      }
    } catch(IOException e)
    {
      e.printStackTrace();
      retryUpdate(30);
      return;
    } catch(IllegalStateException e)
    {
      e.printStackTrace();
      retryUpdate(30 * 60);
      return;
    }
    setExpire((System.currentTimeMillis() / 1000) + 24 * 60 * 60);
  }

  public void retryUpdate(long time)
  {
    setExpire((System.currentTimeMillis() / 1000) + time);
  }

  public BigDecimal getCost(Item item)
  {
    synchronized(cache)
    {
      return cache.get(item.id);
    }
  }
}
