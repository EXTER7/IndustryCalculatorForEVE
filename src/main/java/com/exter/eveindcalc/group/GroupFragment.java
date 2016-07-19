package com.exter.eveindcalc.group;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.exter.cache.Cache;
import com.exter.cache.InfiniteCache;
import com.exter.controls.IntegerEditText;
import com.exter.eveindcalc.EICApplication;
import com.exter.eveindcalc.EICFragmentActivity;
import com.exter.eveindcalc.IEveCalculatorFragment;
import com.exter.eveindcalc.R;
import com.exter.eveindcalc.TaskHelper;
import com.exter.eveindcalc.util.XUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import exter.eveindustry.task.GroupTask;
import exter.eveindustry.task.Task;

public class GroupFragment extends Fragment implements IEveCalculatorFragment
{
  private class TaskListClickListener implements ListView.OnItemClickListener
  {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
      if(!long_pressed)
      {
        String name = (String)tasks_adapter.getItem(position);
        activity.descend(name);
      }
      long_pressed = false;
    }
  }

  private class TaskMenuClickListener implements ImageButton.OnClickListener
  {
    private String task_name;

    TaskMenuClickListener(String name)
    {
      task_name = name;
    }
    
    @Override
    public void onClick(View v)
    {
      showTaskMenu(task_name);
    }
  }

  private class TaskListLongClickListener implements ListView.OnItemLongClickListener
  {
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
    {
      long_pressed = true;
      showTaskMenu((String) tasks_adapter.getItem(position));
      return false;
    }
  }
  
  private class TaskProfitComparator implements Comparator<String>
  {
    @Override
    public int compare(String lhs, String rhs)
    {
      GroupTask group_task = (GroupTask)activity.getCurrentTask();
      Map<String,Task> list = group_task.getTaskList();

      Task l = list.get(lhs);
      Task r = list.get(rhs);
      
      return r.getIncome().subtract(r.getExpense()).compareTo(l.getIncome().subtract(l.getExpense()));
    }
  }

  private class TaskDurationComparator implements Comparator<String>
  {
   
    @Override
    public int compare(String lhs, String rhs)
    {
      GroupTask group_task = (GroupTask)activity.getCurrentTask();
      Map<String,Task> list = group_task.getTaskList();

      Task l = list.get(lhs);
      Task r = list.get(rhs);
      return l.getDuration() - r.getDuration();
    }
  }

  private class TaskProfitHourComparator implements Comparator<String>,Cache.IMissListener<String,BigDecimal>
  {
    private Cache<String,BigDecimal> cache;
    TaskProfitHourComparator()
    {
      cache = new InfiniteCache<>(this);
    }
    
    @Override
    public int compare(String lhs, String rhs)
    {
      BigDecimal pl = cache.get(lhs);
      BigDecimal pr = cache.get(rhs);
      
      if(pl == null)
      {
        if(pr == null)
        {
          return 0;
        } else
        {
          return 1;
        }
      } else
      {
        if(pr == null)
        {
          return -1;
        } else
        {
          return pr.compareTo(pl);
        }
      }
    }

    @Override
    public BigDecimal onCacheMiss(String key)
    {
      GroupTask group_task = (GroupTask)activity.getCurrentTask();
      Task t = group_task.getTaskList().get(key);

      int dur = t.getDuration();
      if(dur < 1)
      {
        return null;
      }
      BigDecimal time = new BigDecimal(dur);
      BigDecimal income_hour = t.getIncome().multiply(SECONDS_HOUR).divide(time,10,BigDecimal.ROUND_UP);
      BigDecimal expense_hour = t.getExpense().multiply(SECONDS_HOUR).divide(time,10,BigDecimal.ROUND_UP);
      return income_hour.subtract(expense_hour);
    }
  }


  private class TaskProfitPercentComparator implements Comparator<String>,Cache.IMissListener<String,BigDecimal>
  {
    private Cache<String,BigDecimal> cache;
    TaskProfitPercentComparator()
    {
      cache = new InfiniteCache<>(this);
    }
    
    @Override
    public int compare(String lhs, String rhs)
    {
      BigDecimal pl = cache.get(lhs);
      BigDecimal pr = cache.get(rhs);
      
      if(pl == null)
      {
        if(pr == null)
        {
          return 0;
        } else
        {
          return 1;
        }
      } else
      {
        if(pr == null)
        {
          return -1;
        } else
        {
          return pr.compareTo(pl);
        }
      }
    }

    @Override
    public BigDecimal onCacheMiss(String key)
    {
      GroupTask group_task = (GroupTask)activity.getCurrentTask();
      Task t = group_task.getTaskList().get(key);

      BigDecimal income = t.getIncome();
      BigDecimal expense = t.getExpense();

      BigDecimal profit = income.subtract(expense);

      BigDecimal percent = BigDecimal.ZERO;
      if(profit.compareTo(BigDecimal.ZERO) < 0)
      {
        if(income.compareTo(BigDecimal.ZERO) == 0)
        {
          percent = new BigDecimal(-2000000000);
        } else
        {
          try
          {
            percent = profit.divide(income, 10, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
          } catch(ArithmeticException ex)
          {
            percent = new BigDecimal(-2000000000);
          }
        }
      } else if(profit.compareTo(BigDecimal.ZERO) > 0)
      {
        if(expense.compareTo(BigDecimal.ZERO) == 0)
        {
          percent = new BigDecimal(2000000000);
        } else
        {
          try
          {
            percent = profit.divide(expense, 10, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
          } catch(ArithmeticException ex)
          {
            percent = new BigDecimal(2000000000);
          }
        }
      }
      return percent;
    }
  }


  private class TaskListAdapter extends BaseAdapter
  {
    private LayoutInflater inflater;
    private List<String> task_names;
    
    TaskListAdapter(Context context)
    {
      inflater = LayoutInflater.from(context);
      task_names = new ArrayList<>();
    }
    
    
    void updateTaskList()
    {
      task_names.clear();
      GroupTask g = (GroupTask)activity.getCurrentTask();
      if(g != null)
      {
        task_names.addAll(g.getTaskList().keySet());
        switch(sort_mode)
        {
          case NAME:
            Collections.sort(task_names);
            break;
          case PROFIT:
            Collections.sort(task_names,new TaskProfitComparator());
            break;
          case DURATION:
            Collections.sort(task_names,new TaskDurationComparator());
            break;
          case PROFITHOUR:
            Collections.sort(task_names,new TaskProfitHourComparator());
            break;
          case PERCENT:
            Collections.sort(task_names,new TaskProfitPercentComparator());
            break;
        }
      }
      notifyDataSetChanged();
    }
      
    @Override
    public int getCount()
    {
      return task_names == null?0:task_names.size();
    }
       
    @Override
    public Object getItem(int position)
    {
      return task_names == null?null:task_names.get(position);
    }
       
    
    @Override
    public long getItemId(int position)
    {
      return position;
    }
    
    final class TaskHolder
    {
      TextView tx_name;
      TextView tx_description;
      TextView tx_profit;
      TextView tx_duration;
      TextView tx_profithour;
      ImageView im_icon;
      ImageButton bt_menu;
      
      TaskHolder(View convertView)
      {
        tx_name = (TextView)convertView.findViewById(R.id.tx_task_name);
        tx_description = (TextView)convertView.findViewById(R.id.tx_task_description);
        tx_profit = (TextView)convertView.findViewById(R.id.tx_task_profit);
        tx_duration = (TextView)convertView.findViewById(R.id.tx_task_duration);
        tx_profithour = (TextView)convertView.findViewById(R.id.tx_task_profithour);
        im_icon = (ImageView)convertView.findViewById(R.id.im_task_icon);
        tx_description.setTextColor(tx_description.getTextColors().withAlpha(192));
        bt_menu = (ImageButton)convertView.findViewById(R.id.bt_task_menu);
      }
      
      private void setProfit(TextView label, BigDecimal income, BigDecimal expense, boolean prefix, boolean percent, String currency)
      {

        BigDecimal profit = income.subtract(expense);
        if(profit.compareTo(BigDecimal.ZERO) < 0)
        {
          String text = "";
          if(prefix)
          {
            text = "Loss: ";
          }
          text += CURRENCY_FORMATTER.format(profit.negate()) + " " + currency;
          if(percent && income.compareTo(BigDecimal.ZERO) > 0)
          {
            BigDecimal p = profit.divide(income, 10, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).negate();
            text += " (" + PERCENT_FORMATTER.format(p) + "%)";
          }
          label.setText(text);
          label.setTextColor(Color.RED);
        } else if(profit.compareTo(BigDecimal.ZERO) > 0)
        {
          String text = "";
          if(prefix)
          {
            text = "Profit: ";
          }
          text += CURRENCY_FORMATTER.format(profit) + " " + currency;
          if(percent && expense.compareTo(BigDecimal.ZERO) > 0)
          {
            BigDecimal p = profit.divide(expense, 10, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
            text += " (" + PERCENT_FORMATTER.format(p) + "%)";
          }
          label.setText(text);
          label.setTextColor(Color.GREEN);
        } else
        {
          if(prefix)
          {
            label.setText("No profit");
          } else
          {
            label.setText(" ");
          }
          label.setTextColor(Color.YELLOW);
        }      
      }
    
      void update(int position)
      {
        String name = task_names.get(position);
        Task t = activity.getCurrentTask();
        if(!(t instanceof GroupTask))
        {
          return;
        }
        GroupTask group_task = (GroupTask)t;
        Task task = group_task.getTaskList().get(name);
        if(task == null)
        {
          return;
        }
        int icon = TaskHelper.getTaskIcon(task);

        application.setImageViewItemIcon(im_icon, icon);

        if(application.helper.taskHasBackground(task))
        {
          im_icon.setBackgroundResource(R.drawable.item_background);
        } else
        {
          im_icon.setBackgroundResource(0);
        }

        tx_name.setText(name);
        tx_description.setText(application.helper.getTaskDescription(task));
        BigDecimal s = new BigDecimal(group_task.getScale());
        BigDecimal income = task.getIncome().multiply(s);
        BigDecimal expense = task.getExpense().multiply(s);

        setProfit(tx_profit, income, expense, true, true, "ISK");

        int dur = task.getDuration() * group_task.getScale();
        if(dur > 0)
        {
          BigDecimal time = new BigDecimal(dur);
          BigDecimal income_hour = income.multiply(SECONDS_HOUR).divide(time,10,BigDecimal.ROUND_UP);
          BigDecimal expense_hour = expense.multiply(SECONDS_HOUR).divide(time,10,BigDecimal.ROUND_UP);
          setProfit(tx_profithour, income_hour, expense_hour, false, false, "ISK/Hour");
          tx_duration.setText(String.format("Duration: %s", XUtil.TimeToStr(dur)));
        } else
        {
          tx_profithour.setText(" ");
          tx_profithour.setTextColor(Color.YELLOW);
          tx_duration.setText(XUtil.TimeToStr(dur));
          tx_duration.setText(" ");
        }
        if(bt_menu != null)
        {
          bt_menu.setOnClickListener(new TaskMenuClickListener(name));
        }
      }
    }
        
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
      TaskHolder holder;

      if (convertView == null)
      {
        convertView = inflater.inflate(R.layout.task, ls_tasks,false);
        holder = new TaskHolder(convertView);
        convertView.setTag(holder);
      } else
      {
        holder = (TaskHolder)convertView.getTag();
      }

      holder.update(position);

      return convertView;
    }
  }


  private void showTaskMenu(String task_name)
  {
    GroupMenuDialogFragment dialog = new GroupMenuDialogFragment();
    Bundle args = new Bundle();
    args.putString("name", task_name);
    dialog.setArguments(args);
    dialog.show(getActivity().getSupportFragmentManager(), "TaskMenuDialogFragment");
  }

  public void addTask()
  {
    AddTaskDialogFragment dialog = new AddTaskDialogFragment();
    dialog.show(getActivity().getSupportFragmentManager(), "AddTaskDialogFragment");
  }

  private class AddTaskListener implements OnClickListener
  {
    @Override
    public void onClick(View v)
    {
      addTask();
    }
  }

  public void addGroup()
  {
    GroupTask group_task = (GroupTask)activity.getCurrentTask();
    GroupTask task = application.factory.newGroup();
    group_task.addTask("New group",task);
    onTaskChanged();
    activity.notifyMaterialSetChanged();
  }
  
  private class AddGroupListener implements OnClickListener
  {
    @Override
    public void onClick(View v)
    {
      addGroup();
    }
  }

  private class ScaleChangeWatcher implements IntegerEditText.ValueListener
  {
    @Override
    public void onValueChanged(int new_value)
    {
      if(activity.isRootTask())
      {
        return;
      }
      GroupTask group_task = (GroupTask)activity.getCurrentTask();
      group_task.setScale(new_value);
      activity.notifyMaterialSetChanged();
      tasks_adapter.notifyDataSetChanged();
    }
  }
  
  private class SortModeItemSelectedListener implements Spinner.OnItemSelectedListener
  {
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
      sort_mode = SortMode.fromInt(pos);
      SharedPreferences sp = getActivity().getSharedPreferences("EIC", Context.MODE_PRIVATE);
      SharedPreferences.Editor ed = sp.edit();
      ed.putInt("group.sort", pos);
      ed.apply();

      tasks_adapter.updateTaskList();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }
  }

  private enum SortMode
  {
    NAME(0),
    PROFIT(1),
    DURATION(2),
    PROFITHOUR(3),
    PERCENT(4);
    
    public final int value;
    
    SortMode(int v)
    {
      value = v;
    }
    
    static private SparseArray<SortMode> intmap;
    
    static public SortMode fromInt(int i)
    {
      if(intmap == null)
      {
        intmap = new SparseArray<>();
        for(SortMode v:values())
        {
          intmap.put(v.value, v);
        }
      }
      return intmap.get(i);
    }
  }


  static private DecimalFormat CURRENCY_FORMATTER = new DecimalFormat("###,###.##");
  static private DecimalFormat PERCENT_FORMATTER = new DecimalFormat("#.#");
  static private BigDecimal SECONDS_HOUR = new BigDecimal(3600);

  private IntegerEditText ed_scale;
  private Spinner sp_sort;
  private ListView ls_tasks;
  private TaskListAdapter tasks_adapter;

  private SortMode sort_mode;
  
  private boolean long_pressed = false;

  private EICApplication application;

  private EICFragmentActivity activity;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    activity = (EICFragmentActivity)getActivity();
    application = (EICApplication)activity.getApplication();


    View rootView;
    rootView = inflater.inflate(R.layout.group_fragment, container, false);


    SharedPreferences sp = application.getApplicationContext().getSharedPreferences("EIC", Context.MODE_PRIVATE);
    sort_mode = SortMode.fromInt(sp.getInt("group.sort", SortMode.NAME.value));

    sp_sort = (Spinner) rootView.findViewById(R.id.sp_menu_sort);
    
    ls_tasks = (ListView) rootView.findViewById(R.id.ls_menu_tasks);

    tasks_adapter = new TaskListAdapter(getActivity());
    ls_tasks.setOnItemClickListener(new TaskListClickListener());
    ls_tasks.setOnItemLongClickListener(new TaskListLongClickListener());

    View footer = inflater.inflate(R.layout.group_footer, ls_tasks, false);
    ls_tasks.addFooterView(footer);
    Button bt_addtask = (Button) footer.findViewById(R.id.bt_menu_add_task);
    Button bt_addgroup = (Button) footer.findViewById(R.id.bt_menu_add_group);
    ls_tasks.setAdapter(tasks_adapter);

    sp_sort.setSelection(sort_mode.value);
    sp_sort.setOnItemSelectedListener(new SortModeItemSelectedListener());
    LinearLayout ly_scale = (LinearLayout) rootView.findViewById(R.id.ly_group_scale);
    if(activity.isRootTask())
    {
      ly_scale.setVisibility(View.GONE);
    } else
    {
      ed_scale = new IntegerEditText((EditText) rootView.findViewById(R.id.ed_group_scale), 1, 99999, 0, new ScaleChangeWatcher());
    }

    bt_addtask.setOnClickListener(new AddTaskListener());
    bt_addgroup.setOnClickListener(new AddGroupListener());

    onTaskChanged();
    
    return rootView;
  }

  @Override
  public void onTaskChanged()
  {
    if(!activity.isRootTask())
    {
      GroupTask group_task = (GroupTask)activity.getCurrentTask();
      ed_scale.setValue(group_task.getScale());
    }
    tasks_adapter.updateTaskList();
  }

  @Override
  public void onPriceValueChanged()
  {
    if(tasks_adapter != null)
    {
      tasks_adapter.notifyDataSetChanged();
    }
  }

  @Override
  public void onMaterialSetChanged()
  {

  }

  @Override
  public void onMaterialChanged(int item)
  {
    
  }

  @Override
  public void onExtraExpenseChanged()
  {

  }

  @Override
  public void onTaskParameterChanged(int param)
  {

  }

  @Override 
  public void onResume()
  {
    super.onResume();
    if(sp_sort != null)
    {
      SharedPreferences sp = application.getApplicationContext().getSharedPreferences("EIC", Context.MODE_PRIVATE);
      sort_mode = SortMode.fromInt(sp.getInt("group.sort", SortMode.NAME.value));
      sp_sort.setSelection(sort_mode.value);
    }
  }
}
