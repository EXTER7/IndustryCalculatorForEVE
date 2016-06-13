package com.exter.eveindcalc;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.exter.eveindcalc.data.EveDatabase;

import exter.eveindustry.task.Task;

public class SettingsSolarSystemDialogFragment extends SolarSystemDialogFragment
{
  boolean required;

  @Override
  protected void onSystemSelected(int system_id)
  {
     if(required)
     {
       Task.Market p = EveDatabase.getDefaultRequiredPrice();
       EveDatabase.setDefaultRequiredPrice(new Task.Market(system_id, p.order, p.manual,p.broker,p.transaction));
     } else
     {
       Task.Market p = EveDatabase.getDefaultProducedPrice();
       EveDatabase.setDefaultProducedPrice(new Task.Market(system_id, p.order, p.manual,p.broker,p.transaction));
     }
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState)
  {
    Dialog d = super.onCreateDialog(savedInstanceState);
    required = getArguments().getBoolean("required");
    return d;
  }
}
