package com.exter.eveindcalc.materials;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.exter.controls.BigDecimalEditText;
import com.exter.eveindcalc.R;
import com.exter.eveindcalc.SolarSystemDialogFragment;
import com.exter.eveindcalc.TaskHelper;
import com.exter.eveindcalc.data.starmap.RecentSystemsDA;
import com.exter.eveindcalc.data.starmap.Starmap;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import exter.eveindustry.task.Task;

public class MarketFetchDialogFragment extends DialogFragment
{
  private class SystemSelectedListener implements Spinner.OnItemSelectedListener
  {
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
      int i = system_ids.get(pos);
      if(i == -1 )
      {
        SolarSystemDialogFragment dialog = new MaterialSolarSystemDialogFragment();
        dialog.show(getActivity().getSupportFragmentManager(), "SolarSystemDialogFragment");
      } else
      {
        price = new Task.Market(i,price.order,price.manual);
      }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
    }
  }

  private class PriceChangeWatcher implements BigDecimalEditText.ValueListener
  {
    @Override
    public void valueChanged(int tag, BigDecimal new_value)
    {
      price = new Task.Market(price.system,price.order,new_value);
    }
  }

  private MarketFetchAcceptListener accept_listener;

  private Spinner sp_system;
  private RadioGroup rg_source;
  private RadioButton rb_sell;
  private RadioButton rb_buy;
  private RadioButton rb_manual;
  private BigDecimalEditText ed_manual;
  private LinearLayout ly_manual;

  private Task.Market price;

  private List<Integer> system_ids;
  
  static public final int TYPE_ITEM = 0;
  static public final int TYPE_PRODUCED = 1;
  static public final int TYPE_REQUIRED = 2;
  int type;
  
  
  int item;

  private class MarketFetchClickListener implements DialogInterface.OnClickListener
  {
    @Override
    public void onClick(DialogInterface dialog, int which)
    {
      if(accept_listener != null)
      {
        switch(type)
        {
          case TYPE_ITEM:
            accept_listener.onAcceptItem(item, price);
            break;
          case TYPE_PRODUCED:
            accept_listener.onAcceptProduced(price);
            break;
          case TYPE_REQUIRED:
            accept_listener.onAcceptRequired(price);
            break;
        }
      }
    }
  }

  private class SellClickListener implements RadioButton.OnClickListener
  {
    @Override
    public void onClick(View v)
    {
      price = new Task.Market(price.system,Task.Market.Order.SELL,price.manual);
      enableBuySell();
    }
  }

  private class BuyClickListener implements RadioButton.OnClickListener
  {
    @Override
    public void onClick(View v)
    {
      price = new Task.Market(price.system,Task.Market.Order.BUY,price.manual);
      enableBuySell();
    }
  }

  private class ManualClickListener implements RadioButton.OnClickListener
  {
    @Override
    public void onClick(View v)
    {
      price = new Task.Market(price.system,Task.Market.Order.MANUAL,price.manual);
      enableManual();
    }
  }
  
  private void enableBuySell()
  {
    sp_system.setEnabled(true);
    ed_manual.setEnabled(false);
    
  }
  
  private void enableManual()
  {
    sp_system.setEnabled(false);
    ed_manual.setEnabled(true);
  }

  public interface MarketFetchAcceptListener
  {
    void onAcceptItem(int item, Task.Market price);
    void onAcceptRequired(Task.Market price);
    void onAcceptProduced(Task.Market price);
  }

  @SuppressLint("InflateParams")
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState)
  {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    LayoutInflater inflater = getActivity().getLayoutInflater();
    View view = inflater.inflate(R.layout.market_fetch, null);
    builder.setView(view);
    builder.setPositiveButton("Set", new MarketFetchClickListener());
    builder.setNegativeButton("Cancel", null);
    Bundle args = getArguments();

    sp_system = (Spinner) view.findViewById(R.id.sp_fetch_system);
    rg_source = (RadioGroup) view.findViewById(R.id.rg_fetch_source);
    rb_sell = (RadioButton) view.findViewById(R.id.rb_fetch_sell);
    rb_buy = (RadioButton) view.findViewById(R.id.rb_fetch_buy);
    rb_manual = (RadioButton) view.findViewById(R.id.rb_fetch_manual);
    ed_manual = new BigDecimalEditText((EditText) view.findViewById(R.id.ed_fetch_manual), -1, BigDecimal.ZERO, new BigDecimal("1000000000000"), BigDecimal.ZERO, new PriceChangeWatcher());
    ly_manual = (LinearLayout) view.findViewById(R.id.ly_fetch_manual);

    price = TaskHelper.PriceFromBundle(args);
    type = args.getInt("type");
    if(type == TYPE_ITEM)
    {
      item = args.getInt("item");
    } else
    {
      item = -1;
      ly_manual.setVisibility(View.GONE);
      if(price.order == Task.Market.Order.MANUAL)
      {
        price = new Task.Market(price.system,Task.Market.Order.SELL,BigDecimal.ZERO);
      }
    }

    ed_manual.setValue(price.manual);

    switch(price.order)
    {
      case SELL:
        rg_source.check(R.id.rb_fetch_sell);
        enableBuySell();
        break;
      case BUY:
        rg_source.check(R.id.rb_fetch_buy);
        enableBuySell();
        break;
      case MANUAL:
        rg_source.check(R.id.rb_fetch_manual);
        enableManual();
        break;

    }

    rb_sell.setOnClickListener(new SellClickListener());
    rb_buy.setOnClickListener(new BuyClickListener());
    rb_manual.setOnClickListener(new ManualClickListener());

    RecentSystemsDA.putSystem(price.system);

    updateSystem();
    
    return builder.create();
  }

  public void setSystem(int id)
  {
    price = new Task.Market(id,price.order,price.manual);
    updateSystem();
  }

  private void updateSystem()
  {
    sp_system.setOnItemSelectedListener(null);
    RecentSystemsDA.putSystem(price.system);

    system_ids = new ArrayList<Integer>();
    List<String> system_names = new ArrayList<String>();
    for(int id:RecentSystemsDA.getSystems())
    {
      system_ids.add(id);
      system_names.add(Starmap.getSolarSystem(id).Name);
    }
    system_ids.add(-1);
    system_names.add("[ Other ... ]");

    ArrayAdapter<String> sys_spinner_adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, system_names);
    sys_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    sp_system.setAdapter(sys_spinner_adapter);
    sp_system.setSelection(system_ids.indexOf(price.system));
    sp_system.setOnItemSelectedListener(new SystemSelectedListener());
  }
  
  public void setOnAcceptListener(MarketFetchAcceptListener listener)
  {
    accept_listener = listener;
  }

}
