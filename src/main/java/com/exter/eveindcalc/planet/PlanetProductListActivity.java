package com.exter.eveindcalc.planet;

import android.app.Activity;
import android.content.Intent;


import com.exter.eveindcalc.EICApplication;
import com.exter.eveindcalc.data.EveDatabase;
import com.exter.eveindcalc.itemlist.ItemListActivity;

import exter.eveindustry.dataprovider.index.Index;
import exter.eveindustry.dataprovider.planet.Planet;

public class PlanetProductListActivity extends ItemListActivity
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
    return "Planet Products";
  }

  @Override
  protected Index loadIndex()
  {
    EveDatabase provider = ((EICApplication)getApplication()).provider;
    Planet planet = provider.getPlanet(getIntent().getIntExtra("planet", -1));
    return provider.getPlanetProductIndex(planet.Advanced);
  }

}
