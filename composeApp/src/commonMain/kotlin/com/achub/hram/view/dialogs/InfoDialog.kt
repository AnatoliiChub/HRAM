package com.achub.hram.view.dialogs

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
import androidx.compose.ui.text.style.TextAlign
import com.achub.hram.style.Dimen24
import com.achub.hram.style.LabelMedium
import com.achub.hram.style.White
import com.achub.hram.view.components.dialog.DialogButton
import com.achub.hram.view.components.dialog.DialogElevatedCard
import com.achub.hram.view.components.dialog.DialogTitle
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.dialog_info_ok
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoDialog(
    title: StringResource,
    message: String,
    buttonText: StringResource = Res.string.dialog_info_ok,
    onDismiss: () -> Unit,
    onButonClick: () -> Unit = { onDismiss() }
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        DialogElevatedCard {
            Column(modifier = Modifier.padding(Dimen24), horizontalAlignment = CenterHorizontally) {
                DialogTitle(title = title)
                Spacer(Modifier.height(Dimen24))
                Text(text = message, color = White, style = LabelMedium, textAlign = TextAlign.Center)
                Spacer(Modifier.height(Dimen24))
                DialogButton(text = stringResource(buttonText), onClick = onButonClick)
            }
        }
    }
}
