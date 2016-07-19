package com.exter.eveindcalc.data.systemcost;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.exter.cache.Cache;
import com.exter.cache.InfiniteCache;
import com.google.gson.stream.JsonReader;

import java.io.IOException;

import exter.eveindustry.data.systemcost.SolarSystemIndustryCost;


public class SystemCostDA
{
  private class CacheMissListener implements Cache.IMissListener<Integer, SolarSystemIndustryCost>
  {
    @Override
    public SolarSystemIndustryCost onCacheMiss(Integer key)
    {
      Cursor c = db.rawQuery("SELECT manufacturing,invention FROM system_cost WHERE id=?", new String[] { String.valueOf(key) });
      if(c.getCount() != 1)
      {
        c.close();
        return new SolarSystemIndustryCost(0,0);
      }
      c.moveToNext();
      SolarSystemIndustryCost sc = new SolarSystemIndustryCost(
          c.getDouble(0),
          c.getDouble(1));
      c.close();
      return sc;
    }
  }
  

  private long expire = -1;

  private final Cache<Integer, SolarSystemIndustryCost> cache = new InfiniteCache<>(new CacheMissListener());

  private SQLiteDatabase db;
  private Context context;

  public SystemCostDA(SQLiteDatabase db,Context context)
  {
    this.db = db;
    this.context = context;
  }

  public boolean IsExpired()
  {
    synchronized(cache)
    {
      if(expire < 0)
      {
        SharedPreferences sp = context.getSharedPreferences("EIC", Context.MODE_PRIVATE);
        expire = sp.getLong("systemcost.exipire", 0);
      }
      long time = (System.currentTimeMillis() / 1000);
      Log.i("SystemCostDA", "Time: " + time + " Expire: " + expire);

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
      ed.putLong("systemcost.exipire", exp);
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
        if (name.equals("items"))
        {
          reader.beginArray();
          while (reader.hasNext())
          {
            int id = -1;
            double man_cost = -1;
            double inv_cost = 0;
            reader.beginObject();
            while (reader.hasNext())
            {
              String val = reader.nextName();
              switch (val)
              {
                case "solarSystem":
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
                case "systemCostIndices":
                  reader.beginArray();
                  while (reader.hasNext())
                  {
                    int act = -1;
                    double acost = 0;
                    reader.beginObject();
                    while (reader.hasNext())
                    {
                      String aname = reader.nextName();
                      switch (aname)
                      {
                        case "activityID":
                          act = reader.nextInt();
                          break;
                        case "costIndex":
                          acost = reader.nextDouble();
                          break;
                        default:
                          reader.skipValue();
                          break;
                      }
                    }
                    reader.endObject();
                    if (act == 1)
                    {
                      man_cost = acost;
                    } else if (act == 8)
                    {
                      inv_cost = acost;
                    }
                  }
                  reader.endArray();
                  break;
                default:
                  reader.skipValue();
                  break;
              }
            }
            reader.endObject();
            if (id > 0 && man_cost >= 0)
            {
              db.execSQL("insert or replace into system_cost (id,manufacturing,invention) values ("
                      + String.valueOf(id) + ","
                      + String.valueOf(man_cost) + ","
                      + String.valueOf(inv_cost) + ");");
              cache.flush(id);
            }
          }
          reader.endArray();
        } else
        {
          reader.skipValue();
        }
      }
    } catch(IllegalStateException e)
    {
      e.printStackTrace();
      retryUpdate(10 * 60);
      return;
    } catch(IOException e)
    {
      e.printStackTrace();
      retryUpdate(30);
      return;
    }
    setExpire((System.currentTimeMillis() / 1000) + 2 * 60 * 60);
  }

  public void retryUpdate(long time)
  {
    setExpire((System.currentTimeMillis() / 1000) + time);
  }

  public SolarSystemIndustryCost getCost(int id)
  {
    return cache.get(id);
  }
}
