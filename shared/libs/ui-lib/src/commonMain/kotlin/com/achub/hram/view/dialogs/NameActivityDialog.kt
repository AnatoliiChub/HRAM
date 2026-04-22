package com.achub.hram.view.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import com.achub.hram.style.Dimen24
import com.achub.hram.style.LabelMedium
import com.achub.hram.view.components.HramTextField
import com.achub.hram.view.components.dialog.DialogButton
import com.achub.hram.view.components.dialog.DialogElevatedCard
import com.achub.hram.view.components.dialog.DialogTitle
import hram.ui_lib.generated.resources.Res
import hram.ui_lib.generated.resources.activity_validation_empty
import hram.ui_lib.generated.resources.dialog_info_ok
import hram.ui_lib.generated.resources.dialog_name_activity_message
import hram.ui_lib.generated.resources.dialog_name_activity_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NameActivityDialog(
    title: StringResource,
    message: StringResource,
    buttonText: StringResource = Res.string.dialog_info_ok,
    name: String,
    error: StringResource?,
    dismissable: Boolean = false,
    onNameChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onButtonClick: (String) -> Unit = { onDismiss() }
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = if (dismissable) {
            DialogProperties()
        } else {
            DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        }
    ) {
        DialogElevatedCard {
            val colorScheme = MaterialTheme.colorScheme
            Column(modifier = Modifier.padding(Dimen24), horizontalAlignment = CenterHorizontally) {
                DialogTitle(title = title)
                Spacer(Modifier.height(Dimen24))
                Text(
                    text = stringResource(message),
                    color = colorScheme.onSurface.copy(alpha = 0.8f),
                    style = LabelMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(Dimen24))
                HramTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = name,
                    onValueChange = { onNameChanged(it) },
                    singleLine = true,
                    isError = error != null,
                    textStyle = LabelMedium.copy(color = colorScheme.onBackground),
                    supportingText = {
                        error?.let {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = stringResource(error),
                                color = colorScheme.error
                            )
                        }
                    },
                )
                Spacer(Modifier.height(Dimen24))
                DialogButton(
                    text = stringResource(buttonText),
                    enabled = error == null && name.isNotBlank(),
                    onClick = { onButtonClick(name) }
                )
            }
        }
    }
}

@Preview(device = "spec:width=1024dp,height=768dp")
@Composable
fun PreviewNameActivityDialog() {
    Scaffold {
        NameActivityDialog(
            title = Res.string.dialog_name_activity_title,
            message = Res.string.dialog_name_activity_message,
            name = "213",
            error = Res.string.activity_validation_empty,
            onNameChanged = {},
            onDismiss = {}
        )
    }
}
