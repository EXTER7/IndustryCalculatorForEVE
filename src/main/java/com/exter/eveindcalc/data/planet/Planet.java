package com.exter.eveindcalc.data.planet;

import com.exter.eveindcalc.data.exception.EveDataException;
import com.exter.eveindcalc.data.inventory.InventoryDA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import exter.eveindustry.data.inventory.IItem;
import exter.eveindustry.data.planet.IPlanet;
import exter.tsl.TSLObject;

public class Planet implements IPlanet
{
  public final List<IItem> Resources;
  public final String TypeName;
  public final int ID;
  public final boolean Advanced;
  
  public Planet(TSLObject tsl) throws EveDataException
  {
    if(tsl == null)
    {
      throw new EveDataException();
    }
    ID = tsl.getStringAsInt("id",-1);
    TypeName = tsl.getString("name",null);
    Advanced = (tsl.getStringAsInt("advanced",0) != 0);
    if(ID < 0 || TypeName == null)
    {
      throw new EveDataException();
    }
    
    List<String> res_str = tsl.getStringList("resource");
    List<IItem> res = new ArrayList<>();
    for(String str:res_str)
    {
      try
      {
        res.add(InventoryDA.getItem(Integer.valueOf(str)));
      } catch(NumberFormatException e)
      {
        throw new EveDataException();
      }
    }
    Resources = Collections.unmodifiableList(res);
  }

  @Override
  public List<IItem> getResources()
  {
    return Resources;
  }

  @Override
  public int getID()
  {
    return ID;
  }

  @Override
  public boolean isAdvanced()
  {
    return Advanced;
  }
}
