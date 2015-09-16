package com.exter.eveindcalc.data.decryptor;

import com.exter.eveindcalc.data.exception.EveDataException;
import com.exter.eveindcalc.data.inventory.InventoryDA;
import com.exter.eveindcalc.data.inventory.Item;

import exter.eveindustry.data.decryptor.IDecryptor;
import exter.eveindustry.data.inventory.IItem;
import exter.tsl.TSLObject;

public class Decryptor implements IDecryptor
{
  public final Item Item;
  public final int ME;
  public final int TE;
  public final int Runs;
  public final double Chance;

  public Decryptor(TSLObject tsl) throws EveDataException
  {
    if(tsl == null)
    {
      throw new EveDataException();
    }
    Item = InventoryDA.getItem(tsl.getStringAsInt("id", -1));
    ME = tsl.getStringAsInt("me",0);
    TE = tsl.getStringAsInt("te",0);
    Runs = tsl.getStringAsInt("runs",0);
    Chance = tsl.getStringAsDouble("chance",1);
    if(Item == null)
    {
      throw new EveDataException();
    }
  }

  @Override
  public int getID()
  {
    return Item.ID;
  }

  @Override
  public IItem getItem()
  {
    return Item;
  }

  @Override
  public int getModifierME()
  {
    return ME;
  }

  @Override
  public int getModifierTE()
  {
    return TE;
  }

  @Override
  public int getModifierRuns()
  {
    return Runs;
  }

  @Override
  public double getModifierChance()
  {
    return Chance;
  }
}
