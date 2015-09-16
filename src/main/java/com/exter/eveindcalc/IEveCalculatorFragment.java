package com.exter.eveindcalc;

public interface IEveCalculatorFragment
{
  void onTaskChanged();
  void onPriceValueChanged();
  void onMaterialSetChanged();
  void onMaterialChanged(int item);
  void onExtraExpenseChanged();
  void onTaskParameterChanged(int param);
}