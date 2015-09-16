package com.exter.eveindcalc.data.starbase;

import com.exter.eveindcalc.data.exception.EveDataException;
import com.exter.eveindcalc.data.inventory.InventoryDA;
import com.exter.eveindcalc.data.inventory.Item;

import exter.eveindustry.data.reaction.IStarbaseTower;
import exter.eveindustry.item.ItemStack;
import exter.tsl.TSLObject;

public class StarbaseTower implements IStarbaseTower
{
  public final Item TowerItem;
  public final ItemStack RequiredFuel;
  public final String Name;

  public StarbaseTower(TSLObject tsl) throws EveDataException
  {
    if(tsl == null)
    {
      throw new EveDataException();
    }
    TowerItem = InventoryDA.getItem(tsl.getStringAsInt("id", -1));
    RequiredFuel = new ItemStack( InventoryDA.getItem(tsl.getStringAsInt("fuel_id", -1)),tsl.getStringAsInt("fuel_amount",-1));
    Name = tsl.getString("name",null);
    if(TowerItem == null || Name == null)
    {
      throw new EveDataException();
    }
  }

  @Override
  public int getID()
  {
    return TowerItem.getID();
  }

  @Override
  public ItemStack getRequiredFuel()
  {
    return RequiredFuel;
  }
}
