package com.exter.eveindcalc.data.starmap;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.exter.eveindcalc.data.EveDatabase;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class RecentSystemsDA
{
  
  static private Set<Integer> systems = null;
  
  static private void loadSystems()
  {
    if(systems != null)
    {
      return;
    }
    systems = new HashSet<>();
    SQLiteDatabase db = EveDatabase.getDatabase();
    Cursor c = db.rawQuery("SELECT id FROM saved_solar_systems;", null);
    while(c.moveToNext())
    {
      systems.add(c.getInt(0));
    }
    c.close();
  }
  
  static public Set<Integer> getSystems()
  {
    loadSystems();
    return Collections.unmodifiableSet(systems);
  }
  
  static public void putSystem(int sys)
  {
    loadSystems();
    if(!systems.contains(sys))
    {
      systems.add(sys);
      SQLiteDatabase db = EveDatabase.getDatabase();
      db.execSQL("insert or replace into solar_systems (id) values ("
          + String.valueOf(sys) + ");");
    }
  }
}
