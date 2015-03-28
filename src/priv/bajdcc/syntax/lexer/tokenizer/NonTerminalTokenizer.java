package priv.bajdcc.syntax.lexer.tokenizer;

import priv.bajdcc.lexer.algorithm.TokenAlgorithm;
import priv.bajdcc.lexer.error.RegexException;
import priv.bajdcc.lexer.token.Token;
import priv.bajdcc.lexer.token.TokenType;

/**
 * ���ս������
 * 
 * @author bajdcc
 *
 */
public class NonTerminalTokenizer extends TokenAlgorithm {

	public NonTerminalTokenizer() throws RegexException {
		super(getRegexString(), null);
	}

	public static String getRegexString() {		
		return "(\\a|_)\\w*";
	}

	@Override
	public boolean getGreedMode() {
		return true;
	}

	/*
	 * ���� Javadoc��
	 * 
	 * @see
	 * priv.bajdcc.lexer.algorithm.ITokenAlgorithm#getToken(java.lang.String,
	 * priv.bajdcc.lexer.token.Token)
	 */
	@Override
	public Token getToken(String string, Token token) {
		token.m_kToken = TokenType.ID;
		token.m_Object = string;
		return token;
	}
}
