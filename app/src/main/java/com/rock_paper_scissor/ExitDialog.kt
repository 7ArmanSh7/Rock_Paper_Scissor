package com.rock_paper_scissor
import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog;
class ExitDialog {
    companion object {
        fun callAlertDialog(context: Context) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Exit alert!")
                .setMessage("Are you sure about quiting the game?")
                .setPositiveButton("Yes") { dialog, id -> System.exit(0)}
                .setNegativeButton("No") { dialog, id -> dialog.cancel()}
                .setCancelable(false).create().show()
        }
    }
}