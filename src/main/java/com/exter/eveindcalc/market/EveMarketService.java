package com.exter.eveindcalc.market;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;

import com.exter.eveindcalc.EICApplication;
import com.exter.eveindcalc.data.market.MarketData;
import com.exter.eveindcalc.data.market.MarketData.PriceValue;
import com.exter.eveindcalc.data.market.MarketDataException;
import com.exter.xml.XmlNode;
import com.exter.xml.XmlNodeException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EveMarketService extends IntentService
{
  static public final String BROADCAST_PROGRESS = "com.exter.eveindcalc.response.FETCH_PROGRESS";

  static private long retry = 0;
  
  static private class FetchResult
  {
    public final BigDecimal buy;
    public final BigDecimal sell;

    public FetchResult(BigDecimal b, BigDecimal s)
    {
      buy = b;
      sell = s;
    }
  }
  

  public EveMarketService()
  {
    super("EIC_MarketService");
  }


  static private BigDecimal getEveCentralOrderPrice(XmlNode xml_order, String type) throws MarketDataException
  {
    XmlNode xml_price = xml_order.FindSubNode(type);
    if(xml_price == null || xml_price.SubNodeCount() != 1)
    {
      Log.e("EIC", "EveCentral Lookup: expected '" + type + "' tag.");
      throw new MarketDataException(false);
    }

    XmlNode xml_price_value = xml_price.SubNode(0);

    BigDecimal pr;
    try
    {
      pr = new BigDecimal(xml_price_value.Text().trim());
      if(pr.compareTo(BigDecimal.ZERO) < 0)
      {
        Log.e("EIC", "EveCentral Lookup: '" + xml_price.Text().trim() + "' is not a valid number.");
        throw new MarketDataException(false);
      }
    } catch(NumberFormatException e)
    {
      Log.e("EIC", "EveCentral Lookup: '" + xml_price.Text().trim() + "' is not a valid number.");
      throw new MarketDataException(false);
    }
    return pr;
  }

  static private SparseArray<FetchResult> getPriceFromEveCentralXML(XmlNode xml) throws MarketDataException
  {
    SparseArray<FetchResult> result = new SparseArray<>();
    if(!xml.Name().equals("evec_api"))
    {
      Log.e("EIC", "EveCentral Lookup: expected 'evec_api' tag.");
      throw new MarketDataException(true);
    }
    XmlNode xml_marketstat = xml.FindSubNode("marketstat");
    if(xml_marketstat == null)
    {
      Log.e("EIC", "EveCentral Lookup: expected 'marketstat' tag.");
      throw new MarketDataException(true);
    }
    int i;
    for(i = 0; i < xml_marketstat.SubNodeCount(); i++)
    {
      XmlNode xml_type = xml_marketstat.SubNode(i);
      if(!xml_type.Name().equals("type"))
      {
        continue;
      }

      String itemid_str = xml_type.FindAttribute("id");
      if(itemid_str == null)
      {
        continue;
      }
      int itemid;
      try
      {
        itemid = Integer.valueOf(itemid_str);
      } catch(NumberFormatException e)
      {
        continue;
      }

      XmlNode xml_order_buy = xml_type.FindSubNode("buy");
      XmlNode xml_order_sell = xml_type.FindSubNode("sell");
      if(xml_order_buy == null || xml_order_sell == null)
      {
        Log.e("EIC", "EveCentral Lookup: expected 'buy/sell' tag.");
        continue;
      }
      BigDecimal buy_price = getEveCentralOrderPrice(xml_order_buy, "max");
      BigDecimal sell_price = getEveCentralOrderPrice(xml_order_sell, "min");
      result.put(itemid, new FetchResult(buy_price, sell_price));
    }
    return result;
  }


  
  private void fetchPrice(List<Integer> items, int system) throws MarketDataException
  {
    SparseArray<FetchResult> fetched = new SparseArray<>();
    ConnectivityManager cm = (ConnectivityManager) EICApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
    if(activeNetwork == null || !activeNetwork.isConnectedOrConnecting())
    {      
      retry = System.currentTimeMillis() + 1000 * 60 * 5;
      return;
    }
    try
    {
      String url_str = "http://api.eve-central.com/api/marketstat?typeid=";
      boolean rest = false;
      for(int item : items)
      {
        if(EICApplication.getDataProvider().getItem(item).Market && !MarketData.hasLocalPrice(item, system))
        {
          if (rest)
          {
            url_str += ",";
          }
          rest = true;
          url_str += String.valueOf(item);
        }
      }
      if(rest)
      {
        url_str += "&usesystem=" + String.valueOf(system);

        Log.i("eic-url", url_str);
        URL url = new URL(url_str);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "EVE_IndCalc3");
        InputStreamReader reader = new InputStreamReader(conn.getInputStream());
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(reader);
        XmlNode xml_evecapi = new XmlNode(parser);
        reader.close();
        fetched = getPriceFromEveCentralXML(xml_evecapi);
      }
    } catch(IOException | XmlNodeException | XmlPullParserException e)
    {
      e.printStackTrace();
    }
    for(int i:items)
    {
      FetchResult fr = fetched.get(i);
      if(fr == null)
      {
        PriceValue pv = MarketData.getLocalPrice(i, system);
        if(pv == null)
        {
          MarketData.setLocalPriceFromCache(i, system, BigDecimal.ZERO, BigDecimal.ZERO);
        } else
        {
          MarketData.setLocalPriceFromCache(i, system, pv.BuyPrice, pv.SellPrice);
        }
      } else
      {
        MarketData.setLocalPriceFromCache(i, system, fr.buy, fr.sell);
      }
    }
  }

  private void broadcastProgress(int progress, int max)
  {
    Intent bin = new Intent();
    bin.setAction(BROADCAST_PROGRESS);
    bin.addCategory(Intent.CATEGORY_DEFAULT);
    bin.putExtra("progress", progress);
    bin.putExtra("max", max);
    sendBroadcast(bin);
  }


  static public void requestMaterialPrices(Context ctx, List<Pair<Integer, Integer>> materials)
  {
    if(System.currentTimeMillis() < retry)
    {
      return;
    }
    Intent msg = new Intent(ctx, EveMarketService.class);
    int i;
    SparseArray<Set<Integer>> systems = new SparseArray<>();
    
    for(Pair<Integer,Integer> m:materials)
    {
      int sys = m.second;
      Set<Integer> items = systems.get(sys);
      if(items == null)
      {
        items = new HashSet<>();
        systems.put(sys, items);
      }
      items.add(m.first);
    }
    for(i = 0; i < systems.size(); i++)
    {
      Bundle b = new Bundle();
      b.putInt("system", systems.keyAt(i));
      b.putIntegerArrayList("items", new ArrayList<>(systems.valueAt(i)));
      msg.putExtra("system" + i, b);
    }
    msg.putExtra("size", systems.size());
    ctx.startService(msg);
  }
  
  @Override
  protected void onHandleIntent(Intent intent)
  {
    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
    // Log.i("eic", "Starting price fetch");
    int size = intent.getIntExtra("size", 0);
    int i;
    List<Integer> fetch = new ArrayList<>();
    
    int max = 0;
    for(i = 0; i < size; i++)
    {
      Bundle b = intent.getBundleExtra("system" + String.valueOf(i));
      ArrayList<Integer> it = b.getIntegerArrayList("items");
      assert it != null;
      max += it.size();
    }
    
    int progress = 0;
    broadcastProgress(0, max);
    
    for(i = 0; i < size; i++)
    {
      
      Bundle b = intent.getBundleExtra("system" + String.valueOf(i));
      int system = b.getInt("system", -1);
      List<Integer> items = b.getIntegerArrayList("items");
      
      int j;
      assert items != null;
      for(j = 0; j < items.size(); j++)
      {
        if(progress++ % 10 == 0)
        {
          broadcastProgress(progress, max);
        }
        fetch.add(items.get(j));
        if(j % 10 == 0)
        {
          try
          {
            fetchPrice(fetch, system);
          } catch(MarketDataException e)
          {
            e.printStackTrace();
            if(e.isCritical())
            {
              broadcastProgress(max, max);
              return;
            }
          }
          fetch.clear();
        }
      }
      if(fetch.size() > 0)
      {
        try
        {
          fetchPrice(fetch, system);
        } catch(MarketDataException e)
        {
          e.printStackTrace();
          if(e.isCritical())
          {
            broadcastProgress(max, max);
            retry = System.currentTimeMillis() + 1000 * 60 * 5;
            return;
          }
        }
      }
    }
    broadcastProgress(max, max);
  }
}
