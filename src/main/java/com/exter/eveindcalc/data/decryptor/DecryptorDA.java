package com.exter.eveindcalc.data.decryptor;

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

public class DecryptorDA
{
  static private SparseArray<Decryptor> decryptors = null;
  static private List<Decryptor> decryptor_list = null;
  
  static private class DecryptorComparator implements Comparator<Decryptor>
  {
    @Override
    public int compare(Decryptor lhs, Decryptor rhs)
    {
      return lhs.Item.Name.compareTo(rhs.Item.Name);
    }
  }
  
  static private void loadDecryptors()
  {
    try
    {
      AssetManager assets = EICApplication.getContext().getAssets();
      InputStream raw = assets.open("blueprint/decryptors.tsl");
      TSLReader tsl = new TSLReader(raw);

      tsl.moveNext();

      if(!tsl.getName().equals("decryptors"))
      {
        throw new EveDataException();
      }
      decryptors = new SparseArray<>();
      decryptor_list = new ArrayList<>();
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
          Decryptor d = new Decryptor(new TSLObject(tsl));
          decryptors.put(d.getID(), d);
          decryptor_list.add(d);
        }
      }
      raw.close();
    } catch(InvalidTSLException | EveDataException | IOException e)
    {
      throw new RuntimeException(e);
    }
    Collections.sort(decryptor_list, new DecryptorComparator());
  }
      
  
  static public Decryptor getDecryptor(int id)
  {
    if(decryptors == null)
    {
      loadDecryptors();
    }
    return decryptors.get(id);
  }
  
  
  static public List<Decryptor> getDecryptors()
  {
    if(decryptor_list == null)
    {
      loadDecryptors();
    }
    return Collections.unmodifiableList(decryptor_list);
  }
}
