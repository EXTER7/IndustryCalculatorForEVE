package com.exter.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InfiniteCache<K,V> extends Cache<K,V>
{
  private Map<K,V> cache;
  private Set<K> nulls;
  
  public InfiniteCache(IMissListener<K,V> miss_listener)
  {
    super(miss_listener);
    cache = new HashMap<>();
    nulls = new HashSet<>();
  }

  @Override
  public synchronized V get(K key)
  {
    if(nulls.contains(key))
    {
      return null;
    }
    V value = cache.get(key);
    if(value == null)
    {
      value = miss.onCacheMiss(key);
    }
    if(value != null)
    {
      cache.put(key, value);
    } else
    {
      nulls.add(key);
    }
    return value;
  }

  @Override
  public synchronized void flushAll()
  {
    cache.clear();
    nulls.clear();
  }

  @Override
  public synchronized void flush(K key)
  {
    cache.remove(key);
    nulls.remove(key);
  }
}
