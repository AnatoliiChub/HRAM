package com.achub.hram.view.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import com.achub.hram.style.Dimen24
import com.achub.hram.style.LabelMedium
import com.achub.hram.style.White
import com.achub.hram.style.White80
import com.achub.hram.view.components.dialog.DialogButton
import com.achub.hram.view.components.dialog.DialogElevatedCard
import com.achub.hram.view.components.dialog.DialogTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NameActivityDialog(
    title: String,
    message: String,
    buttonText: String = "Ok",
    name: String,
    error: String?,
    onNameChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onButonClick: (String) -> Unit = { onDismiss() }
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        DialogElevatedCard {
            Column(modifier = Modifier.padding(Dimen24), horizontalAlignment = CenterHorizontally) {
                DialogTitle(title = title)
                Spacer(Modifier.height(Dimen24))
                Text(text = message, color = White, style = LabelMedium, textAlign = TextAlign.Center)
                Spacer(Modifier.height(Dimen24))
                TextField(
                    value = name,
                    onValueChange = { onNameChanged(it) },
                    singleLine = true,
                    isError = error != null,
                    supportingText = {
                        error?.let {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = error,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TextFieldDefaults.colors(focusedIndicatorColor = White80)
                )
                Spacer(Modifier.height(Dimen24))
                DialogButton(
                    text = buttonText,
                    enabled = error == null && name.isNotBlank(),
                    onClick = { onButonClick(name) })
            }
        }
    }
}

@Preview
@Composable
fun PreviewNameActivityDialog() {
    NameActivityDialog(
        title = "Name your activity",
        message = "Please enter a name for your activity to help you identify it later.",
        name = "213",
        error = "Name cannot be empty",
        onNameChanged = {},
        onDismiss = {}
    )
}
