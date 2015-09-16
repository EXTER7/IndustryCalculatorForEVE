package com.exter.eveindcalc.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import com.exter.eveindcalc.EICApplication;
import com.exter.eveindcalc.EICDatabaseHelper;
import com.exter.eveindcalc.data.basecost.BaseCostDA;
import com.exter.eveindcalc.data.blueprint.BlueprintDA;
import com.exter.eveindcalc.data.blueprint.InstallationDA;
import com.exter.eveindcalc.data.blueprint.InstallationGroup;
import com.exter.eveindcalc.data.decryptor.DecryptorDA;
import com.exter.eveindcalc.data.inventory.InventoryDA;
import com.exter.eveindcalc.data.inventory.Item;
import com.exter.eveindcalc.data.market.MarketData;
import com.exter.eveindcalc.data.planet.PlanetDA;
import com.exter.eveindcalc.data.planet.PlanetProductDA;
import com.exter.eveindcalc.data.reaction.ReactionDA;
import com.exter.eveindcalc.data.refine.RefineDA;
import com.exter.eveindcalc.data.starbase.StarbaseTowerDA;
import com.exter.eveindcalc.data.systemcost.SystemCostDA;

import java.math.BigDecimal;
import java.util.List;

import exter.eveindustry.data.IEVEDataProvider;
import exter.eveindustry.data.blueprint.IBlueprint;
import exter.eveindustry.data.blueprint.IInstallationGroup;
import exter.eveindustry.data.blueprint.IInventionInstallation;
import exter.eveindustry.data.decryptor.IDecryptor;
import exter.eveindustry.data.inventory.IItem;
import exter.eveindustry.data.planet.IPlanet;
import exter.eveindustry.data.planet.IPlanetBuilding;
import exter.eveindustry.data.reaction.IReaction;
import exter.eveindustry.data.reaction.IStarbaseTower;
import exter.eveindustry.data.refinable.IRefinable;
import exter.eveindustry.data.systemcost.ISolarSystemIndustryCost;
import exter.eveindustry.task.Task;
import exter.eveindustry.task.Task.Market;

public class EveDatabase implements IEVEDataProvider
{
  static private EICDatabaseHelper dbhelper;
  static private SQLiteDatabase database;

  static public SQLiteDatabase getDatabase()
  {
    if(database == null)
    {
      if(dbhelper == null)
      {
        dbhelper = new EICDatabaseHelper(EICApplication.getContext());
      }
      database = dbhelper.getWritableDatabase();
    }
    return database;
  }
  
  static public void CloseDatabase()
  {
    if(database == null)
    {
      return;
    }
    database.close();
    database = null;
  }



  static private Task.Market def_required = null;
  static private Task.Market def_produced = null;
  static private int default_me = -1;
  static private int default_te = -1;
  
  @Override
  public int getDefaultSolarSystem()
  {
    return 30000142;
  }


  @Override
  public int getIndustrySkillID()
  {
    return 3380;
  }

  @Override
  public int getAdvancedIndustrySkillID()
  {
    return 3388;
  }

  @Override
  public int getRefiningSkillID()
  {
    return 3388;
  }

  @Override
  public int getRefineryEfficiencySkillID()
  {
    return 3389;
  }

  @Override
  public int getDefaultSkillLevel(int skill)
  {
    SharedPreferences sp = EICApplication.getContext().getSharedPreferences("EIC", Context.MODE_PRIVATE);
    return sp.getInt("skill_" + String.valueOf(skill), 0);
  }  

  @Override
  public Market getDefaultProducedMarket()
  {
    return GetDefaultProducedPrice();
  }

  static public Market GetDefaultProducedPrice()
  {
    if(def_produced == null)
    {
      SharedPreferences sp = EICApplication.getContext().getSharedPreferences("EIC", Context.MODE_PRIVATE);
      def_produced = new Task.Market(
          sp.getInt("market.produced.system", 30000142),
          Task.Market.Order.fromInt(sp.getInt("market.produced.source",Task.Market.Order.SELL.value)),
          BigDecimal.ZERO);
    }
    return def_produced;
  }

  @Override
  public Market getDefaultRequiredMarket()
  {
    return GetDefaultRequiredPrice();
  }

  static public Market GetDefaultRequiredPrice()
  {
    if(def_required == null)
    {
      SharedPreferences sp = EICApplication.getContext().getSharedPreferences("EIC", Context.MODE_PRIVATE);
      def_required = new Task.Market(
          sp.getInt("market.required.system", 30000142),
          Task.Market.Order.fromInt(sp.getInt("market.required.source",Task.Market.Order.SELL.value)),
          BigDecimal.ZERO);
    }
    return def_required;
  }

  static public void SetDefaultProducedPrice(Task.Market p)
  {
    def_produced = p;
    SharedPreferences sp = EICApplication.getContext().getSharedPreferences("EIC", Context.MODE_PRIVATE);
    SharedPreferences.Editor ed = sp.edit();
    ed.putInt("market.produced.system", p.system);
    ed.putInt("market.produced.source", p.order.value);
    ed.apply();
  }

  static public void SetDefaultRequiredPrice(Task.Market p)
  {
    def_required = p;
    SharedPreferences sp = EICApplication.getContext().getSharedPreferences("EIC", Context.MODE_PRIVATE);
    SharedPreferences.Editor ed = sp.edit();
    ed.putInt("market.required.system", p.system);
    ed.putInt("market.required.source", p.order.value);
    ed.apply();
  }

  static public int GetDefaultBlueprintME(IBlueprint bp)
  {
    if(bp != null && ((Item)bp.getProduct().item).MetaGroup != 1)
    {
      return 0;
    }
    if(default_me < 0)
    {
      SharedPreferences sp = EICApplication.getContext().getSharedPreferences("EIC", Context.MODE_PRIVATE);
      default_me = sp.getInt("blueprint.default.me", 0);
    }
    return default_me;
  }

  @Override
  public int getDefaultBlueprintME(IBlueprint bp)
  {
    return GetDefaultBlueprintME(bp);
  }


  static public int GetDefaultBlueprintTE(IBlueprint bp)
  {
    if(bp != null && ((Item)bp.getProduct().item).MetaGroup != 1)
    {
      return 0;
    }
    if(default_te < 0)
    {
      SharedPreferences sp = EICApplication.getContext().getSharedPreferences("EIC", Context.MODE_PRIVATE);
      default_te = sp.getInt("blueprint.default.te", 0);
    }
    return default_te;
  }

  @Override
  public int getDefaultBlueprintTE(IBlueprint bp)
  {
    return GetDefaultBlueprintTE(bp);
  }
  
  static public void SetDefaultBlueprintME(int me)
  {
    default_me = me;
    SharedPreferences sp = EICApplication.getContext().getSharedPreferences("EIC", Context.MODE_PRIVATE);
    SharedPreferences.Editor ed = sp.edit();
    ed.putInt("blueprint.default.me", me);
    ed.apply();
  }

  static public void SetDefaultBlueprintTE(int te)
  {
    default_te = te;
    SharedPreferences sp = EICApplication.getContext().getSharedPreferences("EIC", Context.MODE_PRIVATE);
    SharedPreferences.Editor ed = sp.edit();
    ed.putInt("blueprint.default.te", te);
    ed.apply();
  }

  @Override
  public IItem getItem(int id)
  {
    return InventoryDA.getItem(id);
  }

  @Override
  public IBlueprint getBlueprint(int id)
  {
    return BlueprintDA.getBlueprint(id);
  }

  @Override
  public IInstallationGroup getDefaultInstallation(IBlueprint blueprint)
  {
    List<InstallationGroup> groups = InstallationDA.getBlueprintInstallations(blueprint);
    for(InstallationGroup ig:groups)
    {
      if(ig.Installation == 6)
      {
        return ig;
      }
    }
    return groups.get(0);
  }

  @Override
  public IInstallationGroup getInstallationGroup(int id)
  {
    return InstallationDA.getInstallationGroup(id);
  }

  @Override
  public IInventionInstallation getInventionInstallation(int id)
  {
    return InstallationDA.getInventionInstallation(id);
  }

  @Override
  public IInventionInstallation getDefaultInventionInstallation(IBlueprint blueprint)
  {
    return InstallationDA.getInventionInstallation(blueprint.getInvention().usesRelics() ? 158 : 38);
  }

  @Override
  public IDecryptor getDecryptor(int id)
  {
    return DecryptorDA.GetDecryptor(id);
  }

  @Override
  public IPlanet getPlanet(int id)
  {
    return PlanetDA.getPlanet(id);
  }

  @Override
  public IPlanetBuilding getPlanetBuilding(int id)
  {
    return PlanetProductDA.GetProduct(id);
  }

  @Override
  public IPlanetBuilding getPlanetBuilding(IItem product)
  {
    return PlanetProductDA.GetProduct(product.getID());
  }

  @Override
  public IReaction getReaction(int pid)
  {
    return ReactionDA.GetReaction(pid);
  }

  @Override
  public IRefinable getRefinable(int id)
  {
    return RefineDA.GetRefine(id);
  }

  @Override
  public IStarbaseTower getStarbaseTower(int id)
  {
    return StarbaseTowerDA.GetTower(id);
  }

  @Override
  public BigDecimal getItemBaseCost(IItem item)
  {
    return BaseCostDA.GetCost(item);
  }

  @Override
  public ISolarSystemIndustryCost getSolarSystemIndustryCost(int system_id)
  {
    return SystemCostDA.GetCost(system_id);
  }

  @Override
  public BigDecimal getMarketPrice(IItem item, Market market)
  {
    return MarketData.GetMarketPrice(item, market);
  }

}
