package com.exter.eveindcalc.util;

import android.util.SparseArray;

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
  
  static public int DivCeil(int a,int b)
  {
    return a / b + ((a % b == 0) ? 0 : 1);
  }

  static public long DivCeil(long a,long b)
  {
    return a / b + ((a % b == 0) ? 0 : 1);
  }

  static public long DivCeil(long a,int b)
  {
    return a / b + ((a % b == 0) ? 0 : 1);
  }

  static public <T> SparseArray<T> CloneSparseArray(SparseArray<T> orig)
  {
    SparseArray<T> clone = new SparseArray<T>();
    
    int i;
    for(i = 0; i < orig.size(); i++)
    {
      clone.put(orig.keyAt(i), orig.valueAt(i));
    }
    return clone;
  }

  static public int Clamp(int v,int min,int max)
  {
    if(v < min)
    {
      return min;
    }
    if(v > max)
    {
      return max;
    }
    return v;
  }

  static public double Clamp(double v,double min,double max)
  {
    if(v < min)
    {
      return min;
    }
    if(v > max)
    {
      return max;
    }
    return v;
  }

  static public float Clamp(float v,float min,float max)
  {
    if(v < min)
    {
      return min;
    }
    if(v > max)
    {
      return max;
    }
    return v;
  }
}
