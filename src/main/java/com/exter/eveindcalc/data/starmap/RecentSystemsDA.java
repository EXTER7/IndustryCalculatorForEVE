package com.exter.eveindcalc.data.starmap;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class RecentSystemsDA
{

  private Set<Integer> systems = null;
  private SQLiteDatabase db;

  public RecentSystemsDA(SQLiteDatabase db)
  {
    this.db = db;
  }

  private void loadSystems()
  {
    if(systems != null)
    {
      return;
    }
    systems = new HashSet<>();
    Cursor c = db.rawQuery("SELECT id FROM saved_solar_systems;", null);
    while(c.moveToNext())
    {
      systems.add(c.getInt(0));
    }
    c.close();
  }
  
  public Set<Integer> getSystems()
  {
    loadSystems();
    return Collections.unmodifiableSet(systems);
  }
  
  public void putSystem(int sys)
  {
    loadSystems();
    if(!systems.contains(sys))
    {
      systems.add(sys);
      db.execSQL("insert or replace into saved_solar_systems (id) values ("
          + String.valueOf(sys) + ");");
    }
  }
}
