package com.exter.eveindcalc.data.inventory;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ItemMetaGroup
{
  public final int ID;
  public final String Name;

  static private final String[] COLUMNS = {"id","name"};

  public ItemMetaGroup(SQLiteDatabase db,int i)
  {
    
    Cursor c = db.query("metagroups", COLUMNS, "id=?", new String[] {String.valueOf(i)}, null, null, null);
    if(c == null || c.getCount() != 1)
    {
      Name = null;
      ID = -1;
      return;
    }
    c.moveToNext();
    
    ID = c.getInt(0);
    Name = c.getString(1);
    c.close();
  }

}
