package com.currentlocation.ui.permission.bottom

import android.content.Context
import android.view.View
import androidx.annotation.LayoutRes
import com.currentlocation.ui.permission.bottom.BaseBottomSheetCLDialog


class BottomSheetAlertCLDialog(context: Context,
                               @LayoutRes layoutRes:Int,
                               val positiveBtnId:Int=-1,
                               val negativeBtnId:Int=-1,
                               val onPositiveClick:()->Unit={},
                               val onNegativeClick:()->Unit={}) : BaseBottomSheetCLDialog(context){

    init {
        setCanceledOnTouchOutside(false)
        setContentView(layoutRes)
        onViewRender()
    }

    private fun onViewRender() {
        if (positiveBtnId>-1)
            findViewById<View>(positiveBtnId)?.setOnClickListener {
                onPositiveClick.invoke()
                dismiss()
            }

        if(negativeBtnId >-1){
            findViewById<View>(negativeBtnId)?.setOnClickListener {
                onNegativeClick.invoke()
                dismiss()
            }
        }
    }

    override fun onBackPressed() {
        onNegativeClick.invoke()
        super.onBackPressed()
    }
}
