package com.exter.eveindcalc;

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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.exter.controls.IntegerEditText;
import com.exter.eveindcalc.data.EveDatabase;
import com.exter.eveindcalc.data.starmap.RecentSystemsDA;
import com.exter.eveindcalc.data.starmap.Starmap;

import java.util.ArrayList;
import java.util.List;

import exter.eveindustry.task.Task;

public class SettingsDialogFragment extends DialogFragment
{
  private class SystemSelectedListener implements Spinner.OnItemSelectedListener
  {
    private boolean requirement;
    
    public SystemSelectedListener(boolean req)
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
          Task.Market p = EveDatabase.getDefaultRequiredPrice();
          EveDatabase.setDefaultRequiredPrice(new Task.Market(sys, p.order, p.manual));
        } else
        {
          Task.Market p = EveDatabase.GetDefaultProducedPrice();
          EveDatabase.setDefaultProducedPrice(new Task.Market(sys, p.order, p.manual));
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

  private Spinner sp_buyfrom_system;
  private RadioGroup rg_buyfrom_source;

  private IntegerEditText ed_default_me;
  private IntegerEditText ed_default_te;

  
  private List<Integer> system_ids;


  private class SellClickListener implements RadioButton.OnClickListener
  {
    private boolean requirement;
    
    public SellClickListener(boolean req)
    {
      requirement = req;
    }
    
    @Override
    public void onClick(View v)
    {
      if(requirement)
      {
        Task.Market p = EveDatabase.getDefaultRequiredPrice();
        EveDatabase.setDefaultRequiredPrice(new Task.Market(p.system, Task.Market.Order.SELL, p.manual));
      } else
      {
        Task.Market p = EveDatabase.GetDefaultProducedPrice();
        EveDatabase.setDefaultProducedPrice(new Task.Market(p.system, Task.Market.Order.SELL, p.manual));
      }
    }
  }

  private class BuyClickListener implements RadioButton.OnClickListener
  {
    private boolean requirement;
    
    public BuyClickListener(boolean req)
    {
      requirement = req;
    }

    @Override
    public void onClick(View v)
    {
      if(requirement)
      {
        Task.Market p = EveDatabase.getDefaultRequiredPrice();
        EveDatabase.setDefaultRequiredPrice(new Task.Market(p.system, Task.Market.Order.BUY, p.manual));
      } else
      {
        Task.Market p = EveDatabase.GetDefaultProducedPrice();
        EveDatabase.setDefaultProducedPrice(new Task.Market(p.system, Task.Market.Order.BUY, p.manual));
      }
    }
  }


  private class MeResearchChangeWatcher implements IntegerEditText.ValueListener
  {
    @Override
    public void onValueChanged(int new_value)
    {
      EveDatabase.SetDefaultBlueprintME(new_value);
    }
  }

  private class TeResearchChangeWatcher implements IntegerEditText.ValueListener
  {
    @Override
    public void onValueChanged(int new_value)
    {
      EveDatabase.SetDefaultBlueprintTE(new_value);
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
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    LayoutInflater inflater = getActivity().getLayoutInflater();
    View view = inflater.inflate(R.layout.settings, null);
    builder.setView(view);
    builder.setNeutralButton("Done", new DoneClickListener());

    sp_sellto_system = (Spinner) view.findViewById(R.id.sp_sellto_system);
    rg_sellto_source = (RadioGroup) view.findViewById(R.id.rg_sellto_source);
    RadioButton rb_sellto_sell = (RadioButton) view.findViewById(R.id.rb_sellto_sell);
    RadioButton rb_sellto_buy = (RadioButton) view.findViewById(R.id.rb_sellto_buy);

    sp_buyfrom_system = (Spinner) view.findViewById(R.id.sp_buyfrom_system);
    rg_buyfrom_source = (RadioGroup) view.findViewById(R.id.rg_buyfrom_source);
    RadioButton rb_buyfrom_sell = (RadioButton) view.findViewById(R.id.rb_buyfrom_sell);
    RadioButton rb_buyfrom_buy = (RadioButton) view.findViewById(R.id.rb_buyfrom_buy);
    ed_default_me = new IntegerEditText((EditText) view.findViewById(R.id.ed_settings_melevel), 0, 10, 0, new MeResearchChangeWatcher());
    ed_default_te = new IntegerEditText((EditText) view.findViewById(R.id.ed_settings_pelevel), 0, 20, 0, new TeResearchChangeWatcher());

    updateSettings();
    
    rb_sellto_sell.setOnClickListener(new SellClickListener(false));
    rb_sellto_buy.setOnClickListener(new BuyClickListener(false));
    rb_buyfrom_sell.setOnClickListener(new SellClickListener(true));
    rb_buyfrom_buy.setOnClickListener(new BuyClickListener(true));

    return builder.create();
  }
  
  public void updateSettings()
  {
    sp_sellto_system.setOnItemSelectedListener(null);
    sp_buyfrom_system.setOnItemSelectedListener(null);

    Task.Market req = EveDatabase.getDefaultRequiredPrice();
    Task.Market prod = EveDatabase.GetDefaultProducedPrice();
    
    if(req.order == Task.Market.Order.BUY)
    {
      rg_buyfrom_source.check(R.id.rb_buyfrom_buy);
    } else
    {
      rg_buyfrom_source.check(R.id.rb_buyfrom_sell);
    }      
    if(prod.order == Task.Market.Order.BUY)
    {
      rg_sellto_source.check(R.id.rb_sellto_buy);
    } else
    {
      rg_sellto_source.check(R.id.rb_sellto_sell);
    }      
    updateSystem(sp_sellto_system, prod.system);
    updateSystem(sp_buyfrom_system, req.system);

    sp_sellto_system.setOnItemSelectedListener(new SystemSelectedListener(false));
    sp_buyfrom_system.setOnItemSelectedListener(new SystemSelectedListener(true));
    ed_default_me.setValue(EveDatabase.getDefaultME(null));
    ed_default_te.setValue(EveDatabase.getDefaultTE(null));

  }
  
  @Override
  public void onResume()
  {
    super.onResume();
    Task.Market req = EveDatabase.getDefaultRequiredPrice();
    Task.Market prod = EveDatabase.GetDefaultProducedPrice();

    sp_sellto_system.setOnItemSelectedListener(null);
    updateSystem(sp_sellto_system, prod.system);
    sp_sellto_system.setOnItemSelectedListener(new SystemSelectedListener(false));

    sp_buyfrom_system.setOnItemSelectedListener(null);
    updateSystem(sp_buyfrom_system, req.system);
    sp_buyfrom_system.setOnItemSelectedListener(new SystemSelectedListener(true));
  }

  private void updateSystem(Spinner spinner, int system)
  {
    RecentSystemsDA.putSystem(system);

    system_ids = new ArrayList<>();
    List<String> system_names = new ArrayList<>();
    for(int id:RecentSystemsDA.getSystems())
    {
      system_ids.add(id);
      system_names.add(Starmap.getSolarSystem(id).Name);
    }
    system_ids.add(-1);
    system_names.add("[ Other ... ]");

    ArrayAdapter<String> sys_spinner_adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, system_names);
    sys_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(sys_spinner_adapter);
    spinner.setSelection(system_ids.indexOf(system));
  }
}
