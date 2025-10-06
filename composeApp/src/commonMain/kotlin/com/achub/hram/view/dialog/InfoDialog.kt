package com.achub.hram.view.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.achub.hram.style.LabelMedium
import com.achub.hram.style.White
import com.achub.hram.view.dialog.base.DialogButton
import com.achub.hram.view.dialog.base.DialogElevatedCard
import com.achub.hram.view.dialog.base.DialogTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoDialog(
    title: String,
    message: String,
    buttonText: String = "Ok",
    onDismiss: () -> Unit,
    onButonClick: () -> Unit = { onDismiss() }
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        DialogElevatedCard {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = CenterHorizontally
            ) {
                DialogTitle(title = title)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = message,
                    color = White,
                    style = LabelMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                DialogButton(text = buttonText, onClick = onButonClick)
            }
        }
    }
}
