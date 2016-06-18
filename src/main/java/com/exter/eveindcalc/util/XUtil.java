package com.exter.eveindcalc.util;


public final class XUtil
{
  static public String TimeToStr(int time)
  {
    String s = "";
    if(time % 60 != 0)
    {
      s = String.valueOf(time % 60) + "S";
    }
    time /= 60;
    if(time % 60 != 0)
    {
      s = String.valueOf(time % 60) + "M " + s;
    }
    time /= 60;
    if(time % 24 != 0)
    {
      s = String.valueOf(time % 24) + "H " + s;
    }
    time /= 24;
    if(time != 0)
    {
      s = String.valueOf(time) + "D " + s;
    }
    return s;
  }
  

  static public long divCeil(long a, long b)
  {
    return a / b + ((a % b == 0) ? 0 : 1);
  }
}
