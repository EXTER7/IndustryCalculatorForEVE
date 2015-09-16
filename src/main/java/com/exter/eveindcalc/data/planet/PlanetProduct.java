package com.exter.eveindcalc.data.planet;

import com.exter.eveindcalc.data.exception.EveDataException;
import com.exter.eveindcalc.data.inventory.InventoryDA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import exter.eveindustry.data.planet.IPlanetBuilding;
import exter.eveindustry.item.ItemStack;
import exter.tsl.TSLObject;

public class PlanetProduct implements IPlanetBuilding
{

  public final ItemStack ProductItem;
  public final int Tax;
  public final int Level;
  public final List<ItemStack> Materials;
 
  public PlanetProduct(TSLObject tsl) throws EveDataException
  {
    ArrayList<ItemStack> matlist = new ArrayList<>();
    if(tsl == null)
    {
      throw new EveDataException();
    }
    ProductItem = new ItemStack(InventoryDA.getItem(tsl.getStringAsInt("id", -1)),tsl.getStringAsInt("amount",-1));
    Level = tsl.getStringAsInt("level",-1);
    Tax = tsl.getStringAsInt("tax",-1);
    if(Level < 0 || Tax < 0)
    {
      throw new EveDataException();
    }
    List<TSLObject> tsl_materials = tsl.getObjectList("in");
    if(tsl_materials == null)
    {
      throw new EveDataException();
    }
    for(TSLObject mat_tsl:tsl_materials)
    {
      int mat_id = mat_tsl.getStringAsInt("id",-1);
      int raw_amount = mat_tsl.getStringAsInt("amount",0);
      if (mat_id < 0 || raw_amount == 0)
      {
        throw new EveDataException();
      }
      try
      {
        matlist.add(new ItemStack(InventoryDA.getItem(mat_id), raw_amount));
      } catch(NumberFormatException e)
      {
        throw new EveDataException();
      }
    }
    Materials = Collections.unmodifiableList(matlist);
  }

  @Override
  public ItemStack getProduct()
  {
    return ProductItem;
  }

  @Override
  public int getCustomsOfficeTax()
  {
    return Tax;
  }

  @Override
  public int getLevel()
  {
    return Level;
  }

  @Override
  public List<ItemStack> getMaterials()
  {
    return Materials;
  }

  @Override
  public int getID()
  {
    return ProductItem.item.getID();
  }
}
