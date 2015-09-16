package com.exter.eveindcalc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.exter.eveindcalc.data.starmap.SolarSystem;
import com.exter.eveindcalc.data.starmap.solarSystemRegion;
import com.exter.eveindcalc.data.starmap.Starmap;

import java.util.ArrayList;
import java.util.List;

public abstract class SolarSystemDialogFragment extends DialogFragment
{
  protected abstract void onSystemSelected(int systemid);
  
  private class RegionSelectedListener implements Spinner.OnItemSelectedListener
  {
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
      setRegionSystems(regions.get(pos));
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
      system = systems.get(pos).ID;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }
  }

  private Spinner sp_system;

  private List<solarSystemRegion> regions;
  private List<SolarSystem> systems;

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
    

    regions = Starmap.getRegions();
    ArrayList<CharSequence> region_list = new ArrayList<>();
    for(solarSystemRegion r : regions)
    {
      region_list.add(r.Name);
    }
    ArrayAdapter<CharSequence> region_adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, region_list);
    region_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    solarSystemRegion reg = Starmap.getRegion(Starmap.getSolarSystem(system).Region);
    sp_region.setAdapter(region_adapter);
    sp_region.setSelection(regions.indexOf(reg), true);
    setRegionSystems(reg);
    sp_region.setOnItemSelectedListener(new RegionSelectedListener());
    sp_system.setOnItemSelectedListener(new SystemSelectedListener());
    return builder.create();
  }

  private void setRegionSystems(solarSystemRegion r)
  {
    Activity act = getActivity();
    systems = Starmap.getSolarSystems(r.ID);
    ArrayList<CharSequence> systems_list = new ArrayList<>();
    for(SolarSystem s : systems)
    {
      systems_list.add(s.Name);
    }
    ArrayAdapter<CharSequence> systems_adapter = new ArrayAdapter<>(act, android.R.layout.simple_spinner_item, systems_list);
    systems_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    sp_system.setAdapter(systems_adapter);
    int index = systems.indexOf(Starmap.getSolarSystem(system));
    if(index == -1)
    {
      system = systems.get(0).ID;
      sp_system.setSelection(0, true);
    } else
    {
      sp_system.setSelection(index, true);
    }
  }
}
