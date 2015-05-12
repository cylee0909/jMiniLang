package priv.bajdcc.LALR1.grammar;

import priv.bajdcc.LALR1.grammar.codegen.Codegen;
import priv.bajdcc.LALR1.grammar.error.LostHandler;
import priv.bajdcc.LALR1.grammar.runtime.RuntimeCodePage;
import priv.bajdcc.LALR1.grammar.semantic.ISemanticRecorder;
import priv.bajdcc.LALR1.grammar.semantic.SemanticHandler;
import priv.bajdcc.LALR1.grammar.semantic.SemanticRecorder;
import priv.bajdcc.LALR1.grammar.symbol.IManageSymbol;
import priv.bajdcc.LALR1.grammar.symbol.IQuerySymbol;
import priv.bajdcc.LALR1.grammar.symbol.SymbolTable;
import priv.bajdcc.LALR1.semantic.Semantic;
import priv.bajdcc.LALR1.semantic.token.ISemanticAnalyzier;
import priv.bajdcc.LALR1.syntax.handler.SyntaxException;
import priv.bajdcc.LALR1.syntax.handler.SyntaxException.SyntaxError;
import priv.bajdcc.util.Position;
import priv.bajdcc.util.lexer.error.RegexException;
import priv.bajdcc.util.lexer.token.KeywordType;
import priv.bajdcc.util.lexer.token.OperatorType;
import priv.bajdcc.util.lexer.token.TokenType;

/**
 * <strong>语法分析器</strong>
 * <p>
 * 基于文法与语义动作
 * </p>
 *
 * @author bajdcc
 */
public class Grammar extends Semantic {

	/**
	 * 语义处理
	 */
	private SemanticHandler handler = new SemanticHandler();

	/**
	 * 符号表
	 */
	private SymbolTable symbol = new SymbolTable();

	/**
	 * 代码生成
	 */
	private Codegen code = null;

	/**
	 * 语义错误表
	 */
	private SemanticRecorder recorder = new SemanticRecorder();

	public Grammar(String context) throws RegexException, SyntaxException {
		super(context);
		initialize();
	}

	/**
	 * 初始化
	 * 
	 * @throws SyntaxException
	 */
	private void initialize() throws SyntaxException {
		declareTerminal();
		declareNonTerminal();
		declareErrorHandler();
		declareActionHandler();
		infer();
		check();
		gencode();
	}

	/**
	 * 声明终结符
	 * 
	 * @throws SyntaxException
	 */
	private void declareTerminal() throws SyntaxException {
		addTerminal("ID", TokenType.ID, null);
		addTerminal("BOOLEAN", TokenType.BOOL, null);
		addTerminal("LITERAL", TokenType.STRING, null);
		addTerminal("CHARACTER", TokenType.CHARACTER, null);
		addTerminal("INTEGER", TokenType.INTEGER, null);
		addTerminal("DECIMAL", TokenType.DECIMAL, null);
		for (KeywordType keywordType : KeywordType.values()) {
			addTerminal(keywordType.name(), TokenType.KEYWORD, keywordType);
		}
		addTerminal("ELLIPSIS", TokenType.OPERATOR, OperatorType.ELLIPSIS);
		addTerminal("PTR_OP", TokenType.OPERATOR, OperatorType.POINTER);
		addTerminal("INC_OP", TokenType.OPERATOR, OperatorType.PLUS_PLUS);
		addTerminal("DEC_OP", TokenType.OPERATOR, OperatorType.MINUS_MINUS);
		addTerminal("LEFT_OP", TokenType.OPERATOR, OperatorType.LEFT_SHIFT);
		addTerminal("RIGHT_OP", TokenType.OPERATOR, OperatorType.RIGHT_SHIFT);
		addTerminal("LE_OP", TokenType.OPERATOR,
				OperatorType.LESS_THAN_OR_EQUAL);
		addTerminal("GE_OP", TokenType.OPERATOR,
				OperatorType.GREATER_THAN_OR_EQUAL);
		addTerminal("EQ_OP", TokenType.OPERATOR, OperatorType.EQUAL);
		addTerminal("NE_OP", TokenType.OPERATOR, OperatorType.NOT_EQUAL);
		addTerminal("AND_OP", TokenType.OPERATOR, OperatorType.LOGICAL_AND);
		addTerminal("OR_OP", TokenType.OPERATOR, OperatorType.LOGICAL_OR);
		addTerminal("MUL_ASSIGN", TokenType.OPERATOR, OperatorType.TIMES_ASSIGN);
		addTerminal("DIV_ASSIGN", TokenType.OPERATOR, OperatorType.DIV_ASSIGN);
		addTerminal("MOD_ASSIGN", TokenType.OPERATOR, OperatorType.MOD_ASSIGN);
		addTerminal("ADD_ASSIGN", TokenType.OPERATOR, OperatorType.PLUS_ASSIGN);
		addTerminal("SUB_ASSIGN", TokenType.OPERATOR, OperatorType.MINUS_ASSIGN);
		addTerminal("LEFT_ASSIGN", TokenType.OPERATOR,
				OperatorType.LEFT_SHIFT_ASSIGN);
		addTerminal("RIGHT_ASSIGN", TokenType.OPERATOR,
				OperatorType.RIGHT_SHIFT_ASSIGN);
		addTerminal("AND_ASSIGN", TokenType.OPERATOR, OperatorType.AND_ASSIGN);
		addTerminal("XOR_ASSIGN", TokenType.OPERATOR, OperatorType.XOR_ASSIGN);
		addTerminal("OR_ASSIGN", TokenType.OPERATOR, OperatorType.OR_ASSIGN);
		addTerminal("ADD", TokenType.OPERATOR, OperatorType.PLUS);
		addTerminal("SUB", TokenType.OPERATOR, OperatorType.MINUS);
		addTerminal("MUL", TokenType.OPERATOR, OperatorType.TIMES);
		addTerminal("DIV", TokenType.OPERATOR, OperatorType.DIVIDE);
		addTerminal("MOD", TokenType.OPERATOR, OperatorType.MOD);
		addTerminal("AND", TokenType.OPERATOR, OperatorType.BIT_AND);
		addTerminal("OR", TokenType.OPERATOR, OperatorType.BIT_OR);
		addTerminal("XOR", TokenType.OPERATOR, OperatorType.BIT_XOR);
		addTerminal("NOT", TokenType.OPERATOR, OperatorType.BIT_NOT);
		addTerminal("NOT_OP", TokenType.OPERATOR, OperatorType.LOGICAL_NOT);
		addTerminal("LT", TokenType.OPERATOR, OperatorType.LESS_THAN);
		addTerminal("GT", TokenType.OPERATOR, OperatorType.GREATER_THAN);
		addTerminal("QUERY", TokenType.OPERATOR, OperatorType.QUERY);
		addTerminal("COMMA", TokenType.OPERATOR, OperatorType.COMMA);
		addTerminal("SEMI", TokenType.OPERATOR, OperatorType.SEMI);
		addTerminal("DOT", TokenType.OPERATOR, OperatorType.DOT);
		addTerminal("ASSIGN", TokenType.OPERATOR, OperatorType.ASSIGN);
		addTerminal("COLON", TokenType.OPERATOR, OperatorType.COLON);
		addTerminal("LPA", TokenType.OPERATOR, OperatorType.LPARAN);
		addTerminal("RPA", TokenType.OPERATOR, OperatorType.RPARAN);
		addTerminal("LSQ", TokenType.OPERATOR, OperatorType.LSQUARE);
		addTerminal("RSQ", TokenType.OPERATOR, OperatorType.RSQUARE);
		addTerminal("LBR", TokenType.OPERATOR, OperatorType.LBRACE);
		addTerminal("RBR", TokenType.OPERATOR, OperatorType.RBRACE);
	}

	/**
	 * 声明非终结符
	 * 
	 * @throws SyntaxException
	 */
	private void declareNonTerminal() throws SyntaxException {
		String[] nonTerminals = new String[] { "program", "stmt_list", "stmt",
				"func", "var_stmt", "var_list", "exp_list", "exp", "exp0",
				"exp1", "exp2", "exp3", "exp4", "exp5", "exp6", "exp7", "exp8",
				"exp9", "type", "block", "call_exp", "call_stmt", "ret_stmt",
				"doc_list", "port_stmt" };
		for (String string : nonTerminals) {
			addNonTerminal(string);
		}
	}

	/**
	 * 进行推导
	 * 
	 * @throws SyntaxException
	 */
	private void infer() throws SyntaxException {
		/* 起始符号就是main函数 */
		infer(handler.getSemanticHandler("main"),
				"program -> stmt_list[0]{lost_stmt}");
		/* Block语句 */
		infer(handler.getSemanticHandler("block"),
				"block -> @LBR#do_enter_scope# [stmt_list[0]] @RBR#do_leave_scope#{lost_rbr}");
		/* 当前块（Block）全部由语句组成 */
		infer(handler.getSemanticHandler("stmt_list"),
				"stmt_list -> stmt[0]{lost_stmt} [stmt_list[1]]");
		/* 语句分为变量定义（赋值）、调用语句 */
		infer(handler.getSemanticHandler("copy"),
				"stmt -> var_stmt[0] | call_stmt[0] | ret_stmt[0] | port_stmt[0] | block");
		/* 返回语句 */
		infer(handler.getSemanticHandler("return"),
				"ret_stmt -> @RETURN [exp[0]] @SEMI{lost_semi}");
		/* 变量定义（赋值）语句（由于支持Lambda，函数定义皆为Lambda形式） */
		infer(handler.getSemanticHandler("var"),
				"var_stmt -> (@VARIABLE[11] | @LET[12]) @ID[0]#declear_variable#{lost_token} @ASSIGN{lost_assign} (func[1]{lost_func} | exp[2]{lost_exp}) @SEMI{lost_semi}");
		/* 导入与导出语句 */
		infer(handler.getSemanticHandler("port"),
				"port_stmt -> (@IMPORT[1] | @EXPORT[2]) @LITERAL[0]{lost_string} @SEMI{lost_semi}");
		/* 表达式（算符文法） */
		ISemanticAnalyzier exp_handler = handler.getSemanticHandler("exp");
		infer(exp_handler, "exp -> exp0[0]");
		infer(exp_handler,
				"exp0 -> exp1[0] [@QUERY[4] exp0[6] @COLON[5]{lost_colon} exp0[7]]");
		infer(exp_handler, "exp1 -> [exp1[1] (@AND_OP[2] | @OR_OP[2])] exp2[0]");
		infer(exp_handler,
				"exp2 -> [exp2[1] (@OR[2] | @XOR[2] | @AND[2])] exp3[0]");
		infer(exp_handler, "exp3 -> [exp3[1] (@EQ_OP[2] | @NE_OP[2])] exp4[0]");
		infer(exp_handler,
				"exp4 -> [exp4[1] (@LT[2] | @GT[2] | @LE_OP[2] | @GE_OP[2])] exp5[0]");
		infer(exp_handler,
				"exp5 -> [exp5[1] (@LEFT_OP[2] | @RIGHT_OP[2])] exp6[0]");
		infer(exp_handler, "exp6 -> [exp6[1] (@ADD[2] | @SUB[2])] exp7[0]");
		infer(exp_handler,
				"exp7 -> [exp7[1] (@MUL[2] | @DIV[2] | @MOD[2])] exp8[0]");
		infer(exp_handler, "exp8 -> (@NOT_OP[3] | @NOT[3]) exp8[1] | exp9[0]");
		infer(exp_handler,
				"exp9 -> (@INC_OP[3] | @DEC_OP[3]) exp9[1] | exp9[1] (@INC_OP[3] | @DEC_OP[3]) | type[0]");
		/* 调用语句 */
		infer(handler.getSemanticHandler("call_exp"),
				"call_exp -> @CALL (@LPA{lost_lpa} func[0]{lost_call} @RPA{lost_rpa} | @ID[1]{lost_call}) @LPA{lost_lpa} [exp_list[2]] @RPA{lost_rpa}");
		infer(handler.getSemanticHandler("call_stmt"),
				"call_stmt -> call_exp[0] @SEMI{lost_semi}");
		/* 函数定义 */
		infer(handler.getSemanticHandler("token_list"),
				"var_list -> @ID[0]#declear_param# [@COMMA var_list[1]]");
		infer(handler.getSemanticHandler("exp_list"),
				"exp_list -> exp[0] [@COMMA exp_list[1]]");
		infer(handler.getSemanticHandler("token_list"),
				"doc_list -> @LITERAL[0] [@COMMA exp_list[1]]");
		infer(handler.getSemanticHandler("func"),
				"func -> @FUNCTION [@LSQ doc_list[0]{lost_doc} @RSQ] (@ID[1]#predeclear_funcname#{lost_func_name} | @NOT[1]#predeclear_funcname#{lost_func_name}) @LPA{lost_lpa} [var_list[2]] @RPA{lost_rpa} (@PTR_OP{lost_func_body} exp[3]{lost_exp} | block[4]{lost_func_body})");
		/* 基本数据类型 */
		infer(handler.getSemanticHandler("type"),
				"type -> @ID[0] | @INTEGER[0] | @DECIMAL[0] | @LITERAL[0] | @CHARACTER[0] | @BOOLEAN[0] | @LPA exp[1]{lost_exp} @RPA{lost_rpa} | call_exp[1]");
		initialize("program");
	}

	/**
	 * 声明错误处理器
	 * 
	 * @throws SyntaxException
	 */
	private void declareErrorHandler() throws SyntaxException {
		addErrorHandler("lost_exp", new LostHandler("表达式"));
		addErrorHandler("lost_func", new LostHandler("函数"));
		addErrorHandler("lost_token", new LostHandler("标识符"));
		addErrorHandler("lost_func_name", new LostHandler("函数名"));
		addErrorHandler("lost_func_body", new LostHandler("函数体"));
		addErrorHandler("lost_stmt", new LostHandler("语句"));
		addErrorHandler("lost_string", new LostHandler("字符串"));
		addErrorHandler("lost_assign", new LostHandler("等号'='"));
		addErrorHandler("lost_call", new LostHandler("调用主体"));
		addErrorHandler("lost_lpa", new LostHandler("左圆括号'('"));
		addErrorHandler("lost_rpa", new LostHandler("右圆括号')'"));
		addErrorHandler("lost_lsq", new LostHandler("左方括号'['"));
		addErrorHandler("lost_rsq", new LostHandler("右方括号']'"));
		addErrorHandler("lost_lbr", new LostHandler("左花括号'{'"));
		addErrorHandler("lost_rbr", new LostHandler("右花括号'}'"));
		addErrorHandler("lost_colon", new LostHandler("冒号':'"));
		addErrorHandler("lost_semi", new LostHandler("分号';'"));
		addErrorHandler("lost_doc", new LostHandler("文档"));
	}

	/**
	 * 声明动作处理器
	 * 
	 * @throws SyntaxException
	 */
	private void declareActionHandler() throws SyntaxException {
		String[] actionNames = new String[] { "do_enter_scope",
				"do_leave_scope", "predeclear_funcname", "declear_variable",
				"declear_param" };
		for (String string : actionNames) {
			addActionHandler(string, handler.getActionHandler(string));
		}
	}

	/**
	 * 语法树建成之后的详细语义检查
	 * 
	 * @throws SyntaxException
	 */
	private void check() throws SyntaxException {
		if (recorder.isCorrect()) {
			symbol.check(recorder);
		} else {
			System.err.println(getSemanticError());
			throw new SyntaxException(SyntaxError.COMPILE_ERROR,
					new Position(), "请检查代码");
		}
	}

	/**
	 * 产生中间代码
	 */
	private void gencode() {
		if (recorder.isCorrect()) {
			code = new Codegen(symbol);
			code.gencode();
		}
	}

	@Override
	protected IQuerySymbol getQuerySymbolService() {
		return symbol;
	}

	@Override
	protected IManageSymbol getManageSymbolService() {
		return symbol;
	}

	@Override
	protected ISemanticRecorder getSemanticRecorderService() {
		return recorder;
	}

	/**
	 * 获得语义错误描述
	 */
	public String getSemanticError() {
		return recorder.toString();
	}

	/**
	 * 获得中间代码描述
	 */
	public String getInst() {
		return code.toString();
	}

	/**
	 * 生成目标代码
	 * 
	 * @throws SyntaxException
	 */
	public RuntimeCodePage getCodePage() throws SyntaxException {
		if (!recorder.isCorrect()) {
			System.err.println(getSemanticError());
			throw new SyntaxException(SyntaxError.COMPILE_ERROR,
					new Position(), "请检查代码");
		}
		return code.genCodePage();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		// sb.append(getNGAString());
		// sb.append(getNPAString());
		sb.append(getTrackerError());
		sb.append(getSemanticError());
		sb.append(getTokenList());
		if (recorder != null && recorder.isCorrect()) {
			sb.append(symbol.toString());
			sb.append(getInst());
		}
		return sb.toString();
	}
}
