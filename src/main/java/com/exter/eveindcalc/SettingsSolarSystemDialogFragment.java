package com.exter.eveindcalc;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.exter.eveindcalc.data.EveDatabase;

import exter.eveindustry.market.Market;


public class SettingsSolarSystemDialogFragment extends SolarSystemDialogFragment
{
  private boolean required;

  @Override
  protected void onSystemSelected(int system_id)
  {
     if(required)
     {
       Market p = database.getDefaultRequiredPrice();
       database.setDefaultRequiredMarket(p.withSolarSystem(system_id));
     } else
     {
       Market p = database.getDefaultProducedPrice();
       database.setDefaultProducedMarket(p.withSolarSystem(system_id));
     }
  }

  private EveDatabase database;

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState)
  {
    database = ((EICApplication)getActivity().getApplication()).database;
    Dialog d = super.onCreateDialog(savedInstanceState);
    required = getArguments().getBoolean("required");
    return d;
  }
}
