package priv.bajdcc.syntax;

import java.util.ArrayList;

import priv.bajdcc.syntax.exp.RuleExp;
import priv.bajdcc.syntax.exp.TokenExp;

/**
 * 文法规则（文法推导式表）
 *
 * @author bajdcc
 */
public class Rule {

	/**
	 * 规则表达式列表
	 */
	public ArrayList<RuleItem> m_arrRules = new ArrayList<RuleItem>();

	/**
	 * 规则起始非终结符
	 */
	public RuleExp m_nonTerminal = null;

	/**
	 * 左递归等级：0为否，1为直接，大于1为间接
	 */
	public int m_iRecursiveLevel = 0;

	/**
	 * 终结符First集合
	 */
	public ArrayList<TokenExp> m_arrTokens = new ArrayList<TokenExp>();

	public Rule(RuleExp exp) {
		m_nonTerminal = exp;
	}
}
