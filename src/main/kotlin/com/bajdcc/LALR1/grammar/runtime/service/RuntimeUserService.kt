package com.bajdcc.LALR1.grammar.runtime.service

import com.bajdcc.LALR1.grammar.runtime.RuntimeObject
import com.bajdcc.LALR1.grammar.runtime.RuntimeObjectType
import com.bajdcc.LALR1.grammar.runtime.RuntimeProcess
import com.bajdcc.LALR1.grammar.runtime.data.*
import com.bajdcc.LALR1.ui.user.UIUserGraphics
import com.bajdcc.LALR1.ui.user.UIUserWindow
import okhttp3.*
import org.apache.log4j.Logger
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.swing.JFrame


/**
 * 【运行时】运行时用户服务
 *
 * @author bajdcc
 */
class RuntimeUserService(private val service: RuntimeService) :
        IRuntimeUserService,
        IRuntimeUserService.IRuntimeUserPipeService,
        IRuntimeUserService.IRuntimeUserShareService,
        IRuntimeUserService.IRuntimeUserFileService,
        IRuntimeUserService.IRuntimeUserWindowService,
        IRuntimeUserService.IRuntimeUserNetService {

    enum class UserSignal {
        DESTROY,
        WAKEUP,
    }

    private val fsNodeRoot = RuntimeFsNode.root()
    private val mapNames = mutableMapOf<String, Int>()
    private val arrUsers = Array<UserStruct?>(MAX_USER) { null }
    private val setUserId = mutableSetOf<Int>()
    private var cyclePtr = 0

    override val pipe: IRuntimeUserService.IRuntimeUserPipeService
        get() = this

    override val share: IRuntimeUserService.IRuntimeUserShareService
        get() = this

    override val file: IRuntimeUserService.IRuntimeUserFileService
        get() = this

    override val window: IRuntimeUserService.IRuntimeUserWindowService
        get() = this

    override val net: IRuntimeUserService.IRuntimeUserNetService
        get() = this

    internal enum class UserType(var desc: String) {
        PIPE("管道"),
        SHARE("共享"),
        FILE("文件"),
        WINDOW("窗口"),
        NET("网络"),
    }

    internal interface IUserPipeHandler {
        /**
         * 读取管道
         * @return 读取的对象
         */
        fun read(): RuntimeObject

        /**
         * 写入管道
         * @param obj 写入的对象
         * @return 是否成功
         */
        fun write(obj: RuntimeObject): Boolean
    }

    internal interface IUserShareHandler {
        /**
         * 获取共享
         * @return 共享对象
         */
        fun get(): RuntimeObject?

        /**
         * 设置共享
         * @param obj 共享对象
         * @return 上次保存的内容
         */
        fun set(obj: RuntimeObject?): RuntimeObject?

        /**
         * 锁定共享
         * @return 是否成功
         */
        fun lock(): Boolean

        /**
         * 解锁共享
         * @return 是否成功
         */
        fun unlock(): Boolean
    }

    internal interface IUserFileHandler {

        /**
         * 查询文件
         * @return 0-不存在，1-文件，2-文件夹
         */
        fun query(): Long

        /**
         * 创建文件
         * @param file 是否创建文件
         * @return 是否成功
         */
        fun create(file: Boolean): Boolean

        /**
         * 删除文件
         * @return 是否成功
         */
        fun delete(): Boolean

        /**
         * 读取文件
         * @return 是否成功
         */
        fun read(): ByteArray?

        /**
         * 写入文件
         * @param data 数据
         * @param overwrite 是否覆盖
         * @param createIfNotExist 是否自动创建
         * @return 0-成功，-1:自动创建失败，-2:文件不存在，-3:目标不是文件
         */
        fun write(data: ByteArray, overwrite: Boolean, createIfNotExist: Boolean): Long
    }

    internal interface IUserWindowHandler {

        /**
         * 发送消息
         * @param type 消息类型
         * @param param1 参数1
         * @param param2 参数2
         * @return 是否成功
         */
        fun sendMessage(type: Int, param1: Int, param2: Int): Boolean

        /**
         * SVG 绘图
         * @param op 类型
         * @param x X坐标
         * @param y Y坐标
         * @return 是否成功
         */
        fun svg(op: Char, x: Int, y: Int): Boolean

        /**
         * 设置字符串
         * @param op 类型
         * @param str 字符串
         * @return 是否成功
         */
        fun str(op: Int, str: String): Boolean
    }

    internal interface IUserNetHandler {

        /**
         * HTTP请求返回内容
         * @param method 请求方法（GET、POST等）
         * @param json 是否为JSON
         * @param data 数据
         * @return 响应数据
         */
        fun http(method: Int, json: Boolean, data: String): RuntimeObject
    }

    internal interface IUserHandler {

        val pipe: IUserPipeHandler

        val share: IUserShareHandler

        val file: IUserFileHandler

        val window: IUserWindowHandler

        val net: IUserNetHandler

        fun destroy()

        fun enqueue(pid: Int)

        fun dequeue(): Boolean

        fun dequeue(pid: Int)

        fun signal(type: UserSignal)
    }

    internal open inner class UserHandler(protected var id: Int, private val waitingPids: Deque<Int> = ArrayDeque<Int>()) : IUserHandler {

        protected val isEmpty: Boolean
            get() = waitingPids.isEmpty()

        override val pipe: IUserPipeHandler
            get() = throw NotImplementedError()

        override val share: IUserShareHandler
            get() = throw NotImplementedError()

        override val file: IUserFileHandler
            get() = throw NotImplementedError()

        override val window: IUserWindowHandler
            get() = throw NotImplementedError()

        override val net: IUserNetHandler
            get() = throw NotImplementedError()

        override fun destroy() {
            service.processService.ring3.removeHandle(id)
        }

        override fun enqueue(pid: Int) {
            waitingPids.add(pid)
            service.processService.block(pid)
            service.processService.ring3.blockHandle = id
        }

        override fun dequeue(): Boolean {
            if (waitingPids.isEmpty())
                return false
            val pid = waitingPids.poll()
            service.processService.ring3.blockHandle = -1
            service.processService.wakeup(pid)
            return true
        }

        override fun dequeue(pid: Int) {
            waitingPids.remove(pid)
        }

        override fun signal(type: UserSignal) {
            when (type) {
                UserSignal.DESTROY -> {
                }
                UserSignal.WAKEUP -> while (dequeue());
            }
        }

        override fun toString(): String {
            return String.format("队列：%s", waitingPids.toString())
        }
    }

    internal inner class UserPipeHandler(id: Int) : UserHandler(id), IUserPipeHandler {
        private val queue: Queue<RuntimeObject>

        override val pipe: IUserPipeHandler
            get() = this

        init {
            this.queue = ArrayDeque()
        }

        override fun read(): RuntimeObject {
            if (queue.isEmpty()) {
                val pid = service.processService.pid
                enqueue(pid)
                return RuntimeObject(false, RuntimeObjectType.kNoop)
            }
            return queue.poll()
        }

        override fun write(obj: RuntimeObject): Boolean {
            queue.add(obj)
            dequeue()
            return true
        }

        override fun toString(): String {
            return String.format("%s 管道：%s", super.toString(), queue.toString())
        }
    }

    internal inner class UserShareHandler(id: Int) : UserHandler(id), IUserShareHandler {

        private var obj: RuntimeObject? = null
        private var mutex = false

        override val share: IUserShareHandler
            get() = this

        override fun get(): RuntimeObject? {
            return obj
        }

        override fun set(obj: RuntimeObject?): RuntimeObject? {
            val tmp = this.obj
            this.obj = obj
            return tmp
        }

        override fun lock(): Boolean {
            if (mutex) {
                val pid = service.processService.pid
                enqueue(pid)
            } else {
                mutex = true
            }
            return true
        }

        override fun unlock(): Boolean {
            return if (mutex) {
                if (isEmpty) {
                    mutex = false
                } else {
                    dequeue()
                }
                true
            } else {
                false
            }
        }

        override fun toString(): String {
            return String.format("%s 共享：%s", super.toString(), obj.toString())
        }
    }

    internal inner class UserFileHandler(id: Int) : UserHandler(id), IUserFileHandler {

        private val path: String
            get() = arrUsers[id]?.name?.trimEnd('/') ?: "UNKNOWN"

        override fun query() = fsNodeRoot.query(path)

        override fun create(file: Boolean) = fsNodeRoot.createNode(path, file) != null

        override fun delete() = fsNodeRoot.deleteNode(path)

        override fun read() = fsNodeRoot.read(path)

        override fun write(data: ByteArray, overwrite: Boolean, createIfNotExist: Boolean) =
                fsNodeRoot.write(path, data, overwrite, createIfNotExist)

        override val file: IUserFileHandler
            get() = this
    }

    enum class MsgType {
        CREATE,
        SET_EXIT,
        WAIT,
    }

    internal inner class UserWindowHandler(id: Int) : UserHandler(id), IUserWindowHandler {

        var userWindow: UIUserWindow? = null
        var canExit = false

        override fun destroy() {
            super.destroy()
            destroyWindow()
            while (dequeue());
        }

        private val adapter = object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                if (canExit) {
                    RuntimeProcess.sendUserSignal(id, UserSignal.DESTROY)
                }
            }
        }

        private fun createWindow(param1: Int, param2: Int): Boolean {
            if (userWindow != null)
                return false
            val size = Toolkit.getDefaultToolkit().screenSize
            userWindow = UIUserWindow(arrUsers[id]!!.name,
                    Dimension(clamp(param1, 1, size.width),
                            clamp(param2, 1, size.height)))
            userWindow!!.addWindowListener(adapter)
            return true
        }

        private fun destroyWindow() {
            if (userWindow == null)
                return
            userWindow!!.removeWindowListener(adapter)
            userWindow!!.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
            userWindow!!.dispatchEvent(WindowEvent(userWindow, WindowEvent.WINDOW_CLOSING))
        }

        override fun sendMessage(type: Int, param1: Int, param2: Int): Boolean {
            when (type) {
                MsgType.CREATE.ordinal -> createWindow(param1, param2)
                MsgType.SET_EXIT.ordinal -> run {
                    canExit = param1 != 0
                    true
                }
                MsgType.WAIT.ordinal -> waitWindow()
            }
            return false
        }

        private fun waitWindow(): Boolean {
            if (!canExit)
                canExit = true
            val pid = service.processService.pid
            enqueue(pid)
            return true
        }

        override fun svg(op: Char, x: Int, y: Int): Boolean {
            if (userWindow == null)
                return false
            userWindow!!.graphics.addSVGInst(UIUserGraphics.SVGInst(op, x, y))
            return true
        }

        override fun str(op: Int, str: String): Boolean {
            if (userWindow == null)
                return false
            userWindow!!.graphics.addStrInst(UIUserGraphics.StrInst(op, str))
            return true
        }

        override val window: IUserWindowHandler
            get() = this
    }

    private val mediaTypeJson = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8")

    internal inner class UserNetHandler(id: Int) : UserHandler(id), IUserNetHandler {

        private var running = false
        private var finish = false
        private var cancel = false
        private var str: String? = null
        private var call: Call? = null
        private val url: String?
            get() = arrUsers[id]?.name

        override fun destroy() {
            super.destroy()
            if (running) {
                cancel = true
                call?.cancel()
            }
            while (dequeue());
        }

        override fun http(method: Int, json: Boolean, data: String): RuntimeObject {
            if (running && !finish)
                return RuntimeObject(-1L)
            if (finish) {
                running = false
                finish = false
                while (dequeue());
                return RuntimeObject(str)
            }
            val newUrl = url ?: return RuntimeObject(-2L)
            val build = when (method) {
                0 -> { b: Request.Builder -> b.url(newUrl).get() }
                1 -> { b: Request.Builder ->
                    b.post(RequestBody.create(mediaTypeJson, data))
                }
                else -> { b: Request.Builder -> b }
            }
            val request = Request.Builder()
                    .url(newUrl)
                    .apply { build(this) }
                    .build()
            running = true
            val pid = service.processService.pid
            enqueue(pid)
            httpClient.newCall(request).also { call = it }.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                    if (!cancel) {
                        finish = true
                        RuntimeProcess.sendUserSignal(id, UserSignal.WAKEUP)
                    }
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    finish = true
                    str = response.body()?.string()
                    RuntimeProcess.sendUserSignal(id, UserSignal.WAKEUP)
                }
            })
            return RuntimeObject(0L)
        }

        override val net: IUserNetHandler
            get() = this
    }

    internal inner class UserStruct(var name: String, var page: String, var type: UserType, var handler: IUserHandler)

    private fun newId(): Int {
        while (true) {
            if (arrUsers[cyclePtr] == null) {
                val id = cyclePtr
                setUserId.add(id)
                cyclePtr++
                if (cyclePtr >= MAX_USER) {
                    cyclePtr -= MAX_USER
                }
                service.processService.ring3.addHandle(id)
                return id
            }
            cyclePtr++
            if (cyclePtr >= MAX_USER) {
                cyclePtr -= MAX_USER
            }
        }
    }

    override fun create(name: String, page: String): Int {
        if (setUserId.size >= MAX_USER) {
            return -1
        }
        val splitIndex = name.indexOf('|')
        if (splitIndex <= 0 || splitIndex == name.length - 1)
            return -2
        val type = name.substring(0 until splitIndex)
        val newType = userTypeNames[type.toUpperCase()] ?: return -3
        val newName = name.substring(splitIndex + 1)
        return createHandle(newName, page, newType)
    }

    private fun getUser(id: Int) = if (id in 0 until MAX_USER) arrUsers[id] else null

    override fun read(id: Int): RuntimeObject =
            optional(getUser(id), RuntimeObject(true, RuntimeObjectType.kNoop)) { it.pipe.read() }

    override fun write(id: Int, obj: RuntimeObject) =
            optional(getUser(id), false) { it.pipe.write(obj) }

    override fun get(id: Int) =
            optional(getUser(id), null) { it.share.get() }

    override fun set(id: Int, obj: RuntimeObject?) =
            optional(getUser(id), null) { it.share.set(obj) }

    override fun lock(id: Int) =
            optional(getUser(id), false) { it.share.lock() }

    override fun unlock(id: Int) =
            optional(getUser(id), false) { it.share.unlock() }

    override fun queryFile(id: Int) =
            optional(getUser(id), -1L) { it.file.query() }

    override fun createFile(id: Int, file: Boolean) =
            optional(getUser(id), false) { it.file.create(file) }

    override fun deleteFile(id: Int) =
            optional(getUser(id), false) { it.file.delete() }

    override fun readFile(id: Int) =
            optional(getUser(id), null) { it.file.read() }

    override fun writeFile(id: Int, data: ByteArray, overwrite: Boolean, createIfNotExist: Boolean) =
            optional(getUser(id), -1L) { it.file.write(data, overwrite, createIfNotExist) }

    override fun sendMessage(id: Int, type: Int, param1: Int, param2: Int) =
            optional(getUser(id), false) { it.window.sendMessage(type, param1, param2) }

    override fun svg(id: Int, op: Char, x: Int, y: Int) =
            optional(getUser(id), false) { it.window.svg(op, x, y) }

    override fun str(id: Int, op: Int, str: String) =
            optional(getUser(id), false) { it.window.str(op, str) }

    override fun http(id: Int, method: Int, json: Boolean, data: String) =
            optional(getUser(id), RuntimeObject(null)) { it.net.http(method, json, data) }

    override fun destroy() {
        val handles = service.processService.ring3.handles.toList()
        handles.forEach { handle ->
            destroy(handle)
        }
        val blockHandle = service.processService.ring3.blockHandle
        if (blockHandle != -1) {
            if (setUserId.contains(blockHandle)) {
                val us = arrUsers[blockHandle]!!
                assert(us.type == UserType.PIPE)
                us.handler.dequeue(service.processService.pid)
            }
        }
        httpClient.dispatcher().executorService().shutdown()
    }

    override fun destroy(id: Int): Boolean {
        if (id !in 0 until MAX_USER)
            return false
        if (arrUsers[id] == null)
            return false
        val user = arrUsers[id]!!
        if (!mapNames.containsKey(user.name))
            return false
        logger.debug("${user.type} '${user.name}' #$id destroyed")
        setUserId.remove(id)
        mapNames.remove(user.name)
        user.handler.destroy()
        arrUsers[id] = null
        return true
    }

    override fun stat(api: Boolean): RuntimeArray {
        val array = RuntimeArray()
        if (api) {
            mapNames.values.sortedBy { it }
                    .asSequence()
                    .map { Pair(it, arrUsers[it]!!) }
                    .forEach {
                        val item = RuntimeArray()
                        item.add(RuntimeObject(it.first.toLong()))
                        item.add(RuntimeObject(it.second.type.desc))
                        item.add(RuntimeObject(it.second.name))
                        item.add(RuntimeObject(it.second.page))
                        item.add(RuntimeObject(it.second.handler.toString()))
                        array.add(RuntimeObject(item))
                    }
        } else {
            array.add(RuntimeObject(String.format("   %-5s   %-15s   %-15s   %-20s",
                    "Id", "Name", "Type", "Description")))
            mapNames.values.sortedBy { it }
                    .asSequence()
                    .map { Pair(it, arrUsers[it]!!) }
                    .forEach {
                        array.add(
                                RuntimeObject(String.format("   %-5s   %-15s   %-15s   %-20s",
                                        it.first.toLong(),
                                        it.second.name,
                                        it.second.type.toString(),
                                        it.second.handler.toString())))
                    }
        }
        return array
    }

    override fun signal(id: Int, type: UserSignal) {
        when (type) {
            RuntimeUserService.UserSignal.DESTROY -> destroy(id)
            else -> getUser(id)?.handler?.signal(type)
        }
    }

    private fun createHandlerFromType(type: UserType, id: Int): IUserHandler = when (type) {
        RuntimeUserService.UserType.PIPE -> UserPipeHandler(id)
        RuntimeUserService.UserType.SHARE -> UserShareHandler(id)
        RuntimeUserService.UserType.FILE -> UserFileHandler(id)
        RuntimeUserService.UserType.WINDOW -> UserWindowHandler(id)
        RuntimeUserService.UserType.NET -> UserNetHandler(id)
    }

    private fun createHandle(name: String, page: String, type: UserType): Int {
        val h = mapNames[name]
        if (h != null) {
            return h
        }
        val id = newId()
        logger.debug("$type '$name' #$id created")
        mapNames[name] = id
        arrUsers[id] = UserStruct(name, page, type, createHandlerFromType(type, id))
        return id
    }

    companion object {

        private const val MAX_USER = 1000
        private val logger = Logger.getLogger("user")
        private val userTypeNames = UserType.values().associate { it.toString() to it }
        private val httpClient = OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .build()

        private fun <T> optional(us: UserStruct?, def: T, select: (IUserHandler) -> T): T =
                if (us == null) def
                else select(us.handler)

        private fun <T : Comparable<T>> clamp(v: T, min: T, max: T) =
                if (v > max) max else if (v < min) min else v
    }
}
