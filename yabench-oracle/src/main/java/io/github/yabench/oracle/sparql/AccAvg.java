package io.github.yabench.oracle.sparql;

import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprEvalException;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.aggregate.Accumulator;
import com.hp.hpl.jena.sparql.expr.aggregate.AccumulatorFactory;
import com.hp.hpl.jena.sparql.expr.aggregate.AggCustom;
import com.hp.hpl.jena.sparql.expr.nodevalue.XSDFuncOp;
import com.hp.hpl.jena.sparql.function.FunctionEnv;

public class AccAvg implements Accumulator {

    private final AggCustom agg;
    private NodeValue total = NodeValue.nvZERO;
    private int count = 0;

    public AccAvg(AggCustom agg) {
        this.agg = agg;
    }

    @Override
    public void accumulate(Binding binding, FunctionEnv functionEnv) {
        final ExprList exprList = agg.getExprList();
        for (Expr expr : exprList) {
            final NodeValue nv = expr.eval(binding, functionEnv);
            if (nv.isNumber()) {
                count++;
                if (total == NodeValue.nvZERO) {
                    total = nv;
                } else {
                    total = XSDFuncOp.numAdd(nv, total);
                }
            } else {
                throw new ExprEvalException();
            }
        }
    }

    @Override
    public NodeValue getValue() {
        if (count == 0) {
            return NodeValue.nvEmptyString;
        } else {
            NodeValue nvCount = NodeValue.makeInteger(count);
            return XSDFuncOp.numDivide(total, nvCount);
        }
    }

    public static class Factory implements AccumulatorFactory {

        @Override
        public Accumulator createAccumulator(AggCustom agg) {
            return new AccAvg(agg);
        }

    }

}
