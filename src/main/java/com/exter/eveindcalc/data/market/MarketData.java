package com.exter.eveindcalc.data.market;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import exter.eveindustry.data.item.Item;
import exter.eveindustry.market.Market;


public class MarketData
{
  private static class CacheKey
  {
    @Override
    public int hashCode()
    {
      int result = 1;
      result = result * 83 + EntryItem;
      result = result * 83 + EntrySystem;
      return result;
    }

    @Override
    public boolean equals(Object obj)
    {
      if (this == obj)
      {
        return true;
      }
      if (obj == null)
      {
        return false;
      }
      if (!(obj instanceof CacheKey))
      {
        return false;
      }
      CacheKey other = (CacheKey) obj;
      return EntryItem == other.EntryItem && EntrySystem == other.EntrySystem;
    }

    final int EntryItem;
    final int EntrySystem;

    CacheKey(int item, int system)
    {
      EntryItem = item;
      EntrySystem = system;
    }
  }

  public static class PriceValue
  {
    final long Expire;
    public final BigDecimal BuyPrice;
    public final BigDecimal SellPrice;

    PriceValue(long expire, BigDecimal buy, BigDecimal sell)
    {
      Expire = expire;
      BuyPrice = buy;
      SellPrice = sell;
    }
    boolean isExpired()
    {
      return System.currentTimeMillis() / 1000L > Expire;
    }
  }
  

  
  public final List<Pair<Integer,Integer>> request_prices = new ArrayList<>();

  private HashMap<CacheKey, PriceValue> cache;

  private SQLiteDatabase db;

  public MarketData(SQLiteDatabase db)
  {
    this.db = db;
  }
  
  private PriceValue getPriceFromCache(int item, int system)
  {
    synchronized(MarketData.class)
    {
      if(cache == null)
      {
        cache = new HashMap<>();
      }
      return cache.get(new CacheKey(item, system));
    }
  }

  private void putPriceInCache(int item, int system, PriceValue value)
  {
    synchronized(MarketData.class)
    {
      if(cache == null)
      {
        cache = new HashMap<>();
      }
      cache.put(new CacheKey(item, system), value);
    }
  }

  private void putPriceInCache(int item, int system, long expire, BigDecimal buy, BigDecimal sell)
  {
    putPriceInCache(item, system, new PriceValue(expire, buy, sell));
  }

  public PriceValue getLocalPrice(int item, int system)
  {
    if(db.isOpen())
    {
      PriceValue value = getPriceFromCache(item, system);
      if(value == null)
      {
        value = getLocalPriceFromDatabase(item, system);
      }
    
      return value;
    }
    return null;
  }

  static final private String[] QUERY_COLUMNS = { "buy", "sell", "time" };

  private PriceValue getLocalPriceFromDatabase(int item, int system)
  {
    PriceValue value = null;
    final String[] query_args = { String.valueOf(item), String.valueOf(system) };
    Cursor c = db.query("market_cache", QUERY_COLUMNS, "item=? AND system=?", query_args, null, null, null);
    if(c != null)
    {
      if(c.getCount() > 0)
      {
        c.moveToNext();
        value = new PriceValue(c.getLong(2), new BigDecimal(c.getString(0)), new BigDecimal(c.getString(1)));
        putPriceInCache(item, system, value);
      }
      c.close();
    }
    return value;
  }

  public boolean hasLocalPrice(int item, int system)
  {
    if(db.isOpen())
    {
      PriceValue value = getPriceFromCache(item, system);
      if(value == null)
      {
        value = getLocalPriceFromDatabase(item, system);
      }
      return value != null && !value.isExpired();
    } else
    {
      return false;
    }
  }

  public void setLocalPriceFromCache(int item, int system, BigDecimal buy, BigDecimal sell)
  {
    if(db.isOpen())
    {
      int id = -1;
      Cursor c = db.rawQuery("SELECT id FROM market_cache WHERE item=" + String.valueOf(item) + " AND system=" + String.valueOf(system) + ";", null);
      if(c != null)
      {
        if(c.getCount() > 0)
        {
          c.moveToNext();
          id = c.getInt(0);
        }
        c.close();
      }
      long expire = System.currentTimeMillis() / 1000L + (60 * 20);

      // Log.i("EIC","Writing cache: " + String.valueOf(expire));
      if(id == -1)
      {
        db.execSQL("replace into market_cache (item,time,system,buy,sell) values (" + String.valueOf(item) + "," + String.valueOf(expire) + "," + String.valueOf(system) + "," + buy.toPlainString() + "," + sell.toPlainString() + ");");
      } else
      {
        db.execSQL("update market_cache set" + " item = " + String.valueOf(item) + "," + " time = " + String.valueOf(expire) + "," + " system = " + String.valueOf(system) + "," + " buy = " + buy.toPlainString() + "," + " sell = " + sell.toPlainString() + " where id = " + String.valueOf(id) + ";");
      }
      putPriceInCache(item, system, expire, buy, sell);
    }
  }

  public BigDecimal getMarketPrice(Item item, Market market)
  {
    PriceValue pv;
    switch(market.order)
    {
      case BUY:
        if(!item.marketable)
        {
          return BigDecimal.ZERO;
        }
        pv = getLocalPrice(item.id, market.system);
        if(pv == null)
        {
          synchronized(request_prices)
          {
            request_prices.add(new Pair<>(item.id,market.system));
          }
          return BigDecimal.ZERO;
        }
        if(pv.isExpired())
        {
          synchronized(request_prices)
          {
            request_prices.add(new Pair<>(item.id,market.system));
          }
        }
        return pv.BuyPrice;
      case SELL:
        if(!item.marketable)
        {
          return BigDecimal.ZERO;
        }
        pv = getLocalPrice(item.id, market.system);
        if(pv == null)
        {
          synchronized(request_prices)
          {
            request_prices.add(new Pair<>(item.id,market.system));
          }
          return BigDecimal.ZERO;
        }
        if(pv.isExpired())
        {
          synchronized(request_prices)
          {
            request_prices.add(new Pair<>(item.id,market.system));
          }
        }
        return pv.SellPrice;
      case MANUAL:
        return market.manual;
    }
    return null;
  }
}
