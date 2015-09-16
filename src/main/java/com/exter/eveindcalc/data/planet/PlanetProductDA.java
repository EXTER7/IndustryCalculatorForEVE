package com.exter.eveindcalc.data.planet;

import android.content.res.AssetManager;

import com.exter.cache.Cache;
import com.exter.cache.InfiniteCache;
import com.exter.eveindcalc.EICApplication;
import com.exter.eveindcalc.data.Index;
import com.exter.eveindcalc.data.exception.EveDataException;

import java.io.IOException;
import java.io.InputStream;

import exter.tsl.InvalidTSLException;
import exter.tsl.TSLObject;
import exter.tsl.TSLReader;

public class PlanetProductDA
{
  static private Index index = null;
  static private Index index_advanced = null;

  static private Cache<Integer,PlanetProduct> cache = null;

  static private class ProductCacheMiss implements Cache.IMissListener<Integer,PlanetProduct>
  {
    @Override
    public PlanetProduct onCacheMiss(Integer pid)
    {
      try
      {
        AssetManager assets = EICApplication.getContext().getAssets();
        InputStream raw = assets.open("planet/" + String.valueOf(pid) + ".tsl");
        try
        {
          TSLReader reader = new TSLReader(raw);
          reader.moveNext();
          if(reader.getState() == TSLReader.State.OBJECT && reader.getName().equals("planetbuilding"))
          {
            return new PlanetProduct(new TSLObject(reader));
          } else
          {
            return null;
          }
        } catch(EveDataException e)
        {
          raw.close();
          return null;
        } catch(InvalidTSLException e)
        {
          return null;
        }
      } catch(IOException e)
      {
        return null;
      }
    }
  }

  static public PlanetProduct GetProduct(int pid)
  {
    if(cache == null)
    {
      cache = new InfiniteCache<>(new ProductCacheMiss());
    }
    return cache.get(pid);
  }

  static public boolean IsItemFromPlanet(int itemid)
  {
    try
    {
      AssetManager assets = EICApplication.getContext().getAssets();
      InputStream raw = assets.open("planet/" + String.valueOf(itemid) + ".tsl");
      raw.close();
      return true;
    } catch(IOException e)
    {
      return false;
    }
  }

  static public Index GetIndexAdvanced()
  {
    if(index_advanced == null)
    {
      TSLReader tsl;
      InputStream raw;
      try
      {
        AssetManager assets = EICApplication.getContext().getAssets();
        raw = assets.open("planet/index_advanced.tsl");
        tsl = new TSLReader(raw);
      } catch(IOException e)
      {
        throw new RuntimeException(e);
      }

      try
      {
        index_advanced = new Index(tsl);
      } catch(EveDataException e1)
      {
        throw new RuntimeException(e1);
      }

      try
      {
        raw.close();
      } catch(IOException e)
      {
        throw new RuntimeException(e);
      }
    }
    return index_advanced;
  }

  static public Index GetIndex()
  {
    if(index == null)
    {
      TSLReader tsl;
      InputStream raw;
      try
      {
        AssetManager assets = EICApplication.getContext().getAssets();
        raw = assets.open("planet/index.tsl");
        tsl = new TSLReader(raw);
      } catch(IOException e)
      {
        throw new RuntimeException(e);
      }

      try
      {
        index = new Index(tsl);
      } catch(EveDataException e1)
      {
        throw new RuntimeException(e1);
      }

      try
      {
        raw.close();
      } catch(IOException e)
      {
        throw new RuntimeException(e);
      }
    }
    return index;
  }
}
