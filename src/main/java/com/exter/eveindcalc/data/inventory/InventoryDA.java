package com.exter.eveindcalc.data.inventory;

import android.content.ContentValues;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.exter.cache.Cache;
import com.exter.cache.InfiniteCache;
import com.exter.eveindcalc.data.EveDatabase;
import com.exter.eveindcalc.data.exception.EveDataException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import exter.tsl.InvalidTSLException;
import exter.tsl.TSLObject;
import exter.tsl.TSLReader;

public class InventoryDA
{

  static private class ItemCacheMiss implements Cache.IMissListener<Integer, Item>
  {
    @Override
    public Item onCacheMiss(Integer key)
    {
      SQLiteDatabase database = EveDatabase.getDatabase();
      Item it = new Item(database,key);
      if(it.ID < 0)
      {
        return null;
      }
      return it;
    }
  }

  static private class GroupCacheMiss implements Cache.IMissListener<Integer, ItemCategory>
  {

    @Override
    public ItemCategory onCacheMiss(Integer key)
    {
      SQLiteDatabase database = EveDatabase.getDatabase();
      ItemCategory gr = new ItemCategory(database,key);
      if(gr.ID < 0)
      {
        return null;
      }
      return gr;
    }
  }

  static private class CategoryCacheMiss implements Cache.IMissListener<Integer, ItemGroup>
  {

    @Override
    public ItemGroup onCacheMiss(Integer key)
    {
      SQLiteDatabase database = EveDatabase.getDatabase();
      ItemGroup cat = new ItemGroup(database,key);
      if(cat.ID < 0)
      {
        return null;
      }
      return cat;
    }
  }

  static private class MetaGroupCacheMiss implements Cache.IMissListener<Integer, ItemMetaGroup>
  {

    @Override
    public ItemMetaGroup onCacheMiss(Integer key)
    {
      SQLiteDatabase database = EveDatabase.getDatabase();
      ItemMetaGroup mg = new ItemMetaGroup(database,key);
      if(mg.ID < 0)
      {
        return null;
      }
      return mg;
    }
  }

  static private class CategoryGroupsCacheMiss implements Cache.IMissListener<Integer, List<Integer>>
  {

    @Override
    public List<Integer> onCacheMiss(Integer key)
    {
      SQLiteDatabase database = EveDatabase.getDatabase();
      Cursor c = database.rawQuery("SELECT id FROM groups WHERE cid="+String.valueOf(key)+" AND blueprints = 1 ORDER BY name", null);
      if(c == null)
      {
        return null;
      }
      ArrayList<Integer> list = new ArrayList<>();
      while(c.moveToNext())
      {
        list.add(c.getInt(0));
      }
      c.close();
      return list;
    }
  }

  static private Cache<Integer,Item> items = new InfiniteCache<>(new ItemCacheMiss());
  static private Cache<Integer,ItemCategory> groups = new InfiniteCache<>(new GroupCacheMiss());
  static private Cache<Integer,ItemGroup> categories = new InfiniteCache<>(new CategoryCacheMiss());
  static private Cache<Integer,ItemMetaGroup> metagroups = new InfiniteCache<>(new MetaGroupCacheMiss());
  static private Cache<Integer,List<Integer>> category_groups = new InfiniteCache<>(new CategoryGroupsCacheMiss());
  static private ArrayList<Integer> blueprint_groups = null;


  static public Item getItem(int id)
  {
    return items.get(id);
  }

  static public ItemCategory getCategory(int id)
  {
    return groups.get(id);
  }

  static public ItemGroup getGroup(int id)
  {
    return categories.get(id);
  }

  static public ItemMetaGroup getMetaGroup(int id)
  {
    return metagroups.get(id);
  }

  static public void convertFromAssets(SQLiteDatabase db, AssetManager ass) throws EveDataException
  {
    TSLReader tsl;

    InputStream raw;
    try
    {
      raw = ass.open("inventory.tsl");
      tsl = new TSLReader(raw);
    } catch(IOException e)
    {
      throw new EveDataException();
    }
    try
    {
      tsl.moveNext();
    } catch(InvalidTSLException | IOException e1)
    {
      throw new EveDataException();
    }
    if(!tsl.getName().equals("inventory"))
    {
      throw new EveDataException();
    }
    try
    {
      while(true)
      {
        tsl.moveNext();
        if(tsl.getState() == TSLReader.State.ENDOBJECT)
        {
          break;
        }
        if(tsl.getState() == TSLReader.State.OBJECT)
        {
          switch (tsl.getName())
          {
            case "c":
            {
              TSLObject node = new TSLObject(tsl);
              ContentValues cv = new ContentValues();
              cv.put("id", node.getStringAsInt("id", -1));
              cv.put("name", node.getString("name", null));
              cv.put("icon", node.getStringAsInt("icon", -1));
              cv.put("blueprints", node.getStringAsInt("blueprints", 0));
              db.insert("categories", null, cv);
              break;
            }
            case "g":
            {
              TSLObject node = new TSLObject(tsl);
              ContentValues cv = new ContentValues();
              cv.put("id", node.getStringAsInt("id", -1));
              cv.put("name", node.getString("name", null));
              cv.put("cid", node.getStringAsInt("cid", -1));
              cv.put("icon", node.getStringAsInt("icon", -1));
              cv.put("blueprints", node.getStringAsInt("blueprints", 0));
              db.insert("groups", null, cv);
              break;
            }
            case "m":
            {
              TSLObject node = new TSLObject(tsl);
              ContentValues cv = new ContentValues();
              cv.put("id", node.getStringAsInt("id", -1));
              cv.put("name", node.getString("name", null));
              db.insert("metagroups", null, cv);
              break;
            }
            case "i":
            {
              TSLObject node = new TSLObject(tsl);
              ContentValues cv = new ContentValues();
              cv.put("id", node.getStringAsInt("id", -1));
              cv.put("name", node.getString("name", null));
              cv.put("gid", node.getStringAsInt("gid", -1));
              cv.put("volume", node.getStringAsFloat("vol", -1));
              cv.put("icon", node.getStringAsInt("icon", -1));
              cv.put("market", node.getStringAsInt("market", -1));
              cv.put("metagroup", node.getStringAsInt("mg", 0));
              db.insert("items", null, cv);
              break;
            }
            default:
              tsl.skipObject();
              break;
          }
        }
      }
    } catch(InvalidTSLException | IOException e)
    {
      throw new EveDataException();
    }

    try
    {
      raw.close();
      items.flushAll();
      groups.flushAll();
      categories.flushAll();
      metagroups.flushAll();
    } catch(IOException ignored)
    {

    }
  }

  static public List<Integer> blueprintGroups()
  {
    if(blueprint_groups == null)
    {
      SQLiteDatabase database = EveDatabase.getDatabase();

      Cursor c = database.rawQuery("SELECT id FROM categories WHERE blueprints = 1 ORDER BY name", null);
      if(c == null)
      {
        return null;
      }
      blueprint_groups = new ArrayList<>();
      while(c.moveToNext())
      {
        blueprint_groups.add(c.getInt(0));
      }
      c.close();
    }
    return Collections.unmodifiableList(blueprint_groups);
  }

  static public List<Integer> blueprintCategories(int group)
  {
    return category_groups.get(group);
  }
  
  static public List<Integer> metaGroups()
  {
    SQLiteDatabase database = EveDatabase.getDatabase();

    Cursor c = database.rawQuery("SELECT id FROM metagroups", null);
    if(c == null)
    {
      return null;
    }
    ArrayList<Integer> list = new ArrayList<>();
    while(c.moveToNext())
    {
      list.add(c.getInt(0));
    }
    c.close();
    return list;
  }
}
