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
interface Task<T> {

    fun run(t: T)

}