package com.exter.eveindcalc.refine;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.exter.controls.DoubleEditText;
import com.exter.controls.IntegerEditText;
import com.exter.eveindcalc.EICApplication;
import com.exter.eveindcalc.EICFragmentActivity;
import com.exter.eveindcalc.IEveCalculatorFragment;
import com.exter.eveindcalc.R;

import exter.eveindustry.task.RefiningTask;

public class RefineFragment extends Fragment implements IEveCalculatorFragment
{
  private EICFragmentActivity activity;


  private class RefTaxChangeWatcher implements DoubleEditText.ValueListener
  {
    @Override
    public void valueChanged(int tag, double new_value)
    {   
      if(refine_task == null)
      {
        return;
      }
      refine_task.setRefineryTax((float)new_value);
    }
  }
  
  private class OreAmountChangeWatcher implements IntegerEditText.ValueListener
  {
    @Override
    public void onValueChanged(int new_value)
    {    
      if(refine_task == null)
      {
        return;
      }
      refine_task.setOreAmount(new_value);
    }
  }

  private Spinner sp_refiningskill;
  private Spinner sp_reskill;
  private Spinner sp_processingskill;
  private Spinner sp_hardwiring;
  private Spinner sp_station;
  private TextView tx_skill;
  private TextView tx_amount;

  private DoubleEditText ed_reftax;
  private IntegerEditText ed_amount;
  
  private RefiningTask refine_task;


  private class RefiningSkillItemSelectedListener implements Spinner.OnItemSelectedListener
  {
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
      if(refine_task == null)
      {
        return;
      }
      refine_task.setReprocessingSkillLevel(pos);
      SharedPreferences sp = activity.getSharedPreferences("EIC", Context.MODE_PRIVATE);
      SharedPreferences.Editor ed = sp.edit();
      ed.putInt("refine.skill_3385",refine_task.getReprocessingSkillLevel());
      ed.apply();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
      
    }
  }

  private class ReSkillItemSelectedListener implements Spinner.OnItemSelectedListener
  {
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
      if(refine_task == null)
      {
        return;
      }
      refine_task.setReprocessingEfficiencySkillLevel(pos);
      
      SharedPreferences sp = activity.getSharedPreferences("EIC", Context.MODE_PRIVATE);
      SharedPreferences.Editor ed = sp.edit();
      ed.putInt("refine.skill_3389",refine_task.getReprocessingEfficiencySkillLevel());
      ed.apply();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
      
    }
  }
  
  private class ProcessingSkillItemSelectedListener implements Spinner.OnItemSelectedListener
  {
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
      if(refine_task == null)
      {
        return;
      }
      refine_task.setOreProcessingSkillLevel(pos);
      SharedPreferences sp = activity.getSharedPreferences("EIC", Context.MODE_PRIVATE);
      SharedPreferences.Editor ed = sp.edit();
      ed.putInt("refine.skill_" + String.valueOf(refine_task.getRefinable().skill_id),refine_task.getOreProcessingSkillLevel());
      ed.apply();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
      
    }
  }

  private class HardwiringItemSelectedListener implements Spinner.OnItemSelectedListener
  {
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
      if(refine_task == null)
      {
        return;
      }
      refine_task.setHardwiring(RefiningTask.Hardwiring.fromInt(pos));
      SharedPreferences sp = activity.getSharedPreferences("EIC", Context.MODE_PRIVATE);
      SharedPreferences.Editor ed = sp.edit();
      ed.putInt("refine.hardwiring",pos);
      ed.apply();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
      
    }
  }

  private class StationEfficiencyItemSelectedListener implements Spinner.OnItemSelectedListener
  {
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
      if(refine_task == null)
      {
        return;
      }
      switch(pos)
      {
        default://case 0
          refine_task.setInstallationEfficiency(50);
          break;
        case 1:
          refine_task.setInstallationEfficiency(45);
          break;
        case 2:
          refine_task.setInstallationEfficiency(40);
          break;
        case 3:
          refine_task.setInstallationEfficiency(35);
          break;       
        case 4:
          refine_task.setInstallationEfficiency(52);
          break;       
        case 5:
          refine_task.setInstallationEfficiency(54);
          break;       
      }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
      
    }
  }

  private EICApplication application;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {

    activity = (EICFragmentActivity)getActivity();
    application = (EICApplication)activity.getApplication();
    refine_task = (RefiningTask) activity.getCurrentTask();
    View rootView = inflater.inflate(R.layout.refine_main, container, false);

    
    sp_refiningskill = (Spinner)rootView.findViewById(R.id.sp_refore_refiningskill);
    sp_reskill = (Spinner)rootView.findViewById(R.id.sp_refore_reskill);
    sp_processingskill = (Spinner)rootView.findViewById(R.id.sp_refore_processingskill);
    sp_hardwiring = (Spinner)rootView.findViewById(R.id.sp_refore_hardwiring);
    sp_station = (Spinner)rootView.findViewById(R.id.sp_refore_station);
    ed_reftax = new DoubleEditText((EditText)rootView.findViewById(R.id.ed_refore_reftax),0,0,100,0,new RefTaxChangeWatcher());
    ed_amount = new IntegerEditText((EditText)rootView.findViewById(R.id.ed_refore_amount),0,Integer.MAX_VALUE,0,new OreAmountChangeWatcher());
    tx_skill = (TextView)rootView.findViewById(R.id.tx_refine_skill);
    tx_amount = (TextView)rootView.findViewById(R.id.tx_refine_amount);
    
    sp_refiningskill.setOnItemSelectedListener(new RefiningSkillItemSelectedListener());
    sp_reskill.setOnItemSelectedListener(new ReSkillItemSelectedListener());
    sp_processingskill.setOnItemSelectedListener(new ProcessingSkillItemSelectedListener());
    sp_hardwiring.setOnItemSelectedListener(new HardwiringItemSelectedListener());
    sp_station.setOnItemSelectedListener(new StationEfficiencyItemSelectedListener());
    onTaskChanged();
    
    return rootView;
  }
  

  @Override
  public void onTaskChanged()
  {
    if(activity == null || refine_task == null)
    {
      return;
    }
    int hw_index = 0;
    switch(refine_task.getHardwiring())
    {
      case None:
        hw_index = 0;
        break;
      case ZainouBeancounterH40:
        hw_index = 1;
        break;
      case ZainouBeancounterH50:
        hw_index = 2;
        break;
      case ZainouBeancounterH60:
        hw_index = 3;
        break;
    }
    
    int se_index;

    switch(refine_task.getInstallationEfficiency())
    {
      default://case 50:
        se_index = 0;
        break;
      case 45:
        se_index = 1;
        break;
      case 40:
        se_index = 2;
        break;
      case 35:
        se_index = 3;
        break;
      case 52:
        se_index = 4;
        break;
      case 54:
        se_index = 5;
        break;
    }

    tx_skill.setText(String.format("%s skill:",
            application.factory.items.get(refine_task.getRefinable().skill_id).name));
    tx_amount.setText(String.format("%s amount (units):",
            refine_task.getRefinable().item.item.name));

    ed_amount.setValue((int) refine_task.getOreAmount());
    ed_reftax.setValue(refine_task.getRefineryTax());

    sp_station.setSelection(se_index);
    
    sp_refiningskill.setSelection(refine_task.getReprocessingSkillLevel());
    sp_reskill.setSelection(refine_task.getReprocessingEfficiencySkillLevel());
    sp_processingskill.setSelection(refine_task.getOreProcessingSkillLevel());
    sp_hardwiring.setSelection(hw_index);
    ed_amount.setValue((int) refine_task.getOreAmount());
  }

  @Override
  public void onPriceValueChanged()
  {

  }

  @Override
  public void onMaterialSetChanged()
  {

  }


  @Override
  public void onMaterialChanged(int item)
  {
    
  }


  @Override
  public void onExtraExpenseChanged()
  {

  }

  @Override
  public void onTaskParameterChanged(int param)
  {

  }
}
