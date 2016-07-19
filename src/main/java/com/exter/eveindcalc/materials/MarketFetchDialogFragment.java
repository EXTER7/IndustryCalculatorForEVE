package com.exter.eveindcalc.materials;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.exter.eveindcalc.EICApplication;
import com.exter.eveindcalc.R;
import com.exter.eveindcalc.SolarSystemDialogFragment;
import com.exter.eveindcalc.TaskHelper;
import com.exter.eveindcalc.data.EveDatabase;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import exter.eveindustry.market.Market;

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
        market = new Market(i, market.order, market.manual, market.broker, market.transaction);
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
      market = new Market(market.system, market.order,new_value, market.broker, market.transaction);
    }
  }

  private class BrokerChangeWatcher implements BigDecimalEditText.ValueListener
  {
    @Override
    public void valueChanged(int tag, BigDecimal new_value)
    {
      market = new Market(market.system, market.order, market.manual,new_value, market.transaction);
    }
  }

  private class TaxChangeWatcher implements BigDecimalEditText.ValueListener
  {
    @Override
    public void valueChanged(int tag, BigDecimal new_value)
    {
      market = new Market(market.system, market.order, market.manual, market.broker,new_value);
    }
  }

  private MarketFetchAcceptListener accept_listener;

  private Spinner sp_system;
  private BigDecimalEditText ed_broker;
  private BigDecimalEditText ed_tax;

  private BigDecimalEditText ed_manual;

  private Market market;

  private List<Integer> system_ids;
  
  static final int TYPE_ITEM = 0;
  static final int TYPE_PRODUCED = 1;
  static final int TYPE_REQUIRED = 2;
  private int type;
  private int item;

  private EICApplication application;
  private EveDatabase provider;

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
            accept_listener.onAcceptItem(item, market);
            break;
          case TYPE_PRODUCED:
            accept_listener.onAcceptProduced(market);
            break;
          case TYPE_REQUIRED:
            accept_listener.onAcceptRequired(market);
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
      market = new Market(market.system,Market.Order.SELL, market.manual, market.broker, market.transaction);
      enableBuySell();
    }
  }

  private class BuyClickListener implements RadioButton.OnClickListener
  {
    @Override
    public void onClick(View v)
    {
      market = new Market(market.system,Market.Order.BUY, market.manual, market.broker, market.transaction);
      enableBuySell();
    }
  }

  private class ManualClickListener implements RadioButton.OnClickListener
  {
    @Override
    public void onClick(View v)
    {
      market = new Market(market.system,Market.Order.MANUAL, market.manual, market.broker, market.transaction);
      enableManual();
    }
  }
  
  private void enableBuySell()
  {
    sp_system.setEnabled(true);
    ed_broker.setEnabled(true);
    ed_tax.setEnabled(true);
    ed_manual.setEnabled(false);
    
  }
  
  private void enableManual()
  {
    sp_system.setEnabled(false);
    ed_broker.setEnabled(false);
    ed_tax.setEnabled(false);
    ed_manual.setEnabled(true);
  }

  public interface MarketFetchAcceptListener
  {
    void onAcceptItem(int item, Market price);
    void onAcceptRequired(Market price);
    void onAcceptProduced(Market price);
  }

  @NonNull
  @SuppressLint("InflateParams")
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState)
  {
    application = (EICApplication)getActivity().getApplication();
    provider = application.database;
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    LayoutInflater inflater = getActivity().getLayoutInflater();
    View view = inflater.inflate(R.layout.market_fetch, null);
    builder.setView(view);
    builder.setPositiveButton("Set", new MarketFetchClickListener());
    builder.setNegativeButton("Cancel", null);
    Bundle args = getArguments();

    sp_system = (Spinner) view.findViewById(R.id.sp_fetch_system);
    RadioGroup rg_source = (RadioGroup) view.findViewById(R.id.rg_fetch_source);
    RadioButton rb_sell = (RadioButton) view.findViewById(R.id.rb_fetch_sell);
    RadioButton rb_buy = (RadioButton) view.findViewById(R.id.rb_fetch_buy);
    RadioButton rb_manual = (RadioButton) view.findViewById(R.id.rb_fetch_manual);
    ed_manual = new BigDecimalEditText((EditText) view.findViewById(R.id.ed_fetch_manual), -1, BigDecimal.ZERO, new BigDecimal("1000000000000"), BigDecimal.ZERO, new PriceChangeWatcher());
    ed_broker = new BigDecimalEditText((EditText) view.findViewById(R.id.ed_fetch_broker), -1, BigDecimal.ZERO, new BigDecimal("100"), BigDecimal.ZERO, new BrokerChangeWatcher());
    ed_tax = new BigDecimalEditText((EditText) view.findViewById(R.id.ed_fetch_tax), -1, BigDecimal.ZERO, new BigDecimal("100"), BigDecimal.ZERO, new TaxChangeWatcher());
    LinearLayout ly_manual = (LinearLayout) view.findViewById(R.id.ly_fetch_manual);

    market = TaskHelper.priceFromBundle(args);
    type = args.getInt("type");
    if(type == TYPE_ITEM)
    {
      item = args.getInt("item");
    } else
    {
      item = -1;
      ly_manual.setVisibility(View.GONE);
      if(market.order == Market.Order.MANUAL)
      {
        market = new Market(market.system,Market.Order.SELL,BigDecimal.ZERO, market.broker, market.transaction);
      }
    }

    ed_manual.setValue(market.manual);
    ed_broker.setValue(market.broker);
    ed_tax.setValue(market.transaction);

    switch(market.order)
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

    provider.da_recentsystems.putSystem(market.system);

    updateSystem();
    
    return builder.create();
  }

  public void setSystem(int id)
  {
    market = new Market(id, market.order, market.manual, market.broker, market.transaction);
    updateSystem();
  }

  private void updateSystem()
  {
    sp_system.setOnItemSelectedListener(null);
    provider.da_recentsystems.putSystem(market.system);

    system_ids = new ArrayList<>();
    List<String> system_names = new ArrayList<>();
    for(int id:provider.da_recentsystems.getSystems())
    {
      system_ids.add(id);
      system_names.add(application.factory.solarsystems.get(id).name);
    }
    system_ids.add(-1);
    system_names.add("[ Other ... ]");

    ArrayAdapter<String> sys_spinner_adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, system_names);
    sys_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    sp_system.setAdapter(sys_spinner_adapter);
    sp_system.setSelection(system_ids.indexOf(market.system));
    sp_system.setOnItemSelectedListener(new SystemSelectedListener());
  }
  
  void setOnAcceptListener(MarketFetchAcceptListener listener)
  {
    accept_listener = listener;
  }
}
