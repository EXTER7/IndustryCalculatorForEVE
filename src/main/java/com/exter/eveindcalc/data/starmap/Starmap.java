package com.exter.eveindcalc.data.starmap;

import android.content.res.AssetManager;
import android.util.SparseArray;

import com.exter.eveindcalc.EICApplication;
import com.exter.eveindcalc.data.exception.EveDataException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import exter.tsl.InvalidTSLException;
import exter.tsl.TSLObject;
import exter.tsl.TSLReader;

public class Starmap
{
  
  static private SparseArray<SolarSystemRegion> regions = null;
  static private SparseArray<SolarSystem> systems = null;
  static private List<SolarSystemRegion> region_list = null;
  static private List<SolarSystem> system_list = null;
  
  static private void loadStarmap()
  {
    AssetManager assets = EICApplication.getContext().getAssets();
    TSLReader reader = null;
    InputStream raw = null;
    try
    {
      raw = assets.open("starmap.tsl");
      reader = new TSLReader(raw);
    } catch(IOException e)
    {
      e.printStackTrace();
    }

    try
    {
      systems = new SparseArray<>();
      system_list = new ArrayList<>();

      regions = new SparseArray<>();
      region_list = new ArrayList<>();
      TSLObject node = new TSLObject();
      assert reader != null;
      reader.moveNext();

      if(!reader.getName().equals("starmap"))
      {
        throw new EveDataException();
      }
      while(true)
      {
        reader.moveNext();
        TSLReader.State type = reader.getState();
        if(type == TSLReader.State.ENDOBJECT)
        {
          break;
        } else if(type == TSLReader.State.OBJECT)
        {
          String node_name = reader.getName();
          switch (node_name)
          {
            case "r":
            {
              node.loadFromReader(reader);
              int id = node.getStringAsInt("id", -1);
              String name = node.getString("name", null);
              if (id < 0 || name == null)
              {
                throw new EveDataException();
              }
              SolarSystemRegion r = new SolarSystemRegion(id, name);
              regions.put(id, r);
              region_list.add(r);
              break;
            }
            case "s":
            {
              node.loadFromReader(reader);
              int id = node.getStringAsInt("id", -1);
              String name = node.getString("name", null);
              int region = node.getStringAsInt("region", -1);
              if (id < 0 || name == null || region < 0)
              {
                throw new EveDataException();
              }
              SolarSystem s = new SolarSystem(id, name, region);
              system_list.add(s);
              systems.put(id, s);
              break;
            }
            default:
              reader.skipObject();
              break;
          }
        }
      }
    } catch(InvalidTSLException | EveDataException | IOException e)
    {
      e.printStackTrace();
    }

    try
    {
      raw.close();
    } catch(IOException e)
    {
      e.printStackTrace();
    }
  }
  
  static public List<SolarSystemRegion> getRegions()
  {
    if(region_list == null)
    {
      loadStarmap();
    }
    return region_list;
  }

  static public List<SolarSystem> getSolarSystems(int region)
  {
    if(system_list == null)
    {
      loadStarmap();
    }
    List<SolarSystem> list = new ArrayList<>();
    for(SolarSystem s:system_list)
    {
      if(s.Region == region)
      {
        list.add(s);
      }
    }
    return list;
  }

  static public SolarSystemRegion getRegion(int id)
  {
    if(regions == null)
    {
      loadStarmap();
    }

    return regions.get(id);
  }
  
  static public SolarSystem getSolarSystem(int id)
  {
    if(systems == null)
    {
      loadStarmap();
    }
    return systems.get(id);
  }
}
