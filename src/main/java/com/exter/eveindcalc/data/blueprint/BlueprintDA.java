package com.exter.eveindcalc.data.blueprint;

import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import com.exter.eveindcalc.EICApplication;
import com.exter.eveindcalc.data.EveDatabase;
import com.exter.eveindcalc.data.Index;
import com.exter.eveindcalc.data.exception.EveDataException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import exter.tsl.InvalidTSLException;
import exter.tsl.TSLObject;
import exter.tsl.TSLReader;

public class BlueprintDA
{
  static private List<Integer> blueprints;

  protected String getTable()
  {
    return "blueprints";
  }

  static public Blueprint getBlueprint(int product)
  {
    try
    {
      AssetManager assets = EICApplication.getContext().getAssets();
      InputStream raw = assets.open("blueprint/" + String.valueOf(product) + ".tsl");
      try
      {
        TSLReader reader = new TSLReader(raw);
        reader.moveNext();
        if(reader.getState() == TSLReader.State.OBJECT && reader.getName().equals("blueprint"))
        {
          return new Blueprint(new TSLObject(reader));
        } else
        {
          return null;
        }
      } catch(EveDataException e)
      {
        e.printStackTrace();
        raw.close();
        return null;
      } catch(InvalidTSLException e)
      {
        e.printStackTrace();
        raw.close();
        return null;
      }
    } catch(IOException e)
    {
      e.printStackTrace();
      return null;
    }
  }

  static public boolean isItemBlueprint(int product)
  {
    try
    {
      AssetManager assets = EICApplication.getContext().getAssets();
      InputStream raw = assets.open("blueprint/" + String.valueOf(product) + ".tsl");
      raw.close();
      return true;
    } catch(IOException e)
    {
      return false;
    }
  }

  static public void convertFromAssets(SQLiteDatabase db, AssetManager assets)
  {
    try
    {
      TSLReader tsl;
      InputStream raw;
      try
      {
        raw = assets.open("blueprint/index.tsl");
        tsl = new TSLReader(raw);
      } catch(IOException e)
      {
        e.printStackTrace();
        return;
      }

      Index index = new Index(tsl);
      List<Integer> bp = index.getItems();
      Object[] arg = new Object[1];
      for(int bpid : bp)
      {
        arg[0] = bpid;
        db.execSQL("INSERT INTO blueprints(id) VALUES (?);", arg);
      }

      try
      {
        raw.close();
      } catch(IOException e)
      {
        e.printStackTrace();
      }
    } catch(EveDataException e)
    {
      e.printStackTrace();
    }
  }

  public List<Integer> getAllBlueprints()
  {
    SQLiteDatabase database = EveDatabase.getDatabase();

    boolean base = getTable().equals("blueprints");
    if(base && blueprints != null)
    {
      return blueprints;
    }
    Cursor c = database.rawQuery("SELECT id FROM " + getTable() + ";", null);
    List<Integer> entries = new ArrayList<>();
    if(c == null || c.getCount() < 1)
    {
      return entries;
    }
    while(c.moveToNext())
    {
      entries.add(c.getInt(0));
    }
    c.close();
    if(base)
    {
      blueprints = entries;
    }
    return entries;
  }

  public Cursor queryBlueprints(String name, int group, int category, int metagroup)
  {
    SQLiteDatabase database = EveDatabase.getDatabase();

    if(database == null || !database.isOpen())
    {
      return null;
    }
    String query = "SELECT " + getTable() + ".id FROM items,groups," + getTable() + " WHERE items.id = " + getTable() + ".id AND groups.id = items.gid";
    if(name != null && name.length() > 2)
    {
      String[] tokens = name.split(" ");
      for(String t:tokens)
      {
        if(t != null && t.length() > 1)
        {
          t = DatabaseUtils.sqlEscapeString(t);
          query = query + " AND items.name LIKE '%" + t.substring(1, t.length() - 1) + "%'";
        }
      }
    }
    if(group != -1)
    {
      query = query + " AND items.gid = " + String.valueOf(group);
    }
    if(category != -1)
    {
      query = query + " AND groups.cid = " + String.valueOf(category);
    }
    if(metagroup != -1)
    {
      query = query + " AND items.metagroup = " + String.valueOf(metagroup);
    }
    query = query + " ORDER BY " + QueryOrderBy() + ";";
    return database.rawQuery(query, null);
  }

  protected String QueryOrderBy()
  {
    return "items.name";
  }
}
