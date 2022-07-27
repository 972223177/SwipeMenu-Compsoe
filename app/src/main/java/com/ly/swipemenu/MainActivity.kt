package com.ly.swipemenu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ly.swipemenu.ui.theme.SwipeMenuTheme
import com.ly.swipemenu_compose.SwipeMenu
import com.ly.swipemenu_compose.rememberSwipeMenuState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SwipeMenuTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Column {
                        SwipeMenuUi()
                    }
                }
            }
        }
    }
}

@Composable
fun SwipeMenuUi() {
    val swipeMenuState = rememberSwipeMenuState()
    SwipeMenu(state = swipeMenuState, rightMenu = {
        Row {
            Box(
                modifier = Modifier
                    .width(90.dp)
                    .height(74.dp)
                    .background(color = Color.Red)
                    .clickable(interactionSource = remember {
                        MutableInteractionSource()
                    }, indication = rememberRipple()) {
                        swipeMenuState.reset()
                    }
            )
        }
    }) {
        Box(
            modifier = Modifier
                .height(74.dp)
                .background(color = Color.LightGray)
                .clickable(interactionSource = remember {
                    MutableInteractionSource()
                }, indication = rememberRipple()) {
                    swipeMenuState.showRightMenu()
                }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SwipeMenuTheme {
        SwipeMenuUi()
    }
}