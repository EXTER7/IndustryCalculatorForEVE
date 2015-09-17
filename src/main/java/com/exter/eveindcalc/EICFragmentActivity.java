package com.exter.eveindcalc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.exter.eveindcalc.data.EveDatabase;
import com.exter.eveindcalc.data.inventory.InventoryDA;
import com.exter.eveindcalc.data.market.MarketData;
import com.exter.eveindcalc.group.GroupFragment;
import com.exter.eveindcalc.group.ImportTaskDialogFragment;
import com.exter.eveindcalc.manufacturing.ManufacturingFragment;
import com.exter.eveindcalc.market.EveApiService;
import com.exter.eveindcalc.market.EveMarketService;
import com.exter.eveindcalc.materials.MarketFetchDialogFragment;
import com.exter.eveindcalc.materials.MaterialsFragment;
import com.exter.eveindcalc.planet.PlanetFragment;
import com.exter.eveindcalc.reaction.ReactionFragment;
import com.exter.eveindcalc.refine.RefineFragment;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import exter.eveindustry.item.ItemStack;
import exter.eveindustry.task.GroupTask;
import exter.eveindustry.task.ManufacturingTask;
import exter.eveindustry.task.PlanetTask;
import exter.eveindustry.task.ReactionTask;
import exter.eveindustry.task.RefiningTask;
import exter.eveindustry.task.Task;
import exter.eveindustry.task.TaskLoadException;
import exter.tsl.InvalidTSLException;
import exter.tsl.TSLObject;
import exter.tsl.TSLReader;

public class EICFragmentActivity extends FragmentActivity
{
  private class TaskListener implements Task.ITaskListener
  {
    @Override
    public void onMaterialSetChanged(Task t)
    {
      notifyMaterialSetChanged();
      onProfitChanged();
    }

    @Override
    public void onParameterChanged(Task task, int parameter)
    {
      notifyTaskParameterChanged(parameter);
    }
  }

  private class ResponseReceiver extends BroadcastReceiver
  {
    @Override
    public void onReceive(Context context, Intent intent)
    {
      int progress = intent.getIntExtra("progress", 0);
      int max = intent.getIntExtra("max", 1);
            
      if(progress == max && getCurrentTask() != null)
      {
        ly_market_fetch.setVisibility(View.GONE);
        notifyPricesChanged();
        onProfitChanged();
      } else
      {
        ly_market_fetch.setVisibility(View.VISIBLE);
        pb_market_fetch.setMax(max);
        pb_market_fetch.setProgress(progress);
      }
    }
  }

  private class BaseCostResponseReceiver extends BroadcastReceiver
  {
    @Override
    public void onReceive(Context context, Intent intent)
    {
      if(getCurrentTask() != null)
      {
        notiftyExtraExpenseChanged();
        onProfitChanged();
      }
    }
  }

  private class PagerAdapter extends FragmentStatePagerAdapter
  {
    public final IEveCalculatorFragment[] fragments;

    public PagerAdapter(FragmentManager fm)
    {
      super(fm);
      fragments = new IEveCalculatorFragment[2];
    }

    @Override
    public Fragment getItem(int pos)
    {
      Fragment fragment = null;
      switch(pos)
      {
        case 0:
          try
          {
            fragment = task_fragments.get(getCurrentTask().getClass()).getConstructor().newInstance();
          } catch(IllegalArgumentException e)
          {
            throw e;
          } catch(Exception e)
          {
            throw new RuntimeException(e);
          }
          break;
        case 1:
          fragment = new MaterialsFragment();
          break;

      }
      fragments[pos] = (IEveCalculatorFragment) fragment;
      return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object)
    {
      super.destroyItem(container, position, object);
      fragments[position] = null;
    }

    @Override
    public final int getCount()
    {
      return getCurrentTask() == null?0:2;
    }

    @Override
    public final CharSequence getPageTitle(int position)
    {
      String title = "object";
      switch(position)
      {
        case 0:
          title = task_names.get(getCurrentTask().getClass());
          break;
        case 1:
          title = "Materials";
      }
      return title;
    }
  }

  //Pager adapter for tablets
  private class LargePagerAdapter extends PagerAdapter
  {
    public LargePagerAdapter(FragmentManager fm)
    {
      super(fm);
    }

    @Override
    public Fragment getItem(int pos)
    {

      if(fragments[pos] != null)
      {
        return (Fragment) fragments[pos];
      }
      return super.getItem(pos);
    }
  }

  static private class LoaderHandler extends Handler
  {
    private WeakReference<EICFragmentActivity> activity;

    public LoaderHandler(EICFragmentActivity act)
    {
      activity = new WeakReference<>(act);
    }

    @Override
    public void handleMessage(Message msg)
    {
      EICFragmentActivity act = activity.get();
      if(act == null)
      {
        return;
      }
      switch(msg.what)
      {
        case 0:
          act.progress_dialog.setMessage("Updating database");
          break;
        case 1:
          act.progress_dialog.setMessage("Loading Tasks");
          EveApiService.updateBaseCosts(act);
          EveApiService.updateSystemCosts(act);
          break;
        case 2:
          act.onTaskLoaded();
          act.progress_dialog.dismiss();
          break;
        case 3:
          Toast.makeText(act, "Task imported succesfully.", Toast.LENGTH_SHORT).show();
          break;
        case 4:
          Toast.makeText(act, "Error inporting task.", Toast.LENGTH_SHORT).show();
          break;
      }
    }
  }

  private class LoaderThread implements Runnable
  {
    private boolean importTask(Uri data)
    {
      ContentResolver cr = getContentResolver();
      try
      {
        Log.i("importTask", data.toString());
        InputStream is = cr.openInputStream(data);
        if(is == null)
        {
          return false;
        }

        TSLReader reader = new TSLReader(is);
        reader.moveNext();
        if(reader.getState() != TSLReader.State.OBJECT || !reader.getName().equals("task"))
        {
          Log.e("importTask", "Not a task collection");
          return false;
        }

        TSLObject tsl = new TSLObject(reader);
        is.close();

        String name = tsl.getString("name", null);
        if(name == null)
        {
          Log.e("importTask", "name is null");
          return false;
        }
        Task t = null;
        try
        {
          t = Task.loadPromTSL(tsl);
        } catch(TaskLoadException e)
        {
          e.printStackTrace();
        }
        if(t == null)
        {
          Log.e("importTask", "task is null");
          return false;
        }

        EICApplication.getTasks().addTask(name, t);
        return true;
      } catch(IOException | InvalidTSLException ignored)
      {

      }
      return false;
    }

    //Loads the tasks from storage
    @Override
    public void run()
    {
      load_handler.sendEmptyMessage(0);
      EICDatabaseHelper helper = new EICDatabaseHelper(getApplicationContext());
      SQLiteDatabase db = helper.getWritableDatabase();
      db.close();
      helper.close();

      load_handler.sendEmptyMessage(1);
      EICApplication.loadTasks();

      Uri data = getIntent().getData();
      if(data == null)
      {
        Bundle extras = getIntent().getExtras();
        if(extras != null)
        {
          data = (Uri)extras.get(Intent.EXTRA_STREAM);
        }
      }
      if(data != null)
      {
        if(importTask(data))
        {
          load_handler.sendEmptyMessage(3);
        } else
        {
          load_handler.sendEmptyMessage(4);
        }
      }
      load_handler.sendEmptyMessage(2);
    }
  }

  private Stack<Pair<String, Task>> task_path;
  protected GroupTask parent;
  private ResponseReceiver receiver;
  private BaseCostResponseReceiver receiver_basecost;

  private TextView tx_profit;

  private AdView ads;
  private ViewPager pager;
  private LinearLayout ly_fragment_main;
  private TextView tx_fragment_main_title;
  private LinearLayout ly_fragment_material;
  private PagerAdapter pager_adapter;
  static private Map<Class<? extends Task>, Class<? extends Fragment>> task_fragments;
  static private Map<Class<? extends Task>, String> task_names;
  private ProgressDialog progress_dialog;
  private LoaderHandler load_handler;
  private List<String> path;
  private ProgressBar pb_market_fetch;
  private LinearLayout ly_market_fetch;

  // Get the current task.
  public Task getCurrentTask()
  {
    if(task_path == null)
    {
      return null;
    }
    if(task_path.isEmpty())
    {
      return EICApplication.getTasks();
    }
    return task_path.peek().second;
  }

  // Get the group in which the current task belongs. return null if the current task is the root group.
  public GroupTask getCurrentTaskParentGroup()
  {
    if(task_path == null)
    {
      return null;
    }
    if(task_path.isEmpty())
    {
      return EICApplication.getTasks();
    }
    Pair<String,Task> top = task_path.pop();
    GroupTask result = (GroupTask) getCurrentTask();
    task_path.push(top);
    return result;
  }

  public class EveCalculatorMarketFetchAcceptListener implements MarketFetchDialogFragment.MarketFetchAcceptListener
  {
    @Override
    public void onAcceptItem(int item, Task.Market p)
    {
      getCurrentTask().setMaterialMarket(InventoryDA.getItem(item), p);
      notifyMaterialChanged(item);
      onProfitChanged();
    }

    @Override
    public void onAcceptRequired(Task.Market price)
    {
      Task task = getCurrentTask();
      for(ItemStack m:task.getRequiredMaterials())
      {
        task.setMaterialMarket(m.item, price);
        notifyMaterialChanged(m.item.getID());
      }
      onProfitChanged();
    }

    @Override
    public void onAcceptProduced(Task.Market price)
    {
      Task task = getCurrentTask();
      for(ItemStack m:task.getProducedMaterials())
      {
        task.setMaterialMarket(m.item, price);
        notifyMaterialChanged(m.item.getID());
      }
      onProfitChanged();
    }
  }

  private void onTaskLoaded()
  {
    task_path = new Stack<>();
    if(path != null)
    {
      for(String name : path)
      {
        GroupTask group = (GroupTask) getCurrentTask();
        task_path.push(new Pair<>(name, group.getTask(name)));
      }
    }
    getCurrentTask().registerListener(listener);
    onTaskChanged();
  }

  @Override
  protected final void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setResult(RESULT_OK);
    setContentView(R.layout.task_main);

    if(task_fragments == null)
    {
      task_fragments = new HashMap<>();
      task_fragments.put(ManufacturingTask.class, ManufacturingFragment.class);
      task_fragments.put(RefiningTask.class, RefineFragment.class);
      task_fragments.put(ReactionTask.class, ReactionFragment.class);
      task_fragments.put(PlanetTask.class, PlanetFragment.class);
      task_fragments.put(GroupTask.class, GroupFragment.class);
    }

    if(task_names == null)
    {
      task_names = new HashMap<>();
      task_names.put(ManufacturingTask.class, "Manufacturing");
      task_names.put(RefiningTask.class, "Refining");
      task_names.put(ReactionTask.class, "Reactions");
      task_names.put(PlanetTask.class, "Planet");
      task_names.put(GroupTask.class, "Tasks");
    }

    tx_profit = (TextView) findViewById(R.id.tx_profit);
    ads = (AdView) findViewById(R.id.advertisement);
    pager = (ViewPager) findViewById(R.id.pager);
    if(pager == null)
    {
      ly_fragment_main = (LinearLayout) findViewById(R.id.ly_task_fragment_main);
      ly_fragment_material = (LinearLayout) findViewById(R.id.ly_task_fragment_material);
      tx_fragment_main_title = (TextView) findViewById(R.id.tx_task_fragment_main_title);
    } else
    {
      pager.setPageMargin(2);
      pager.setPageMarginDrawable(new ColorDrawable(0xFFAAAAAA));
      pager_adapter = new PagerAdapter(getSupportFragmentManager());
      pager.setAdapter(pager_adapter);
    }
    
    pb_market_fetch = (ProgressBar) findViewById(R.id.pb_marketprogress);
    ly_market_fetch = (LinearLayout) findViewById(R.id.ly_progress);
    ly_market_fetch.setVisibility(View.GONE);
    if(savedInstanceState != null)
    {
      path = savedInstanceState.getStringArrayList("path");
    } else
    {
      path = null;
    }
    
    listener = new TaskListener();

    // Load task in a different thread while showing a progress dialog indicating that the tasks are loading.
    progress_dialog = ProgressDialog.show(this, "Loading...", "Updating database", true, false);
    load_handler = new LoaderHandler(this);
    Thread thread = new Thread(new LoaderThread());
    thread.start();

    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    AdRequest.Builder request = new AdRequest.Builder();
    request.setBirthday(Date.valueOf("1988-01-01"));
    request.setGender(AdRequest.GENDER_MALE);
    // request.setKeywords(new HashSet<String>(Arrays.asList(new
    // String[]{"gaming","games","eve online","industry","trade"})));
    ads.loadAd(request.build());

  }

  protected TaskListener listener;

  public Task.ITaskListener GetListener()
  {
    return listener;
  }


  @Override
  public boolean onPrepareOptionsMenu(Menu menu)
  {
    MenuInflater inflater = getMenuInflater();
    menu.clear();
    if(getCurrentTask() instanceof GroupTask)
    {
      inflater.inflate(R.menu.menu_group, menu);
    } else
    {
      inflater.inflate(R.menu.menu_main, menu);
    }
    return true;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data)
  {
    super.onActivityResult(requestCode, resultCode, data);
    if(resultCode == Activity.RESULT_OK && requestCode == 42)
    {
      ContentResolver cr = getContentResolver();
      try
      {
        InputStream is = cr.openInputStream(data.getData());

        TSLReader reader = new TSLReader(is);
        reader.moveNext();
        if(reader.getState() != TSLReader.State.OBJECT || !reader.getName().equals("task"))
        {
          Log.e("importTask", "Not a task collection");
          return;
        }

        TSLObject tsl = new TSLObject(reader);
        is.close();

        String name = tsl.getString("name", null);
        if(name == null)
        {
          Log.e("importTask", "name is null");
          return;
        }
        Task t = null;
        try
        {
          t = Task.loadPromTSL(tsl);
        } catch(TaskLoadException e)
        {
          e.printStackTrace();
        }
        if(t == null)
        {
          Log.e("importTask", "task is null");
          return;
        }
        GroupTask group = (GroupTask) getCurrentTask();
        group.addTask(name, t);
      } catch(InvalidTSLException | IOException ignored)
      {

      }
    }
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch(item.getItemId())
    {
      case R.id.menu_import:
        if(EICApplication.isChrome())
        {
          Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
          intent.setType("text/eic");
          startActivityForResult(intent, 42);
        } else
        {
          ImportTaskDialogFragment dialog = new ImportTaskDialogFragment();
          dialog.show(getSupportFragmentManager(), "ImportTaskDialogFragment");
        }
        return true;
      case R.id.menu_about:
        startActivity(new Intent(EICFragmentActivity.this, AboutActivity.class));
        return true;
      case R.id.menu_addtask:
        ((GroupFragment)pager_adapter.fragments[0]).addTask();
        return true;
      case R.id.menu_addgroup:
        ((GroupFragment)pager_adapter.fragments[0]).addGroup();
        return true;
      case R.id.menu_settings:
        SettingsDialogFragment sdialog = new SettingsDialogFragment();
        sdialog.show(getSupportFragmentManager(), "SetingsDialogFragment");
      default:
        return super.onOptionsItemSelected(item);
    }
  }
  
  @Override
  public void onDestroy()
  {
    ads.destroy();
    Task t = getCurrentTask();
    if(t != null)
    {
      t.unregisterListener(listener);
    }
    listener = null;
    EICApplication.saveTasks();
    EveDatabase.closeDatabase();
    super.onDestroy();
  }

  @Override
  protected void onPause()
  {
    unregisterReceiver(receiver);
    unregisterReceiver(receiver_basecost);
    Task t = getCurrentTask();
    if(t != null)
    {
      t.unregisterListener(listener);
    }
    ads.pause();
    EICApplication.saveTasks();
    super.onPause();
  }

  @Override
  protected void onResume()
  {
    super.onResume();
    ads.resume();
    IntentFilter filter = new IntentFilter(EveMarketService.BROADCAST_PROGRESS);
    filter.addCategory(Intent.CATEGORY_DEFAULT);
    receiver = new ResponseReceiver();
    registerReceiver(receiver, filter);

    receiver_basecost = new BaseCostResponseReceiver();

    IntentFilter filter_base = new IntentFilter(EveApiService.BROADCAST_BASECOST_UPDATED);
    filter.addAction(EveApiService.BROADCAST_SYSTEMCOST_UPDATED);
    filter.addCategory(Intent.CATEGORY_DEFAULT);
    registerReceiver(receiver_basecost, filter_base);

    Task task = getCurrentTask();
    if(task != null)
    {
      notifyMaterialSetChanged();
      notifyTaskChanged();
      task.registerListener(listener);
      onProfitChanged();
    }
    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
  }

  public void notifyTaskChanged()
  {
    for(IEveCalculatorFragment frag : pager_adapter.fragments)
    {
      if(frag != null)
      {
        frag.onTaskChanged();
      }
    }
  }

  private void notifyPricesChanged()
  {
    for(IEveCalculatorFragment frag : pager_adapter.fragments)
    {
      if(frag != null)
      {
        frag.onPriceValueChanged();
      }
    }
  }

  public void notifyMaterialSetChanged()
  {
    for(IEveCalculatorFragment frag : pager_adapter.fragments)
    {
      if(frag != null)
      {
        frag.onMaterialSetChanged();
      }
    }
  }

  private void notifyMaterialChanged(int item)
  {
    for(IEveCalculatorFragment frag : pager_adapter.fragments)
    {
      if(frag != null)
      {
        frag.onMaterialChanged(item);
      }
    }
  }

  public void notiftyExtraExpenseChanged()
  {
    for(IEveCalculatorFragment frag : pager_adapter.fragments)
    {
      if(frag != null)
      {
        frag.onExtraExpenseChanged();
      }
    }
  }

  public void notifyTaskParameterChanged(int param)
  {
    for(IEveCalculatorFragment frag : pager_adapter.fragments)
    {
      if(frag != null)
      {
        frag.onTaskParameterChanged(param);
      }
    }
  }

  // Called when changes in tasks parameters causes the profit to change.
  // Updates the profit TextView.
  public void onProfitChanged()
  {
    Task task = getCurrentTask();
    BigDecimal income = task.getIncome();
    BigDecimal expense = task.getExpense();

    DecimalFormat formatter = new DecimalFormat("###,###.##");
    BigDecimal profit = income.subtract(expense);

    if(profit.compareTo(BigDecimal.ZERO) < 0)
    {
      String text = "Loss: " + formatter.format(profit.negate()) + " ISK";
      if(income.compareTo(BigDecimal.ZERO) > 0)
      {
        DecimalFormat pformatter = new DecimalFormat("#.#");
        BigDecimal percent = profit.divide(income, 10, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).negate();
        text += " (" + pformatter.format(percent) + "%)";
      }
      tx_profit.setText(text);
      tx_profit.setTextColor(Color.RED);
    } else if(profit.compareTo(BigDecimal.ZERO) > 0)
    {
      String text = "Profit: " + formatter.format(profit) + " ISK";
      if(expense.compareTo(BigDecimal.ZERO) > 0)
      {
        DecimalFormat pformatter = new DecimalFormat("#.#");
        BigDecimal percent = profit.divide(expense, 10, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
        text += " (" + pformatter.format(percent) + "%)";
      }
      tx_profit.setText(text);
      tx_profit.setTextColor(Color.GREEN);
    } else
    {
      tx_profit.setText("No profit");
      tx_profit.setTextColor(Color.YELLOW);
    }
    
    synchronized(MarketData.request_prices)
    {
      if(MarketData.request_prices.size() > 0)
      {
        EveMarketService.requestMaterialPrices(EICFragmentActivity.this, MarketData.request_prices);
        MarketData.request_prices.clear();
      }
    }
  }

  // Get the current task name to display in the title bar.
  private String getTaskName()
  {
    if(task_path == null)
    {
      return null;
    }
    if(task_path.isEmpty())
    {
      return this.getString(R.string.app_name);
    }
    return task_path.peek().first;
  }

  // Called then users changes the current task (by going back or picking a task in a group).
  // Updates the fragments.
  private void onTaskChanged()
  {
    if(pager == null)
    {
      // Executes in tablets.
      FragmentManager fm = getSupportFragmentManager();
      pager_adapter = new LargePagerAdapter(fm);
      FragmentTransaction ft = fm.beginTransaction();
      ft.replace(ly_fragment_main.getId(), pager_adapter.getItem(0), "framentMain");
      ft.replace(ly_fragment_material.getId(), pager_adapter.getItem(1), "framentMaterials");
      ft.commit();
      tx_fragment_main_title.setText(task_names.get(getCurrentTask().getClass()));
    } else
    {
      // Executes in phones.
      pager_adapter = new PagerAdapter(getSupportFragmentManager());
      pager.setAdapter(pager_adapter);
    }
    setTitle(getTaskName());
    onProfitChanged();
  }

  // true if the current task is the root task group.
  public boolean isRootTask()
  {
    return task_path.isEmpty();
  }

  //Called when the user selects a task in a group.
  public void descend(String name)
  {
    GroupTask group = (GroupTask) getCurrentTask();
    Task t = group.getTask(name);
    group.unregisterListener(listener);
    task_path.push(new Pair<>(name, t));
    t.registerListener(listener);
    onTaskChanged();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState)
  {
    if(task_path == null)
    {
      return;
    }
    ArrayList<String> names = new ArrayList<>();
    for(Pair<String, Task> p : task_path)
    {
      names.add(p.first);
    }
    outState.putStringArrayList("path", names);
  }

  @Override
  public void onBackPressed()
  {
    if(task_path == null || task_path.isEmpty())
    {
      super.onBackPressed();
      return;
    }
    getCurrentTask().unregisterListener(listener);
    task_path.pop().second.registerListener(listener);
    onTaskChanged();
  }
}
