package com.exter.eveindcalc.data.inventory;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ItemGroup
{
  public final int ID;
  public final int Category;
  public final String Name;
  public final int Icon;
  
  static private final String[] COLUMNS = {"id","name","cid","icon"};
  
  public ItemGroup(SQLiteDatabase db,int i)
  {
    Cursor c = db.query("groups", COLUMNS, "id=?", new String[] {String.valueOf(i)}, null, null, null);
    if(c == null || c.getCount() != 1)
    {
      Name = null;
      ID = -1;
      Category = -1;
      Icon = -1;
      return;
    }
    c.moveToNext();
    
    ID = c.getInt(0);
    Name = c.getString(1);
    Category = c.getInt(2);
    Icon = c.getInt(3);
    c.close();
  }
}
