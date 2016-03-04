package com.exter.eveindcalc;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.exter.eveindcalc.data.EveDatabase;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import exter.eveindustry.dataprovider.blueprint.InstallationGroup;
import exter.eveindustry.dataprovider.item.Item;
import exter.eveindustry.dataprovider.item.ItemCategory;
import exter.eveindustry.dataprovider.item.ItemGroup;
import exter.eveindustry.dataprovider.item.ItemMetaGroup;
import exter.eveindustry.dataprovider.starmap.Region;
import exter.eveindustry.dataprovider.starmap.SolarSystem;


public class EICDatabaseHelper extends SQLiteOpenHelper
{
  private static final String DATABASE_NAME = "eic.db";

  // Increment this if the data in the assets directory is changed (like when a new EVE expansion is released).
  public static final int DATABASE_VERSION = 121;

  // Increment this if the schema of non-static tables are changed (resets, non-static data).
  private static final int NONSTATIC_VERSION = 120;

  public static final String GROUPS_CREATE = "create table groups"
  +"( id integer primary key,"
  + "cid integer not null);";

  public static final String CATEGORIES_CREATE = "create table categories"
  +"( id integer primary key);";
  
  public static final String METAGROUPS_CREATE = "create table metagroups"
  +"( id integer primary key );";

  public static final String BLUEPRINTS_CREATE = "create table blueprints"
  +"(id integer primary key,"
  + "name varchar not null,"
  + "gid integer not null,"
  + "mgid integer not null);";

  public static final String GROUPINSTALLATIONS_CREATE = "create table group_installations"
  +"( id integer primary key,"
  + " gid integer not null,"
  + " installation integer not null);";

  public static final String SOLARSYSTEMS_CREATE = "create table solar_systems"
  +"( id integer primary key,"
  + " rid integer not null,"
  + " name varchar not null);";

  public static final String REGIONS_CREATE = "create table regions"
  +"( id integer primary key,"
  + "name varchar not null);";

  // Non-static table
  public static final String MARKETCACHE_CREATE = "create table market_cache"
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
  public static final String BASECOST_CREATE = "create table base_cost"
  +"( id integer primary key,"
  + "cost varchar not null);";

  // Non-static table
  public static final String SYSTEMCOST_CREATE = "create table system_cost"
  +"( id integer primary key,"
  + "manufacturing varchar not null,"
  + "invention varchar not null);";

  // Non-static table
  public static final String SAVED_SOLARSYSTEMS_CREATE = "create table saved_solar_systems"
  +"( id integer primary key );";

  public EICDatabaseHelper(Context context)
  {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
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

  private void buildStaticData(SQLiteDatabase db)
  {
    db.execSQL(GROUPS_CREATE);
    db.execSQL(CATEGORIES_CREATE);
    db.execSQL(METAGROUPS_CREATE);
    db.execSQL(BLUEPRINTS_CREATE);
    db.execSQL(GROUPINSTALLATIONS_CREATE);
    db.execSQL(SOLARSYSTEMS_CREATE);
    db.execSQL(REGIONS_CREATE);

    EveDatabase data = EICApplication.getDataProvider();
    Set<Integer> bp_groups = new HashSet<>();
    for(Iterator<Item> iter = data.allItems(); iter.hasNext();)
    {
      Item item = iter.next();
      if(data.getBlueprintIndex().getItemIDs().contains(item.ID))
      {
        ContentValues cv = new ContentValues();
        cv.put("id", item.ID);
        cv.put("name", item.NameLowercase);
        cv.put("gid", item.Group);
        cv.put("mgid", item.MetaGroup);
        db.insert("blueprints", null, cv);
        bp_groups.add(item.Group);
      }
    }
    for(Iterator<ItemGroup> iter = data.allItemGroups(); iter.hasNext();)
    {
      ItemGroup group = iter.next();
      ContentValues cv = new ContentValues();
      cv.put("id", group.ID);
      cv.put("cid", group.Category);
      db.insert("groups", null, cv);
    }
    for(Iterator<ItemCategory> iter = data.allItemCategories(); iter.hasNext();)
    {
      ItemCategory category = iter.next();
      ContentValues cv = new ContentValues();
      cv.put("id", category.ID);
      db.insert("categories", null, cv);
    }
    for(Iterator<ItemMetaGroup> iter = data.allItemMetaGroups(); iter.hasNext();)
    {
      ItemMetaGroup group = iter.next();
      ContentValues cv = new ContentValues();
      cv.put("id", group.ID);
      db.insert("metagroups", null, cv);
    }
    for(Iterator<InstallationGroup> iter = data.allInstallationGroups(); iter.hasNext();)
    {
      InstallationGroup group = iter.next();
      ContentValues values = new ContentValues();
      values.put("id", group.ID);
      values.put("gid", group.GroupID);
      values.put("installation", group.InstallationID);
      db.insert("group_installations", null, values);
    }

    for(Iterator<SolarSystem> iter = data.allSolarSystems(); iter.hasNext();)
    {
      SolarSystem system = iter.next();
      ContentValues values = new ContentValues();
      values.put("id", system.ID);
      values.put("rid", system.Region);
      values.put("name", system.Name);
      db.insert("solar_systems", null, values);
    }
    for(Iterator<Region> iter = data.allSolarSystemRegions(); iter.hasNext();)
    {
      Region region = iter.next();
      ContentValues values = new ContentValues();
      values.put("id", region.ID);
      values.put("name", region.Name);
      db.insert("regions", null, values);
    }
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
      db.execSQL("DROP TABLE IF EXISTS invention_installations");
    }
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
