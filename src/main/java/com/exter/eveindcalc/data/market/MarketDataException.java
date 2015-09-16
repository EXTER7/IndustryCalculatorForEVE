package com.exter.eveindcalc.data.market;

public class MarketDataException extends Exception
{
  boolean critical;
  /**
   * 
   */
  private static final long serialVersionUID = 5753979149571396618L;

  public MarketDataException(boolean crit)
  {
    critical = crit;
  }
  
  public boolean Critical()
  {
    return critical;
  }
}