package com.exter.eveindcalc.group;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.Toast;

import com.exter.eveindcalc.EICApplication;
import com.exter.eveindcalc.EICFragmentActivity;
import com.exter.eveindcalc.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import exter.eveindustry.task.GroupTask;
import exter.eveindustry.task.Task;
import exter.eveindustry.task.TaskFactory;
import exter.eveindustry.task.TaskLoadException;
import exter.eveindustry.util.Utils;
import exter.tsl.TSLObject;
import exter.tsl.TSLWriter;

public class GroupMenuDialogFragment extends DialogFragment
{ 
  private class MoveToListComparator implements Comparator<CharSequence>
  {
    private Map<CharSequence,Integer> weights = null;
    
    MoveToListComparator()
    {
      if(weights == null)
      {
        weights = new HashMap<>();
        weights.put(String.format(" [%s]",getString(R.string.group_parent)), 1);
        weights.put(String.format(" [%s]",getString(R.string.group_new)), 2);
      }
    }
    
    @Override
    public int compare(CharSequence lhs, CharSequence rhs)
    {
      int wl = Utils.mapGet(weights, lhs, 0);
      int wr = Utils.mapGet(weights, rhs, 0);
      
      if(wl*wr == 0)
      {
        return lhs.toString().compareTo(rhs.toString());
      } else
      {
        return wr - wl;
      }
    }
  }

  private class TaskMenuDialogClickListener implements DialogInterface.OnClickListener
  {
    private class MoveToClickListener implements DialogInterface.OnClickListener
    {
      private CharSequence[] choices;
      private boolean parent;
      
      MoveToClickListener(CharSequence[] ch, boolean has_parent)
      {
        choices = ch;
        parent = has_parent;
      }
      
      @Override
      public void onClick(DialogInterface dialog, int which)
      {

        GroupTask group = (GroupTask)activity.getCurrentTask();
        Task t = group.getTask(name);
        if(parent)
        {
          if(which == 1)
          {
            group.removeTask(name);
            activity.getCurrentTaskParentGroup().addTask(name, t);
            activity.notifyTaskChanged();
            activity.onProfitChanged();
            return;
          }
        }
        if(which == 0)
        {
          group.removeTask(name);
          GroupTask newgroup = factory.newGroup();
          newgroup.addTask(name, t);
          group.addTask(name, newgroup);
          activity.notifyTaskChanged();
          activity.onProfitChanged();
          return;
        }
        group.removeTask(name);
        ((GroupTask)group.getTask(String.valueOf(choices[which]))).addTask(name, t);
        activity.notifyTaskChanged();
        activity.onProfitChanged();
        dialog.dismiss();
      }
    }

    private class ExportOverrideDialogClockListener implements DialogInterface.OnClickListener
    {

      private final File file;
      private final Task task;

      ExportOverrideDialogClockListener(File f, Task t)
      {
        file = f;
        task = t;
      }

      @Override
      public void onClick(DialogInterface dialog, int which)
      {
        if(which == DialogInterface.BUTTON_POSITIVE)
        {
          exportTask(task, file);
        }
      }
    }

    private void exportTask(Task t, File f)
    {
      try
      {
        FileOutputStream fd = new FileOutputStream(f);
        TSLWriter w = new TSLWriter(fd);
        TSLObject tsl = new TSLObject();
        t.writeToTSL(tsl);
        tsl.putString("name", name);
        tsl.write(w, "task");
        fd.close();
        Toast.makeText(activity, "Task exported to '" + f.getAbsolutePath() + "'.", Toast.LENGTH_LONG).show();
      } catch(IOException e)
      {
        Toast.makeText(activity, "Error Exporting task", Toast.LENGTH_SHORT).show();
      }
    }

    private void export(GroupTask group)
    {
      {
        Task t = group.getTask(name);
        File external_dir = Environment.getExternalStorageDirectory();
        if(external_dir == null)
        {
          Log.e("TaskExport", "external_dir is null");
          return;
        }
        File dir = new File(external_dir + "/Eve Industry Calculator");
        if(!dir.mkdir())
        {
          Log.w("TaskExport", "cannnot create '/Eve Industry Calculator' directory");
        }
        File export = new File(dir + "/" + name + ".eic");
        if(export.exists())
        {
          AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
          ExportOverrideDialogClockListener l = new ExportOverrideDialogClockListener(export,t);
          builder.setMessage("File exists, overwrite?").setPositiveButton("Yes", l).setNegativeButton("No", l).show();
        } else
        {
          exportTask(t,export);
        }
      }
    }

    @SuppressLint("SetWorldReadable")
    private void share(GroupTask group)
    {
      Task t = group.getTask(name);
      Intent intent = new Intent(Intent.ACTION_SEND);
      
      File path = new File(activity.getCacheDir() + "/" + name + ".eic");
      
      try
      {
        FileOutputStream fd = new FileOutputStream(path); //activity.openFileOutput( path, Context.MODE_WORLD_READABLE);
        TSLWriter w = new TSLWriter(fd);
        TSLObject tsl = new TSLObject();
        t.writeToTSL(tsl);
        tsl.putString("name", name);
        tsl.write(w, "task");
        fd.close();
        //noinspection ResultOfMethodCallIgnored
        path.setReadable(true, false);
      } catch(IOException e)
      {
        throw new RuntimeException(e);
      }
      intent.setType("application/com.exter.eveincalc");
      intent.putExtra(Intent.EXTRA_SUBJECT, name +  " - EVE Industry Calculator");
      intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(path));
      startActivity(Intent.createChooser(intent, "Share Task"));
      path.deleteOnExit();
    }

    @Override
    public void onClick(DialogInterface dialog, int which)
    {
      GroupTask group = (GroupTask)activity.getCurrentTask();
      switch(which)
      {
        case 0:
          group.removeTask(name);
          activity.notifyTaskChanged();
          activity.onProfitChanged();
          break;
        case 1:
          {
            TaskNameDialogFragment name_dialog = new TaskNameDialogFragment();
            Bundle args = new Bundle();
            args.putString("name", name);
            name_dialog.setArguments(args);
            name_dialog.show(activity.getSupportFragmentManager(), "TaskNameDialogFragment");
          }
          break;
        case 2:
          {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Move To:");
             
            Map<String,GroupTask> groups = new HashMap<>();
            for(Map.Entry<String, Task> e:group.getTaskList().entrySet())
            {
              Task t = e.getValue();
              if(!e.getKey().equals(name) && t instanceof GroupTask)
              {
                groups.put(e.getKey(), (GroupTask)t);
              }
            }
            boolean parent = !activity.isRootTask();
            int start = (parent?2:1);
            CharSequence[] choices = new CharSequence[groups.keySet().size() + start];
            
            choices[0] = " [New group]";
            if(parent)
            {
              choices[1] = " [Parent group]";
            }
            
            int i = start;
            for(String s:groups.keySet())
            {
              choices[i++] = s;
            }            
            Arrays.sort(choices,new MoveToListComparator());
            
            builder.setItems(choices,new MoveToClickListener(choices,parent));
            AlertDialog alert = builder.create();
            alert.show();
          }
          break;
        case 3:
          {
            Task t = group.getTask(name);
            TSLObject tsl = new TSLObject();
            t.writeToTSL(tsl);
            Task nt = null;
            try
            {
              nt = factory.fromTSL(tsl);
            } catch(TaskLoadException e)
            {
              e.printStackTrace();
            }
            group.addTask(name, nt);
            activity.notifyTaskChanged();
            activity.onProfitChanged();
          }
          break;
        case 4:
          if(EICApplication.isChrome())
          {
            share(group);
          } else
          {
            export(group);
          }
          break;
        case 5:
          share(group);
          break;
      }
    }
  }
  
  private EICFragmentActivity activity;
  private TaskFactory factory;
  private String name;


  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState)
  {
    activity = (EICFragmentActivity)getActivity();
    factory = ((EICApplication)activity.getApplication()).factory;
    name = getArguments().getString("name");
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setTitle("Task");
    if(EICApplication.isChrome())
    {
      builder.setItems(R.array.task_menu_chrome, new TaskMenuDialogClickListener());
    } else
    {
      builder.setItems(R.array.task_menu, new TaskMenuDialogClickListener());
    }
    return builder.create();
  }
}