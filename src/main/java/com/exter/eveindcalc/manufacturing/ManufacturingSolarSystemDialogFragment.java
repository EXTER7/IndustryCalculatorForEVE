package com.exter.eveindcalc.manufacturing;

import android.content.Context;
import android.content.SharedPreferences;

import com.exter.eveindcalc.EICFragmentActivity;
import com.exter.eveindcalc.SolarSystemDialogFragment;

import exter.eveindustry.task.ManufacturingTask;

public class ManufacturingSolarSystemDialogFragment extends SolarSystemDialogFragment
{

  @Override
  protected void onSystemSelected(int systemid)
  {
    EICFragmentActivity activity = (EICFragmentActivity)getActivity();
    ((ManufacturingTask)activity.getCurrentTask()).setSolarSystem(systemid);
    SharedPreferences sp = activity.getSharedPreferences("EIC", Context.MODE_PRIVATE);
    SharedPreferences.Editor ed = sp.edit();
    ed.putInt("manufacturing.system", systemid);
    ed.apply();
    activity.notiftyExtraExpenseChanged();
    activity.notifyTaskChanged();
  }
}
