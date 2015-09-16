package com.exter.cache;

public abstract class Cache<K,V>
{
  public interface IMissListener<K,V>
  {
    V onCacheMiss(K key);
  }
  
  protected final IMissListener<K,V> miss;
  
  public Cache(IMissListener<K,V> miss_listener)
  {
    miss = miss_listener;
  }
  
  public abstract void flushAll();

  public abstract void flush(K key);

  public abstract V get(K key);
}
