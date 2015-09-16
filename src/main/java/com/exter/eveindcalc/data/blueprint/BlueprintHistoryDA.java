package com.exter.eveindcalc.data.blueprint;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;

import com.exter.eveindcalc.data.EveDatabase;

public class BlueprintHistoryDA
{
  static public class Entry
  {
    public final int product;
    private int me_level;
    private int te_level;
    private boolean changed;
    
    public int getME()
    {
      return me_level;
    }

    public void setME(int me)
    {
      me_level = me;
      changed = true;
    }

    
    public int getTE()
    {
      return te_level;
    }

    public void setTE(int te)
    {
      te_level = te;
      changed = true;
    }

    
    public void update()
    {
      if(!changed)
      {
        return;
      }
      if(cache == null)
      {
        cache = new SparseArray<>();
      }
      cache.put(product, this);
      SQLiteDatabase db = EveDatabase.getDatabase();
      db.execSQL("insert or replace into blueprint_history (product,me,te) values ("
      + String.valueOf(product) + ","
      + String.valueOf(me_level) + ","
      + String.valueOf(te_level) + ");");
    }
    
    public Entry(int prod,int me,int te)
    {
      product = prod;
      me_level = me;
      te_level = te;
      changed = true;
    }
  }
  
  static private SparseArray<Entry> cache;
  
  static public Entry getEntry(int item)
  {
    if(cache == null)
    {
      cache = new SparseArray<>();
    }
    Entry e = cache.get(item);
    if(e != null)
    {
      return e;
    }
    SQLiteDatabase db = EveDatabase.getDatabase();
    Cursor c = db.rawQuery("SELECT me,te FROM blueprint_history WHERE product="+String.valueOf(item)+";", null);
    if(c == null || c.getCount() != 1)
    {
      return null;
    }
    c.moveToNext();
    e = new Entry(item,c.getInt(0),c.getInt(1));
    e.changed = false;
    c.close();
    cache.put(item, e);
    return e;
  }
}
