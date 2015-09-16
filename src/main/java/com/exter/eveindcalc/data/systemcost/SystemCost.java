package com.exter.eveindcalc.data.systemcost;

import exter.eveindustry.data.systemcost.ISolarSystemIndustryCost;


public class SystemCost implements ISolarSystemIndustryCost
{
  public final int System;
  public final double Manufacturing;
  public final double Invention;
  
  public SystemCost(int sys,double man,double inv)
  {
    System = sys;
    Manufacturing = man;
    Invention = inv;
  }

  @Override
  public double getManufacturingCost()
  {
    return Manufacturing;
  }

  @Override
  public double getInventionCost()
  {
    return Invention;
  }
}
