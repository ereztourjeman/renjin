package r.compiler.ir.tac;

import java.util.List;
import java.util.Map;

import r.compiler.ir.tac.functions.FunctionCallTranslator;
import r.compiler.ir.tac.functions.FunctionCallTranslators;
import r.compiler.ir.tac.functions.TranslationContext;
import r.compiler.ir.tac.instructions.Assignment;
import r.compiler.ir.tac.instructions.ExprStatement;
import r.compiler.ir.tac.instructions.Return;
import r.compiler.ir.tac.instructions.Statement;
import r.compiler.ir.tac.operand.Constant;
import r.compiler.ir.tac.operand.DynamicCall;
import r.compiler.ir.tac.operand.LValue;
import r.compiler.ir.tac.operand.Operand;
import r.compiler.ir.tac.operand.PrimitiveCall;
import r.compiler.ir.tac.operand.SimpleExpr;
import r.compiler.ir.tac.operand.Temp;
import r.compiler.ir.tac.operand.Variable;
import r.lang.ExpressionVector;
import r.lang.FunctionCall;
import r.lang.Null;
import r.lang.SEXP;
import r.lang.Symbol;
import r.lang.Vector;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class TacFactory {
  
  private int nextTemp = 0;
  private int nextLabel = 0;
  
  private FunctionCallTranslators builders = new FunctionCallTranslators();
 
  private List<Statement> statements;
  private Map<Label, Integer> labels;
  
  public IRBlock build(SEXP exp) {
    
    statements = Lists.newArrayList();
    labels = Maps.newHashMap();
    
    TranslationContext context = new TopLevelContext();
    Operand returnValue = translateExpression(context, exp);
    
    statements.add(new Return(returnValue));
   
    return new IRBlock(statements, labels, nextTemp);
  }
  
  public void dump(SEXP exp) {
    System.out.println( build(exp ).toString());
  }

  public Operand translateExpression(TranslationContext context, SEXP exp) {
    if(exp instanceof ExpressionVector) {
      return translateExpressionList(context, (ExpressionVector)exp);
    } else if(exp instanceof Vector) {
      return new Constant(exp);
    } else if(exp instanceof Symbol) {
      return new Variable((Symbol)exp);
    } else if(exp instanceof FunctionCall) {
      FunctionCallTranslator builder = builders.get(((FunctionCall) exp).getFunction());
      if(builder == null) {
        return translateCall(context, (FunctionCall) exp);
      } else {
        return builder.translateToExpression(this, null, (FunctionCall)exp);
      }
    } else {
      throw new UnsupportedOperationException(exp.toString());
    }
  }
  
  public void translateStatements(TranslationContext context, SEXP exp) {
    if(exp instanceof FunctionCall) {
      FunctionCallTranslator builder = builders.get(((FunctionCall) exp).getFunction());
      if(builder == null) {
        addStatement(new ExprStatement(translateCall(context, (FunctionCall)exp)));
      } else {
        builder.addStatement(this, context, (FunctionCall)exp);
      }
    } else {
      addStatement(new ExprStatement(translateExpression(context, exp)));
    }
  }
  
  public Operand translateCall(TranslationContext context, FunctionCall call) {
    Symbol name = (Symbol)call.getFunction();
    if(name.isReservedWord()) {
      return new PrimitiveCall(name, makeOperandList(context, call));
    } else {
      return new DynamicCall(name, makeOperandList(context, call));
    }
  }

  private List<Operand> makeOperandList(TranslationContext context, FunctionCall call) {
    List<Operand> arguments = Lists.newArrayList();
    for(SEXP arg : call.getArguments().values()) {
      arguments.add( simplify( translateExpression(context, arg) ));
    }
    return arguments;
  }

  public SimpleExpr simplify(Operand rvalue) {
    if(rvalue instanceof SimpleExpr) {
      return (SimpleExpr) rvalue;
    } else {
      Temp temp = newTemp();
      statements.add(new Assignment(temp, rvalue));
      return temp;      
    }
  }

  public SimpleExpr translateSimpleExpression(TranslationContext context, SEXP exp) {
    return simplify(translateExpression(context, exp));
  }
  
  private Operand translateExpressionList(TranslationContext context, ExpressionVector vector) {
    if(vector.length() == 0) {
      return new Constant(Null.INSTANCE);
    } else {
      for(int i=0;i+1<vector.length();++i) {
        translateStatements(context, vector.getElementAsSEXP(i));
      }
      return translateExpression(context, vector.getElementAsSEXP(vector.length()-1));
    }
  }
  
  public Temp newTemp() {
    return new Temp(nextTemp++);
  }
  
  public Label newLabel() {
    return new Label(nextLabel++);
  }

  public void addStatement(Statement statement) {
    statements.add(statement);
  }

  public void addLabel(Label label) {
    labels.put(label, statements.size());
  }
  
  private static class TopLevelContext implements TranslationContext {
    
  }
  
}
