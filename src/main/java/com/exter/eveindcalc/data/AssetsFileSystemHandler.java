package com.exter.eveindcalc.data;

import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;


import exter.eveindustry.data.filesystem.IFileSystemHandler;

public class AssetsFileSystemHandler implements IFileSystemHandler
{
  private AssetManager assets;

  public AssetsFileSystemHandler(AssetManager assets)
  {
    this.assets = assets;
  }

  @Override
  public <T> T readFile(String path, IReadHandler<T> handler)
  {
    try
    {
      InputStream stream = assets.open(path);
      T result = handler.readFile(stream);
      stream.close();
      return result;
    } catch(IOException e)
    {
      return null;
    }
  }
}