package app.aaps.core.ui.compose

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import app.aaps.core.ui.R

/**
 * A simple alert dialog with a title, message, and OK button.
 *
 * @param title The dialog title
 * @param message The message to display (supports HTML)
 * @param onDismiss Called when dialog is dismissed
 */
@Composable
fun OkDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(text = AnnotatedString.fromHtml(message.replace("\n", "<br>")))
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.ok))
            }
        },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    )
}

/**
 * A confirmation dialog with OK and Cancel buttons.
 *
 * @param title The dialog title (optional)
 * @param message The message to display (supports HTML)
 * @param secondMessage Optional secondary message in accent color
 * @param icon Optional drawable resource for an icon
 * @param onConfirm Called when OK is clicked
 * @param onDismiss Called when Cancel is clicked or dialog is dismissed
 */
@Composable
fun OkCancelDialog(
    title: String? = null,
    message: String,
    secondMessage: String? = null,
    @DrawableRes icon: Int? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = icon?.let {
            {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            }
        },
        title = title?.let {
            {
                Text(
                    text = title,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = AnnotatedString.fromHtml(message.replace("\n", "<br>")),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                secondMessage?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = secondMessage,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    )
}

/**
 * A dialog with Yes, No, and Cancel buttons.
 *
 * @param title The dialog title
 * @param message The message to display (supports HTML)
 * @param onYes Called when Yes is clicked
 * @param onNo Called when No is clicked
 * @param onCancel Called when Cancel is clicked or dialog is dismissed
 */
@Composable
fun YesNoCancelDialog(
    title: String,
    message: String,
    onYes: () -> Unit,
    onNo: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column {
                Text(text = AnnotatedString.fromHtml(message.replace("\n", "<br>")))
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onCancel) {
                        Text(stringResource(R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onNo) {
                        Text(stringResource(R.string.no))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onYes) {
                        Text(stringResource(R.string.yes))
                    }
                }
            }
        },
        confirmButton = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    )
}

/**
 * An error/warning dialog with a warning icon, dismiss button, and optional positive button.
 *
 * @param title The dialog title
 * @param message The message to display (supports HTML)
 * @param positiveButton Optional text for positive button (if null, only dismiss is shown)
 * @param onPositive Called when positive button is clicked
 * @param onDismiss Called when dismiss is clicked or dialog is dismissed
 */
@Composable
fun ErrorDialog(
    title: String,
    message: String,
    positiveButton: String? = null,
    onPositive: () -> Unit = {},
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = AnnotatedString.fromHtml(message.replace("\n", "<br>")),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            positiveButton?.let {
                TextButton(onClick = onPositive) {
                    Text(positiveButton)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dismiss))
            }
        },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    )
}
