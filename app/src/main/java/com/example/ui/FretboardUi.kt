package com.example.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.GuitarTheory
import com.example.db.ScoreRecord
import com.example.ui.theme.*
import com.example.viewmodel.FretboardViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FretboardTrainerApp(viewModel: FretboardViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Guitar Icon",
                            tint = GoldPrimary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "FRETBOARD TRAINER",
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.SansSerif,
                            color = GoldPrimary,
                            letterSpacing = 1.5.sp,
                            fontSize = 18.sp
                        )
                    }
                },
                actions = {
                    if (viewModel.activeScreen != "dashboard" && viewModel.activeScreen != "stats" && viewModel.activeScreen != "settings") {
                        IconButton(
                            onClick = {
                                viewModel.stopGuitarRound()
                                viewModel.stopPlayAlong()
                                viewModel.activeScreen = "dashboard"
                            },
                            modifier = Modifier.testTag("nav_home")
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = GoldPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BlackBackground,
                    titleContentColor = GoldPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = BlackBackground,
                contentColor = GoldPrimary,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = viewModel.activeScreen == "dashboard" || viewModel.activeScreen == "find_it" || viewModel.activeScreen == "name_it" || viewModel.activeScreen == "guitar_input" || viewModel.activeScreen == "play_along",
                    onClick = {
                        viewModel.stopGuitarRound()
                        viewModel.stopPlayAlong()
                        viewModel.activeScreen = "dashboard"
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BlackBackground,
                        selectedTextColor = GoldPrimary,
                        unselectedIconColor = GoldSecondary.copy(alpha = 0.6f),
                        unselectedTextColor = GoldSecondary.copy(alpha = 0.6f),
                        indicatorColor = GoldPrimary
                    )
                )
                NavigationBarItem(
                    selected = viewModel.activeScreen == "stats",
                    onClick = {
                        viewModel.stopGuitarRound()
                        viewModel.stopPlayAlong()
                        viewModel.activeScreen = "stats"
                    },
                    icon = { Icon(Icons.Default.Info, contentDescription = "Stats") },
                    label = { Text("Stats", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BlackBackground,
                        selectedTextColor = GoldPrimary,
                        unselectedIconColor = GoldSecondary.copy(alpha = 0.6f),
                        unselectedTextColor = GoldSecondary.copy(alpha = 0.6f),
                        indicatorColor = GoldPrimary
                    )
                )
                NavigationBarItem(
                    selected = viewModel.activeScreen == "settings",
                    onClick = {
                        viewModel.stopGuitarRound()
                        viewModel.stopPlayAlong()
                        viewModel.activeScreen = "settings"
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BlackBackground,
                        selectedTextColor = GoldPrimary,
                        unselectedIconColor = GoldSecondary.copy(alpha = 0.6f),
                        unselectedTextColor = GoldSecondary.copy(alpha = 0.6f),
                        indicatorColor = GoldPrimary
                    )
                )
            }
        },
        containerColor = BlackBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BlackBackground)
        ) {
            AnimatedContent(
                targetState = viewModel.activeScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    "dashboard" -> DashboardScreen(viewModel)
                    "find_it" -> FindItScreen(viewModel)
                    "name_it" -> NameItScreen(viewModel)
                    "guitar_input" -> GuitarInputScreen(viewModel)
                    "play_along" -> PlayAlongScreen(viewModel)
                    "stats" -> StatsScreen(viewModel)
                    "settings" -> SettingsScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(viewModel: FretboardViewModel) {
    val scores by viewModel.allScores.collectAsState()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Hero Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(SlateSurfaceVariant, SlateSurface)
                        )
                    )
                    .border(1.dp, GoldPrimary, RoundedCornerShape(16.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Column {
                    Text(
                        text = "MASTER THE NECK",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldPrimary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Train your visual note finding, name highlighted frets, or plug in your physical guitar for pitch-listening active challenges.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Section Title: Training Modes
        item {
            Text(
                text = "TRAINING MODES",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = GoldSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // 4 Practice Modes Grid in 2x2 or stack
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ModeCard(
                    title = "Find-it Mode",
                    description = "A target note is shown. Find and click all correct string & fret locations on the neck.",
                    icon = Icons.Default.Search,
                    testTag = "mode_find_it",
                    onClick = {
                        viewModel.activeScreen = "find_it"
                        viewModel.startFindItRound()
                    }
                )
                ModeCard(
                    title = "Name-it Mode",
                    description = "A fret position is highlighted. Choose the correct standard note name from multiple choices.",
                    icon = Icons.Default.Info,
                    testTag = "mode_name_it",
                    onClick = {
                        viewModel.activeScreen = "name_it"
                        viewModel.startNameItRound()
                    }
                )
                ModeCard(
                    title = "Guitar Pitch Mode",
                    description = "Play target notes on your physical guitar. The app listens and checks your pitch accuracy in real-time.",
                    icon = Icons.Default.PlayArrow,
                    testTag = "mode_guitar_input",
                    onClick = {
                        viewModel.activeScreen = "guitar_input"
                        viewModel.startGuitarRound()
                    }
                )
                ModeCard(
                    title = "Play-along Metronome",
                    description = "Follow a scrolling rhythmic metronome track. Play highlighted notes in sync with the beat.",
                    icon = Icons.Default.PlayArrow,
                    testTag = "mode_play_along",
                    onClick = {
                        viewModel.activeScreen = "play_along"
                    }
                )
            }
        }

        // Quick Stats / History
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT SESSIONS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = GoldSecondary,
                    letterSpacing = 1.sp
                )
                TextButton(
                    onClick = { viewModel.activeScreen = "stats" },
                    modifier = Modifier.testTag("btn_view_all_stats")
                ) {
                    Text("View Stats", color = GoldPrimary, fontSize = 13.sp)
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "View Details",
                        tint = GoldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        if (scores.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SlateSurface)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Empty Stats",
                            tint = TextSecondary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No sessions recorded yet.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Text(
                            "Complete a mode above to save your first score!",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(scores.take(3)) { score ->
                ScoreListItem(score)
            }
        }
    }
}

@Composable
fun ModeCard(
    title: String,
    description: String,
    icon: ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .shadow(8.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(GoldPrimary.copy(alpha = 0.6f), GoldTertiary.copy(alpha = 0.1f))))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            SlateSurface,
                            SlateSurfaceVariant.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(GoldPrimary.copy(alpha = 0.25f), GoldTonal)
                        )
                    )
                    .border(1.dp, GoldPrimary.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = GoldPrimary, modifier = Modifier.size(24.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = GoldPrimary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
            
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Start",
                tint = GoldPrimary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun ScoreListItem(score: ScoreRecord) {
    val modeName = when (score.mode) {
        "find_it" -> "Find-it Practice"
        "name_it" -> "Name-it Quiz"
        "guitar" -> "Guitar Pitch Game"
        "play_along" -> "Play-along Session"
        else -> "Practice Round"
    }
    
    val dateStr = remember(score.timestamp) {
        val sdf = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(score.timestamp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SlateSurface)
            .border(0.5.dp, SlateSurfaceVariant, RoundedCornerShape(10.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(modeName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
            Text(dateStr, fontSize = 10.sp, color = TextSecondary)
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${score.score} / ${score.total}",
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = GoldPrimary,
                modifier = Modifier.padding(end = 8.dp)
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (score.percentage >= 80f) GreenSuccess.copy(alpha = 0.2f) else GoldTonal),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${score.percentage.toInt()}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (score.percentage >= 80f) GreenSuccess else GoldPrimary
                )
            }
        }
    }
}

// --- Segmented Progress Bar for Note Positions ---
@Composable
fun SegmentedProgressBar(
    filledCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until totalCount) {
                val isFilled = i < filledCount
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            color = if (isFilled) GoldPrimary else SlateSurfaceVariant
                        )
                )
            }
        }
        
        Text(
            text = "$filledCount of $totalCount bars filled",
            fontSize = 11.sp,
            color = GoldSecondary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

// --- Fretboard Custom Graphic View ---
@Composable
fun FretboardView(
    maxFret: Int,
    highlightedPositions: List<Pair<Int, Int>> = emptyList(), // (stringIndex, fret)
    clickedPosition: Pair<Int, Int>? = null,
    isCorrectClick: Boolean? = null,
    onPositionSelected: ((Int, Int) -> Unit)? = null,
    showNoteNames: Boolean = false,
    labelOverride: Map<Pair<Int, Int>, String> = emptyMap(), // Map of positions to custom labels like "?"
    isBlinking: Boolean = false
) {
    val density = LocalDensity.current
    val nutWidthDp = 50.dp
    val fretWidthDp = 64.dp
    val fretboardWidth = nutWidthDp + (maxFret * 64).dp
    val fretboardHeight = 180.dp
    
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(fretboardHeight + 48.dp)
            .horizontalScroll(rememberScrollState())
    ) {
        Column(modifier = Modifier.width(fretboardWidth)) {
            // Main neck overlay box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(fretboardHeight)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 16.dp)
                        .pointerInput(maxFret, onPositionSelected) {
                            if (onPositionSelected == null) return@pointerInput
                            detectTapGestures { offset ->
                                val nutWidthPx = with(density) { 50.dp.toPx() }
                                val totalWidthPx = size.width
                                val usableWidthPx = totalWidthPx - nutWidthPx
                                val fretWidthPx = usableWidthPx / maxFret
                                
                                val clickedFret = if (offset.x < nutWidthPx) {
                                    0
                                } else {
                                    val calculated = ((offset.x - nutWidthPx) / fretWidthPx).toInt() + 1
                                    minOf(maxFret, maxOf(1, calculated))
                                }
                                
                                val stringSpacingPx = size.height / 6
                                val clickedString = 6 - (offset.y / stringSpacingPx).toInt()
                                val clampedString = minOf(6, maxOf(1, clickedString))
                                
                                onPositionSelected(clampedString, clickedFret)
                            }
                        }
                ) {
                    val nutWidth = 50.dp.toPx()
                    val usableWidth = size.width - nutWidth
                    val fretWidth = usableWidth / maxFret
                    val stringSpacing = size.height / 6
                    
                    // 1. Draw Rosewood Fretboard Neck
                    drawRect(
                        color = WoodFretboard,
                        topLeft = Offset(0f, 0f),
                        size = Size(size.width, size.height)
                    )
                    
                    // 2. Draw Nut (Bone)
                    drawRect(
                        color = GoldPrimary,
                        topLeft = Offset(nutWidth - 8f, 0f),
                        size = Size(8f, size.height)
                    )
                    
                    // 3. Draw Frets (Silver lines)
                    for (fret in 1..maxFret) {
                        val x = nutWidth + (fret * fretWidth)
                        drawLine(
                            color = StringSilver.copy(alpha = 0.8f),
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 3f
                        )
                    }
                    
                    // 4. Draw Position Markers (Dots on frets 3, 5, 7, 9, 12)
                    val dots = listOf(3, 5, 7, 9, 12)
                    for (fret in dots) {
                        if (fret <= maxFret) {
                            val fretCenter = nutWidth + ((fret - 1) * fretWidth) + (fretWidth / 2)
                            if (fret == 12) {
                                // Double dots stacked
                                drawCircle(
                                    color = GoldSecondary.copy(alpha = 0.6f),
                                    radius = 12f,
                                    center = Offset(fretCenter, stringSpacing * 1.5f)
                                )
                                drawCircle(
                                    color = GoldSecondary.copy(alpha = 0.6f),
                                    radius = 12f,
                                    center = Offset(fretCenter, stringSpacing * 4.5f)
                                )
                            } else {
                                // Single dot center
                                drawCircle(
                                    color = GoldSecondary.copy(alpha = 0.6f),
                                    radius = 12f,
                                    center = Offset(fretCenter, size.height / 2)
                                )
                            }
                        }
                    }
                    
                    // 5. Draw 6 Strings (Standard gauges: thick at bottom, thin at top)
                    for (s in 0 until 6) {
                        val y = (s * stringSpacing) + (stringSpacing / 2)
                        val thickness = 1f + (s * 1.2f) // increasing thickness for lower pitch strings
                        drawLine(
                            color = StringSilver,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = thickness
                        )
                    }
                    
                    // 6. Draw Highlighted Nodes (Gold, pulsed, or user selections)
                    highlightedPositions.forEach { pos ->
                        val str = pos.first
                        val fret = pos.second
                        if (fret <= maxFret) {
                            val x = if (fret == 0) nutWidth / 2 else nutWidth + ((fret - 1) * fretWidth) + (fretWidth / 2)
                            val y = ((6 - str) * stringSpacing) + (stringSpacing / 2)
                            
                            val glowAlpha = if (isBlinking) alpha else 1.0f
                            // Outer glow
                            drawCircle(
                                color = GoldPrimary.copy(alpha = glowAlpha),
                                radius = 22f,
                                center = Offset(x, y)
                            )
                            drawCircle(
                                color = BlackBackground,
                                radius = 18f,
                                center = Offset(x, y)
                            )
                        }
                    }

                    // Clicked feedback overlay
                    clickedPosition?.let { pos ->
                        val str = pos.first
                        val fret = pos.second
                        if (fret <= maxFret) {
                            val x = if (fret == 0) nutWidth / 2 else nutWidth + ((fret - 1) * fretWidth) + (fretWidth / 2)
                            val y = ((6 - str) * stringSpacing) + (stringSpacing / 2)
                            val color = if (isCorrectClick == true) GreenSuccess else RedError
                            drawCircle(
                                color = color,
                                radius = 22f,
                                center = Offset(x, y)
                            )
                            drawCircle(
                                color = BlackBackground,
                                radius = 18f,
                                center = Offset(x, y)
                            )
                        }
                    }
                }

                // Overlay Texts (Note Names or labels)
                val stringHeightDp = (180 / 6).dp
                
                // Render Text Labels precisely on top of the positions
                for (str in 1..6) {
                    for (fret in 0..maxFret) {
                        val xOffset = if (fret == 0) nutWidthDp / 2 - 10.dp else nutWidthDp + fretWidthDp * (fret - 1) + fretWidthDp / 2 - 10.dp
                        val yOffset = stringHeightDp * (6 - str) + stringHeightDp / 2 - 1.dp
                        val isHighlighted = highlightedPositions.contains(Pair(str, fret))
                        val isClicked = clickedPosition == Pair(str, fret)
                        
                        if (isHighlighted || isClicked || showNoteNames) {
                            val noteName = GuitarTheory.getNoteName(str, fret)
                            val label = labelOverride[Pair(str, fret)] ?: noteName
                            val textColor = if (isClicked) {
                                    if (isCorrectClick == true) GreenSuccess else RedError
                                } else {
                                    GoldPrimary
                                }
                            
                            val labelAlpha = if (isHighlighted && isBlinking) alpha else 1.0f
                            Text(
                                text = label,
                                color = textColor.copy(alpha = labelAlpha),
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .offset(x = xOffset, y = yOffset)
                                    .width(20.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Fret Numbers at the very bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nut / Fret 0
                Box(
                    modifier = Modifier.width(nutWidthDp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nut", color = GoldSecondary, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
                // Frets 1 to maxFret (capping at 12)
                for (f in 1..maxFret) {
                    Box(
                        modifier = Modifier.width(fretWidthDp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("$f", color = GoldSecondary, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

// --- Game Screens ---

@Composable
fun FindItScreen(viewModel: FretboardViewModel) {
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Selector: Learn vs Quiz
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(SlateSurface)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isLearn = viewModel.isLearnMode
            Button(
                onClick = { 
                    viewModel.isLearnMode = true 
                    viewModel.allStringsChallengeActive = false
                    viewModel.nextFindItQuestion() 
                },
                modifier = Modifier.weight(1f).padding(end = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLearn) GoldPrimary else Color.Transparent,
                    contentColor = if (isLearn) BlackBackground else GoldPrimary
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Text("Learn Mode", fontWeight = FontWeight.Black, fontSize = 13.sp)
            }
            Button(
                onClick = { 
                    viewModel.isLearnMode = false 
                    viewModel.allStringsChallengeActive = false
                    viewModel.nextFindItQuestion() 
                },
                modifier = Modifier.weight(1f).padding(start = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isLearn) GoldPrimary else Color.Transparent,
                    contentColor = if (!isLearn) BlackBackground else GoldPrimary
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Text("Quiz Mode", fontWeight = FontWeight.Black, fontSize = 13.sp)
            }
        }

        // Stats Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Round Score: ${viewModel.sessionScore}/${viewModel.sessionTotal}",
                color = GoldSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                "Question: ${viewModel.currentQuestionIndex}/${viewModel.totalQuestions}",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
        
        // Target note Display Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "TARGET NOTE",
                    fontSize = 12.sp,
                    letterSpacing = 1.5.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                // High/Low Relative Directional red display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LEFT red guess if clicked/played is LOWER
                    if (viewModel.wrongGuessNote != null && viewModel.wrongGuessDirection == "lower") {
                        Text(
                            text = viewModel.wrongGuessNote!!,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold,
                            color = RedError,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Lower",
                            tint = RedError,
                            modifier = Modifier.size(32.dp).padding(end = 8.dp)
                        )
                    }

                    // Main display
                    Text(
                        text = viewModel.findItTargetNote,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldPrimary,
                        fontFamily = FontFamily.SansSerif
                    )

                    // RIGHT red guess if clicked/played is HIGHER
                    if (viewModel.wrongGuessNote != null && viewModel.wrongGuessDirection == "higher") {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Higher",
                            tint = RedError,
                            modifier = Modifier.size(32.dp).padding(start = 8.dp)
                        )
                        Text(
                            text = viewModel.wrongGuessNote!!,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold,
                            color = RedError,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }

                // Segmented Progress Bar
                SegmentedProgressBar(
                    filledCount = viewModel.foundPositionsForCurrentNote.size,
                    totalCount = viewModel.getTargetBarCount(viewModel.findItTargetNote),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
                
                // Target string display for Learn Mode
                if (viewModel.isLearnMode && !viewModel.allStringsChallengeActive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GoldTonal)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Find on String ${viewModel.findItTargetString}",
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = viewModel.feedbackMessage,
                    fontSize = 14.sp,
                    color = if (viewModel.isAnswered || viewModel.wrongGuessNote != null) {
                        if (viewModel.wasCorrect) GreenSuccess else RedError
                    } else {
                        TextPrimary
                    },
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Fretboard setup
        val correctPositions = remember(viewModel.findItTargetNote, viewModel.maxFretSelected) {
            GuitarTheory.findNotePositions(viewModel.findItTargetNote, viewModel.maxFretSelected)
        }

        // In Learn Mode, we blink the note location on the target string!
        val learnModeTargetPosition = remember(viewModel.findItTargetNote, viewModel.findItTargetString) {
            GuitarTheory.findNotePositions(viewModel.findItTargetNote, 12)
                .filter { it.first == viewModel.findItTargetString }
        }
        
        Text(
            text = "TAP ON THE FRETBOARD TO GUESS",
            fontSize = 10.sp,
            color = TextSecondary,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.Bold
        )

        FretboardView(
            maxFret = viewModel.maxFretSelected,
            highlightedPositions = if (viewModel.allStringsChallengeActive) {
                // If in all-strings challenge, highlight all correct positions they have found so far
                correctPositions.filter { viewModel.allStringsChallengeFound.contains(it.first) }
            } else if (viewModel.isLearnMode && !viewModel.isAnswered) {
                learnModeTargetPosition
            } else if (viewModel.findItRevealCorrectPositions) {
                correctPositions
            } else {
                emptyList()
            },
            clickedPosition = viewModel.findItSelectedPosition,
            isCorrectClick = if (viewModel.isAnswered) viewModel.wasCorrect else null,
            onPositionSelected = { str, fret ->
                viewModel.handleFindItPositionSelected(str, fret)
            },
            isBlinking = viewModel.isLearnMode && !viewModel.isAnswered
        )
        
        // Find Note on All Strings Challenge button
        Button(
            onClick = {
                viewModel.allStringsChallengeActive = !viewModel.allStringsChallengeActive
                viewModel.nextFindItQuestion()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (viewModel.allStringsChallengeActive) GoldSecondary else SlateSurfaceVariant,
                contentColor = if (viewModel.allStringsChallengeActive) BlackBackground else GoldPrimary
            ),
            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                imageVector = if (viewModel.allStringsChallengeActive) Icons.Default.CheckCircle else Icons.Default.Star,
                contentDescription = "Challenge"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (viewModel.allStringsChallengeActive) {
                    "All Strings Found: ${viewModel.allStringsChallengeFound.size}/6"
                } else {
                    "Find Note on All Strings"
                },
                fontWeight = FontWeight.Bold
            )
        }

        // Navigation Control Button
        if (viewModel.isAnswered || (viewModel.allStringsChallengeActive && viewModel.allStringsChallengeFound.size >= 6)) {
            Button(
                onClick = { 
                    if (viewModel.allStringsChallengeActive) {
                        viewModel.nextFindItQuestion()
                    } else {
                        viewModel.advanceFindIt() 
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_find_it_next"),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BlackBackground)
            ) {
                Text(
                    text = if (viewModel.currentQuestionIndex == viewModel.totalQuestions) "Finish Quiz" else "Next Note",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Icon(Icons.Default.ArrowForward, contentDescription = "Next")
            }
        }
    }
}

@Composable
fun NameItScreen(viewModel: FretboardViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Round Score: ${viewModel.sessionScore}/${viewModel.sessionTotal}",
                color = GoldSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                "Question: ${viewModel.currentQuestionIndex}/${viewModel.totalQuestions}",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "NAME HIGHLIGHTED FRET",
                    fontSize = 11.sp,
                    letterSpacing = 1.5.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "String ${viewModel.nameItHighlightedPosition.first}, Fret ${viewModel.nameItHighlightedPosition.second}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GoldPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    viewModel.feedbackMessage,
                    fontSize = 13.sp,
                    color = if (viewModel.isAnswered) {
                        if (viewModel.wasCorrect) GreenSuccess else RedError
                    } else {
                        TextPrimary
                    },
                    textAlign = TextAlign.Center
                )
            }
        }

        // Fretboard displaying a glowing question mark at the target coordinate
        FretboardView(
            maxFret = viewModel.maxFretSelected,
            highlightedPositions = listOf(viewModel.nameItHighlightedPosition),
            labelOverride = mapOf(viewModel.nameItHighlightedPosition to "?"),
            onPositionSelected = null // Clicking fretboard does nothing in name-it mode
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Multiple choice grid
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val chunks = viewModel.nameItChoices.chunked(2)
            chunks.forEach { rowChoices ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowChoices.forEach { choice ->
                        val isSelected = viewModel.nameItSelectedAnswer == choice
                        val isCorrect = choice == GuitarTheory.getNoteName(viewModel.nameItHighlightedPosition.first, viewModel.nameItHighlightedPosition.second)
                        
                        val btnColor = when {
                            !viewModel.isAnswered -> SlateSurface
                            isSelected && isCorrect -> GreenSuccess.copy(alpha = 0.3f)
                            isSelected && !isCorrect -> RedError.copy(alpha = 0.3f)
                            isCorrect -> GreenSuccess.copy(alpha = 0.3f) // highlight correct answer
                            else -> SlateSurface
                        }
                        
                        val borderCol = when {
                            isSelected -> GoldPrimary
                            isCorrect && viewModel.isAnswered -> GreenSuccess
                            else -> GoldTertiary.copy(alpha = 0.3f)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(btnColor)
                                .border(1.2.dp, borderCol, RoundedCornerShape(8.dp))
                                .clickable(enabled = !viewModel.isAnswered) {
                                    viewModel.handleNameItAnswerSelected(choice)
                                }
                                .testTag("choice_$choice"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = choice,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = if (isSelected || (isCorrect && viewModel.isAnswered)) GoldPrimary else TextPrimary
                            )
                        }
                    }
                }
            }
        }

        if (viewModel.isAnswered) {
            Button(
                onClick = { viewModel.advanceNameIt() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(top = 8.dp)
                    .testTag("btn_name_it_next"),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BlackBackground)
            ) {
                Text(
                    text = if (viewModel.currentQuestionIndex == viewModel.totalQuestions) "Finish Quiz" else "Next Question",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Icon(Icons.Default.ArrowForward, contentDescription = "Next")
            }
        }
    }
}

@Composable
fun GuitarInputScreen(viewModel: FretboardViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var hasRecordPermission by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.RECORD_AUDIO
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasRecordPermission = granted
        if (granted) {
            viewModel.startGuitarRound()
        }
    }

    LaunchedEffect(hasRecordPermission) {
        if (!hasRecordPermission) {
            launcher.launch(android.Manifest.permission.RECORD_AUDIO)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!hasRecordPermission) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Warning, contentDescription = "Mic Off", tint = RedError, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Microphone Permission Required",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "The app needs microphone access to listen to your physical guitar pitches.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { launcher.launch(android.Manifest.permission.RECORD_AUDIO) },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BlackBackground)
                    ) {
                        Text("Grant Permission", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Round Score: ${viewModel.sessionScore}/${viewModel.sessionTotal}",
                    color = GoldSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    "Question: ${viewModel.currentQuestionIndex}/${viewModel.totalQuestions}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            // Target note display
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "PLAY THIS NOTE ON YOUR GUITAR",
                        fontSize = 10.sp,
                        letterSpacing = 1.5.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        viewModel.guitarTargetNote,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Segmented Progress Bar
                    SegmentedProgressBar(
                        filledCount = viewModel.foundPositionsForCurrentNote.size,
                        totalCount = viewModel.getTargetBarCount(viewModel.guitarTargetNote),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        viewModel.feedbackMessage,
                        fontSize = 13.sp,
                        color = if (viewModel.isAnswered) GreenSuccess else TextPrimary,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Real-time Pitch Tuner Meter Visualizer
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(SlateSurfaceVariant)
                    .border(2.dp, GoldTertiary, CircleShape)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Pitch feedback dial
                val pitchAngle by animateFloatAsState(
                    targetValue = if (viewModel.liveFrequency > 0) {
                        val targetFreq = GuitarTheory.midiToFrequency(GuitarTheory.frequencyToMidi(viewModel.liveFrequency))
                        val delta = (viewModel.liveFrequency - targetFreq) / targetFreq
                        ((delta * 90.0).coerceIn(-45.0, 45.0)).toFloat()
                    } else 0f,
                    label = "DialAngle"
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw dial lines
                    drawArc(
                        color = GoldSecondary.copy(alpha = 0.2f),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        style = Stroke(width = 8f, cap = StrokeCap.Round)
                    )
                    
                    // Draw center notch (Perfect pitch marker)
                    drawLine(
                        color = GoldPrimary,
                        start = Offset(size.width / 2, 0f),
                        end = Offset(size.width / 2, 24f),
                        strokeWidth = 6f
                    )
                    
                    // Draw physical dial needle based on pitch variance
                    val needleRad = Math.toRadians((pitchAngle - 90).toDouble())
                    val needleLen = size.width / 2 - 12f
                    val needleEnd = Offset(
                        (size.width / 2 + needleLen * Math.cos(needleRad)).toFloat(),
                        (size.height / 2 + needleLen * Math.sin(needleRad)).toFloat()
                    )
                    drawLine(
                        color = if (viewModel.liveNoteName == viewModel.guitarTargetNote) GreenSuccess else GoldPrimary,
                        start = Offset(size.width / 2, size.height / 2),
                        end = needleEnd,
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )
                }

                // Digital text in center
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = viewModel.liveNoteName ?: "---",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = if (viewModel.liveNoteName == viewModel.guitarTargetNote) GreenSuccess else GoldPrimary
                    )
                    Text(
                        text = if (viewModel.liveFrequency > 0) String.format("%.1f Hz", viewModel.liveFrequency) else "Listening...",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            // Real-time Mic Input indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = "RMS Level", tint = GoldSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Volume: ${viewModel.liveRms.toInt()}", fontSize = 11.sp, color = TextSecondary)
                }
                Text("Keep note stable!", fontSize = 11.sp, color = TextSecondary)
            }

            // Live detected fret indicator: highlight where the user is playing on their guitar!
            val livePositions = remember(viewModel.liveNoteName) {
                viewModel.liveNoteName?.let {
                    GuitarTheory.findNotePositions(it, viewModel.maxFretSelected)
                } ?: emptyList()
            }
            
            Text(
                "LIVE GUITAR FRETBOARD POSITIONS DETECTED",
                fontSize = 10.sp,
                color = TextSecondary,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )

            FretboardView(
                maxFret = viewModel.maxFretSelected,
                highlightedPositions = livePositions,
                showNoteNames = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!viewModel.isAnswered) {
                    OutlinedButton(
                        onClick = { viewModel.skipGuitarQuestion() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_guitar_skip"),
                        border = BorderStroke(1.dp, GoldPrimary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary)
                    ) {
                        Text("Skip Note", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { viewModel.advanceGuitar() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_guitar_next"),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BlackBackground)
                    ) {
                        Text(
                            text = if (viewModel.currentQuestionIndex == viewModel.totalQuestions) "Finish Round" else "Next Note",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlayAlongScreen(viewModel: FretboardViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var hasRecordPermission by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.RECORD_AUDIO
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasRecordPermission = granted
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopPlayAlong()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!hasRecordPermission) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Warning, contentDescription = "Mic Off", tint = RedError, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Microphone Permission Required",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { launcher.launch(android.Manifest.permission.RECORD_AUDIO) },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BlackBackground)
                    ) {
                        Text("Grant Permission", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Header Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Play-along Score: ${viewModel.playAlongScore} / ${viewModel.playAlongSequence.size}",
                    color = GoldSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    "Tempo: ${viewModel.tempoBpm} BPM",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            // Metronome Settings configuration
            if (!viewModel.isPlayingAlong) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    border = BorderStroke(0.5.dp, GoldTertiary.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "METRONOME CONFIG",
                            fontSize = 11.sp,
                            letterSpacing = 1.5.sp,
                            color = GoldSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("BPM Speed", color = TextPrimary, fontSize = 14.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { viewModel.tempoBpm = maxOf(40, viewModel.tempoBpm - 5) }) {
                                    Text("-", color = GoldPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    "${viewModel.tempoBpm}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = GoldPrimary,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                                IconButton(onClick = { viewModel.tempoBpm = minOf(160, viewModel.tempoBpm + 5) }) {
                                    Icon(Icons.Default.Add, contentDescription = "Speed Up", tint = GoldPrimary)
                                }
                            }
                        }
                    }
                }
            }

            // Rolling note sequence visualization
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        viewModel.playAlongFeedback,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (viewModel.currentPlayedMatch) GreenSuccess else GoldPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Scrolling note tracks
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        viewModel.playAlongSequence.forEachIndexed { idx, note ->
                            val isActive = viewModel.isPlayingAlong && viewModel.playAlongIndex == idx
                            val cardColor = when {
                                isActive -> GoldPrimary
                                idx < viewModel.playAlongIndex -> GreenSuccess.copy(alpha = 0.3f)
                                else -> SlateSurfaceVariant
                            }
                            val borderCol = if (isActive) GoldSecondary else GoldTertiary.copy(alpha = 0.2f)

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(cardColor)
                                    .border(1.dp, borderCol, RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = note,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isActive) BlackBackground else TextPrimary
                                )
                            }
                        }
                    }
                    
                    // Progress bar inside current beat
                    if (viewModel.isPlayingAlong) {
                        Spacer(modifier = Modifier.height(14.dp))
                        LinearProgressIndicator(
                            progress = { viewModel.playAlongBeatProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = GoldPrimary,
                            trackColor = SlateSurfaceVariant,
                        )
                    }
                }
            }

            // Real-time Mic pitch feedback during metronome
            if (viewModel.isPlayingAlong) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Mic Feedback", tint = GoldSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (viewModel.liveNoteName != null) "Detected Played Note: ${viewModel.liveNoteName}" else "Listening...",
                        fontSize = 12.sp,
                        color = if (viewModel.liveNoteName == viewModel.playAlongSequence.getOrNull(viewModel.playAlongIndex)) GreenSuccess else GoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Big metronome visual flasher (pulser)
            val pulseScale by animateFloatAsState(
                targetValue = if (viewModel.isPlayingAlong && viewModel.playAlongBeatProgress < 0.2f) 1.2f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "PulseScale"
            )

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(if (viewModel.isPlayingAlong && viewModel.playAlongBeatProgress < 0.2f) GoldPrimary else SlateSurfaceVariant)
                    .border(1.dp, GoldPrimary, CircleShape)
                    .padding(12.dp)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Metronome Pulse",
                    tint = if (viewModel.isPlayingAlong && viewModel.playAlongBeatProgress < 0.2f) BlackBackground else GoldPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Play button controls
            if (!viewModel.isPlayingAlong) {
                Button(
                    onClick = { viewModel.startPlayAlong() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_start_play_along"),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BlackBackground)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Start Play-along")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("START SESSION", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            } else {
                OutlinedButton(
                    onClick = { viewModel.stopPlayAlong() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_stop_play_along"),
                    border = BorderStroke(1.dp, RedError),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RedError)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Stop Play-along")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("STOP SESSION", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun StatsScreen(viewModel: FretboardViewModel) {
    val scores by viewModel.allScores.collectAsState()
    val guesses by viewModel.allGuesses.collectAsState()

    val totalSessions = scores.size
    val averageScore = remember(scores) {
        if (scores.isEmpty()) 0
        else (scores.map { it.percentage }.average()).toInt()
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Text(
                "PERFORMANCE ANALYTICS",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = GoldPrimary,
                letterSpacing = 1.sp
            )
        }

        // Summary Metric Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    border = BorderStroke(0.5.dp, GoldTertiary.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("TOTAL SESSIONS", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$totalSessions", fontSize = 28.sp, fontWeight = FontWeight.Black, color = GoldPrimary)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    border = BorderStroke(0.5.dp, GoldTertiary.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("AVERAGE ACCURACY", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$averageScore%", fontSize = 28.sp, fontWeight = FontWeight.Black, color = if (averageScore >= 80) GreenSuccess else GoldPrimary)
                    }
                }
            }
        }

        // String Accuracy Section
        item {
            Text(
                "STRING ACCURACY (PRACTICE GUESSES)",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = GoldSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                border = BorderStroke(0.5.dp, GoldTertiary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val stringsList = listOf(
                        1 to "High E (String 1)",
                        2 to "B (String 2)",
                        3 to "G (String 3)",
                        4 to "D (String 4)",
                        5 to "A (String 5)",
                        6 to "Low E (String 6)"
                    )
                    for ((sIndex, sName) in stringsList) {
                        val sGuesses = guesses.filter { it.stringIndex == sIndex }
                        val accuracy = if (sGuesses.isNotEmpty()) {
                            (sGuesses.count { it.isCorrect }.toFloat() / sGuesses.size * 100f).toInt()
                        } else {
                            -1 // No data
                        }
                        
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(sName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = if (accuracy >= 0) "$accuracy%" else "No Data",
                                    color = if (accuracy >= 80) GreenSuccess else if (accuracy >= 0) GoldPrimary else TextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { if (accuracy >= 0) accuracy / 100f else 0f },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = if (accuracy >= 80) GreenSuccess else GoldPrimary,
                                trackColor = SlateSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Note Accuracy Section
        item {
            Text(
                "NOTE ACCURACY (PRACTICE GUESSES)",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = GoldSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                border = BorderStroke(0.5.dp, GoldTertiary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val notes = GuitarTheory.chromaticScale
                    
                    notes.chunked(3).forEach { rowNotes ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (note in rowNotes) {
                                val nGuesses = guesses.filter { it.note.uppercase() == note.uppercase() }
                                val accuracy = if (nGuesses.isNotEmpty()) {
                                    (nGuesses.count { it.isCorrect }.toFloat() / nGuesses.size * 100f).toInt()
                                } else {
                                    -1 // No data
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SlateSurfaceVariant)
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(note, color = GoldPrimary, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (accuracy >= 0) "$accuracy%" else "-",
                                            color = if (accuracy >= 80) GreenSuccess else if (accuracy >= 0) TextPrimary else TextSecondary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                "PRACTICE HISTORY LOGS",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = GoldSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (scores.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No sessions recorded yet.", color = TextSecondary, fontSize = 13.sp)
                }
            }
        } else {
            items(scores) { score ->
                ScoreListItem(score)
            }
            
            item {
                OutlinedButton(
                    onClick = { viewModel.clearStats() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .testTag("btn_clear_stats"),
                    border = BorderStroke(1.dp, RedError),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RedError)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear Performance Logs")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CLEAR PERFORMANCE LOGS", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(viewModel: FretboardViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            "TRAINER CONFIGURATION",
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            color = GoldPrimary,
            letterSpacing = 1.sp
        )

        // accidentals config
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            border = BorderStroke(0.5.dp, GoldTertiary.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "PITCH RANGE AND SHARPS/FLATS",
                    fontSize = 11.sp,
                    color = GoldSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Include Accidentals", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Include Sharps (#) and Flats (b) in target queries.", color = TextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = viewModel.includeAccidentals,
                        onCheckedChange = { viewModel.includeAccidentals = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = BlackBackground,
                            checkedTrackColor = GoldPrimary,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = SlateSurfaceVariant
                        )
                    )
                }
            }
        }

        // Fret limit config (Locked to 12)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            border = BorderStroke(0.5.dp, GoldTertiary.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "FRETBOARD SCALING RANGE",
                    fontSize = 11.sp,
                    color = GoldSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Max Trainer Fret", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Fixed to 12-fret octave for full scale support.", color = TextSecondary, fontSize = 11.sp)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GoldTonal)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("12 Frets", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // About the App card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            border = BorderStroke(0.5.dp, GoldTertiary.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "DEVELOPMENT SYSTEM INFO",
                    fontSize = 11.sp,
                    color = GoldSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "This application is compiled on Jetpack Compose utilizing local SQLite Room databases for history logging and a multi-threaded PCM microphone listener for high-fidelity physical guitar pitch analysis.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
