package com.example.kaspribadi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaspribadi.data.*
import com.example.kaspribadi.ui.theme.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getDatabase(this)
        val dao = db.appDao()

        setContent {
            var themeOption by remember { mutableStateOf("SYSTEM") }
            var selectedLanguage by remember { mutableStateOf("ID") }
            var currencySymbol by remember { mutableStateOf("Rp") }
            var fontSizeScale by remember { mutableStateOf("MEDIUM") }

            KasPribadiTheme(themeOption = themeOption) {
                MainAppNavHost(
                    dao = dao,
                    themeOption = themeOption,
                    onThemeChange = { themeOption = it },
                    selectedLanguage = selectedLanguage,
                    onLanguageChange = { lang ->
                        selectedLanguage = lang
                        currencySymbol = when (lang) {
                            "EN" -> "$"
                            "JP" -> "¥"
                            "FR", "ES" -> "€"
                            else -> "Rp"
                        }
                    },
                    currencySymbol = currencySymbol,
                    onCurrencyChange = { currencySymbol = it },
                    fontSizeScale = fontSizeScale,
                    onFontSizeChange = { fontSizeScale = it }
                )
            }
        }
    }
}

// MULTI-LANGUAGE TRANSLATION DICTIONARY
fun getString(key: String, lang: String): String {
    val translations = mapOf(
        "home" to mapOf("ID" to "Beranda", "EN" to "Home", "JP" to "ホーム", "FR" to "Accueil", "ES" to "Inicio"),
        "history" to mapOf("ID" to "Riwayat", "EN" to "History", "JP" to "履歴", "FR" to "Historique", "ES" to "Historial"),
        "settings" to mapOf("ID" to "Pengaturan", "EN" to "Settings", "JP" to "設定", "FR" to "Paramètres", "ES" to "Ajustes"),
        "remaining_cash" to mapOf("ID" to "Sisa Kas Utama", "EN" to "Main Cash Balance", "JP" to "残高", "FR" to "Solde Principal", "ES" to "Saldo Principal"),
        "income" to mapOf("ID" to "Pemasukan", "EN" to "Income", "JP" to "収入", "FR" to "Revenus", "ES" to "Ingresos"),
        "expense" to mapOf("ID" to "Pengeluaran", "EN" to "Expense", "JP" to "支出", "FR" to "Dépenses", "ES" to "Gastos"),
        "savings" to mapOf("ID" to "Tabungan", "EN" to "Savings", "JP" to "貯金", "FR" to "Épargne", "ES" to "Ahorros"),
        "total_savings" to mapOf("ID" to "Total Terkumpul di Tabungan", "EN" to "Total Saved Balance", "JP" to "貯金合計", "FR" to "Total Épargné", "ES" to "Total Ahorrado"),
        "create_target" to mapOf("ID" to "Buat Target", "EN" to "Create Goal", "JP" to "目標作成", "FR" to "Créer Objectif", "ES" to "Crear Meta"),
        "savings_goals" to mapOf("ID" to "Target Tabungan Kamu", "EN" to "Your Savings Goals", "JP" to "貯金目標", "FR" to "Vos Objectifs", "ES" to "Tus Metas"),
        "no_goals" to mapOf("ID" to "Belum ada target tabungan.", "EN" to "No savings goals yet.", "JP" to "目標がありません。", "FR" to "Aucun objectif.", "ES" to "Sin metas aún."),
        "theme" to mapOf("ID" to "Tampilan Tema", "EN" to "Theme Display", "JP" to "テーマ表示", "FR" to "Affichage du Thème", "ES" to "Tema de Pantalla"),
        "language" to mapOf("ID" to "Bahasa / Language", "EN" to "Language", "JP" to "言語", "FR" to "Langue", "ES" to "Idioma"),
        "currency" to mapOf("ID" to "Simbol Mata Uang", "EN" to "Currency Symbol", "JP" to "通貨記号", "FR" to "Symbole Monétaire", "ES" to "Símbolo de Moneda"),
        "font_size" to mapOf("ID" to "Ukuran Teks / Font", "EN" to "Font Size", "JP" to "文字サイズ", "FR" to "Taille de Police", "ES" to "Tamaño de Fuente"),
        "all" to mapOf("ID" to "Semua", "EN" to "All", "JP" to "すべて", "FR" to "Tout", "ES" to "Todo"),
        "add_custom_cat" to mapOf("ID" to "+ Buat Kategori Baru", "EN" to "+ Create New Category", "JP" to "+ 新規カテゴリ作成", "FR" to "+ Nouvelle Catégorie", "ES" to "+ Nueva Categoría")
    )
    return translations[key]?.get(lang) ?: translations[key]?.get("ID") ?: key
}

@Composable
fun getFontSize(baseSp: Float, scale: String): TextUnit {
    val multiplier = when (scale) {
        "SMALL" -> 0.85f
        "LARGE" -> 1.15f
        else -> 1.0f
    }
    return (baseSp * multiplier).sp
}

sealed class Screen(val titleKey: String, val icon: @Composable () -> Unit) {
    object Home : Screen("home", { Icon(Icons.Default.Home, contentDescription = null) })
    @Suppress("DEPRECATION")
    object History : Screen("history", { Icon(Icons.Default.List, contentDescription = null) })
    object Settings : Screen("settings", { Icon(Icons.Default.Settings, contentDescription = null) })
}

@Composable
fun MainAppNavHost(
    dao: AppDao,
    themeOption: String,
    onThemeChange: (String) -> Unit,
    selectedLanguage: String,
    onLanguageChange: (String) -> Unit,
    currencySymbol: String,
    onCurrencyChange: (String) -> Unit,
    fontSizeScale: String,
    onFontSizeChange: (String) -> Unit
) {
    var currentTab by remember { mutableStateOf<Screen>(Screen.Home) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                val items = listOf(Screen.Home, Screen.History, Screen.Settings)
                items.forEach { screen ->
                    val selected = currentTab == screen
                    NavigationBarItem(
                        icon = screen.icon,
                        label = { Text(getString(screen.titleKey, selectedLanguage), fontSize = getFontSize(12f, fontSizeScale), fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                        selected = selected,
                        onClick = { currentTab = screen },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.outlineVariant,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentTab) {
                is Screen.Home -> HomeScreenGoPay(dao, currencySymbol, fontSizeScale, selectedLanguage)
                is Screen.History -> HistoryScreenWithChart(dao, currencySymbol, fontSizeScale, selectedLanguage)
                is Screen.Settings -> SettingsScreen(
                    themeOption = themeOption,
                    onThemeChange = onThemeChange,
                    selectedLanguage = selectedLanguage,
                    onLanguageChange = onLanguageChange,
                    currencySymbol = currencySymbol,
                    onCurrencyChange = onCurrencyChange,
                    fontSizeScale = fontSizeScale,
                    onFontSizeChange = onFontSizeChange
                )
            }
        }
    }
}

// ==========================================
// 1. BERANDA (TANPA AKTIVITAS TERAKHIR)
// ==========================================
@Composable
fun HomeScreenGoPay(dao: AppDao, currencySymbol: String, fontSizeScale: String, lang: String) {
    val scope = rememberCoroutineScope()
    val transactions by dao.getAllTransactions().collectAsState(initial = emptyList())
    val goals by dao.getAllGoals().collectAsState(initial = emptyList())
    val categories by dao.getAllCategories().collectAsState(initial = emptyList())

    // INISIALISASI HANYA JIKA DATABASE BENAR-BENAR KOSONG
    LaunchedEffect(categories) {
        if (categories.isEmpty()) {
            dao.insertCategory(CategoryEntity(nama = "Gaji", jenisInOut = "IN"))
            dao.insertCategory(CategoryEntity(nama = "Makan/Minum", jenisInOut = "OUT"))
            dao.insertCategory(CategoryEntity(nama = "Transportasi", jenisInOut = "OUT"))
            dao.insertCategory(CategoryEntity(nama = "Sosial", jenisInOut = "OUT"))
            dao.insertCategory(CategoryEntity(nama = "Belanja", jenisInOut = "OUT"))
        }
    }

    var showAddTxDialog by remember { mutableStateOf(false) }
    var defaultTxType by remember { mutableStateOf("OUT") }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var goalToEdit by remember { mutableStateOf<SavingsGoalEntity?>(null) }
    var goalToDeposit by remember { mutableStateOf<SavingsGoalEntity?>(null) }

    var totalIn = 0.0
    var totalOut = 0.0
    transactions.forEach { tx ->
        val cat = categories.find { it.id == tx.categoryId }
        if (cat?.jenisInOut == "IN") totalIn += tx.nominal else totalOut += tx.nominal
    }
    val totalKas = totalIn - totalOut
    val totalTabungan = goals.sumOf { it.terkumpul }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(getString("remaining_cash", lang), color = Color.White.copy(alpha = 0.9f), fontSize = getFontSize(14f, fontSizeScale))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(formatCustomCurrency(totalKas, currencySymbol), color = Color.White, fontSize = getFontSize(30f, fontSizeScale), fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        GoPayActionButton(icon = Icons.Default.ArrowUpward, label = getString("income", lang), fontSizeScale) {
                            defaultTxType = "IN"
                            showAddTxDialog = true
                        }
                        GoPayActionButton(icon = Icons.Default.ArrowDownward, label = getString("expense", lang), fontSizeScale) {
                            defaultTxType = "OUT"
                            showAddTxDialog = true
                        }
                        GoPayActionButton(icon = Icons.Default.Savings, label = getString("savings", lang), fontSizeScale) {
                            if (goals.isNotEmpty()) goalToDeposit = goals.first() else showAddGoalDialog = true
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(getString("total_savings", lang), fontSize = getFontSize(12f, fontSizeScale), color = Color.Gray)
                        Text(formatCustomCurrency(totalTabungan, currencySymbol), fontSize = getFontSize(18f, fontSizeScale), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Button(
                        onClick = { showAddGoalDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outlineVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(getString("create_target", lang), color = MaterialTheme.colorScheme.primary, fontSize = getFontSize(12f, fontSizeScale), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text(getString("savings_goals", lang), fontSize = getFontSize(16f, fontSizeScale), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }

        if (goals.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(getString("no_goals", lang), color = Color.Gray, fontSize = getFontSize(13f, fontSizeScale))
                    }
                }
            }
        } else {
            items(goals) { goal ->
                GoalItemGoPay(
                    goal = goal,
                    currencySymbol = currencySymbol,
                    fontSizeScale = fontSizeScale,
                    onAddDepositClick = { goalToDeposit = goal },
                    onEditGoalClick = { goalToEdit = goal },
                    onDeleteGoalClick = { scope.launch { dao.deleteGoal(goal) } }
                )
            }
        }
    }

    if (showAddTxDialog) {
        AddTransactionDialogGoPay(
            categories = categories,
            initialType = defaultTxType,
            currencySymbol = currencySymbol,
            lang = lang,
            onDismiss = { showAddTxDialog = false },
            onAddCategory = { newCatName, type ->
                scope.launch { dao.insertCategory(CategoryEntity(nama = newCatName, jenisInOut = type)) }
            },
            onSave = { catId, nominal, note ->
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                scope.launch { dao.insertTransaction(TransactionEntity(categoryId = catId, nominal = nominal, tanggal = currentDate, catatan = note)) }
                showAddTxDialog = false
            }
        )
    }

    if (showAddGoalDialog) {
        GoalFormDialog(title = getString("create_target", lang), initialName = "", initialTarget = 0.0, currencySymbol = currencySymbol, onDismiss = { showAddGoalDialog = false }) { name, target ->
            scope.launch { dao.insertGoal(SavingsGoalEntity(targetNama = name, targetNominal = target)) }
            showAddGoalDialog = false
        }
    }

    goalToEdit?.let { targetGoal ->
        GoalFormDialog(title = "Edit Target", initialName = targetGoal.targetNama, initialTarget = targetGoal.targetNominal, currencySymbol = currencySymbol, onDismiss = { goalToEdit = null }) { name, target ->
            scope.launch { dao.updateGoal(targetGoal.copy(targetNama = name, targetNominal = target)) }
            goalToEdit = null
        }
    }

    goalToDeposit?.let { targetGoal ->
        DepositDialog(goalName = targetGoal.targetNama, currencySymbol = currencySymbol, onDismiss = { goalToDeposit = null }) { amount ->
            scope.launch { dao.updateGoal(targetGoal.copy(terkumpul = targetGoal.terkumpul + amount)) }
            goalToDeposit = null
        }
    }
}

// ==========================================
// 2. PENGATURAN (SEMUA MODEL DROPDOWN)
// ==========================================
@Composable
fun SettingsScreen(
    themeOption: String,
    onThemeChange: (String) -> Unit,
    selectedLanguage: String,
    onLanguageChange: (String) -> Unit,
    currencySymbol: String,
    onCurrencyChange: (String) -> Unit,
    fontSizeScale: String,
    onFontSizeChange: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(getString("settings", selectedLanguage), fontSize = getFontSize(22f, fontSizeScale), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }

        // 1. TEMA DROPDOWN
        item {
            val themeMap = mapOf(
                "SYSTEM" to "Default System",
                "LIGHT" to "Light",
                "DARK" to "Dark"
            )
            SettingsDropdownCard(
                title = getString("theme", selectedLanguage),
                icon = Icons.Default.Palette,
                fontSizeScale = fontSizeScale,
                options = themeMap.values.toList(),
                selectedOption = themeMap[themeOption] ?: "Default System",
                onOptionSelected = { label ->
                    val key = themeMap.entries.firstOrNull { it.value == label }?.key ?: "SYSTEM"
                    onThemeChange(key)
                }
            )
        }

        // 2. BAHASA DROPDOWN
        item {
            val langMap = mapOf(
                "ID" to "Bahasa Indonesia",
                "EN" to "English",
                "JP" to "日本語 (Jepang)",
                "FR" to "Français (Prancis)",
                "ES" to "Español (Spanyol)"
            )
            SettingsDropdownCard(
                title = getString("language", selectedLanguage),
                icon = Icons.Default.Language,
                fontSizeScale = fontSizeScale,
                options = langMap.values.toList(),
                selectedOption = langMap[selectedLanguage] ?: "Bahasa Indonesia",
                onOptionSelected = { label ->
                    val key = langMap.entries.firstOrNull { it.value == label }?.key ?: "ID"
                    onLanguageChange(key)
                }
            )
        }

        // 3. MATA UANG DROPDOWN
        item {
            val currencyMap = listOf("Rupiah (Rp)", "Dollar ($)", "Yen (¥)", "Euro (€)", "Pound (£)")
            val currentSymbolLabel = when (currencySymbol) {
                "$" -> "Dollar ($)"
                "¥" -> "Yen (¥)"
                "€" -> "Euro (€)"
                "£" -> "Pound (£)"
                else -> "Rupiah (Rp)"
            }

            SettingsDropdownCard(
                title = getString("currency", selectedLanguage),
                icon = Icons.Default.AttachMoney,
                fontSizeScale = fontSizeScale,
                options = currencyMap,
                selectedOption = currentSymbolLabel,
                onOptionSelected = { label ->
                    val sym = when {
                        label.contains("$") -> "$"
                        label.contains("¥") -> "¥"
                        label.contains("€") -> "€"
                        label.contains("£") -> "£"
                        label.contains("₩") -> "₩"
                        else -> "Rp"
                    }
                    onCurrencyChange(sym)
                }
            )
        }

        // 4. UKURAN FONT DROPDOWN
        item {
            val fontMap = mapOf(
                "SMALL" to "Small",
                "MEDIUM" to "Medium",
                "LARGE" to "Large"
            )
            SettingsDropdownCard(
                title = getString("font_size", selectedLanguage),
                icon = Icons.Default.TextFields,
                fontSizeScale = fontSizeScale,
                options = fontMap.values.toList(),
                selectedOption = fontMap[fontSizeScale] ?: "Medium",
                onOptionSelected = { label ->
                    val key = fontMap.entries.firstOrNull { it.value == label }?.key ?: "MEDIUM"
                    onFontSizeChange(key)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDropdownCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    fontSizeScale: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = getFontSize(14f, fontSizeScale), color = MaterialTheme.colorScheme.onSurface)
            }

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                @Suppress("DEPRECATION")
                OutlinedTextField(
                    value = selectedOption,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, fontSize = getFontSize(14f, fontSizeScale)) },
                            onClick = {
                                onOptionSelected(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. RIWAYAT & CHART (SELURUH AKTIVITAS)
// ==========================================
@Composable
fun HistoryScreenWithChart(dao: AppDao, currencySymbol: String, fontSizeScale: String, lang: String) {
    val transactions by dao.getAllTransactions().collectAsState(initial = emptyList())
    val categories by dao.getAllCategories().collectAsState(initial = emptyList())

    var selectedFilter by remember { mutableStateOf("OUT") }

    val filteredList = transactions.filter { tx ->
        val cat = categories.find { it.id == tx.categoryId }
        when (selectedFilter) {
            "IN" -> cat?.jenisInOut == "IN"
            "OUT" -> cat?.jenisInOut == "OUT"
            else -> true
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(getString("history", lang), fontSize = getFontSize(22f, fontSizeScale), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            FilterTabButton(getString("expense", lang), selectedFilter == "OUT") { selectedFilter = "OUT" }
            FilterTabButton(getString("income", lang), selectedFilter == "IN") { selectedFilter = "IN" }
            FilterTabButton(getString("all", lang), selectedFilter == "ALL") { selectedFilter = "ALL" }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (selectedFilter != "ALL" && filteredList.isNotEmpty()) {
                item { ExpenseDonutChartCard(transactions = filteredList, categories = categories, filterType = selectedFilter, currencySymbol = currencySymbol, fontSizeScale = fontSizeScale) }
            }

            if (filteredList.isEmpty()) {
                item { Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("Tidak ada riwayat.", color = Color.Gray) } }
            } else {
                items(filteredList) { tx ->
                    val category = categories.find { it.id == tx.categoryId }
                    TransactionItem(tx = tx, category = category, currencySymbol = currencySymbol, fontSizeScale = fontSizeScale)
                }
            }
        }
    }
}

@Composable
fun ExpenseDonutChartCard(transactions: List<TransactionEntity>, categories: List<CategoryEntity>, filterType: String, currencySymbol: String, fontSizeScale: String) {
    val totalAmount = transactions.sumOf { it.nominal }
    val categoryTotals = transactions.groupBy { it.categoryId }.mapValues { entry -> entry.value.sumOf { it.nominal } }

    val colors = listOf(Color(0xFF8FBC8F), Color(0xFFE57373), Color(0xFF64B5F6), Color(0xFFFFB74D), Color(0xFFA1887F), Color(0xFFBA68C8))

    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(if (filterType == "OUT") "Pengeluaran" else "Pemasukan", fontWeight = FontWeight.Bold, fontSize = getFontSize(14f, fontSizeScale))
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                    Canvas(modifier = Modifier.size(100.dp)) {
                        var startAngle = -90f
                        categoryTotals.values.forEachIndexed { index, amount ->
                            val sweepAngle = ((amount / totalAmount) * 360).toFloat()
                            drawArc(color = colors[index % colors.size], startAngle = startAngle, sweepAngle = sweepAngle, useCenter = false, style = Stroke(width = 24.dp.toPx()))
                            startAngle += sweepAngle
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total", fontSize = getFontSize(10f, fontSizeScale), color = Color.Gray)
                        Text(formatCustomCurrency(totalAmount, currencySymbol), fontSize = getFontSize(11f, fontSizeScale), fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                    categoryTotals.entries.forEachIndexed { index, (catId, amount) ->
                        val catName = categories.find { it.id == catId }?.nama ?: "Lainnya"
                        val percentage = (amount / totalAmount * 100).toInt()

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(colors[index % colors.size], CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(catName, fontSize = getFontSize(11f, fontSizeScale), modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                            Text("$percentage%", fontSize = getFontSize(11f, fontSizeScale), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// DIALOG CATAT TRANSAKSI (+ MASUKKAN KATEGORI SENDIRI)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionDialogGoPay(
    categories: List<CategoryEntity>,
    initialType: String,
    currencySymbol: String,
    lang: String,
    onDismiss: () -> Unit,
    onAddCategory: (String, String) -> Unit,
    onSave: (Long, Double, String) -> Unit
) {
    var selectedType by remember { mutableStateOf(initialType) }
    val filteredCategories = categories.filter { it.jenisInOut == selectedType }
    var selectedCat by remember { mutableStateOf(filteredCategories.firstOrNull()) }
    var rawNominalInput by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    var showNewCatField by remember { mutableStateOf(false) }
    var newCatInput by remember { mutableStateOf("") }

    LaunchedEffect(selectedType) { selectedCat = categories.firstOrNull { it.jenisInOut == selectedType } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catat Transaksi Baru", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp)).padding(2.dp)) {
                    FilterTabButton(getString("expense", lang), selectedType == "OUT") { selectedType = "OUT" }
                    FilterTabButton(getString("income", lang), selectedType == "IN") { selectedType = "IN" }
                }

                Text("Pilih Kategori:", fontSize = 12.sp, color = Color.Gray)

                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    filteredCategories.forEach { cat ->
                        FilterChip(
                            selected = selectedCat?.id == cat.id,
                            onClick = { selectedCat = cat },
                            label = { Text(cat.nama, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = Color.White)
                        )
                    }
                }

                if (!showNewCatField) {
                    TextButton(onClick = { showNewCatField = true }) {
                        Text(getString("add_custom_cat", lang), fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newCatInput,
                            onValueChange = { newCatInput = it },
                            label = { Text("Nama Kategori Baru", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        Button(onClick = {
                            if (newCatInput.isNotEmpty()) {
                                onAddCategory(newCatInput, selectedType)
                                newCatInput = ""
                                showNewCatField = false
                            }
                        }) {
                            Text("+")
                        }
                    }
                }

                OutlinedTextField(
                    value = formatInputNumber(rawNominalInput),
                    onValueChange = { input -> rawNominalInput = input.filter { it.isDigit() } },
                    label = { Text("Nominal") },
                    prefix = { Text("$currencySymbol ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Catatan / Keterangan") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                val num = rawNominalInput.toDoubleOrNull() ?: 0.0
                if (selectedCat != null && num > 0) onSave(selectedCat!!.id, num, note)
            }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text("Simpan", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = MaterialTheme.colorScheme.primary) } }
    )
}

// HELPER DUMMY / PENDUKUNG
@Composable
fun GoalItemGoPay(goal: SavingsGoalEntity, currencySymbol: String, fontSizeScale: String, onAddDepositClick: () -> Unit, onEditGoalClick: () -> Unit, onDeleteGoalClick: () -> Unit) {
    val progress = (goal.terkumpul / goal.targetNominal).toFloat().coerceIn(0f, 1f)
    val percentage = (progress * 100).toInt()

    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Savings, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(goal.targetNama, fontWeight = FontWeight.Bold, fontSize = getFontSize(15f, fontSizeScale), color = MaterialTheme.colorScheme.onSurface)
                }
                Row {
                    IconButton(onClick = onEditGoalClick, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray) }
                    IconButton(onClick = onDeleteGoalClick, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFC62828)) }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${formatCustomCurrency(goal.terkumpul, currencySymbol)} terkumpul", fontSize = getFontSize(12f, fontSizeScale), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                Text("Target: ${formatCustomCurrency(goal.targetNominal, currencySymbol)}", fontSize = getFontSize(12f, fontSizeScale), color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.outlineVariant)

            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("$percentage% Tercapai", fontSize = getFontSize(11f, fontSizeScale), color = Color.Gray, fontWeight = FontWeight.Medium)
                Button(onClick = onAddDepositClick, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp), shape = RoundedCornerShape(8.dp)) {
                    Text("+ Isi Tabungan", color = Color.White, fontSize = getFontSize(11f, fontSizeScale))
                }
            }
        }
    }
}

@Composable
fun TransactionItem(tx: TransactionEntity, category: CategoryEntity?, currencySymbol: String, fontSizeScale: String) {
    val isIn = category?.jenisInOut == "IN"
    val colorNominal = if (isIn) Color(0xFF2E7D32) else Color(0xFFC62828)
    val prefix = if (isIn) "+ " else "- "

    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(36.dp).background(if (isIn) MaterialTheme.colorScheme.outlineVariant else Color(0xFFFFEBEE), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(imageVector = if (isIn) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward, contentDescription = null, tint = if (isIn) Color(0xFF2E7D32) else Color(0xFFC62828), modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(category?.nama ?: "Umum", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = getFontSize(14f, fontSizeScale))
                    if (tx.catatan.isNotEmpty()) Text(tx.catatan, style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontSize = getFontSize(12f, fontSizeScale))
                    Text(tx.tanggal, style = MaterialTheme.typography.labelSmall, color = Color.LightGray, fontSize = getFontSize(10f, fontSizeScale))
                }
            }
            Text("$prefix${formatCustomCurrency(tx.nominal, currencySymbol)}", color = colorNominal, fontWeight = FontWeight.Bold, fontSize = getFontSize(14f, fontSizeScale))
        }
    }
}

@Composable
fun GoPayActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, fontSizeScale: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { onClick() }.padding(8.dp)) {
        Box(modifier = Modifier.size(40.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = Color.White, fontSize = getFontSize(12f, fontSizeScale), fontWeight = FontWeight.Medium)
    }
}

@Composable
fun RowScope.FilterTabButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent).clickable { onClick() }.padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun GoalFormDialog(title: String, initialName: String, initialTarget: Double, currencySymbol: String, onDismiss: () -> Unit, onSave: (String, Double) -> Unit) {
    var name by remember { mutableStateOf(initialName) }
    var rawTargetInput by remember { mutableStateOf(if (initialTarget > 0) initialTarget.toLong().toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Target") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = formatInputNumber(rawTargetInput),
                    onValueChange = { input -> rawTargetInput = input.filter { it.isDigit() } },
                    label = { Text("Target Nominal") },
                    prefix = { Text("$currencySymbol ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val num = rawTargetInput.toDoubleOrNull() ?: 0.0
                if (name.isNotEmpty() && num > 0) onSave(name, num)
            }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text("Simpan", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = MaterialTheme.colorScheme.primary) } }
    )
}

@Composable
fun DepositDialog(goalName: String, currencySymbol: String, onDismiss: () -> Unit, onSave: (Double) -> Unit) {
    var rawAmountInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Isi Tabungan: $goalName") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = formatInputNumber(rawAmountInput),
                    onValueChange = { input -> rawAmountInput = input.filter { it.isDigit() } },
                    label = { Text("Setor Nominal") },
                    prefix = { Text("$currencySymbol ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val num = rawAmountInput.toDoubleOrNull() ?: 0.0
                if (num > 0) onSave(num)
            }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text("Setor Sekarang", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = MaterialTheme.colorScheme.primary) } }
    )
}

fun formatCustomCurrency(number: Double, symbol: String): String {
    val formattedNumber = NumberFormat.getNumberInstance(Locale("id", "ID")).format(number)
    return "$symbol $formattedNumber"
}

fun formatInputNumber(digits: String): String {
    if (digits.isEmpty()) return ""
    val parsed = digits.toLongOrNull() ?: return digits
    return NumberFormat.getNumberInstance(Locale("id", "ID")).format(parsed)
}