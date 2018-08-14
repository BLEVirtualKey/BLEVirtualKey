package cn.cltx.mobile.dongfeng.dialog

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import com.virtualkey.app.R
import kotlinx.android.synthetic.main.update_hint.*


/**
 * @Author: GaiQS
 * @E-mail:gaiqs@sina.com
 * @Creation Date: 2018-08-02
 * @Date Modified: 2018-08-02
 * @Describe:
 * @param:<T>
 * @FIXME
 */
class DialogHint(paramContext: Context, val str: String, val listener: View.OnClickListener?) : DialogBase(paramContext) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    fun initView() {
        tv_des!!.text = str
    }

    override fun onClick(v: View) {
        when (v.id) {
        }
    }

    fun setDes(str: String) {
        val msg = Message.obtain()
        msg.obj = str
        msg.what = 0
        mHandler.sendMessage(msg)
    }

    override fun layoutId(): Int {
        return R.layout.update_hint
    }

    val mHandler = object : Handler() {

        override fun handleMessage(msg: Message) {
            val what = msg.what
            if (what == 0) {
                tv_des!!.text = msg!!.obj.toString()
            }
        }
    }
}
