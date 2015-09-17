package com.exter.eveindcalc.data.planet;

import android.content.res.AssetManager;
import android.util.SparseArray;

import com.exter.eveindcalc.EICApplication;
import com.exter.eveindcalc.data.exception.EveDataException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import exter.tsl.InvalidTSLException;
import exter.tsl.TSLObject;
import exter.tsl.TSLReader;

public class PlanetDA
{
  static private class PlanetNameComparator implements Comparator<Integer>
  {
    @Override
    public int compare(Integer lhs, Integer rhs)
    {
      return getPlanet(lhs).TypeName.compareTo(getPlanet(rhs).TypeName);
    }
  }

  static private SparseArray<Planet> planets = null;
  
  static private void loadPlanets()
  {
    try
    {
      AssetManager assets = EICApplication.getContext().getAssets();
      InputStream raw = assets.open("planet/planets.tsl");
      TSLReader tsl = new TSLReader(raw);

      tsl.moveNext();

      if(!tsl.getName().equals("planets"))
      {
        throw new EveDataException();
      }
      planets = new SparseArray<>();
      while(true)
      {
        tsl.moveNext();
        TSLReader.State type = tsl.getState();
        if(type == TSLReader.State.ENDOBJECT)
        {
          break;
        }

        if(type == TSLReader.State.OBJECT)
        {
          Planet p = new Planet(new TSLObject(tsl));
          planets.put(p.ID, p);
        }
      }
      raw.close();
    } catch(InvalidTSLException | EveDataException | IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  static public Planet getPlanet(int id)
  {
    if(planets == null)
    {
      loadPlanets();
    }
    return planets.get(id);
  }
  
  
  static public List<Integer> getPlanetIDs()
  {
    if(planets == null)
    {
      loadPlanets();
    }
    List<Integer> ids = new ArrayList<>();
    int i;
    for(i = 0; i < planets.size(); i++)
    {
      ids.add(planets.keyAt(i));
    }
    Collections.sort(ids, new PlanetNameComparator());
    return Collections.unmodifiableList(ids);
  }
}
