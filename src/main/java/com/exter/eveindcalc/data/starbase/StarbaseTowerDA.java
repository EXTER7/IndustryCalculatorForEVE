package com.exter.eveindcalc.data.starbase;

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

public class StarbaseTowerDA
{
  static private class ToweNameComparator implements Comparator<Integer>
  {
    @Override
    public int compare(Integer lhs, Integer rhs)
    {
      return GetTower(lhs).Name.compareTo(GetTower(rhs).Name);
    }
  }

  static private SparseArray<StarbaseTower> towers = null;
  
  static private void LoadTowers()
  {
    TSLReader tsl = null;
    InputStream raw = null;
    try
    {
      AssetManager assets = EICApplication.getContext().getAssets();
      raw = assets.open("starbases.tsl");
      tsl = new TSLReader(raw);
    } catch(IOException e)
    {
      throw new RuntimeException(e);
    }

    try
    {
      tsl.moveNext();

      if(!tsl.getName().equals("starbases"))
      {
        throw new EveDataException();
      }
      towers = new SparseArray<StarbaseTower>();
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
          StarbaseTower t = new StarbaseTower(new TSLObject(tsl));
          towers.put(t.TowerItem.ID, t);
        }
      }
    } catch(InvalidTSLException | EveDataException | IOException e)
    {
      throw new RuntimeException(e);
    }

    try
    {
      raw.close();
    } catch(IOException e)
    {
      throw new RuntimeException(e);
    }
  }
      
  
  static public StarbaseTower GetTower(int id)
  {
    if(towers == null)
    {
      LoadTowers();  
    }
    return towers.get(id);
  }
  
  
  static public List<Integer> GetTowerIDs()
  {
    if(towers == null)
    {
      LoadTowers();  
    }
    List<Integer> ids = new ArrayList<>();
    int i;
    for(i = 0; i < towers.size(); i++)
    {
      ids.add(towers.keyAt(i));
    }
    Collections.sort(ids, new ToweNameComparator());
    return ids;
  }
}
