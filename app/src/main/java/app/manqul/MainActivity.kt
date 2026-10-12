package app.manqul

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject
import java.io.File
import java.util.concurrent.Executors

val NAMES = ("Al-Fatihah,Al-Baqarah,Ali ’Imran,An-Nisa’,Al-Ma’idah,Al-An’am,Al-A’raf,Al-Anfal,At-Taubah,Yunus,Hud,Yusuf,Ar-Ra’d,Ibrahim,Al-Hijr,An-Nahl,Al-Isra’,Al-Kahf,Maryam,Taha,Al-Anbiya’,Al-Hajj,Al-Mu’minun,An-Nur,Al-Furqan,Asy-Syu’ara’,An-Naml,Al-Qasas,Al-’Ankabut,Ar-Rum,Luqman,As-Sajdah,Al-Ahzab,Saba’,Fatir,Yasin,As-Saffat,Sad,Az-Zumar,Gafir,Fussilat,Asy-Syura,Az-Zukhruf,Ad-Dukhan,Al-Jasiyah,Al-Ahqaf,Muhammad,Al-Fath,Al-Hujurat,Qaf,Az-Zariyat,At-Tur,An-Najm,Al-Qamar,Ar-Rahman,Al-Waqi’ah,Al-Hadid,Al-Mujadilah,Al-Hasyr,Al-Mumtahanah,As-Saff,Al-Jumu’ah,Al-Munafiqun,At-Tagabun,At-Talaq,At-Tahrim,Al-Mulk,Al-Qalam,Al-Haqqah,Al-Ma’arij,Nuh,Al-Jinn,Al-Muzzammil,Al-Muddassir,Al-Qiyamah,Al-Insan,Al-Mursalat,An-Naba’,An-Nazi’at,’Abasa,At-Takwir,Al-Infitar,Al-Mutaffifin,Al-Insyiqaq,Al-Buruj,At-Tariq,Al-A’la,Al-Gasyiyah,Al-Fajr,Al-Balad,Asy-Syams,Al-Lail,Ad-Duha,Al-Insyirah,At-Tin,Al-’Alaq,Al-Qadr,Al-Bayyinah,Az-Zalzalah,Al-’Adiyat,Al-Qari’ah,At-Takasur,Al-’Asr,Al-Humazah,Al-Fil,Quraisy,Al-Ma’un,Al-Kausar,Al-Kafirun,An-Nasr,Al-Lahab,Al-Ikhlas,Al-Falaq,An-Nas").split(",")

val BUILTIN: Map<Int, List<String>> = mapOf(1 to listOf(
    "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ", "ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَٰلَمِينَ", "ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
    "مَٰلِكِ يَوْمِ ٱلدِّينِ", "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "ٱهْدِنَا ٱلصِّرَٰطَ ٱلْمُسْتَقِيمَ",
    "صِرَٰطَ ٱلَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ ٱلْمَغْضُوبِ عَلَيْهِمْ وَلَا ٱلضَّآلِّينَ"))

val COLORS = listOf(Color(0xFFFACC15), Color(0xFF34D399), Color(0xFF60A5FA), Color(0xFFF472B6), Color(0xFFFB923C))

data class Note(val m: String, val c: Int, val len: Int)
data class Sel(val ayah: Int, val start: Int, val len: Int)

fun normAr(w: String) = w.replace(Regex("[\\u064B-\\u065F\\u0670\\u06D6-\\u06ED\\u0640]"), "").replace(Regex("[ٱأإآ]"), "ا")

fun stripBism(s: Int, a: Int, t: String): String {
    if (a != 1 || s == 1 || s == 9) return t
    val w = t.split(" ").filter { it.isNotEmpty() }
    return if (w.size > 4 && w.take(4).joinToString(" ") { normAr(it) } == "بسم الله الرحمن الرحيم") w.drop(4).joinToString(" ") else t
}

fun parseQuran(raw: String): Map<Int, List<String>>? {
    val m = HashMap<Int, MutableList<String>>()
    var n = 0
    raw.lineSequence().forEach { l ->
        val p = l.split("|", limit = 3)
        if (p.size == 3) {
            val s = p[0].trim().toIntOrNull()
            val a = p[1].trim().toIntOrNull()
            if (s != null && a != null && a >= 1 && p[2].isNotBlank()) {
                val list = m.getOrPut(s) { mutableListOf() }
                while (list.size < a) list.add("")
                list[a - 1] = stripBism(s, a, p[2].trim())
                n++
            }
        }
    }
    return if (n >= 6000) m else null
}

class Store(ctx: Context) {
    val notes = mutableStateMapOf<String, Note>()
    val ayat = mutableStateMapOf<String, String>()
    var surah by mutableStateOf(1)
    var fontScale by mutableStateOf(1f)
    var noteScale by mutableStateOf(1f)
    var perPage by mutableStateOf(10)
    var page by mutableStateOf(0)
    var quran by mutableStateOf(BUILTIN)
    var full by mutableStateOf(false)
    private val io = Executors.newSingleThreadExecutor()
    private val dataFile = File(ctx.filesDir, "data.json")
    private val textFile = File(ctx.filesDir, "quran.txt")

    init {
        try {
            if (dataFile.exists()) {
                val o = JSONObject(dataFile.readText())
                surah = o.optInt("surah", 1)
                fontScale = o.optDouble("font", 1.0).toFloat()
                noteScale = o.optDouble("note", 1.0).toFloat()
                perPage = o.optInt("per", 10)
                o.optJSONObject("notes")?.let { n ->
                    n.keys().forEach { k ->
                        val x = n.getJSONObject(k)
                        notes[k] = Note(x.getString("m"), x.optInt("c"), x.optInt("len", 1))
                    }
                }
                o.optJSONObject("ayat")?.let { a -> a.keys().forEach { k -> ayat[k] = a.getString(k) } }
            }
        } catch (e: Exception) { }
        try {
            if (textFile.exists()) parseQuran(textFile.readText())?.let { quran = it; full = true }
        } catch (e: Exception) { }
    }

    fun json(): String {
        val n = JSONObject()
        notes.forEach { (k, v) -> n.put(k, JSONObject().put("m", v.m).put("c", v.c).put("len", v.len)) }
        val a = JSONObject()
        ayat.forEach { (k, v) -> a.put(k, v) }
        val s = JSONObject().put("surah", surah).put("font", fontScale.toDouble()).put("note", noteScale.toDouble()).put("per", perPage).put("notes", n).put("ayat", a).toString()
        return s
    }

    fun save() {
        val s = json()
        io.execute { dataFile.writeText(s) }
    }

    fun importData(raw: String): Boolean = try {
        val o = JSONObject(raw)
        o.optJSONObject("notes")?.let { n ->
            n.keys().forEach { k ->
                val x = n.getJSONObject(k)
                notes[k] = Note(x.getString("m"), x.optInt("c"), x.optInt("len", 1))
            }
        }
        o.optJSONObject("ayat")?.let { a -> a.keys().forEach { k -> ayat[k] = a.getString(k) } }
        save()
        true
    } catch (e: Exception) {
        false
    }

    fun importText(raw: String): Boolean {
        val q = parseQuran(raw) ?: return false
        textFile.writeText(raw)
        quran = q
        full = true
        return true
    }

    fun removeText() {
        textFile.delete()
        quran = BUILTIN
        full = false
        surah = 1
        page = 0
        save()
    }
}

fun key(s: Int, i: Int, j: Int) = "$s|$i|$j"
fun wordsOf(st: Store, i: Int): List<String> =
    (st.quran[st.surah].orEmpty().getOrNull(i) ?: "").split(" ").filter { it.isNotEmpty() }

fun spanAt(st: Store, i: Int, j: Int): Pair<Int, Int> {
    for (a in 0..j) {
        val l = st.notes[key(st.surah, i, a)]?.len ?: 1
        if (a + l > j) return a to l
    }
    return j to 1
}

fun clearRange(st: Store, i: Int, from: Int, to: Int) {
    for (j in 0 until wordsOf(st, i).size) {
        val x = st.notes[key(st.surah, i, j)]
        if (x != null && j < to && j + x.len > from) st.notes.remove(key(st.surah, i, j))
    }
}

fun arabicNum(n: Int) = n.toString().map { '٠' + (it - '0') }.joinToString("")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val st = Store(applicationContext)
        setContent {
            MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
                Surface(Modifier.fillMaxSize()) { App(st) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(st: Store) {
    var dlg by remember { mutableStateOf(0) }
    var sel by remember { mutableStateOf<Sel?>(null) }
    val list = rememberLazyListState()
    val ayat = st.quran[st.surah].orEmpty()
    LaunchedEffect(st.surah, st.page) { list.scrollToItem(0) }
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("${st.surah}. ${NAMES[st.surah - 1]}") },
            actions = {
                TextButton(onClick = { dlg = 1 }) { Text("Surah") }
                TextButton(onClick = { dlg = 2 }) { Text("Pengaturan") }
            })
    }) { pad ->
        LazyColumn(Modifier.padding(pad), state = list) {
            if (ayat.isEmpty()) item {
                Text("Teks surah ini belum dimuat. Buka Pengaturan untuk memuat seluruh Al-Quran.", Modifier.padding(16.dp))
            }
            val per = st.perPage.coerceAtLeast(1)
            val pages = maxOf(1, (ayat.size + per - 1) / per)
            val pg = st.page.coerceIn(0, pages - 1)
            val lo = pg * per
            val hi = minOf(lo + per, ayat.size)
            if (pages > 1) item { Pager(st, pg, pages, ayat.size) }
            if (pg == 0 && ayat.isNotEmpty() && st.surah != 1 && st.surah != 9) item {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Text("بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ", fontSize = (28 * st.fontScale).sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
                }
            }
            items(hi - lo) { n -> AyahCard(st, lo + n) { sel = it } }
            if (pages > 1) item { Pager(st, pg, pages, ayat.size) }
        }
    }
    if (dlg == 1) SurahDialog(st) { dlg = 0 }
    if (dlg == 2) SettingsDialog(st) { dlg = 0 }
    sel?.let { s -> key(s) { Editor(st, s) { sel = null } } }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AyahCard(st: Store, i: Int, onPick: (Sel) -> Unit) {
    val s = st.surah
    val ws = wordsOf(st, i)
    Card(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
        Column(Modifier.padding(14.dp)) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    var j = 0
                    while (j < ws.size) {
                        val n = st.notes[key(s, i, j)]
                        val l = minOf(n?.len ?: 1, ws.size - j)
                        val start = j
                        Column(
                            Modifier.clip(RoundedCornerShape(8.dp))
                                .background(if (n != null) COLORS[n.c.coerceIn(0, 4)].copy(alpha = 0.4f) else Color.Transparent)
                                .clickable { onPick(Sel(i, start, l)) }
                                .padding(horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(ws.subList(start, start + l).joinToString(" "), fontSize = (28 * st.fontScale).sp)
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Text(n?.m ?: " ", fontSize = (12 * st.fontScale).sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        j += l
                    }
                    Text("۝" + arabicNum(i + 1), fontSize = (22 * st.fontScale).sp, color = MaterialTheme.colorScheme.primary)
                }
            }
            val parts = mutableListOf<String>()
            var jj = 0
            while (jj < ws.size) {
                val n = st.notes[key(s, i, jj)]
                val p = n?.m ?: "…"
                if (!(p == "…" && parts.lastOrNull() == "…")) parts.add(p)
                jj += n?.len ?: 1
            }
            if (parts.any { it != "…" }) Text(parts.joinToString(" "), Modifier.padding(top = 8.dp))
            val ak = "$s|$i"
            OutlinedTextField(
                value = st.ayat[ak] ?: "",
                onValueChange = { if (it.isBlank()) st.ayat.remove(ak) else st.ayat[ak] = it; st.save() },
                placeholder = { Text("Keterangan atau penjelasan ayat ini", fontSize = (16 * st.noteScale).sp, fontStyle = FontStyle.Italic) },
                textStyle = LocalTextStyle.current.copy(fontSize = (16 * st.noteScale).sp, fontStyle = FontStyle.Italic),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun Editor(st: Store, sel0: Sel, onClose: () -> Unit) {
    var sel by remember { mutableStateOf(sel0) }
    val ws = wordsOf(st, sel.ayah)
    val ex = st.notes[key(st.surah, sel.ayah, sel.start)]
    var text by remember { mutableStateOf(ex?.m ?: "") }
    var col by remember { mutableStateOf(ex?.c ?: 0) }
    val phrase = ws.subList(sel.start, minOf(ws.size, sel.start + sel.len)).joinToString(" ")
    AlertDialog(
        onDismissRequest = onClose,
        title = { CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) { Text(phrase, fontSize = 30.sp) } },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Arti") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    COLORS.forEachIndexed { idx, c ->
                        Box(
                            Modifier.size(32.dp).clip(CircleShape).background(c)
                                .border(if (idx == col) 3.dp else 0.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                .clickable { col = idx }
                        )
                    }
                }
                Text("Gabung kata:", fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = {
                        if (sel.start > 0) {
                            val (a, _) = spanAt(st, sel.ayah, sel.start - 1)
                            sel = Sel(sel.ayah, a, sel.start + sel.len - a)
                        }
                    }) { Text("→ Sebelumnya") }
                    TextButton(onClick = {
                        val end = sel.start + sel.len
                        if (end < ws.size) {
                            val (a, l) = spanAt(st, sel.ayah, end)
                            sel = Sel(sel.ayah, sel.start, a + l - sel.start)
                        }
                    }) { Text("Berikutnya ←") }
                }
                TextButton(onClick = { sel = sel.copy(len = 1) }) { Text("Satu kata saja") }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                clearRange(st, sel.ayah, sel.start, sel.start + sel.len)
                val t = text.trim()
                if (t.isNotEmpty()) st.notes[key(st.surah, sel.ayah, sel.start)] = Note(t, col, sel.len)
                st.save()
                onClose()
            }) { Text("Simpan") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = {
                    clearRange(st, sel.ayah, sel.start, sel.start + sel.len)
                    st.save()
                    onClose()
                }) { Text("Hapus") }
                TextButton(onClick = onClose) { Text("Batal") }
            }
        }
    )
}

@Composable
fun SurahDialog(st: Store, onClose: () -> Unit) {
    var q by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Pilih surah") },
        text = {
            Column {
                OutlinedTextField(value = q, onValueChange = { q = it }, label = { Text("Cari nama atau nomor") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                if (!st.full) Text("Baru Al-Fatihah yang tersedia. Muat seluruh Al-Quran di Pengaturan.", fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
                LazyColumn(Modifier.height(340.dp)) {
                    items(114) { n ->
                        val name = "${n + 1}. ${NAMES[n]}"
                        if (q.isBlank() || name.contains(q.trim(), ignoreCase = true)) {
                            Text(
                                name,
                                Modifier.fillMaxWidth().clickable { st.surah = n + 1; st.page = 0; st.save(); onClose() }.padding(vertical = 12.dp),
                                fontWeight = if (n + 1 == st.surah) FontWeight.Bold else null
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onClose) { Text("Tutup") } }
    )
}

@Composable
fun SettingsDialog(st: Store, onClose: () -> Unit) {
    val ctx = LocalContext.current
    var msg by remember { mutableStateOf("") }
    val backup = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            msg = try {
                ctx.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(st.json()) }
                "Cadangan berhasil disimpan."
            } catch (e: Exception) { "Gagal menyimpan cadangan." }
        }
    }
    val restore = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val raw = try { ctx.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } } catch (e: Exception) { null }
            msg = if (raw != null && st.importData(raw)) "Cadangan berhasil dipulihkan." else "File cadangan tidak valid."
        }
    }
    val pick = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val raw = try { ctx.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } } catch (e: Exception) { null }
            msg = if (raw != null && st.importText(raw)) "Berhasil memuat Al-Quran lengkap." else "File tidak dikenali. Pakai format Tanzil \"Text (with aya numbers)\"."
        }
    }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Pengaturan") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Ukuran huruf: ${(st.fontScale * 100).toInt()}%")
                Slider(value = st.fontScale, onValueChange = { st.fontScale = (Math.round(it * 10) / 10f) }, onValueChangeFinished = { st.save() }, valueRange = 0.8f..2f)
                Text("Ukuran keterangan ayat: ${(st.noteScale * 100).toInt()}%")
                Slider(value = st.noteScale, onValueChange = { st.noteScale = (Math.round(it * 10) / 10f) }, onValueChangeFinished = { st.save() }, valueRange = 0.8f..2f)
                Text("Ayat per halaman (makin sedikit makin ringan)")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(5, 10, 20, 30).forEach { n ->
                        FilterChip(selected = st.perPage == n, onClick = { st.perPage = n; st.page = 0; st.save() }, label = { Text("$n") })
                    }
                }
                Text(if (st.full) "Al-Quran lengkap sudah dimuat (114 surah)." else "Baru Al-Fatihah bawaan. Unduh teks dari Tanzil lalu pilih filenya.")
                Text("Pilih jenis teks Uthmani atau Simple, format \"Text (with aya numbers)\".", fontSize = 13.sp)
                Button(onClick = { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://tanzil.net/download"))) }) { Text("Buka tanzil.net/download") }
                Button(onClick = { pick.launch(arrayOf("*/*")) }) { Text("Pilih file teks Al-Quran") }
                if (st.full) TextButton(onClick = { st.removeText() }) { Text("Hapus teks tersimpan") }
                Text("Cadangan arti dan keterangan (juga bisa memakai file ekspor dari versi web)")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { backup.launch("manqul-cadangan.json") }) { Text("Ekspor") }
                    Button(onClick = { restore.launch(arrayOf("*/*")) }) { Text("Impor") }
                }
                if (msg.isNotEmpty()) Text(msg)
                Text("Arti dan keterangan tersimpan di HP ini dan ikut tercadangkan ke akun Google bila pencadangan Android aktif.", fontSize = 12.sp)
            }
        },
        confirmButton = { TextButton(onClick = onClose) { Text("Tutup") } }
    )
}

@Composable
fun Pager(st: Store, pg: Int, pages: Int, total: Int) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = { st.page = pg - 1 }, enabled = pg > 0) { Text("← Sebelumnya") }
        Text("Ayat ${pg * st.perPage + 1}–${minOf(total, (pg + 1) * st.perPage)} dari $total", fontSize = 13.sp)
        TextButton(onClick = { st.page = pg + 1 }, enabled = pg < pages - 1) { Text("Berikutnya →") }
    }
}
