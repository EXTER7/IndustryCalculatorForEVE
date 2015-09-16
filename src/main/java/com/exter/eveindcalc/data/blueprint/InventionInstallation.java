package com.exter.eveindcalc.data.blueprint;

import exter.eveindustry.data.blueprint.IInventionInstallation;

public class InventionInstallation implements IInventionInstallation
{
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(Cost);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ID;
    result = prime * result + ((Name == null) ? 0 : Name.hashCode());
    temp = Double.doubleToLongBits(Time);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if(this == obj)
    {
      return true;
    }
    if(obj == null)
    {
      return false;
    }
    if(getClass() != obj.getClass())
      return false;
    InventionInstallation other = (InventionInstallation) obj;
    if(Double.doubleToLongBits(Cost) != Double.doubleToLongBits(other.Cost))
    {
      return false;
    }
    if(ID != other.ID)
    {
      return false;
    }
    if(Name == null)
    {
      if(other.Name != null)
        return false;
    } else if(!Name.equals(other.Name))
    {
      return false;
    }
    return Double.doubleToLongBits(Time) == Double.doubleToLongBits(other.Time);
  }

  public final int ID;
  public final String Name;
  public final double Time;
  public final double Cost;
  public final boolean Relics;
  
  public InventionInstallation(int i, String n,double t,double c,boolean r)
  {
    Name = n;
    ID = i;
    Time = t;
    Cost = c;
    Relics = r;
  }

  @Override
  public int getID()
  {
    return ID;
  }

  @Override
  public double getTimeBonus()
  {
    return Time;
  }

  @Override
  public double getCostBonus()
  {
    return Cost;
  }

  @Override
  public boolean isForRelics()
  {
    return Relics;
  }
}
