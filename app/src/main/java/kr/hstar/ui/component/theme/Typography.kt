package kr.hstar.ui.component.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// 폰트(사용하지 않음)와 텍스트 스타일을 정의
val captionTextStyle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.W400,
    fontSize = 16.sp
)

val adNetTypography = Typography(
    h1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W300,
        fontSize = 96.sp
    ),
    h2 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W400,
        fontSize = 60.sp
    ),
    h3 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W600,
        fontSize = 48.sp
    ),
    h4 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W600,
        fontSize = 34.sp
    ),
    h5 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W600,
        fontSize = 24.sp
    ),
    h6 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W400,
        fontSize = 20.sp
    ),
    subtitle1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500,
        fontSize = 16.sp
    ),
    subtitle2 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W600,
        fontSize = 14.sp
    ),
    body1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W600,
        fontSize = 16.sp
    ),
    body2 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W400,
        fontSize = 14.sp
    ),
    button = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W600,
        fontSize = 14.sp
    ),
    caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500,
        fontSize = 12.sp
    ),
    overline = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W400,
        fontSize = 12.sp
    )
)
