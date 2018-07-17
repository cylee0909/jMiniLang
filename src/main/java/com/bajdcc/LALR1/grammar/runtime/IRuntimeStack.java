package com.bajdcc.LALR1.grammar.runtime;

/**
 * 运行时堆栈接口
 *
 * @author bajdcc
 */
public interface IRuntimeStack {

	RuntimeObject load() throws RuntimeException;

	void store(RuntimeObject obj) throws RuntimeException;

	void push() throws RuntimeException;

	void pop() throws RuntimeException;

	void opLoad() throws RuntimeException;

	void opLoadFunc() throws RuntimeException;

	void opReloadFunc() throws RuntimeException;

	void opStore() throws RuntimeException;

	void opStoreCopy() throws RuntimeException;

	void opStoreDirect() throws RuntimeException;

	void opOpenFunc() throws RuntimeException;

	void opLoadArgs() throws RuntimeException;

	void opPushArgs() throws RuntimeException;

	void opReturn() throws RuntimeException;

	void opCall() throws RuntimeException;

	void opPushNull() throws RuntimeException;

	void opPushZero() throws RuntimeException;

	void opPushNan() throws RuntimeException;

	void opPushPtr(int pc) throws RuntimeException;

	void opPushObj(RuntimeObject obj) throws RuntimeException;

	void opLoadVar() throws RuntimeException;

	void opJump() throws RuntimeException;

	void opJumpBool(boolean bool) throws RuntimeException;

	void opJumpBoolRetain(boolean bool) throws RuntimeException;

	void opJumpZero(boolean bool) throws RuntimeException;

	void opJumpYield() throws RuntimeException;

	void opJumpNan() throws RuntimeException;

	void opImport() throws RuntimeException;

	void opLoadExtern() throws RuntimeException;

	void opCallExtern(boolean invoke) throws Exception;

	void opYield(boolean input) throws RuntimeException;

	void opYieldSwitch(boolean forward) throws RuntimeException;

	void opYieldCreateContext() throws Exception;

	void opYieldDestroyContext() throws RuntimeException;

	void opScope(boolean enter) throws RuntimeException;

	void opArr() throws RuntimeException;

	void opMap() throws RuntimeException;

	void opIndex() throws RuntimeException;

	void opIndexAssign() throws RuntimeException;

	void opTry() throws RuntimeException;

	void opThrow() throws RuntimeException;
}