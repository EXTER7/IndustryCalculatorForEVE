package com.exter.eveindcalc.group;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.Toast;

import com.exter.eveindcalc.EICApplication;
import com.exter.eveindcalc.EICFragmentActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import exter.eveindustry.task.GroupTask;
import exter.eveindustry.task.Task;
import exter.eveindustry.task.TaskFactory;
import exter.eveindustry.task.TaskLoadException;
import exter.tsl.InvalidTSLException;
import exter.tsl.TSLObject;
import exter.tsl.TSLReader;

public class ImportTaskDialogFragment extends DialogFragment
{ 
  static private class FileNameComparator implements Comparator<File>
  {
    @Override
    public int compare(File lhs, File rhs)
    {
      return lhs.getName().compareTo(rhs.getName());
    }
  }

  private class TaskMenuDialogClickListener implements DialogInterface.OnClickListener
  {
    private boolean importTask(File fd)
    {
      try
      {
        InputStream is = new FileInputStream(fd);

        TSLReader reader = new TSLReader(is);
        reader.moveNext();
        if(reader.getState() != TSLReader.State.OBJECT || !reader.getName().equals("task"))
        {
          Log.e("importTask", "Not a task collection");
          return false;
        }

        TSLObject tsl = new TSLObject(reader);
        is.close();

        String name = tsl.getString("name", null);
        if(name == null)
        {
          Log.e("importTask", "name is null");
          return false;
        }
        Task t = null;
        try
        {
          t = factory.fromTSL(tsl);
        } catch(TaskLoadException e)
        {
          e.printStackTrace();
        }
        if(t == null)
        {
          Log.e("importTask", "task is null");
          return false;
        }
        GroupTask group = (GroupTask)activity.getCurrentTask();
        group.addTask(name, t);
        return true;
      } catch(InvalidTSLException | IOException ignore)
      {

      }
      return false;
    }
    
    @SuppressLint("WorldReadableFiles")
    @Override
    public void onClick(DialogInterface dialog, int which)
    {
      String text;
      if(importTask(files.get(which)))
      {
        text = "Task imported succesfully.";
      } else
      {
        text = "Error inporting task.";
      }
      activity.notifyTaskChanged();
      Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }
  }
  
  private EICFragmentActivity activity;
  private TaskFactory factory;
  private List<File> files;
  
  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState)
  {
    activity = (EICFragmentActivity)getActivity();
    factory = ((EICApplication)activity.getApplication()).factory;
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setTitle("Task");
    files = new ArrayList<>();
    
    File external_dir = Environment.getExternalStorageDirectory();
    if(external_dir != null)
    {
      File dir = new File(external_dir + "/Eve Industry Calculator");
      if(dir.isDirectory())
      {
        for(File f:dir.listFiles())
        {
          if(f.isFile())
          {
            files.add(f);
          }
        }
        Collections.sort(files,new FileNameComparator());
      }
    } else
    {
      Log.e("TaskImport", "external_dir is null");
    }
    CharSequence[] items = new CharSequence[files.size()];
    int i;
    for(i = 0; i < items.length; i++)
    {
      items[i] = files.get(i).getName();
    }

    builder.setItems(items, new TaskMenuDialogClickListener());
    return builder.create();
  }
}