package com.exter.eveindcalc;


import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.exter.eveindcalc.data.blueprint.BlueprintDA;
import com.exter.eveindcalc.data.blueprint.InstallationDA;
import com.exter.eveindcalc.data.exception.EveDataException;
import com.exter.eveindcalc.data.inventory.InventoryDA;

public class EICDatabaseHelper extends SQLiteOpenHelper
{
  private final Context ctx;
  private static final String DATABASE_NAME = "eic.db";

  // Increment this if the data in the assets directory is changed (like when a new EVE expansion is released).
  public static final int DATABASE_VERSION = 119;

  // Increment this if the schema of non-static tables are changed (resets, non-static data).
  private static final int NONSTATIC_VERSION = 110;

  public static final String ITEMS_CREATE = "create table items"
  +"( id integer primary key,"
  + "name varchar not null,"
  + "gid integer not null,"
  + "volume decimal not null,"
  + "icon integer not null,"
  + "market integer not null,"
  + "metagroup integer not null);";

  public static final String GROUPS_CREATE = "create table groups"
  +"( id integer primary key,"
  + "name varchar not null,"
  + "cid integer not null,"
  + "icon integer not null,"
  + "blueprints integer not null);";

  public static final String CATEGORIES_CREATE = "create table categories"
  +"( id integer primary key,"
  + "name varchar not null,"
  + "icon integer not null,"
  + "blueprints integer not null);";
  
  public static final String METAGROUPS_CREATE = "create table metagroups"
  +"( id integer primary key,"
  + "name varchar not null);";

  public static final String BLUEPRINTS_CREATE = "create table blueprints"
  +"(seqid integer primary key autoincrement,"
  + "id integer not null);";

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
  public static final String SOLARSYSTEMS_CREATE = "create table solar_systems"
  +"( id integer primary key );";
  
  public static final String GROUPINSTALLATION_CREATE = "create table group_installations"
  +"( id integer primary key,"
  + " gid integer not null,"
  + " installation integer not null,"
  + " material decimal not null,"
  + " time decimal not null,"
  + " cost decimal not null);";
  
  public static final String INSTALLATION_CREATE = "create table installations"
  +"( id integer primary key,"
  + " name varchar not null);";

  public static final String INVENTIONINSTALLATION_CREATE = "create table invention_installations"
  +"( id integer primary key,"
  + " name varchar not null,"
  + " time decimal not null,"
  + " cost decimal not null,"
  + " relics integer not null);";

  public EICDatabaseHelper(Context context)
  {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
    ctx = context;
  }

  @Override
  public void onCreate(SQLiteDatabase db)
  {
    db.execSQL(ITEMS_CREATE);
    db.execSQL(GROUPS_CREATE);
    db.execSQL(CATEGORIES_CREATE);
    db.execSQL(METAGROUPS_CREATE);
    db.execSQL(BLUEPRINTS_CREATE);
    db.execSQL(INSTALLATION_CREATE);
    db.execSQL(GROUPINSTALLATION_CREATE);
    db.execSQL(INVENTIONINSTALLATION_CREATE);
    db.execSQL(MARKETCACHE_CREATE);
    db.execSQL(BPHISTORY_CREATE);
    db.execSQL(BASECOST_CREATE);
    db.execSQL(SYSTEMCOST_CREATE);
    db.execSQL(SOLARSYSTEMS_CREATE);

    // Add trade hubs to the solar system list.
    db.execSQL("insert or replace into solar_systems (id) values ( 30000142 )");
    db.execSQL("insert or replace into solar_systems (id) values ( 30002187 )");
    db.execSQL("insert or replace into solar_systems (id) values ( 30002659 )");
    db.execSQL("insert or replace into solar_systems (id) values ( 30002510 )");
    db.execSQL("insert or replace into solar_systems (id) values ( 30002053 )");
    db.execSQL("insert or replace into solar_systems (id) values ( 31000005 )");
    
   
    try
    {
      AssetManager assets = ctx.getAssets();
      InventoryDA.convertFromAssets(db, assets);
      BlueprintDA.convertFromAssets(db, assets);
      InstallationDA.convertFromAssets(db, assets);
    } catch(EveDataException ignored)
    {
      
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
      db.execSQL("DROP TABLE IF EXISTS solar_systems");
      db.execSQL(MARKETCACHE_CREATE);
      db.execSQL(BPHISTORY_CREATE);
      db.execSQL(BASECOST_CREATE);
      db.execSQL(SYSTEMCOST_CREATE);
      db.execSQL(SOLARSYSTEMS_CREATE);
      db.execSQL("insert or replace into solar_systems (id) values ( 30000142 )");
      db.execSQL("insert or replace into solar_systems (id) values ( 30002187 )");
      db.execSQL("insert or replace into solar_systems (id) values ( 30002659 )");
      db.execSQL("insert or replace into solar_systems (id) values ( 30002510 )");
      db.execSQL("insert or replace into solar_systems (id) values ( 30002053 )");
      db.execSQL("insert or replace into solar_systems (id) values ( 31000005 )");
    }


    db.execSQL("DROP TABLE IF EXISTS items");
    db.execSQL("DROP TABLE IF EXISTS categories");
    db.execSQL("DROP TABLE IF EXISTS groups");
    db.execSQL("DROP TABLE IF EXISTS metagroups");
    db.execSQL("DROP TABLE IF EXISTS blueprints");
    db.execSQL("DROP TABLE IF EXISTS installations");
    db.execSQL("DROP TABLE IF EXISTS group_installations");
    db.execSQL("DROP TABLE IF EXISTS invention_installations");

    db.execSQL(ITEMS_CREATE);
    db.execSQL(GROUPS_CREATE);
    db.execSQL(CATEGORIES_CREATE);
    db.execSQL(METAGROUPS_CREATE);
    db.execSQL(BLUEPRINTS_CREATE);
    db.execSQL(INSTALLATION_CREATE);
    db.execSQL(GROUPINSTALLATION_CREATE);
    db.execSQL(INVENTIONINSTALLATION_CREATE);
    try
    {
      AssetManager assets = ctx.getAssets();
      InventoryDA.convertFromAssets(db, assets);
      BlueprintDA.convertFromAssets(db, assets);
      InstallationDA.convertFromAssets(db, assets);
    } catch(EveDataException e)
    {
      e.printStackTrace();
    }
  }
}
