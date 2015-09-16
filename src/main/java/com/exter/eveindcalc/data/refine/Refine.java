package com.exter.eveindcalc.data.refine;

import com.exter.eveindcalc.data.exception.EveDataException;
import com.exter.eveindcalc.data.inventory.InventoryDA;
import com.exter.eveindcalc.data.inventory.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import exter.eveindustry.data.refinable.IRefinable;
import exter.eveindustry.item.ItemStack;
import exter.tsl.TSLObject;

public class Refine implements IRefinable
{
  public final List<ItemStack> Products;
  public final ItemStack RefineItem;
  public final int Skill;

  public Refine(TSLObject tsl) throws EveDataException
  {
    if(tsl == null)
    {
      throw new EveDataException();
    }
    ArrayList<ItemStack> product_list = new ArrayList<>();

    RefineItem = new ItemStack(InventoryDA.getItem(tsl.getStringAsInt("id", -1)),tsl.getStringAsInt("batch",-1));
    Skill = tsl.getStringAsInt("sid",-1);
    if(Skill < 0)
    {
      throw new EveDataException();
    }
    
    List<TSLObject> products_tsl = tsl.getObjectList("product");
    if(products_tsl == null)
    {
      throw new EveDataException();
    }
    for(TSLObject min:products_tsl)
    {
      Item min_id = InventoryDA.getItem(min.getStringAsInt("id", -1));
      int min_amount = min.getStringAsInt("amount",-1);
      if(min_id == null || min_amount < 0)
      {
        throw new EveDataException();
      }
      product_list.add(new ItemStack(min_id, min_amount));
    }
    Products = Collections.unmodifiableList(product_list);
  }

  @Override
  public List<ItemStack> getProducts()
  {
    return Products;
  }

  @Override
  public int getID()
  {
    return RefineItem.item.getID();
  }

  @Override
  public ItemStack getRequiredItem()
  {
    return RefineItem;
  }

  @Override
  public int getSkill()
  {
    return Skill;
  }
}
