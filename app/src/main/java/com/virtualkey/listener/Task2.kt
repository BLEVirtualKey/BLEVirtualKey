package cn.cltx.mobile.dongfeng.listener

/**
 * @Author: GaiQS
 * @E-mail:gaiqs@sina.com
 * @Creation Date: 2018-08-02
 * @Date Modified: 2018-08-02
 * @Describe:
 * @param:<T>
 * @FIXME
 */
interface Task2<T, V, S> {
    fun run(t: T, v: V, s: S)
}