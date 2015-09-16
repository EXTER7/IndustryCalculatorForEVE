package com.exter.eveindcalc.materials;

import android.support.v4.app.Fragment;
import android.util.Log;

import com.exter.eveindcalc.SolarSystemDialogFragment;

public class MaterialSolarSystemDialogFragment extends SolarSystemDialogFragment
{

  @Override
  protected void onSystemSelected(int systemid)
  {
    Fragment f = getActivity().getSupportFragmentManager().findFragmentByTag("MarketFetchDialogFragment");
    if(f == null)
    {
      Log.w("fragment", "Dialog Frament not found.");
      return;
    }
    MarketFetchDialogFragment df = (MarketFetchDialogFragment)f;
    df.setSystem(systemid);
  }
}
