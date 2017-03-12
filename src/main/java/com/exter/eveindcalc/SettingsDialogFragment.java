package com.exter.eveindcalc;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.exter.controls.BigDecimalEditText;
import com.exter.controls.IntegerEditText;
import com.exter.eveindcalc.data.EveDatabase;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import exter.eveindustry.market.Market;

public class SettingsDialogFragment extends DialogFragment
{
  private class SystemSelectedListener implements Spinner.OnItemSelectedListener
  {
    private boolean requirement;
    
    SystemSelectedListener(boolean req)
    {
      requirement = req;
    }
    
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
      int sys = system_ids.get(pos);
      if(sys == -1)
      {
        SettingsSolarSystemDialogFragment dialog = new SettingsSolarSystemDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean("required", requirement);
        dialog.setArguments(args);
        dialog.show(getActivity().getSupportFragmentManager(), "SettingsSolarSystemDialogFragment");
      } else
      {
        if(requirement)
        {
          Market p = database.getDefaultRequiredMarket();
          database.setDefaultRequiredMarket(p.withSolarSystem(sys));
        } else
        {
          Market p = database.getDefaultProducedMarket();
          database.setDefaultProducedMarket(p.withSolarSystem(sys));
        }
      }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
    }
  }


  private Spinner sp_sellto_system;
  private RadioGroup rg_sellto_source;
  private BigDecimalEditText ed_sellto_broker;
  private BigDecimalEditText ed_sellto_tax;

  private Spinner sp_buyfrom_system;
  private RadioGroup rg_buyfrom_source;
  private BigDecimalEditText ed_buyfrom_broker;
  private BigDecimalEditText ed_buyfrom_tax;

  private IntegerEditText ed_default_me;
  private IntegerEditText ed_default_te;

  private CheckBox cb_tablemode;

  private List<Integer> system_ids;
  private EICApplication application;
  private EveDatabase database;


  private class SellClickListener implements RadioButton.OnClickListener
  {
    private boolean requirement;
    
    SellClickListener(boolean req)
    {
      requirement = req;
    }
    
    @Override
    public void onClick(View v)
    {
      if(requirement)
      {
        Market p = database.getDefaultRequiredMarket();
        database.setDefaultRequiredMarket(p.withOrder(Market.Order.SELL));
      } else
      {
        Market p = database.getDefaultProducedMarket();
        database.setDefaultProducedMarket(p.withOrder(Market.Order.SELL));
      }
    }
  }

  private class BuyClickListener implements RadioButton.OnClickListener
  {
    private boolean requirement;
    
    BuyClickListener(boolean req)
    {
      requirement = req;
    }

    @Override
    public void onClick(View v)
    {
      if(requirement)
      {
        Market p = database.getDefaultRequiredMarket();
        database.setDefaultRequiredMarket(p.withOrder(Market.Order.BUY));
      } else
      {
        Market p = database.getDefaultProducedMarket();
        database.setDefaultProducedMarket(p.withOrder(Market.Order.BUY));
      }
    }
  }

  private class BrokerChangeWatcher implements BigDecimalEditText.ValueListener
  {
    private boolean requirement;

    BrokerChangeWatcher(boolean req)
    {
      requirement = req;
    }

    @Override
    public void valueChanged(int tag, BigDecimal new_value)
    {
      if(requirement)
      {
        Market p = database.getDefaultRequiredMarket();
        database.setDefaultRequiredMarket(p.withBrokerFee(new_value));
      } else
      {
        Market p = database.getDefaultProducedMarket();
        database.setDefaultProducedMarket(p.withBrokerFee(new_value));
      }
    }
  }

  private class TaxChangeWatcher implements BigDecimalEditText.ValueListener
  {
    private boolean requirement;

    TaxChangeWatcher(boolean req)
    {
      requirement = req;
    }
    @Override
    public void valueChanged(int tag, BigDecimal new_value)
    {
      if(requirement)
      {
        Market p = database.getDefaultRequiredMarket();
        database.setDefaultRequiredMarket(p.withTransactionTax(new_value));
      } else
      {
        Market p = database.getDefaultProducedMarket();
        database.setDefaultProducedMarket(p.withTransactionTax(new_value));
      }
    }
  }

  private class TableModeCheckedChangeListener implements CompoundButton.OnCheckedChangeListener
  {
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
      SharedPreferences sp = application.getSharedPreferences("EIC", Context.MODE_PRIVATE);
      SharedPreferences.Editor ed = sp.edit();
      ed.putBoolean("ui.tablet", isChecked);
      ed.apply();
    }
  }

  private class MeResearchChangeWatcher implements IntegerEditText.ValueListener
  {
    @Override
    public void onValueChanged(int new_value)
    {
      database.setDefaultBlueprintME(new_value);
    }
  }

  private class TeResearchChangeWatcher implements IntegerEditText.ValueListener
  {
    @Override
    public void onValueChanged(int new_value)
    {
      database.setDefaultBlueprintTE(new_value);
    }
  }


  private class DoneClickListener implements DialogInterface.OnClickListener
  {
    @Override
    public void onClick(DialogInterface dialog, int which)
    {
      ((EICFragmentActivity)getActivity()).onProfitChanged();
    }
  }

  @NonNull
  @SuppressLint("InflateParams")
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState)
  {
    application = (EICApplication)getActivity().getApplication();
    database = application.database;

    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    LayoutInflater inflater = getActivity().getLayoutInflater();
    View view = inflater.inflate(R.layout.settings, null);
    builder.setView(view);
    builder.setNeutralButton("Done", new DoneClickListener());

    sp_sellto_system = (Spinner) view.findViewById(R.id.sp_sellto_system);
    rg_sellto_source = (RadioGroup) view.findViewById(R.id.rg_sellto_source);
    RadioButton rb_sellto_sell = (RadioButton) view.findViewById(R.id.rb_sellto_sell);
    RadioButton rb_sellto_buy = (RadioButton) view.findViewById(R.id.rb_sellto_buy);
    ed_sellto_broker = new BigDecimalEditText((EditText) view.findViewById(R.id.ed_sellto_broker), -1, BigDecimal.ZERO, new BigDecimal("100"), BigDecimal.ZERO, new BrokerChangeWatcher(false));
    ed_sellto_tax = new BigDecimalEditText((EditText) view.findViewById(R.id.ed_sellto_tax), -1, BigDecimal.ZERO, new BigDecimal("100"), BigDecimal.ZERO, new TaxChangeWatcher(false));

    sp_buyfrom_system = (Spinner) view.findViewById(R.id.sp_buyfrom_system);
    rg_buyfrom_source = (RadioGroup) view.findViewById(R.id.rg_buyfrom_source);
    RadioButton rb_buyfrom_sell = (RadioButton) view.findViewById(R.id.rb_buyfrom_sell);
    RadioButton rb_buyfrom_buy = (RadioButton) view.findViewById(R.id.rb_buyfrom_buy);
    ed_buyfrom_broker = new BigDecimalEditText((EditText) view.findViewById(R.id.ed_buyfrom_broker), -1, BigDecimal.ZERO, new BigDecimal("100"), BigDecimal.ZERO, new BrokerChangeWatcher(true));
    ed_buyfrom_tax = new BigDecimalEditText((EditText) view.findViewById(R.id.ed_buyfrom_tax), -1, BigDecimal.ZERO, new BigDecimal("100"), BigDecimal.ZERO, new TaxChangeWatcher(true));


    ed_default_me = new IntegerEditText((EditText) view.findViewById(R.id.ed_settings_melevel), 0, 10, 0, new MeResearchChangeWatcher());
    ed_default_te = new IntegerEditText((EditText) view.findViewById(R.id.ed_settings_pelevel), 0, 20, 0, new TeResearchChangeWatcher());

    cb_tablemode = (CheckBox) view.findViewById(R.id.cb_settings_tabletmode);

    updateSettings();
    
    rb_sellto_sell.setOnClickListener(new SellClickListener(false));
    rb_sellto_buy.setOnClickListener(new BuyClickListener(false));
    rb_buyfrom_sell.setOnClickListener(new SellClickListener(true));
    rb_buyfrom_buy.setOnClickListener(new BuyClickListener(true));

    cb_tablemode.setOnCheckedChangeListener(new TableModeCheckedChangeListener());

    return builder.create();
  }
  
  private void updateSettings()
  {
    sp_sellto_system.setOnItemSelectedListener(null);
    sp_buyfrom_system.setOnItemSelectedListener(null);

    Market req = database.getDefaultRequiredMarket();
    Market prod = database.getDefaultProducedMarket();
    
    if(req.order == Market.Order.BUY)
    {
      rg_buyfrom_source.check(R.id.rb_buyfrom_buy);
    } else
    {
      rg_buyfrom_source.check(R.id.rb_buyfrom_sell);
    }      
    if(prod.order == Market.Order.BUY)
    {
      rg_sellto_source.check(R.id.rb_sellto_buy);
    } else
    {
      rg_sellto_source.check(R.id.rb_sellto_sell);
    }
    updateSystem(sp_sellto_system, prod.system);
    updateSystem(sp_buyfrom_system, req.system);

    ed_sellto_broker.setValue(prod.broker);
    ed_sellto_tax.setValue(prod.transaction);
    ed_buyfrom_broker.setValue(req.broker);
    ed_buyfrom_tax.setValue(req.transaction);

    sp_sellto_system.setOnItemSelectedListener(new SystemSelectedListener(false));
    sp_buyfrom_system.setOnItemSelectedListener(new SystemSelectedListener(true));
    ed_default_me.setValue(database.getDefaultME(null));
    ed_default_te.setValue(database.getDefaultTE(null));
    SharedPreferences sp = application.getSharedPreferences("EIC", Context.MODE_PRIVATE);
    cb_tablemode.setChecked(application.useTableUI());
  }
  
  @Override
  public void onResume()
  {
    super.onResume();
    Market req = database.getDefaultRequiredMarket();
    Market prod = database.getDefaultProducedMarket();

    sp_sellto_system.setOnItemSelectedListener(null);
    updateSystem(sp_sellto_system, prod.system);
    sp_sellto_system.setOnItemSelectedListener(new SystemSelectedListener(false));

    sp_buyfrom_system.setOnItemSelectedListener(null);
    updateSystem(sp_buyfrom_system, req.system);
    sp_buyfrom_system.setOnItemSelectedListener(new SystemSelectedListener(true));
  }

  private void updateSystem(Spinner spinner, int system)
  {
    database.da_recentsystems.putSystem(system);

    system_ids = new ArrayList<>();
    List<String> system_names = new ArrayList<>();
    for(int id: database.da_recentsystems.getSystems())
    {
      system_ids.add(id);
      system_names.add(application.factory.solarsystems.get(id).name);
    }
    system_ids.add(-1);
    system_names.add("[ Other ... ]");

    ArrayAdapter<String> sys_spinner_adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, system_names);
    sys_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(sys_spinner_adapter);
    spinner.setSelection(system_ids.indexOf(system));
  }
}
