package com.exter.eveindcalc.data.blueprint;

import exter.eveindustry.data.blueprint.IInstallationGroup;

public class InstallationGroup implements IInstallationGroup
{
  public final int ID;
  public final int Group;
  public final int Installation;
  public final double Time;
  public final double Material;
  public final double Cost;
  
  public InstallationGroup(int i,int g,int in,float t,float m,float c)
  {
    ID = i;
    Group = g;
    Installation = in;
    Time = t;
    Material = m;
    Cost = c;
  }

  @Override
  public int hashCode()
  {
    return Group * 16553 + Installation;
  }

  @Override
  public boolean equals(Object obj)
  {
    if(obj == null)
    {
      return false;
    }
    if(this == obj)
    {
      return true;
    }
    if(getClass() != obj.getClass())
    {
      return false;
    }
    InstallationGroup other = (InstallationGroup) obj;
    return !(Group != other.Group || Installation != other.Installation);
  }

  @Override
  public int getID()
  {
    return ID;
  }

  @Override
  public int getGroupID()
  {
    return Group;
  }

  @Override
  public double getTimeBonus()
  {
    return Time;
  }

  @Override
  public double getMaterialBonus()
  {
    return Material;
  }

  @Override
  public double getCostBonus()
  {
    return Cost;
  }
}
