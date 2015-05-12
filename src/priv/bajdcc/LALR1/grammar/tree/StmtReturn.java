package priv.bajdcc.LALR1.grammar.tree;

import priv.bajdcc.LALR1.grammar.codegen.ICodegen;
import priv.bajdcc.LALR1.grammar.runtime.RuntimeInst;
import priv.bajdcc.LALR1.grammar.semantic.ISemanticRecorder;
import priv.bajdcc.util.lexer.token.KeywordType;
import priv.bajdcc.util.lexer.token.OperatorType;

/**
 * 【语义分析】返回语句
 *
 * @author bajdcc
 */
public class StmtReturn implements IStmt {

	private IExp exp = null;

	public IExp getExp() {
		return exp;
	}

	public void setExp(IExp exp) {
		this.exp = exp;
	}

	@Override
	public void analysis(ISemanticRecorder recorder) {
		if (exp != null) {
			exp.analysis(recorder);
		}
	}

	@Override
	public void genCode(ICodegen codegen) {
		if (exp != null) {
			exp.genCode(codegen);
		} else {
			codegen.genCode(RuntimeInst.ipushx);
		}
		codegen.genCode(RuntimeInst.iret);
	}

	@Override
	public String toString() {
		return print(new StringBuilder());
	}

	@Override
	public String print(StringBuilder prefix) {
		StringBuilder sb = new StringBuilder();
		sb.append(prefix.toString());
		sb.append(KeywordType.RETURN.getName());
		if (exp != null) {
			sb.append("");
			sb.append(exp.print(prefix));
		}
		sb.append(OperatorType.SEMI.getName());
		return sb.toString();
	}
}
