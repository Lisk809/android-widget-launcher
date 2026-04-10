package com.widgetlauncher.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

class WidgetRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("widget_launcher_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_WIDGETS = "widgets"

        @Volatile
        private var instance: WidgetRepository? = null

        fun getInstance(context: Context): WidgetRepository =
            instance ?: synchronized(this) {
                instance ?: WidgetRepository(context.applicationContext).also { instance = it }
            }
    }

    fun getWidgets(): MutableList<Widget> {
        val json = prefs.getString(KEY_WIDGETS, null) ?: return getDefaultWidgets()
        return try {
            val type = object : TypeToken<MutableList<Widget>>() {}.type
            gson.fromJson(json, type) ?: getDefaultWidgets()
        } catch (e: Exception) {
            getDefaultWidgets()
        }
    }

    fun saveWidget(widget: Widget) {
        val widgets = getWidgets()
        val idx = widgets.indexOfFirst { it.id == widget.id }
        if (idx >= 0) {
            widgets[idx] = widget.copy(updatedAt = System.currentTimeMillis())
        } else {
            widgets.add(0, widget)
        }
        saveAll(widgets)
    }

    fun deleteWidget(id: String) {
        val widgets = getWidgets()
        widgets.removeAll { it.id == id }
        saveAll(widgets)
    }

    fun getWidget(id: String): Widget? = getWidgets().find { it.id == id }

    private fun saveAll(widgets: List<Widget>) {
        prefs.edit().putString(KEY_WIDGETS, gson.toJson(widgets)).apply()
    }

    private fun getDefaultWidgets(): MutableList<Widget> {
        val defaults = mutableListOf(
            Widget(
                id = UUID.randomUUID().toString(),
                name = "Digital Clock",
                description = "Live clock widget",
                code = CLOCK_HTML,
                framework = WidgetFramework.VANILLA,
                size = WidgetSize.LARGE,
                color = "#6366f1"
            ),
            Widget(
                id = UUID.randomUUID().toString(),
                name = "Quick Tasks",
                description = "Minimal todo list",
                code = TODO_HTML,
                framework = WidgetFramework.VANILLA,
                size = WidgetSize.MEDIUM,
                color = "#10b981"
            ),
            Widget(
                id = UUID.randomUUID().toString(),
                name = "Crypto Ticker",
                description = "Live crypto prices",
                code = CRYPTO_HTML,
                framework = WidgetFramework.VANILLA,
                size = WidgetSize.WIDE,
                color = "#f59e0b"
            )
        )
        saveAll(defaults)
        return defaults
    }
}

private val CLOCK_HTML = """
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width,initial-scale=1">
<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body {
  background: linear-gradient(135deg, #1e1b4b, #312e81);
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  height: 100vh; font-family: 'Courier New', monospace;
  color: #e0e7ff; overflow: hidden;
}
.time { font-size: 14vw; font-weight: bold; letter-spacing: 2px; text-shadow: 0 0 20px rgba(129,140,248,0.8); }
.date { font-size: 4vw; margin-top: 8px; color: #a5b4fc; letter-spacing: 2px; }
.dot { animation: blink 1s infinite; }
@keyframes blink { 0%,100%{opacity:1} 50%{opacity:0} }
</style>
</head>
<body>
<div class="time" id="time">00<span class="dot">:</span>00</div>
<div class="date" id="date"></div>
<script>
function update() {
  const now = new Date();
  const h = String(now.getHours()).padStart(2,'0');
  const m = String(now.getMinutes()).padStart(2,'0');
  document.getElementById('time').innerHTML = h + '<span class="dot">:</span>' + m;
  const days = ['Sun','Mon','Tue','Wed','Thu','Fri','Sat'];
  const months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
  document.getElementById('date').textContent = days[now.getDay()] + ', ' + months[now.getMonth()] + ' ' + now.getDate();
}
update(); setInterval(update, 1000);
</script>
</body>
</html>
""".trimIndent()

private val TODO_HTML = """
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width,initial-scale=1">
<style>
* { margin:0; padding:0; box-sizing:border-box; }
body { background:#0f172a; height:100vh; font-family:-apple-system,sans-serif; color:#f8fafc; padding:12px; overflow:hidden; display:flex; flex-direction:column; }
h3 { font-size:11px; color:#22c55e; margin-bottom:10px; letter-spacing:1px; }
.input-row { display:flex; gap:6px; margin-bottom:10px; }
input { flex:1; background:#1e293b; border:1px solid #334155; color:#f8fafc; padding:6px 8px; border-radius:6px; font-size:12px; outline:none; }
button { background:#22c55e; color:#0f172a; border:none; border-radius:6px; padding:6px 10px; font-weight:bold; cursor:pointer; }
.item { display:flex; align-items:center; gap:6px; padding:6px 0; border-bottom:1px solid #1e293b; }
.check { width:16px; height:16px; border-radius:50%; border:2px solid #334155; cursor:pointer; flex-shrink:0; }
.check.done { background:#22c55e; border-color:#22c55e; }
.text { font-size:12px; flex:1; }
.text.done { text-decoration:line-through; color:#64748b; }
.del { color:#475569; cursor:pointer; }
</style>
</head>
<body>
<h3>TASKS</h3>
<div class="input-row">
<input id="inp" placeholder="Add task..." onkeydown="if(event.key==='Enter')add()">
<button onclick="add()">+</button>
</div>
<div id="list"></div>
<script>
let tasks=JSON.parse(localStorage.getItem('t')||'[{"text":"Buy groceries","done":false},{"text":"Call dentist","done":true}]');
function save(){localStorage.setItem('t',JSON.stringify(tasks));}
function render(){document.getElementById('list').innerHTML=tasks.map((t,i)=>`<div class="item"><div class="check ${t.done?'done':''}" onclick="toggle(${i})"></div><span class="text ${t.done?'done':''}">${t.text}</span><span class="del" onclick="del(${i})">×</span></div>`).join('');}
function add(){const v=document.getElementById('inp').value.trim();if(!v)return;tasks.unshift({text:v,done:false});save();render();document.getElementById('inp').value='';}
function toggle(i){tasks[i].done=!tasks[i].done;save();render();}
function del(i){tasks.splice(i,1);save();render();}
render();
</script>
</body>
</html>
""".trimIndent()

private val CRYPTO_HTML = """
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width,initial-scale=1">
<style>
* { margin:0; padding:0; box-sizing:border-box; }
body { background:#0c0a09; height:100vh; font-family:'Courier New',monospace; color:#fef3c7; display:flex; flex-direction:row; align-items:center; padding:12px; gap:16px; overflow:hidden; }
.coin { flex:1; text-align:center; }
.sym { font-weight:bold; font-size:14px; }
.price { font-size:12px; margin-top:2px; }
.change { font-size:11px; padding:2px 4px; border-radius:4px; display:inline-block; margin-top:2px; }
.up { color:#4ade80; background:rgba(74,222,128,0.1); }
.down { color:#f87171; background:rgba(248,113,113,0.1); }
</style>
</head>
<body>
<div id="coins"></div>
<script>
const data=[{sym:'BTC',price:67240,change:2.3},{sym:'ETH',price:3820,change:-1.1},{sym:'SOL',price:178,change:5.7},{sym:'BNB',price:412,change:0.8}];
function fmt(n){return n>=1000?'$'+(n/1000).toFixed(1)+'k':('$'+n.toFixed(0));}
function render(){document.getElementById('coins').innerHTML=data.map(c=>{const cls=c.change>=0?'up':'down';return `<div class="coin"><div class="sym">${c.sym}</div><div class="price">${fmt(c.price)}</div><span class="change ${cls}">${c.change>=0?'+':''}${c.change}%</span></div>`;}).join('');}
function tick(){data.forEach(c=>{c.price=Math.max(1,c.price+(Math.random()-0.48)*c.price*0.002);c.change=+(c.change+(Math.random()-0.5)*0.2).toFixed(2);});render();}
render();setInterval(tick,2000);
</script>
</body>
</html>
""".trimIndent()
