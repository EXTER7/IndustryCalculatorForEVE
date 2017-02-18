package com.exter.eveindcalc;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.exter.eveindcalc.data.Index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import exter.eveindustry.data.blueprint.InstallationGroup;
import exter.eveindustry.data.blueprint.InventionInstallation;
import exter.eveindustry.data.item.Item;
import exter.eveindustry.data.item.ItemCategory;
import exter.eveindustry.data.item.ItemGroup;
import exter.eveindustry.data.item.ItemMetaGroup;
import exter.eveindustry.data.starmap.Region;
import exter.eveindustry.data.starmap.SolarSystem;
import exter.eveindustry.task.TaskFactory;


public class EICDatabaseHelper extends SQLiteOpenHelper
{
  private static final String DATABASE_NAME = "eic.db";

  // Increment this if the data in the assets directory is changed (like when a new EVE expansion is released).
  private static final int DATABASE_VERSION = 134;

  // Change this to the value of DATABASE_VERSION the schema of non-static tables are changed (resets non-static data).
  private static final int NONSTATIC_VERSION = 120;

  private static final String GROUPS_CREATE = "create table groups"
  +"( id integer primary key,"
  + " cid integer not null,"
  + " name varchar not null);";

  private static final String CATEGORIES_CREATE = "create table categories"
  + "( id integer primary key,"
  + " name varchar not null);";
  
  private static final String METAGROUPS_CREATE = "create table metagroups"
  +"( id integer primary key );";

  private static final String BLUEPRINTS_CREATE = "create table blueprints"
  +"(id integer primary key,"
  + "name varchar not null,"
  + "gid integer not null,"
  + "mgid integer not null);";

  private static final String GROUPINSTALLATIONS_CREATE = "create table group_installations"
  +"( id integer primary key,"
  + " gid integer not null,"
  + " installation integer not null);";


  private static final String INVENTIONNSTALLATIONS_CREATE = "create table invention_installations"
  +"( id integer primary key);";

  private static final String SOLARSYSTEMS_CREATE = "create table solar_systems"
  +"( id integer primary key,"
  + " rid integer not null,"
  + " name varchar not null);";

  private static final String REGIONS_CREATE = "create table regions"
  +"( id integer primary key,"
  + "name varchar not null);";

  // Non-static table
  private static final String MARKETCACHE_CREATE = "create table market_cache"
  +"( id integer primary key autoincrement,"
  + " time number(12) not null,"
  + " item integer not null,"
  + " system integer not null,"
  + " buy varchar not null,"
  + " sell varchar not null);";


  // Non-static table
  private static final String BPHISTORY_CREATE = "create table blueprint_history"
  +"( product integer primary key,"
  + "me integer not null,"
  + "te integer not null);";

  // Non-static table
  private static final String BASECOST_CREATE = "create table base_cost"
  +"( id integer primary key,"
  + "cost varchar not null);";

  // Non-static table
  private static final String SYSTEMCOST_CREATE = "create table system_cost"
  +"( id integer primary key,"
  + "manufacturing varchar not null,"
  + "invention varchar not null);";

  // Non-static table
  private static final String SAVED_SOLARSYSTEMS_CREATE = "create table saved_solar_systems"
  +"( id integer primary key );";

  private EICApplication application;

  public EICDatabaseHelper(EICApplication application)
  {
    super(application.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    this.application = application;
  }

  @Override
  public void onCreate(SQLiteDatabase db)
  {
    db.execSQL(MARKETCACHE_CREATE);
    db.execSQL(BPHISTORY_CREATE);
    db.execSQL(BASECOST_CREATE);
    db.execSQL(SYSTEMCOST_CREATE);
    db.execSQL(SAVED_SOLARSYSTEMS_CREATE);

    // Add trade hubs to the solar system list.
    db.execSQL("insert or replace into saved_solar_systems (id) values ( 30000142 )");
    db.execSQL("insert or replace into saved_solar_systems (id) values ( 30002187 )");
    db.execSQL("insert or replace into saved_solar_systems (id) values ( 30002659 )");
    db.execSQL("insert or replace into saved_solar_systems (id) values ( 30002510 )");
    db.execSQL("insert or replace into saved_solar_systems (id) values ( 30002053 )");
    db.execSQL("insert or replace into saved_solar_systems (id) values ( 31000005 )");

    buildStaticData(db);
  }

  private List<Integer> listIDs(String path)
  {
    List<Integer> result = new ArrayList<>();
    try
    {
      String[] contents = application.getAssets().list(path);
      for(String file: contents)
      {
        if(file.endsWith(".tsl"))
        {
          try
          {
            result.add(Integer.valueOf(file.replace(".tsl", "")));
          } catch(NumberFormatException ignore)
          {

          }
        }
      }
      return result;
    } catch(IOException e1)
    {
      return result;
    }
  }

  private void buildStaticData(SQLiteDatabase db)
  {
    db.execSQL(GROUPS_CREATE);
    db.execSQL(CATEGORIES_CREATE);
    db.execSQL(METAGROUPS_CREATE);
    db.execSQL(BLUEPRINTS_CREATE);
    db.execSQL(GROUPINSTALLATIONS_CREATE);
    db.execSQL(INVENTIONNSTALLATIONS_CREATE);
    db.execSQL(SOLARSYSTEMS_CREATE);
    db.execSQL(REGIONS_CREATE);

    TaskFactory factory = new TaskFactory(application.fs,application.database);

    Set<Integer> bp_groups = new HashSet<>();
    Set<Integer> bp_categories = new HashSet<>();
    Set<Integer> bp_metagroups = new HashSet<>();

    Index blueprints = new Index(application.fs,"blueprint/index.tsl");

    db.beginTransaction();
    for(int id:blueprints.getItemIDs())
    {
      Item item = factory.items.get(id);
      ContentValues cv = new ContentValues();
      cv.put("id", item.id);
      cv.put("name", item.name.toLowerCase());
      cv.put("gid", item.group_id);
      cv.put("mgid", item.metagroup_id);
      db.insert("blueprints", null, cv);
      bp_groups.add(item.group_id);
      bp_metagroups.add(item.metagroup_id);
    }

    for(int gid:bp_groups)
    {
      ItemGroup group = factory.item_groups.get(gid);
      ContentValues cv = new ContentValues();
      cv.put("id", group.id);
      cv.put("cid", group.category_id);
      cv.put("name", group.name);
      db.insert("groups", null, cv);
      bp_categories.add(group.category_id);
    }
    for(int cid:bp_categories)
    {
      ItemCategory category = factory.item_categories.get(cid);
      ContentValues cv = new ContentValues();
      cv.put("id", category.id);
      cv.put("name", category.name);
      db.insert("categories", null, cv);
    }
    for(int mgid:bp_metagroups)
    {
      ItemMetaGroup metagroup = factory.item_metagroups.get(mgid);
      ContentValues cv = new ContentValues();
      cv.put("id", metagroup.id);
      db.insert("metagroups", null, cv);
    }

    for(int id:listIDs("blueprint/installation/group"))
    {
      InstallationGroup group = factory.installation_groups.get(id);
      ContentValues values = new ContentValues();
      values.put("id", group.id);
      values.put("gid", group.group_id);
      values.put("installation", group.installation_id);
      db.insert("group_installations", null, values);
    }
    for(int id:listIDs("blueprint/installation/invention"))
    {
      InventionInstallation inst = factory.invention_installations.get(id);
      ContentValues values = new ContentValues();
      values.put("id", inst.id);
      db.insert("invention_installations", null, values);
    }

    for(int id:listIDs("solarsystem"))
    {
      SolarSystem system = factory.solarsystems.get(id);
      ContentValues values = new ContentValues();
      values.put("id", system.id);
      values.put("rid", system.region);
      values.put("name", system.name);
      db.insert("solar_systems", null, values);
    }
    for(int id:listIDs("solarsystem/region"))
    {
      Region region = factory.regions.get(id);
      ContentValues values = new ContentValues();
      values.put("id", region.id);
      values.put("name", region.name);
      db.insert("regions", null, values);
    }
    db.setTransactionSuccessful();
    db.endTransaction();
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
  {
    if(oldVersion < NONSTATIC_VERSION)
    {
      db.execSQL("DROP TABLE IF EXISTS blueprint_history");
      db.execSQL("DROP TABLE IF EXISTS blueprint_favorites");
      db.execSQL("DROP TABLE IF EXISTS material_prices");
      db.execSQL("DROP TABLE IF EXISTS market_cache");
      db.execSQL("DROP TABLE IF EXISTS base_cost");
      db.execSQL("DROP TABLE IF EXISTS system_cost");
      db.execSQL("DROP TABLE IF EXISTS saved_solar_systems");
      db.execSQL(MARKETCACHE_CREATE);
      db.execSQL(BPHISTORY_CREATE);
      db.execSQL(BASECOST_CREATE);
      db.execSQL(SYSTEMCOST_CREATE);
      db.execSQL(SAVED_SOLARSYSTEMS_CREATE);
      db.execSQL("insert or replace into saved_solar_systems (id) values ( 30000142 )");
      db.execSQL("insert or replace into saved_solar_systems (id) values ( 30002187 )");
      db.execSQL("insert or replace into saved_solar_systems (id) values ( 30002659 )");
      db.execSQL("insert or replace into saved_solar_systems (id) values ( 30002510 )");
      db.execSQL("insert or replace into saved_solar_systems (id) values ( 30002053 )");
      db.execSQL("insert or replace into saved_solar_systems (id) values ( 31000005 )");
    }


    if(oldVersion < 120)
    {
      db.execSQL("DROP TABLE IF EXISTS items");
      db.execSQL("DROP TABLE IF EXISTS installations");
    }
    db.execSQL("DROP TABLE IF EXISTS invention_installations");
    db.execSQL("DROP TABLE IF EXISTS categories");
    db.execSQL("DROP TABLE IF EXISTS groups");
    db.execSQL("DROP TABLE IF EXISTS metagroups");
    db.execSQL("DROP TABLE IF EXISTS blueprints");
    db.execSQL("DROP TABLE IF EXISTS group_installations");
    db.execSQL("DROP TABLE IF EXISTS solar_systems");
    db.execSQL("DROP TABLE IF EXISTS regions");

    buildStaticData(db);
  }
}
