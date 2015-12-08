package com.exter.eveindcalc.data.blueprint;

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

import exter.eveindustry.data.blueprint.IBlueprint;
import exter.tsl.InvalidTSLException;
import exter.tsl.TSLObject;
import exter.tsl.TSLReader;

public class InstallationDA
{
  static private List<Integer> invention_installation_list = null;
  static private List<Integer> relic_invention_installation_list = null;
  
  static private class InstallationMissListener implements Cache.IMissListener<Integer,Installation>
  {
    static private final String[] COLUMNS = {"id","name"};
    @Override
    public Installation onCacheMiss(Integer key)
    {
      SQLiteDatabase db = EveDatabase.getDatabase();
      Cursor c = db.query("installations", COLUMNS, "id = ?", new String[] {String.valueOf(key)}, null, null, null);
      if(!c.moveToNext())
      {
        throw new IllegalArgumentException("Installation not found: " + key.toString());
      }
      Installation i = new Installation(c.getInt(0),c.getString(1));
      c.close();
      return i;
    }
  }

  
  static private class InstallationGroupMissListener implements Cache.IMissListener<Integer,InstallationGroup>
  {
    static private final String[] COLUMNS = {"id","gid","installation","time","material","cost"};
    @Override
    public InstallationGroup onCacheMiss(Integer key)
    {
      
      SQLiteDatabase db = EveDatabase.getDatabase();
      Cursor c = db.query("group_installations", COLUMNS, "id = ?", new String[] {String.valueOf(key)}, null, null, null);
      if(!c.moveToNext())
      {
        return null;
      }
      InstallationGroup ig = new InstallationGroup(c.getInt(0),c.getInt(1),c.getInt(2),c.getFloat(3),c.getFloat(4),c.getFloat(5));
      c.close();
      return ig;
    }
  }

  static private class GroupInstallationsMissListener implements Cache.IMissListener<Integer,List<InstallationGroup>>
  {
    static private final String[] COLUMNS = {"id","gid","installation","time","material","cost"};
    @Override
    public List<InstallationGroup> onCacheMiss(Integer key)
    {
      List<InstallationGroup> groups = new ArrayList<>();
      SQLiteDatabase db = EveDatabase.getDatabase();
      Cursor c = db.query("group_installations", COLUMNS, "gid = ?", new String[] {String.valueOf(key)}, null, null, null);
      while(c.moveToNext())
      {
        groups.add(new InstallationGroup(c.getInt(0),c.getInt(1),c.getInt(2),c.getFloat(3),c.getFloat(4),c.getFloat(5)));
      }
      c.close();
      return groups;
    }
  }

  static private class InventionInstallationMissListener implements Cache.IMissListener<Integer,InventionInstallation>
  {
    static private final String[] COLUMNS = {"id","name","time","cost", "relics"};
    @Override
    public InventionInstallation onCacheMiss(Integer key)
    {
      SQLiteDatabase db = EveDatabase.getDatabase();
      Cursor c = db.query("invention_installations", COLUMNS, "id = ?", new String[] {String.valueOf(key)}, null, null, null);
      if(!c.moveToNext())
      {
        return null;
      }
      InventionInstallation i = new InventionInstallation(c.getInt(0),c.getString(1),c.getFloat(2),c.getFloat(3),c.getInt(4) > 0);
      c.close();
      return i;
    }
  }

  static private Cache<Integer,Installation> installations = new InfiniteCache<>(new InstallationMissListener());
  static private Cache<Integer,InstallationGroup> installation_groups = new InfiniteCache<>(new InstallationGroupMissListener());
  static private Cache<Integer,InventionInstallation> invention_installations = new InfiniteCache<>(new InventionInstallationMissListener());
  static private Cache<Integer,List<InstallationGroup>> group_installations = new InfiniteCache<>(new GroupInstallationsMissListener());

  static public void convertFromAssets(SQLiteDatabase db, AssetManager assets)
  {
    try
    {
      InputStream raw = assets.open("blueprint/installations.tsl");
      TSLReader reader = new TSLReader(raw);

      reader.moveNext();

      if(!reader.getName().equals("installations"))
      {
        throw new EveDataException();
      }
      TSLObject node = new TSLObject();
      while(true)
      {
        reader.moveNext();
        TSLReader.State type = reader.getState();
        if(type == TSLReader.State.ENDOBJECT)
        {
          break;
        }

        if(type == TSLReader.State.OBJECT)
        {
          switch (reader.getName())
          {
            case "group":
            {
              node.loadFromReader(reader);
              ContentValues values = new ContentValues();
              values.put("id", node.getStringAsInt("id", -1));
              values.put("gid", node.getStringAsInt("group", -1));
              values.put("installation", node.getStringAsInt("installation", -1));
              values.put("time", node.getStringAsFloat("time", -1));
              values.put("material", node.getStringAsFloat("material", -1));
              values.put("cost", node.getStringAsFloat("cost", -1));
              db.insert("group_installations", null, values);

              break;
            }
            case "installation":
            {
              node.loadFromReader(reader);
              ContentValues values = new ContentValues();
              values.put("id", node.getStringAsInt("id", -1));
              values.put("name", node.getString("name", null));
              db.insert("installations", null, values);
              break;
            }
            case "invention":
            {
              node.loadFromReader(reader);
              ContentValues values = new ContentValues();
              values.put("id", node.getStringAsInt("id", -1));
              values.put("name", node.getString("name", null));
              values.put("cost", node.getStringAsFloat("cost", -1));
              values.put("time", node.getStringAsFloat("time", -1));
              values.put("relics", node.getStringAsFloat("relics", -1));
              db.insert("invention_installations", null, values);
              break;
            }
            default:
              reader.skipObject();
              break;
          }
        }
      }
      raw.close();
    } catch(InvalidTSLException | EveDataException | IOException e)
    {
      throw new RuntimeException(e);
    }
  }
  
  static public List<InstallationGroup> getBlueprintInstallations(IBlueprint bp)
  {
    return group_installations.get(bp.getProduct().item.getGroupID());
  }

  static public Installation getInstallation(int id)
  {
    return installations.get(id);
  }

  static public InstallationGroup getInstallationGroup(int id)
  {
    return installation_groups.get(id);
  }
  
  static public int getDefaultInstallation()
  {
    return 6;
  }

  static public InventionInstallation getInventionInstallation(int id)
  {
    return invention_installations.get(id);
  }

  static public List<Integer> getInventionInstallationIDs()
  {
    if(invention_installation_list == null)
    {
      SQLiteDatabase db = EveDatabase.getDatabase();
      Cursor c = db.query("invention_installations", new String[] {"id"}, "relics = 0", null, null, null, null);
      invention_installation_list = new ArrayList<>();
      while(c.moveToNext())
      {
        invention_installation_list.add(c.getInt(0));
      }
      c.close();
    }
    return Collections.unmodifiableList(invention_installation_list);
  }


  static public List<Integer> getRelicInventionInstallationIDs()
  {
    if(relic_invention_installation_list == null)
    {
      SQLiteDatabase db = EveDatabase.getDatabase();
      Cursor c = db.query("invention_installations", new String[] {"id"}, "relics = 1", null, null, null, null);
      relic_invention_installation_list = new ArrayList<>();
      while(c.moveToNext())
      {
        relic_invention_installation_list.add(c.getInt(0));
      }
      c.close();
    }
    return Collections.unmodifiableList(relic_invention_installation_list);
  }
}
