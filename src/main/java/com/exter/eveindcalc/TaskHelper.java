package com.exter.eveindcalc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.exter.cache.Cache;
import com.exter.cache.LFUCache;
import com.exter.eveindcalc.data.inventory.InventoryDA;
import com.exter.eveindcalc.data.inventory.Item;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import exter.eveindustry.data.planet.IPlanetBuilding;
import exter.eveindustry.data.reaction.IReaction;
import exter.eveindustry.item.ItemStack;
import exter.eveindustry.task.GroupTask;
import exter.eveindustry.task.ManufacturingTask;
import exter.eveindustry.task.PlanetTask;
import exter.eveindustry.task.ReactionTask;
import exter.eveindustry.task.RefiningTask;
import exter.eveindustry.task.Task;

public class TaskHelper
{

  // Used to provide the task icon and description based on the type of task
  private interface ITaskIconProvider
  {
    int getIcon(Task task);
    boolean hasBackground(Task task);
    String getDescription(Task task);
  }

  static private class ManufacturingTaskDescriptionProvider implements ITaskIconProvider
  {
    @Override
    public int getIcon(Task task)
    {
       return ((Item)((ManufacturingTask)task).getBlueprint().getProduct().item).Icon;
    }

    @Override
    public boolean hasBackground(Task task)
    {
      return true;
    }

    @Override
    public String getDescription(Task task)
    {
      ManufacturingTask t = (ManufacturingTask)task;
      int r = t.getRuns();
      int c = t.getCopies();
      return "ME: " + String.valueOf(t.getME())
           + "\nTE: " + String.valueOf(t.getTE())
           + "\n" + String.valueOf(r) + (r == 1?" run":" runs")
           + "\n" + String.valueOf(c) + (c == 1?" copy":" copies");
    }
  }

  static private class RefiningTaskIconProvider implements ITaskIconProvider
  {
    @Override
    public int getIcon(Task task)
    {
      return ((Item)((RefiningTask)task).getRefinable().getRequiredItem().item).Icon;
    }

    @Override
    public boolean hasBackground(Task task)
    {
      return true;
    }

    @Override
    public String getDescription(Task task)
    {
      RefiningTask t = (RefiningTask)task;
      return String.valueOf(t.getOreAmount()) + " units\n" + String.valueOf((int)(t.getEfficiency() * 100)) + "% Effiency";
    }
  }

  static private class ReactionTaskIconProvider implements ITaskIconProvider
  {
    @Override
    public int getIcon(Task task)
    {
      List<ItemStack> m = task.getProducedMaterials();
      if(m.size() == 1)
      {
        return ((Item)m.iterator().next().item).Icon;
      }
      return InventoryDA.getItem((((ReactionTask) task).getStarbaseTower().getID())).Icon;
    }

    @Override
    public boolean hasBackground(Task task)
    {
      return task.getProducedMaterials().size() == 1;
    }

    @Override
    public String getDescription(Task task)
    {
      ReactionTask t = (ReactionTask)task;
      int reactors = 0;
      int miners = 0;
      for(IReaction r:t.getReactions())
      {
        if(r.getInputs().size() > 0)
        {
          reactors++;
        } else
        {
          miners++;
        }
      }

      return String.valueOf(reactors) + (reactors == 1?" reactor":" reactors")
          + "\n" + String.valueOf(miners) + (miners == 1?" miner":" miners");
    }
  }

  static private class PlanetTaskIconProvider implements ITaskIconProvider
  {
    @Override
    public int getIcon(Task task)
    {
      List<ItemStack> m = task.getProducedMaterials();
      if(m.size() == 1)
      {
        return ((Item)m.iterator().next().item).Icon;
      }
      return InventoryDA.getItem(((PlanetTask) task).getPlanet().getID()).Icon;
    }

    @Override
    public boolean hasBackground(Task task)
    {
      return task.getProducedMaterials().size() == 1;
    }

    @Override
    public String getDescription(Task task)
    {
      PlanetTask t = (PlanetTask)task;
      int factories = 0;
      int extractors = 0;
      for(IPlanetBuilding p:t.getBuildings())
      {
        if(p.getMaterials().size() > 0)
        {
          factories++;
        } else
        {
          extractors++;
        }
      }
      
      return String.valueOf(factories) + (factories == 1?" factory":" factories")
            + "\n" + String.valueOf(extractors) + (extractors == 1?" extractor":" extractors");
    }
  }

  static private class GroupTaskIconProvider implements ITaskIconProvider
  {
    @Override
    public int getIcon(Task task)
    {
      List<ItemStack> m = task.getProducedMaterials();
      if(m.size() == 1)
      {
        return ((Item)m.iterator().next().item).Icon;
      }
      return -1;
    }

    @Override
    public boolean hasBackground(Task task)
    {
      return task.getProducedMaterials().size() == 1;
    }

    @Override
    public String getDescription(Task task)
    {
      GroupTask t = (GroupTask)task;
      int s = t.getTaskList().size();
      return String.valueOf(s) + (s == 1?" Task":" Tasks")
            +"\nScale: " + String.valueOf(t.getScale());
    }
  }

  static public Task.Market PriceFromBundle(Bundle bundle)
  {
    int system = bundle.getInt("system",30000142);
    Task.Market.Order source = Task.Market.Order.fromInt(bundle.getInt("source",Task.Market.Order.SELL.value));
    String manual_str = bundle.getString("manual");
    BigDecimal manual;
    if(manual_str == null)
    {
      manual = BigDecimal.ZERO;
    } else
    {
      manual = new BigDecimal(manual_str);
    }
    return new Task.Market(system, source, manual);
  }

  static public void PriceToBundle(Task.Market p, Bundle bundle)
  {
    bundle.putInt("system", p.system);
    bundle.putInt("source", p.order.value);
    bundle.putString("manual", p.manual.toPlainString());
  }

  static private class CacheMiss implements Cache.IMissListener<Integer, Bitmap>
  {

    @Override
    public Bitmap onCacheMiss(Integer icon_id)
    {
      try
      {
        Context ctx = EICApplication.getContext();
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

  static private Cache<Integer,Bitmap> cache = new LFUCache<>(64,new CacheMiss());

  static private Map<Class<? extends Task>,ITaskIconProvider> icon_providers;
  
  static public void setImageViewItemIcon(ImageView view, Item item)
  {
    if(item != null)
    {
      setImageViewItemIcon(view, item.Icon);
    }
  }

  static public void setImageViewItemIcon(ImageView view, int iconid)
  {
    setImageViewItemIcon(view, iconid, 1.0f);
  }

  static public void setImageViewItemIcon(ImageView view, int iconid, float scale)
  {
    Context ctx = EICApplication.getContext();
    switch(ctx.getResources().getDisplayMetrics().densityDpi)
    {
      case DisplayMetrics.DENSITY_LOW:
        scale *= 0.5f;
        break;
      case DisplayMetrics.DENSITY_MEDIUM:
        scale *= 0.75f;
        break;
      default:
    }
    int width, height;

    Bitmap bitmap = cache.get(iconid);
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
    view.setScaleType(ScaleType.MATRIX);
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

  static public int getTaskIcon(Task task)
  {
    if(task == null)
    {
      return -1;
    }
    return icon_providers.get(task.getClass()).getIcon(task);
  }

  static public boolean taskHasBackground(Task task)
  {
    return task != null && icon_providers.get(task.getClass()).hasBackground(task);
  }
  
  static public String getTaskDescription(Task task)
  {
    if(task == null)
    {
      return "";
    }
    return icon_providers.get(task.getClass()).getDescription(task);
  }

  static
  {
    icon_providers = new HashMap<>();
    icon_providers.put(ManufacturingTask.class, new ManufacturingTaskDescriptionProvider());
    icon_providers.put(RefiningTask.class, new RefiningTaskIconProvider());
    icon_providers.put(ReactionTask.class, new ReactionTaskIconProvider());
    icon_providers.put(GroupTask.class, new GroupTaskIconProvider());
    icon_providers.put(PlanetTask.class, new PlanetTaskIconProvider());
  }
}
