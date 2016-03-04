package com.exter.eveindcalc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


import com.exter.eveindcalc.data.EveDatabase;

import java.util.ArrayList;
import java.util.List;


public abstract class SolarSystemDialogFragment extends DialogFragment
{
  protected abstract void onSystemSelected(int system_id);
  
  private class RegionSelectedListener implements Spinner.OnItemSelectedListener
  {
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
      setRegionSystems(region_ids.get(pos));
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }
  }

  private class SystemSelectedListener implements Spinner.OnItemSelectedListener
  {
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
      system = system_ids.get(pos);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }
  }

  private Spinner sp_system;

  static private List<Integer> region_ids = null;
  static private ArrayList<CharSequence> region_names = null;
  private List<Integer> system_ids;

  private int system;

  private class MarketFetchClickListener implements DialogInterface.OnClickListener
  {
    @Override
    public void onClick(DialogInterface dialog, int which)
    {
      onSystemSelected(system);
    }
  }

  @NonNull
  @SuppressLint("InflateParams")
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState)
  {
    FragmentActivity activity = getActivity();
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    LayoutInflater inflater = activity.getLayoutInflater();
    View view = inflater.inflate(R.layout.solarsystem, null);
    builder.setView(view);
    builder.setPositiveButton("Set", new MarketFetchClickListener());
    builder.setNegativeButton("Cancel", null);

    Spinner sp_region = (Spinner) view.findViewById(R.id.sp_solarsystem_region);
    sp_system = (Spinner) view.findViewById(R.id.sp_solarsystem_system);

    system = 30000142;


    if(region_ids == null)
    {
      region_names = new ArrayList<>();
      region_ids = new ArrayList<>();
      Cursor c = EveDatabase.getDatabase().query("regions",new String[] { "id", "name" },null, null, null, null, "name");
      while(c.moveToNext())
      {
        region_ids.add(c.getInt(0));
        region_names.add(c.getString(1));
      }
      c.close();
    }
    ArrayAdapter<CharSequence> region_adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, region_names);
    region_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    int reg = EICApplication.getDataProvider().getSolarSystem(system).Region;
    sp_region.setAdapter(region_adapter);
    sp_region.setSelection(region_ids.indexOf(reg), true);
    setRegionSystems(reg);
    sp_region.setOnItemSelectedListener(new RegionSelectedListener());
    sp_system.setOnItemSelectedListener(new SystemSelectedListener());
    return builder.create();
  }

  private void setRegionSystems(int region_id)
  {
    Activity act = getActivity();
    system_ids = new ArrayList<>();
    ArrayList<CharSequence> system_names = new ArrayList<>();
    Cursor c = EveDatabase.getDatabase().query("solar_systems",new String[] { "id", "name" },"rid = ?", new String[] {String.valueOf(region_id)}, null, null, "name");
    while(c.moveToNext())
    {
      system_ids.add(c.getInt(0));
      system_names.add(c.getString(1));
    }
    c.close();

    ArrayAdapter<CharSequence> systems_adapter = new ArrayAdapter<>(act, android.R.layout.simple_spinner_item, system_names);
    systems_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    sp_system.setAdapter(systems_adapter);
    int index = system_ids.indexOf(system);
    if(index == -1)
    {
      system = system_ids.get(0);
      sp_system.setSelection(0, true);
    } else
    {
      sp_system.setSelection(index, true);
    }
  }
}
