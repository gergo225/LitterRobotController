package com.gergo225.litterrobotcontroller

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun MotorControlsView(
    onRotateLeft: () -> Unit,
    onRotateRight: () -> Unit,
    onStop: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(48.dp)
    ) {
        Row(horizontalArrangement = Arrangement.SpaceEvenly) {
            ControlButton(onClick = onRotateLeft, drawableId = R.drawable.rotate_left)
            ControlButton(onClick = onRotateRight, drawableId = R.drawable.rotate_right)
        }
        ControlButton(onClick = onStop, drawableId = R.drawable.stop_circle)
    }
}

@Composable
private fun ControlButton(onClick: () -> Unit, @DrawableRes drawableId: Int) {
    val buttonSize = 150.dp

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(10.dp)
            .clip(CircleShape)
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true)
            )
            .size(buttonSize)
    ) {
        Icon(
            painter = painterResource(drawableId),
            contentDescription = null,
            modifier = Modifier.size(buttonSize)
        )
    }
}

@Preview
@Composable
fun MotorControlsPreview() {
    MotorControlsView({}, {}, {})
}
