package com.achub.hram.view.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.W600
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.achub.hram.style.DarkGray
import com.achub.hram.style.LabelLarge
import com.achub.hram.style.Red
import com.achub.hram.style.White
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseDialog(
    title: String,
    onDismissRequest: () -> Unit,
    buttonTitle: String = "OK",
    isButtonVisible: Boolean = true,
    onButtonClick: () -> Unit,
    content: @Composable () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest
    ) {
        val backgroundCardColor = DarkGray
        ElevatedCard(
            elevation = CardDefaults.cardElevation(16.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
            colors = CardColors(
                contentColor = White,
                containerColor = backgroundCardColor,
                disabledContainerColor = backgroundCardColor,
                disabledContentColor = White
            ),
        ) {
            Column(
                modifier = Modifier.align(CenterHorizontally),
                horizontalAlignment = CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = title,
                    style = LabelLarge.copy(color = White, fontWeight = W600),
                    textAlign = TextAlign.Center
                )
                content()
                if (isButtonVisible) {
                    Button(
                        modifier = Modifier
                            .padding(16.dp),
                        onClick = onButtonClick,
                        colors = ButtonColors(
                            containerColor = Red,
                            contentColor = White,
                            disabledContainerColor = Red.copy(alpha = 0.25f),
                            disabledContentColor = White.copy(alpha = 0.25f)
                        )
                    ) {
                        Text(text = buttonTitle, style = LabelLarge)
                    }
                }
            }
        }
    }
}

@Composable
@Preview
private fun BaseDialogPreview() {
    BaseDialog(
        title = "Title",
        buttonTitle = "Confirm",
        onDismissRequest = {},
        onButtonClick = {}
    ) {
        Text(
            text = "This is the content of the dialog. It can be multiple lines long.",
            style = LabelLarge.copy(color = White),
            textAlign = TextAlign.Center
        )
    }
}
