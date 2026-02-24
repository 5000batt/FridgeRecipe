package com.kjw.fridgerecipe.presentation.ui.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kjw.fridgerecipe.R
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
fun OnboardingOverlay(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    val pages =
        listOf(
            Triple(
                R.drawable.step1,
                stringResource(R.string.on_boarding_overlay_step1_title),
                stringResource(R.string.on_boarding_overlay_step1_content),
            ),
            Triple(
                R.drawable.step2,
                stringResource(R.string.on_boarding_overlay_step2_title),
                stringResource(R.string.on_boarding_overlay_step2_content),
            ),
            Triple(
                R.drawable.step3,
                stringResource(R.string.on_boarding_overlay_step3_title),
                stringResource(R.string.on_boarding_overlay_step3_content),
            ),
        )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 상단 건너뛰기
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, end = 16.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                if (pagerState.currentPage < 2) {
                    TextButton(onClick = onFinish) {
                        Text(
                            stringResource(R.string.on_boarding_overlay_skip),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }

            // 슬라이드
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
            ) { page ->
                val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                val scale = 1f - (pageOffset * 0.15f).coerceIn(0f, 1f)
                val alpha = 1f - (pageOffset * 0.8f).coerceIn(0f, 1f)

                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Image(
                        painter = painterResource(id = pages[page].first),
                        contentDescription = stringResource(R.string.on_boarding_overlay_guide_number_format, page + 1),
                        modifier =
                            Modifier
                                .fillMaxWidth(0.9f)
                                .weight(1f)
                                .padding(bottom = 32.dp)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    this.alpha = alpha
                                }.clip(RoundedCornerShape(24.dp)),
                    )

                    Text(
                        text = pages[page].second,
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.graphicsLayer { this.alpha = alpha },
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = pages[page].third,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.graphicsLayer { this.alpha = alpha },
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // 하단 인디케이터 및 버튼
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 32.dp),
                ) {
                    repeat(3) { index ->
                        Box(
                            modifier =
                                Modifier
                                    .size(if (pagerState.currentPage == index) 10.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (pagerState.currentPage == index) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                        },
                                    ),
                        )
                    }
                }

                Button(
                    onClick = {
                        if (pagerState.currentPage == 2) {
                            onFinish()
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                ) {
                    Text(
                        text =
                            if (pagerState.currentPage ==
                                2
                            ) {
                                stringResource(R.string.on_boarding_overlay_start)
                            } else {
                                stringResource(R.string.on_boarding_overlay_next)
                            },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }
    }
}
