package com.exter.eveindcalc.group;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.exter.eveindcalc.EICFragmentActivity;
import com.exter.eveindcalc.R;

import exter.eveindustry.task.GroupTask;
import exter.eveindustry.task.Task;


public class TaskNameDialogFragment extends DialogFragment
{
  private String name;
  private EICFragmentActivity activity;
  
  private class TaskRenameClickListener implements DialogInterface.OnClickListener
  {
    @Override
    public void onClick(DialogInterface dialog, int which)
    {
      GroupTask group_task = (GroupTask)activity.getTask();
      Task task = group_task.getTaskList().get(name);
      group_task.removeTask(name);
      group_task.addTask(ed_name.getText().toString(), task);
      activity.notifyTaskChanged();
    }
  }
  
  private class TaskRenameCancelClickListener implements DialogInterface.OnClickListener
  {
    @Override
    public void onClick(DialogInterface dialog, int which)
    {

    }
  }


  private EditText ed_name;

  @NonNull
  @SuppressLint("InflateParams")
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState)
  {
    activity = (EICFragmentActivity)getActivity();
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    LayoutInflater inflater = getActivity().getLayoutInflater();
    View view = inflater.inflate(R.layout.task_name, null);
    name = getArguments().getString("name");
    builder.setView(view);
    String accept_label = "Rename";
    builder.setPositiveButton(accept_label, new TaskRenameClickListener());
    builder.setNegativeButton("Cancel", new TaskRenameCancelClickListener());

    ed_name = (EditText) view.findViewById(R.id.ed_task_rename);
    ed_name.setText(name);
    
    return builder.create();
  }
}
