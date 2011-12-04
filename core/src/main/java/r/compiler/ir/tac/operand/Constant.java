package r.compiler.ir.tac.operand;

import r.lang.Context;


public class Constant implements SimpleExpr {

  private Object value;
  
  public Constant(Object value) {
    this.value = value;
  }
  
  public Object getValue() {
    return value;
  }
  
  @Override
  public String toString() {
    return value.toString();
  }

  @Override
  public Object retrieveValue(Context context, Object[] temps) {
    return value;
  }
}