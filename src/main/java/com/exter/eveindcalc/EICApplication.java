package com.exter.eveindcalc;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.exter.cache.Cache;
import com.exter.cache.LFUCache;
import com.exter.eveindcalc.data.EveDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import exter.eveindustry.dataprovider.item.Item;
import exter.eveindustry.task.GroupTask;
import exter.eveindustry.task.Task;
import exter.eveindustry.task.TaskLoadException;
import exter.tsl.InvalidTSLException;
import exter.tsl.TSLObject;
import exter.tsl.TSLReader;
import exter.tsl.TSLWriter;

public class EICApplication extends Application
{
  private class ImageCacheMiss implements Cache.IMissListener<Integer, Bitmap>
  {

    @Override
    public Bitmap onCacheMiss(Integer icon_id)
    {
      try
      {
        Context ctx = getApplicationContext();
        InputStream istr = ctx.getAssets().open("icons/" + String.valueOf(icon_id) + ".png");
        Bitmap bitmap = BitmapFactory.decodeStream(istr);
        istr.close();
        return bitmap;
      } catch(IOException e)
      {
        return null;
      }
    }
  }

  static private GroupTask tasks;

  public EveDatabase provider;

  private Cache<Integer,Bitmap> icon_cache;

  // return true if the app is running on the ARC runtime in Chrome.
  static public boolean isChrome()
  {
    return Build.BRAND.contains("chromium") && Build.MANUFACTURER.contains("chromium");
  }
  
  @Override
  public void onCreate()
  {
    super.onCreate();
    provider = new EveDatabase(this);
    icon_cache = new LFUCache<>(64,new ImageCacheMiss());
    Task.setDataProvider(provider);
  }

  // get the root group task.
  static GroupTask getTasks()
  {
    return tasks;
  }

  // write all tasks to storage.
  void saveTasks()
  {
    Log.i("Application","Saving tasks.");
    if(tasks == null)
    {
      return;
    }
    
    synchronized(EICApplication.class)
    {
      Context context = getApplicationContext();
      File tmp = new File(context.getFilesDir() + "/tasks.tsl.tmp");
      File dest = new File(context.getFilesDir() + "/tasks.tsl");
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

  private void createTaskGroup()
  {
    tasks = new GroupTask();
    saveTasks();
  }

  void loadTasks()
  {
    InputStream s = null;
    Context context = getApplicationContext();
    File path = new File(context.getFilesDir() + "/tasks.tsl");
    File backup = new File(context.getFilesDir() + "/tasks.tsl.bak");
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
    provider.closeDatabase();
    super.onTerminate();
  }

  public void setImageViewItemIcon(ImageView view, Item item)
  {
    if(item != null)
    {
      setImageViewItemIcon(view, item.Icon);
    }
  }

  public void setImageViewItemIcon(ImageView view, int iconid)
  {
    setImageViewItemIcon(view, iconid, 1.0f);
  }

  public void setImageViewItemIcon(ImageView view, int iconid, float scale)
  {
    Context ctx = getApplicationContext();
    int density = ctx.getResources().getDisplayMetrics().densityDpi;
    switch(density)
    {
      case DisplayMetrics.DENSITY_LOW:
        scale *= 0.5f;
        break;
      case DisplayMetrics.DENSITY_MEDIUM:
        scale *= 0.75f;
        break;
      default:
    }
    if(density > DisplayMetrics.DENSITY_XHIGH)
    {
      scale *= 2.0f;
    }
    int width, height;

    Bitmap bitmap = icon_cache.get(iconid);
    if(bitmap != null)
    {
      view.setImageBitmap(bitmap);
      width = (int) (bitmap.getWidth() * scale);
      height = (int) (bitmap.getHeight() * scale);
    } else
    {
      view.setImageResource(R.drawable.icon);
      width = (int) (64 * scale);
      height = (int) (64 * scale);
    }
    view.setScaleType(ImageView.ScaleType.MATRIX);
    view.setAdjustViewBounds(true);
    Matrix mat = new Matrix();
    mat.postScale(scale, scale);
    view.setImageMatrix(mat);
    Object params_obj = view.getLayoutParams();
    if(params_obj instanceof RelativeLayout.LayoutParams)
    {
      RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) params_obj;
      params.width = width;
      params.height = height;
      view.setLayoutParams(params);
    } else
    {
      LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) params_obj;
      params.width = width;
      params.height = height;
      view.setLayoutParams(params);
    }
  }

}
