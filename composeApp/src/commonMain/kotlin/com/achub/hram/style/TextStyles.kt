package com.achub.hram.style

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.sp


val TextHeading3 = 40.sp
val TextSizeHeadingLarge = 32.sp
val TextSizeHeadingMedium = 30.sp
val TextSizeHeadingSmall = 28.sp
val TextSizeLabelLarge = 22.sp
val TextSizeLabelBig = 18.sp
val TextSizeLabelMedium = 16.sp
val TextSizeLabelSmall = 14.sp
val TextSizeLabelTiny = 12.sp


val Heading3 = TextStyle(fontSize = TextHeading3, color = White, letterSpacing = 1.5.sp, fontWeight = Bold)
val HeadingLarge = TextStyle(fontSize = TextSizeHeadingLarge, color = White, letterSpacing = 1.2.sp)
val HeadingMedium = TextStyle(fontSize = TextSizeHeadingMedium, color = White, letterSpacing = 1.2.sp)
val HeadingMediumBold = HeadingMedium.copy(fontWeight = Bold)
val HeadingSmall = TextStyle(fontSize = TextSizeHeadingSmall, color = White, letterSpacing = 1.2.sp)
val LabelLarge = TextStyle(fontSize = TextSizeLabelLarge, color = White)
val LabelBig = TextStyle(fontSize = TextSizeLabelBig, color = White)
val LabelBigBold = LabelBig.copy(fontWeight = Bold)
val LabelMedium = TextStyle(fontSize = TextSizeLabelMedium, color = White)
val LabelMediumBold = LabelMedium.copy(fontWeight = Bold)

val LabelSmall = TextStyle(fontSize = TextSizeLabelSmall, color = White)

