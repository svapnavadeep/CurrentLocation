package com.currentlocation.ui.permission.bottom

import android.content.Context
import com.currentlocaton.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog


open class BaseBottomSheetCLDialog (context: Context):BottomSheetDialog(context, R.style.CL_BottomSheetDialog){

    init {
        dismissWithAnimation=true
        behavior.apply {
            isDraggable=false
            state= BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun show() {
        if(isShowing)return
        behavior.state=BottomSheetBehavior.STATE_EXPANDED
        super.show()
    }
}

