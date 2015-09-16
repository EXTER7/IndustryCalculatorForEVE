package com.exter.eveindcalc.util;

import android.annotation.SuppressLint;
import android.util.SparseArray;

public class ImmutableSparseArray<E> extends SparseArray<E>
{
  private final SparseArray<E> target;

  public ImmutableSparseArray(SparseArray<E> sa)
  {
    target = sa;
  }

  @Override
  public void append(int key, E value)
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
  public void put(int key, E value)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void remove(int key)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeAt(int index)
  {
    throw new UnsupportedOperationException();
  }

  @SuppressLint("Override")
  public void removeAtRange(int index, int size)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setValueAt(int index, E value)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public E get(int key)
  {
    return target.get(key);
  }

  @Override
  public E get(int key, E valueIfKeyNotFound)
  {
    return target.get(key, valueIfKeyNotFound);
  }

  @Override
  public int indexOfKey(int key)
  {
    return target.indexOfKey(key);
  }

  @Override
  public int indexOfValue(E value)
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
  public E valueAt(int index)
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
