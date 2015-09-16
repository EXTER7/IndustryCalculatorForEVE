package com.exter.eveindcalc.planet;

import android.app.Activity;
import android.content.Intent;

import com.exter.eveindcalc.data.Index;
import com.exter.eveindcalc.data.planet.Planet;
import com.exter.eveindcalc.data.planet.PlanetDA;
import com.exter.eveindcalc.itemlist.ItemListActivity;

public class PlanetResourceListActivity extends ItemListActivity
{
  @Override
  protected void onPickItem(int item)
  {
    Intent i = new Intent();
    i.putExtra("planetproduct", item);
    setResult(Activity.RESULT_OK,i);
    finish();
  }


  @Override
  protected String getListTitle()
  {
    return "Planet Resources";
  }

  @Override
  protected Index loadIndex()
  {
    Planet p = PlanetDA.getPlanet(getIntent().getIntExtra("planet", -1));
    return new Index(p.TypeName,p.Resources);
  }
}
