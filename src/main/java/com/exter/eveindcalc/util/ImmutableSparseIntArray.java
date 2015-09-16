package com.exter.eveindcalc.util;

import android.util.SparseIntArray;

public class ImmutableSparseIntArray extends SparseIntArray
{
  private final SparseIntArray target;

  public ImmutableSparseIntArray(SparseIntArray sa)
  {
    target = sa;
  }

  @Override
  public void append(int key, int value)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(int key)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void put(int key, int value)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeAt(int index)
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public int get(int key)
  {
    return target.get(key);
  }

  @Override
  public int get(int key, int valueIfKeyNotFound)
  {
    return target.get(key, valueIfKeyNotFound);
  }

  @Override
  public int indexOfKey(int key)
  {
    return target.indexOfKey(key);
  }

  @Override
  public int indexOfValue(int value)
  {
    return target.indexOfValue(value);
  }

  @Override
  public int keyAt(int index)
  {
    return target.keyAt(index);
  }

  @Override
  public int size()
  {
    return target.size();
  }

  @Override
  public String toString()
  {
    return target.toString();
  }

  @Override
  public int valueAt(int index)
  {
    return target.valueAt(index);
  }

  @Override
  public boolean equals(Object o)
  {
    return target.equals(o);
  }

  @Override
  public int hashCode()
  {
    return target.hashCode();
  }
}
