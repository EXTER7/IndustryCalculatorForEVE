package com.exter.eveindcalc.data.refine;

import android.content.res.AssetManager;

import com.exter.eveindcalc.EICApplication;
import com.exter.eveindcalc.data.Index;
import com.exter.eveindcalc.data.exception.EveDataException;

import java.io.IOException;
import java.io.InputStream;

import exter.tsl.InvalidTSLException;
import exter.tsl.TSLObject;
import exter.tsl.TSLReader;

public class RefineDA
{
  static private Index index = null;
  
  @SuppressWarnings("TryFinallyCanBeTryWithResources")
  static public Refine getRefine(int refine)
  {
    try
    {
      AssetManager assets = EICApplication.getContext().getAssets();
      InputStream raw = assets.open("refine/" + String.valueOf(refine) + ".tsl");
      try
      {
        TSLReader reader = new TSLReader(raw);
        reader.moveNext();
        if(reader.getState() == TSLReader.State.OBJECT && reader.getName().equals("refine"))
        {
          return new Refine(new TSLObject(reader));
        } else
        {
          return null;
        }
      } catch(EveDataException | InvalidTSLException e)
      {
        throw new RuntimeException(e);
      } finally
      {
        raw.close();
      }
    } catch(IOException e)
    {
      return null;
    }
  }
  
  static public Index getIndex()
  {
    if(index == null)
    {
      try
      {
        AssetManager assets = EICApplication.getContext().getAssets();
        InputStream raw = assets.open("refine/index.tsl");
        TSLReader tsl = new TSLReader(raw);
        index = new Index(tsl);
        raw.close();
      } catch( EveDataException | IOException e)
      {
        throw new RuntimeException(e);
      }
    }
    return index;
  }
}
