package com.exter.eveindcalc.data.reaction;

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

public class ReactionDA
{
  static private Index index = null;
  static private Index index_moon = null;

  static private Cache<Integer,Reaction> cache = null;

  static private class ProductCacheMiss implements Cache.IMissListener<Integer,Reaction>
  {
    @Override
    public Reaction onCacheMiss(Integer rid)
    {
      try
      {
        AssetManager assets = EICApplication.getContext().getAssets();
        InputStream raw = assets.open("reaction/" + String.valueOf(rid) + ".tsl");
        try
        {
          TSLReader reader = new TSLReader(raw);
          reader.moveNext();
          if(reader.getState() == TSLReader.State.OBJECT && reader.getName().equals("reaction"))
          {
            return new Reaction(new TSLObject(reader));
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

  static public Reaction getReaction(int pid)
  {
    if(cache == null)
    {
      cache = new InfiniteCache<>(new ProductCacheMiss());
    }
    return cache.get(pid);
  }

  static public boolean isItemReaction(int reaction)
  {
    try
    {
      AssetManager assets = EICApplication.getContext().getAssets();
      InputStream raw = assets.open("reaction/" + String.valueOf(reaction) + ".tsl");
      raw.close();
      return true;
    } catch(IOException e)
    {
      return false;
    }
  }

  static public Index getIndex()
  {
    if(index == null)
    {
      try
      {
        AssetManager assets = EICApplication.getContext().getAssets();
        InputStream raw = assets.open("reaction/index.tsl");
        TSLReader tsl = new TSLReader(raw);
        index = new Index(tsl);
        raw.close();
      } catch(EveDataException | IOException e)
      {
        throw new RuntimeException(e);
      }
    }
    return index;
  }

  static public Index getMoonIndex()
  {
    if(index_moon == null)
    {
      TSLReader tsl;
      InputStream raw;
      try
      {
        AssetManager assets = EICApplication.getContext().getAssets();
        raw = assets.open("reaction/index_moon.tsl");
        tsl = new TSLReader(raw);
        index_moon = new Index(tsl);
        raw.close();
      } catch(EveDataException | IOException e)
      {
        throw new RuntimeException(e);
      }
    }
    return index_moon;
  }
}
