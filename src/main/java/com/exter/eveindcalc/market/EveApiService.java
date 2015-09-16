package com.exter.eveindcalc.market;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Process;
import android.util.Log;

import com.exter.eveindcalc.EICApplication;
import com.exter.eveindcalc.data.basecost.BaseCostDA;
import com.exter.eveindcalc.data.systemcost.SystemCostDA;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class EveApiService extends IntentService
{
  static public final String BROADCAST_BASECOST_UPDATED = "com.exter.eveindcalc.response.BASECOST_UPDATED";
  static public final String BROADCAST_SYSTEMCOST_UPDATED = "com.exter.eveindcalc.response.SYSTEMCOST_UPDATED";

  public EveApiService()
  {
    super("EIC_ApiService");
  }

  private void fetchBaseCosts()
  {
    ConnectivityManager cm = (ConnectivityManager) EICApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
    if(activeNetwork == null || !activeNetwork.isConnectedOrConnecting())
    {
      return;
    }
    try
    {
      URL url = new URL("http://public-crest.eveonline.com/market/prices/");
      Log.i("eic-url", url.toString());
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestProperty("User-Agent","EVE_IndCalc3");
      InputStreamReader reader = new InputStreamReader(conn.getInputStream());
      JsonReader json = new JsonReader(reader);

      BaseCostDA.update(json);
      reader.close();
      
      Intent bin = new Intent();
      bin.setAction(BROADCAST_BASECOST_UPDATED);
      bin.addCategory(Intent.CATEGORY_DEFAULT);
      sendBroadcast(bin);
    } catch(IOException e)
    {
      BaseCostDA.retryUpdate(30);
      e.printStackTrace();
    }
  }


  private void fetchSystemCosts()
  {
    ConnectivityManager cm = (ConnectivityManager) EICApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
    if(activeNetwork == null || !activeNetwork.isConnectedOrConnecting())
    {
      return;
    }
    try
    {
      URL url = new URL("http://public-crest.eveonline.com/industry/systems/");
      Log.i("eic-url", url.toString());
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestProperty("User-Agent","EVE_IndCalc3");
      InputStreamReader reader = new InputStreamReader(conn.getInputStream());
      JsonReader json = new JsonReader(reader);

      SystemCostDA.update(json);
      reader.close();
      
      Intent bin = new Intent();
      bin.setAction(BROADCAST_SYSTEMCOST_UPDATED);
      bin.addCategory(Intent.CATEGORY_DEFAULT);
      sendBroadcast(bin);

    } catch(IOException e)
    {
      SystemCostDA.retryUpdate(30);
    }
  }

  static public void updateBaseCosts(Context ctx)
  {
    if(BaseCostDA.IsExpired())
    {
      Intent msg = new Intent(ctx, EveApiService.class);
      msg.putExtra("basecost", true);
      ctx.startService(msg);
    }
  }

  static public void updateSystemCosts(Context ctx)
  {
    if(SystemCostDA.IsExpired())
    {
      Intent msg = new Intent(ctx, EveApiService.class);
      msg.putExtra("systemcost", true);
      ctx.startService(msg);
    }
  }

  @Override
  protected void onHandleIntent(Intent intent)
  {
    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

    if(intent.getBooleanExtra("basecost",false))
    {
      fetchBaseCosts();
    } else if(intent.getBooleanExtra("systemcost",false))
    {
      fetchSystemCosts();
    }
  }
}
