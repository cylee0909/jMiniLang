package priv.bajdcc.LALR1.grammar.runtime.service;

/**
 * 【运行时】运行时进程服务接口
 *
 * @author bajdcc
 */
public interface IRuntimeProcessService {

	/**
	 * 进程阻塞
	 *
	 * @param pid 进程ID
	 * @return 成功与否
	 */
	boolean block(int pid);

	/**
	 * 进程唤醒
	 *
	 * @param pid 进程ID
	 * @return 成功与否
	 */
	boolean wakeup(int pid);

	/**
	 * 进程休眠
	 * @param pid 进程ID
	 * @param turn 休眠趟数
	 * @return 总休眠趟数
	 */
	int sleep(int pid, int turn) ;

	/**
	 * 进程等待
	 * @param joined 等待的进程ID
	 * @param pid 当前进程ID
	 * @return 是则继续等待
	 */
	boolean join(int joined, int pid);

	/**
	 * 进行存活
	 *
	 * @param pid 页名
	 * @return 是否存活
	 */
	boolean live(int pid);

	/**
	 * 添加代码页
	 * @param name 页名
	 * @param code 代码
	 * @return 是否成功
	 */
	boolean addCodePage(String name, String code);

	/**
	 * 让解释器等待一段时间，让UI刷新
	 */
	void waitForUI();
}
