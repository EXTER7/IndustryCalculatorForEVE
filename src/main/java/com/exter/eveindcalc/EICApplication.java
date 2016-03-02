package com.exter.eveindcalc;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.exter.eveindcalc.data.EveDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import exter.eveindustry.task.GroupTask;
import exter.eveindustry.task.Task;
import exter.eveindustry.task.TaskLoadException;
import exter.tsl.InvalidTSLException;
import exter.tsl.TSLObject;
import exter.tsl.TSLReader;
import exter.tsl.TSLWriter;

public class EICApplication extends Application
{
  static private Context context;
  static private GroupTask tasks;

  static private EveDatabase provider;

  // return true if the app is running on the ARC runtime in Chrome.
  static public boolean isChrome()
  {
    return Build.BRAND.contains("chromium") && Build.MANUFACTURER.contains("chromium");
  }
  
  @Override
  public void onCreate()
  {
    super.onCreate();
    context = getApplicationContext();
    provider = new EveDatabase(getAssets());
    Task.setDataProvider(provider);
  }

  // get the root group task.
  static public GroupTask getTasks()
  {
    return tasks;
  }

  // write all tasks to storage.
  static public void saveTasks()
  {
    Log.i("Application","Saving tasks.");
    if(tasks == null)
    {
      return;
    }
    
    synchronized(EICApplication.class)
    {
      File tmp = new File(getContext().getFilesDir() + "/tasks.tsl.tmp");
      File dest = new File(getContext().getFilesDir() + "/tasks.tsl");
      try
      {
        OutputStream s = new FileOutputStream(tmp);
        TSLWriter w = new TSLWriter(s);
        TSLObject tsl = new TSLObject();
        tasks.writeToTSL(tsl);
        tsl.write(w, "task");
        s.close();
        //noinspection ResultOfMethodCallIgnored
        dest.delete();
        //noinspection ResultOfMethodCallIgnored
        tmp.renameTo(dest);
      } catch(IOException ignored)
      {

      }
    }
  }

  static public void createTaskGroup()
  {
    tasks = new GroupTask();
    saveTasks();
  }

  static public void loadTasks()
  {
    InputStream s = null;
    File path = new File(getContext().getFilesDir() + "/tasks.tsl");
    File backup = new File(getContext().getFilesDir() + "/tasks.tsl.bak");
    if(tasks != null)
    {
      return;
    }
    try
    {
      try
      {
        Log.i("Application","Loading Tasks");
        s = new FileInputStream(path);
        TSLReader r = new TSLReader(s);
        r.moveNext();

        TSLObject tsl = new TSLObject(r);
        synchronized(EICApplication.class)
        {
          tasks = (GroupTask) Task.loadPromTSL(tsl);
        }
      } catch(TaskLoadException | InvalidTSLException e)
      {
        s.close();
        //noinspection ResultOfMethodCallIgnored
        backup.delete();
        //noinspection ResultOfMethodCallIgnored
        path.renameTo(backup);
        createTaskGroup();
        e.printStackTrace();
      }
      s.close();
    } catch(FileNotFoundException e)
    {
      createTaskGroup();
    } catch(IOException e)
    {
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public void onTerminate()
  {
    EveDatabase.closeDatabase();
    super.onTerminate();
  }

  static public Context getContext()
  {
    return context;
  }

  static public EveDatabase getDataProvider()
  {
    return provider;
  }
}
