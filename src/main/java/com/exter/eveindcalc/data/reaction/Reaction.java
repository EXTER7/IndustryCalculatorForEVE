package com.exter.eveindcalc.data.reaction;

import com.exter.eveindcalc.data.exception.EveDataException;
import com.exter.eveindcalc.data.inventory.InventoryDA;
import com.exter.eveindcalc.data.inventory.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import exter.eveindustry.data.reaction.IReaction;
import exter.eveindustry.item.ItemStack;
import exter.tsl.TSLObject;

public class Reaction implements IReaction
{
  public final List<ItemStack> Inputs;
  public final List<ItemStack> Outputs;
  public final int ID;

  public Reaction(TSLObject tsl) throws EveDataException
  {
    if(tsl == null)
    {
      throw new EveDataException();
    }
    ID = tsl.getStringAsInt("id",-1);
    if(ID < 0)
    {
      throw new EveDataException();
    }
    
    List<ItemStack> matlist = new ArrayList<ItemStack>();
    List<TSLObject> materials_tsl = tsl.getObjectList("in");
    if(materials_tsl == null)
    {
      throw new EveDataException();
    }
    for(TSLObject mat:materials_tsl)
    {
      Item mat_id = InventoryDA.getItem(mat.getStringAsInt("id", -1));
      int mat_amount = mat.getStringAsInt("amount",-1);
      if(mat_id == null || mat_amount < 0)
      {
        throw new EveDataException();
      }
      matlist.add(new ItemStack(mat_id,mat_amount));
    }
    Inputs = Collections.unmodifiableList(matlist);

    matlist = new ArrayList<ItemStack>();
    materials_tsl = tsl.getObjectList("out");
    if(materials_tsl == null)
    {
      throw new EveDataException();
    }
    for(TSLObject mat:materials_tsl)
    {
      Item mat_id = InventoryDA.getItem(mat.getStringAsInt("id", -1));
      int mat_amount = mat.getStringAsInt("amount",-1);
      if(mat_id == null || mat_amount < 0)
      {
        throw new EveDataException();
      }
      matlist.add(new ItemStack(mat_id,mat_amount));
    }
    Outputs = Collections.unmodifiableList(matlist);
  }

  @Override
  public List<ItemStack> getInputs()
  {
    return Inputs;
  }

  @Override
  public List<ItemStack> getOutputs()
  {
    return Outputs;
  }

  @Override
  public int getID()
  {
    return ID;
  }
  
  public long GetMainOutputAmount()
  {
    for(ItemStack m:Outputs)
    {
      if(m.item.getID() == ID)
      {
        return m.amount;
      }
    }
    return 0;
  }
}
