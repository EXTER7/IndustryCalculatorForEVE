package com.exter.cache;

import java.util.HashMap;
import java.util.Map;

public class LFUCache<K,V> extends Cache<K,V>
{
  static private class Entry<T>
  {
    public final T value;
    public int used;
    public long time;
    
    public Entry(T v,long t)
    {
      value = v;
      used = 1;
      time = t;
    }
  }
  
  private Map<K,Entry<V>> cache;
  
  private final int max;

  public LFUCache(int size,IMissListener<K, V> miss_listener)
  {
    super(miss_listener);
    cache = new HashMap<>();
    max = size;
  }

  @Override
  public synchronized V get(K key)
  {
    Entry<V> e = cache.get(key);
    if(e != null)
    {
      e.used++;
      return e.value;
    }
    V value = miss.onCacheMiss(key);
    if( value != null)
    {
      if(cache.size() == max)
      {
        Map.Entry<K, Entry<V>> toremove = null;
        for(Map.Entry<K, Entry<V>> en:cache.entrySet())
        {
          Entry<V> ven = en.getValue();
          Entry<V> vtr = (toremove != null)?toremove.getValue():null;
          if(vtr == null || ven.used < vtr.used)
          {
            toremove = en;
          } else if(ven.used == vtr.used && ven.time > vtr.time)
          {
            toremove = en;
          }
        }
        if(toremove != null)
        {
          cache.remove(toremove.getKey());
        }
      }      
      cache.put(key, new Entry<>(value,System.currentTimeMillis()));
    }
    return value;
  }

  @Override
  public synchronized void flushAll()
  {
    cache.clear();
  }

  @Override
  public synchronized void flush(K key)
  {
    cache.remove(key);
  }
}
