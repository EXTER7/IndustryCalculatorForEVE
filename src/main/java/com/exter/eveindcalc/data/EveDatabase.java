package com.exter.eveindcalc.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;

import com.exter.eveindcalc.EICApplication;
import com.exter.eveindcalc.EICDatabaseHelper;
import com.exter.eveindcalc.data.basecost.BaseCostDA;
import com.exter.eveindcalc.data.blueprint.BlueprintHistoryDA;
import com.exter.eveindcalc.data.market.MarketData;
import com.exter.eveindcalc.data.starmap.RecentSystemsDA;
import com.exter.eveindcalc.data.systemcost.SystemCostDA;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import exter.eveindustry.dataprovider.EVEDataProvider;
import exter.eveindustry.data.blueprint.IBlueprint;
import exter.eveindustry.data.inventory.IItem;
import exter.eveindustry.data.systemcost.ISolarSystemIndustryCost;
import exter.eveindustry.dataprovider.filesystem.IFileSystemHandler;
import exter.eveindustry.dataprovider.item.Item;
import exter.eveindustry.task.Task;
import exter.eveindustry.task.Task.Market;

public class EveDatabase extends EVEDataProvider
{


  static private class AssetsFileSystemHandler implements IFileSystemHandler
  {
    private AssetManager assets;

    AssetsFileSystemHandler(AssetManager assets)
    {
      this.assets = assets;
    }

    @Override
    public <T> T readFile(String path, IReadHandler<T> handler)
    {
      try
      {
        InputStream stream = assets.open(path);
        T result = handler.readFile(stream);
        stream.close();
        return result;
      } catch(IOException e)
      {
        return null;
      }
    }

    @Override
    public List<String> listDirectoryFiles(String path)
    {
      List<String> result = new ArrayList<>();
      try
      {
        String[] contents = assets.list(path);
        for(String file: contents)
        {
          if(file.endsWith(".tsl"))
          {
            result.add(path + "/" + file);
          }
        }
        return result;
      } catch(IOException e1)
      {
        return result;
      }
    }
  }

  public SQLiteDatabase getDatabase()
  {
    return database;
  }

  public void initDatabase()
  {
    if(database == null)
    {
      if(db_helper == null)
      {
        db_helper = new EICDatabaseHelper(application.getApplicationContext(),this);
      }
      database = db_helper.getWritableDatabase();
    }
    da_market = new MarketData(database);
    da_systemconst = new SystemCostDA(database,context);
    da_basecost = new BaseCostDA(database,context);
    da_recentsystems = new RecentSystemsDA(database);
    da_blueprinthistory = new BlueprintHistoryDA(database);
  }
  
  public void closeDatabase()
  {
    if(database == null)
    {
      return;
    }
    database.close();
    database = null;
  }


  private EICDatabaseHelper db_helper;
  private SQLiteDatabase database;
  private EICApplication application;
  private Context context;
  public SystemCostDA da_systemconst;
  public BaseCostDA da_basecost;
  public MarketData da_market;
  public RecentSystemsDA da_recentsystems;
  public BlueprintHistoryDA da_blueprinthistory;

  static private Task.Market def_required = null;
  static private Task.Market def_produced = null;
  static private int default_me = -1;
  static private int default_te = -1;

  public EveDatabase(EICApplication application)
  {
    super(new AssetsFileSystemHandler(application.getApplicationContext().getAssets()));
    this.application = application;
    this.context = application.getApplicationContext();
  }

  @Override
  public int getDefaultSkillLevel(int skill)
  {
    SharedPreferences sp = context.getSharedPreferences("EIC", Context.MODE_PRIVATE);
    return sp.getInt("skill_" + String.valueOf(skill), 0);
  }  

  @Override
  public Market getDefaultProducedMarket()
  {
    return getDefaultProducedPrice();
  }

  public Market getDefaultProducedPrice()
  {
    if(def_produced == null)
    {
      SharedPreferences sp = context.getSharedPreferences("EIC", Context.MODE_PRIVATE);
      def_produced = new Task.Market(
          sp.getInt("market.produced.system", 30000142),
          Task.Market.Order.fromInt(sp.getInt("market.produced.source",Task.Market.Order.SELL.value)),
          BigDecimal.ZERO,
          new BigDecimal(sp.getString("market.produced.broker", "3")),
          new BigDecimal(sp.getString("market.produced.tax", "2")));
    }
    return def_produced;
  }

  @Override
  public Market getDefaultRequiredMarket()
  {
    return getDefaultRequiredPrice();
  }

  public Market getDefaultRequiredPrice()
  {
    if(def_required == null)
    {
      SharedPreferences sp = context.getSharedPreferences("EIC", Context.MODE_PRIVATE);
      def_required = new Task.Market(
          sp.getInt("market.required.system", 30000142),
          Task.Market.Order.fromInt(sp.getInt("market.required.source",Task.Market.Order.SELL.value)),
          BigDecimal.ZERO,
          new BigDecimal(sp.getString("market.required.broker", "3")),
          new BigDecimal(sp.getString("market.required.tax", "2")));
    }
    return def_required;
  }

  public void setDefaultProducedPrice(Task.Market p)
  {
    def_produced = p;
    SharedPreferences sp = context.getSharedPreferences("EIC", Context.MODE_PRIVATE);
    SharedPreferences.Editor ed = sp.edit();
    ed.putInt("market.produced.system", p.system);
    ed.putInt("market.produced.source", p.order.value);
    ed.putString("market.produced.broker",p.broker.toString());
    ed.putString("market.produced.tax",p.transaction.toString());
    ed.apply();
  }

  public void setDefaultRequiredPrice(Task.Market p)
  {
    def_required = p;
    SharedPreferences sp = context.getSharedPreferences("EIC", Context.MODE_PRIVATE);
    SharedPreferences.Editor ed = sp.edit();
    ed.putInt("market.required.system", p.system);
    ed.putInt("market.required.source", p.order.value);
    ed.putString("market.required.broker",p.broker.toString());
    ed.putString("market.required.tax",p.transaction.toString());
    ed.apply();
  }

  public int getDefaultME(IBlueprint bp)
  {
    if(bp != null && ((Item)bp.getProduct().item).MetaGroup != 1)
    {
      return 0;
    }
    if(default_me < 0)
    {
      SharedPreferences sp = context.getSharedPreferences("EIC", Context.MODE_PRIVATE);
      default_me = sp.getInt("blueprint.default.me", 0);
    }
    return default_me;
  }

  @Override
  public int getDefaultBlueprintME(IBlueprint bp)
  {
    return getDefaultME(bp);
  }


  public int getDefaultTE(IBlueprint bp)
  {
    if(bp != null && ((Item)bp.getProduct().item).MetaGroup != 1)
    {
      return 0;
    }
    if(default_te < 0)
    {
      SharedPreferences sp = context.getSharedPreferences("EIC", Context.MODE_PRIVATE);
      default_te = sp.getInt("blueprint.default.te", 0);
    }
    return default_te;
  }

  @Override
  public int getDefaultBlueprintTE(IBlueprint bp)
  {
    return getDefaultTE(bp);
  }
  
  public void setDefaultBlueprintME(int me)
  {
    default_me = me;
    SharedPreferences sp = context.getSharedPreferences("EIC", Context.MODE_PRIVATE);
    SharedPreferences.Editor ed = sp.edit();
    ed.putInt("blueprint.default.me", me);
    ed.apply();
  }

  public void setDefaultBlueprintTE(int te)
  {
    default_te = te;
    SharedPreferences sp = context.getSharedPreferences("EIC", Context.MODE_PRIVATE);
    SharedPreferences.Editor ed = sp.edit();
    ed.putInt("blueprint.default.te", te);
    ed.apply();
  }

  @Override
  public BigDecimal getItemBaseCost(IItem item)
  {
    return da_basecost.getCost(item);
  }

  @Override
  public ISolarSystemIndustryCost getSolarSystemIndustryCost(int system_id)
  {
    return da_systemconst.getCost(system_id);
  }

  @Override
  public BigDecimal getMarketPrice(IItem item, Market market)
  {
    return da_market.getMarketPrice(item, market);
  }
}
