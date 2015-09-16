package com.exter.eveindcalc.data.inventory;


import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import exter.eveindustry.data.inventory.IItem;

public class Item implements IItem
{
  public final int ID;
  public final String Name;
  public final int Category;
  public final double Volume;
  public final int Icon;
  public final boolean Market;
  public final int MetaGroup;
  
  public final String NameLowercase;

  @Override
  public int hashCode()
  {
    return ID;
  }

  public boolean equals(Object obj)
  {
    return (obj instanceof Item) && equals((Item)obj);
  }

  public boolean equals(Item it)
  {
    return ID == it.ID;
  }
  
  private static final String[] id_arg = new String[1];
  private static final String[] COLUMNS = new String[]{"id","name","gid","volume","icon","market","metagroup"};
  
  @SuppressLint("DefaultLocale")
  public Item(SQLiteDatabase db,int i)
  {
    Cursor c;
    synchronized(id_arg)
    {
      id_arg[0] = String.valueOf(i);
      c = db.query("items", COLUMNS, "id=?", id_arg, null, null, null);
    }
    if(c == null || c.getCount() != 1)
    {
      Name = null;
      ID = -1;
      NameLowercase = null;
      Category = -1;
      Icon = -1;
      Market = false;
      Volume = 0;
      MetaGroup = -1;
      return;
    }
    c.moveToNext();
    
    ID = c.getInt(0);
    Name = c.getString(1);
    Category = c.getInt(2);
    Volume = c.getDouble(3);
    Icon = c.getInt(4);
    Market = c.getInt(5) != 0;
    MetaGroup = c.getInt(6);
    NameLowercase = Name.toLowerCase();
    c.close();    
  }

  @Override
  public int getID()
  {
    return ID;
  }


  @Override
  public int getGroupID()
  {
    return Category;
  }
}
