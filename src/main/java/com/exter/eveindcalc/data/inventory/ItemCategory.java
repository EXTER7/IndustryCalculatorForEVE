package com.exter.eveindcalc.data.inventory;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ItemCategory
{
  public final int ID;
  public final String Name;
  public final int Icon;
  
  static private final String[] COLUMNS = {"id","name","icon"};
  
  public ItemCategory(SQLiteDatabase db,int i)
  {
    Cursor c = db.query("categories", COLUMNS, "id=?", new String[] {String.valueOf(i)}, null, null, null);
    if(c == null || c.getCount() != 1)
    {
      Name = null;
      ID = -1;
      Icon = -1;
      return;
    }
    c.moveToNext();
    
    ID = c.getInt(0);
    Name = c.getString(1);
    Icon = c.getInt(2);
    c.close();
  }

}
