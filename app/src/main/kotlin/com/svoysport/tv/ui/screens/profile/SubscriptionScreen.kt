package com.svoysport.tv.ui.screens.profile

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import com.svoysport.tv.R
import com.svoysport.tv.ui.theme.Primary
import com.svoysport.tv.ui.theme.PrimaryPressed

private val subscriptionButtonGradient = Brush.horizontalGradient(
    listOf(Color(0xFF4556EB), Color(0xFF273185))
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    onBack: () -> Unit,
    onSubscribe: (SubscriptionPlan) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    var selectedPlan by remember { mutableStateOf(defaultSubscriptionPlan) }
    var showOffer by remember { mutableStateOf(false) }
    val defaultPlanFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { defaultPlanFocus.requestFocus() } }

    BoxWithConstraints(modifier.fillMaxSize().background(Color(0xFF0F0F10))) {
        val scale = minOf(maxWidth.value / 1920f, maxHeight.value / 1080f, 1f).coerceAtLeast(0.35f)

        Box(
            Modifier.fillMaxSize().background(
                Brush.radialGradient(
                    colors = listOf(Color(0x55273185), Color.Transparent),
                    center = Offset(360f * scale, 900f * scale),
                    radius = 850f * scale
                )
            )
        )

        BackButton(
            onClick = onBack,
            scale = scale,
            modifier = Modifier.align(Alignment.TopStart).padding(
                start = (60f * scale).dp,
                top = (60f * scale).dp
            )
        )

        Column(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = (65f * scale).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy((16f * scale).dp)
        ) {
            Text(
                "Подписка",
                color = Color(0xFFE3E3E3),
                fontSize = (54f * scale).sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                "Оформите подписку чтобы смотреть любые трансляции без ограничений",
                color = Color(0xFFE3E3E3),
                fontSize = (28f * scale).sp,
                fontWeight = FontWeight.Medium
            )
        }

        Row(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = (241f * scale).dp),
            horizontalArrangement = Arrangement.spacedBy((20f * scale).dp)
        ) {
            subscriptionPlans.forEach { plan ->
                PlanCard(
                    plan = plan,
                    selected = selectedPlan.id == plan.id,
                    scale = scale,
                    focusRequester = if (plan.id == defaultSubscriptionPlan.id) defaultPlanFocus else null,
                    onSelect = { selectedPlan = plan }
                )
            }
        }

        Text(
            "*1 месяц равен 30 календарным дням",
            color = Color(0xFFA8A9B2),
            fontSize = (24f * scale).sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.TopStart).padding(
                start = (340f * scale).dp,
                top = (727f * scale).dp
            )
        )

        SubscribeButton(
            scale = scale,
            onClick = { onSubscribe(selectedPlan) },
            modifier = Modifier.align(Alignment.TopCenter).padding(top = (803f * scale).dp)
        )

        Surface(
            onClick = { showOffer = true },
            modifier = Modifier.align(Alignment.TopCenter).padding(top = (962f * scale).dp),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent
            ),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.04f)
        ) {
            Text(
                "Договор публичной оферты",
                color = Color(0xFF65666E),
                fontSize = (20f * scale).sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.padding((8f * scale).dp)
            )
        }

        if (showOffer) {
            OfferModal(scale = scale, onClose = { showOffer = false })
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun BackButton(onClick: () -> Unit, scale: Float, modifier: Modifier = Modifier) {
    var focused by remember { mutableStateOf(false) }
    val animatedScale by animateFloatAsState(if (focused) 1.08f else 1f, tween(150), label = "back")
    Surface(
        onClick = onClick,
        modifier = modifier.size((80f * scale).dp).onFocusChanged { focused = it.isFocused }.scale(animatedScale),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(200.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color(0xB3565A80),
            focusedContainerColor = Primary,
            pressedContainerColor = PrimaryPressed
        ),
        scale = ClickableSurfaceDefaults.scale(scale = 1f, focusedScale = 1f)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_left),
                contentDescription = "Назад",
                tint = Color(0xFFE3E3E3),
                modifier = Modifier.size((40f * scale).dp)
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun PlanCard(
    plan: SubscriptionPlan,
    selected: Boolean,
    scale: Float,
    focusRequester: FocusRequester?,
    onSelect: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val animatedScale by animateFloatAsState(if (focused) 1.04f else 1f, tween(150), label = "plan")
    val background = if (plan.id == defaultSubscriptionPlan.id) {
        Brush.verticalGradient(listOf(Color(0xFF1E245C), Color(0xFF050507)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF292A31), Color(0xFF050506)))
    }
    val shape = RoundedCornerShape((20f * scale).dp)

    Surface(
        onClick = onSelect,
        modifier = Modifier
            .size(width = (400f * scale).dp, height = (462f * scale).dp)
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .onFocusChanged {
                focused = it.isFocused
                if (it.isFocused) onSelect()
            }
            .scale(animatedScale)
            .background(background, shape),
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            pressedContainerColor = Color.Transparent
        ),
        border = ClickableSurfaceDefaults.border(
            border = Border(BorderStroke(if (selected) 2.dp else 3.dp, if (selected) Color(0xFF7B88FB) else Color(0x1AFFFFFF)), shape = shape),
            focusedBorder = Border(BorderStroke(4.dp, Color(0xFF4556EB)), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(scale = 1f, focusedScale = 1f)
    ) {
        Box(Modifier.fillMaxSize().padding((24f * scale).dp)) {
            if (plan.badge != null) {
                Box(
                    modifier = Modifier.align(Alignment.TopCenter)
                        .background(
                            if (plan.id == defaultSubscriptionPlan.id) subscriptionButtonGradient else Brush.horizontalGradient(listOf(Color(0xFF595A62), Color(0xFF2C2C2F))),
                            RoundedCornerShape(103.dp)
                        )
                        .padding(horizontal = (16f * scale).dp, vertical = (8f * scale).dp)
                ) {
                    Text(plan.badge, color = Color.White, fontSize = (20f * scale).sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Column(
                modifier = Modifier.fillMaxSize().padding(top = (52f * scale).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(plan.title, color = Color(0xFFE3E3E3), fontSize = (36f * scale).sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height((72f * scale).dp))
                if (plan.monthlyPrice != null) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(plan.monthlyPrice, color = Color(0xFFE3E3E3), fontSize = (54f * scale).sp, fontWeight = FontWeight.SemiBold)
                        Text(" BYN / мес", color = Color(0xFFE3E3E3), fontSize = (32f * scale).sp)
                    }
                } else {
                    Text(
                        "Цена на сайте",
                        color = Color(0xFFE3E3E3),
                        fontSize = (34f * scale).sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (plan.total != null) {
                    Text("Итого ${plan.total}", color = Color(0xFFA8A9B2), fontSize = (26f * scale).sp)
                }
                Spacer(Modifier.weight(1f))
                Text(plan.hint, color = Color(0xFFA8A9B2), fontSize = (20f * scale).sp)
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SubscribeButton(scale: Float, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(width = (380f * scale).dp, height = (80f * scale).dp)
            .background(subscriptionButtonGradient, RoundedCornerShape((20f * scale).dp)),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape((20f * scale).dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(BorderStroke(4.dp, Color(0xFF7B88FB)), shape = RoundedCornerShape((20f * scale).dp))
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.06f)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Оформить подписку", color = Color(0xFFE3E3E3), fontSize = (28f * scale).sp, fontWeight = FontWeight.Medium)
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun OfferModal(scale: Float, onClose: () -> Unit) {
    BackHandler(onBack = onClose)
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)), contentAlignment = Alignment.Center) {
        Box(
            Modifier.size(width = (1056f * scale).dp, height = (940f * scale).dp)
                .background(Color.White, RoundedCornerShape((32f * scale).dp))
                .padding((48f * scale).dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy((20f * scale).dp)) {
                Text("Договор публичной оферты", color = Color(0xFF0F0F10), fontSize = (36f * scale).sp, fontWeight = FontWeight.SemiBold)
                Text(
                    "на приобретение доступа к просмотру трансляций через спортивный портал sport-tv.by",
                    color = Color(0xFF1E1F20), fontSize = (28f * scale).sp
                )
                Text("1. Общие положения.", color = Color(0xFF1E1F20), fontSize = (32f * scale).sp)
                Text(
                    "1.1. Настоящий договор определяет порядок приобретения доступа к просмотру трансляций.\n\n" +
                        "1.2. Фактом принятия условий является подтверждение выбранного тарифа и оплаты на странице оформления.\n\n" +
                        "Полный актуальный текст договора публикуется на сайте sport-tv.by.",
                    color = Color(0xFF1E1F20), fontSize = (26f * scale).sp, lineHeight = (40f * scale).sp
                )
            }
            Surface(
                onClick = onClose,
                modifier = Modifier.align(Alignment.TopEnd).size((80f * scale).dp),
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(200.dp)),
                colors = ClickableSurfaceDefaults.colors(containerColor = Color(0x33565A80), focusedContainerColor = Color(0x66565A80))
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("×", color = Color(0xFF404147), fontSize = (40f * scale).sp, textAlign = TextAlign.Center)
                }
            }
        }
    }
}
