package cn.cltx.mobile.dongfeng.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationSet
import com.virtualkey.app.R
import com.virtualkey.dialog.OptAnimationLoader

/**
 * @Author: GaiQS
 * @E-mail:gaiqs@sina.com
 * @Creation Date: 2018-08-02
 * @Date Modified: 2018-08-02
 * @Describe:
 * @param:<T>
 * @FIXME
 */
abstract class DialogBase : Dialog, View.OnClickListener {
    var mDialogView: View? = null
    var mModalInAnim: AnimationSet? = null
    var onClickListener: View.OnClickListener? = null
    abstract fun layoutId(): Int

    constructor(paramContext: Context) : super(paramContext, R.style.alert_dialog)

    constructor(paramContext: Context, onClickListener: View.OnClickListener) : super(paramContext, R.style.alert_dialog) {
        this.onClickListener = onClickListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId())
        mModalInAnim = OptAnimationLoader.loadAnimation(context, R.anim.modal_in) as AnimationSet
        mDialogView = window!!.decorView.findViewById(android.R.id.content)
    }

    override fun show() {
        super.show()
    }

    override fun dismiss() {
        super.dismiss()
    }

    override fun onClick(v: View) {
        onClickListener?.onClick(v)
    }

    override fun onStart() {
        super.onStart()
        mDialogView!!.startAnimation(mModalInAnim)
    }
}
