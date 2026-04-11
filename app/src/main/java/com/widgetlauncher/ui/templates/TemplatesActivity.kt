package com.widgetlauncher.ui.templates

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.widgetlauncher.databinding.ActivityTemplatesBinding
import com.widgetlauncher.ui.editor.EditorActivity

data class WidgetTemplateItem(
    val id: String,
    val name: String,
    val description: String,
    val framework: String,
    val previewColor: String,
    val code: String
)

class TemplatesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTemplatesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTemplatesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Templates"

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = TemplatesAdapter(TEMPLATES) { template ->
            startActivity(Intent(this, EditorActivity::class.java).apply {
                putExtra(EditorActivity.EXTRA_TEMPLATE_CODE, template.code)
                putExtra(EditorActivity.EXTRA_TEMPLATE_NAME, template.name)
                putExtra(EditorActivity.EXTRA_TEMPLATE_FRAMEWORK, template.framework)
            })
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    companion object {
        val TEMPLATES = listOf(
            WidgetTemplateItem("clock", "Digital Clock", "Live clock with date", "vanilla", "#6366f1", CLOCK_CODE),
            WidgetTemplateItem("weather", "Weather Card", "Static weather display", "vanilla", "#3b82f6", WEATHER_CODE),
            WidgetTemplateItem("todo", "Quick Tasks", "Minimal todo list", "vanilla", "#10b981", TODO_CODE),
            WidgetTemplateItem("pomodoro", "Pomodoro Timer", "Focus timer", "vanilla", "#ef4444", POMODORO_CODE),
            WidgetTemplateItem("crypto", "Crypto Ticker", "Live crypto prices", "vanilla", "#f59e0b", CRYPTO_CODE),
            WidgetTemplateItem("vue-counter", "Vue Counter", "Interactive counter", "vue", "#42b883", VUE_CODE),
            WidgetTemplateItem("react-quote", "React Quote Card", "Daily quotes", "react", "#8b5cf6", REACT_CODE),
        )
    }
}

private val CLOCK_CODE = """<!DOCTYPE html>
<html><head><meta name="viewport" content="width=device-width,initial-scale=1">
<style>* { margin:0;padding:0;box-sizing:border-box; } body { background:linear-gradient(135deg,#1e1b4b,#312e81);display:flex;flex-direction:column;align-items:center;justify-content:center;height:100vh;font-family:'Courier New',monospace;color:#e0e7ff;overflow:hidden; } .time { font-size:14vw;font-weight:bold;letter-spacing:2px;text-shadow:0 0 20px rgba(129,140,248,0.8); } .date { font-size:4vw;margin-top:8px;color:#a5b4fc;letter-spacing:2px; } .dot { animation:blink 1s infinite; } @keyframes blink { 0%,100%{opacity:1}50%{opacity:0} }</style></head>
<body><div class="time" id="t">00<span class="dot">:</span>00</div><div class="date" id="d"></div>
<script>function u(){const n=new Date();const h=String(n.getHours()).padStart(2,'0');const m=String(n.getMinutes()).padStart(2,'0');document.getElementById('t').innerHTML=h+'<span class="dot">:</span>'+m;const days=['Sun','Mon','Tue','Wed','Thu','Fri','Sat'];const months=['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];document.getElementById('d').textContent=days[n.getDay()]+', '+months[n.getMonth()]+' '+n.getDate();}u();setInterval(u,1000);</script>
</body></html>"""

private val WEATHER_CODE = """<!DOCTYPE html>
<html><head><meta name="viewport" content="width=device-width,initial-scale=1">
<style>* {margin:0;padding:0;box-sizing:border-box;} body {background:linear-gradient(180deg,#0ea5e9,#0284c7);height:100vh;display:flex;align-items:center;justify-content:center;font-family:-apple-system,sans-serif;color:white;} .card {text-align:center;padding:24px;} .icon {font-size:60px;animation:float 3s ease-in-out infinite;} @keyframes float {0%,100%{transform:translateY(0)}50%{transform:translateY(-10px)}} .temp {font-size:48px;font-weight:200;margin:8px 0;} .city {font-size:16px;opacity:0.9;letter-spacing:1px;}</style></head>
<body><div class="card"><div class="icon">⛅</div><div class="temp">24°C</div><div class="city">San Francisco</div></div></body></html>"""

private val TODO_CODE = """<!DOCTYPE html>
<html><head><meta name="viewport" content="width=device-width,initial-scale=1">
<style>* {margin:0;padding:0;box-sizing:border-box;} body {background:#0f172a;height:100vh;font-family:-apple-system,sans-serif;color:#f8fafc;padding:14px;overflow:hidden;display:flex;flex-direction:column;} h3 {font-size:11px;color:#22c55e;margin-bottom:10px;letter-spacing:1px;} .input-row {display:flex;gap:6px;margin-bottom:10px;} input {flex:1;background:#1e293b;border:1px solid #334155;color:#f8fafc;padding:7px 10px;border-radius:8px;font-size:13px;outline:none;} button {background:#22c55e;color:#0f172a;border:none;border-radius:8px;padding:7px 12px;font-weight:bold;cursor:pointer;} .item {display:flex;align-items:center;gap:7px;padding:7px 0;border-bottom:1px solid #1e293b;} .check {width:17px;height:17px;border-radius:50%;border:2px solid #334155;cursor:pointer;flex-shrink:0;} .check.done {background:#22c55e;border-color:#22c55e;} .text {font-size:13px;flex:1;} .text.done {text-decoration:line-through;color:#64748b;} .del {color:#475569;cursor:pointer;font-size:16px;}</style></head>
<body><h3>TASKS</h3><div class="input-row"><input id="inp" placeholder="Add task..." onkeydown="if(event.key==='Enter')add()"><button onclick="add()">+</button></div><div id="list"></div>
<script>let t=JSON.parse(localStorage.getItem('t')||'[{"text":"Buy groceries","done":false},{"text":"Call dentist","done":true}]');function save(){localStorage.setItem('t',JSON.stringify(t));}function render(){document.getElementById('list').innerHTML=t.map((i,n)=>`<div class="item"><div class="check \${i.done?'done':''}" onclick="toggle(\${n})"></div><span class="text \${i.done?'done':''}">\${i.text}</span><span class="del" onclick="del(\${n})">×</span></div>`).join('');}function add(){const v=document.getElementById('inp').value.trim();if(!v)return;t.unshift({text:v,done:false});save();render();document.getElementById('inp').value='';}function toggle(n){t[n].done=!t[n].done;save();render();}function del(n){t.splice(n,1);save();render();}render();</script></body></html>"""

private val POMODORO_CODE = """<!DOCTYPE html>
<html><head><meta name="viewport" content="width=device-width,initial-scale=1">
<style>* {margin:0;padding:0;box-sizing:border-box;} body {background:linear-gradient(135deg,#7f1d1d,#991b1b);height:100vh;display:flex;align-items:center;justify-content:center;font-family:-apple-system,sans-serif;color:white;} .wrap {text-align:center;} .mode {font-size:11px;letter-spacing:3px;color:#fca5a5;margin-bottom:16px;} .timer {font-size:56px;font-weight:200;font-variant-numeric:tabular-nums;} .btn {background:rgba(255,255,255,0.15);border:none;color:white;padding:10px 28px;border-radius:20px;cursor:pointer;font-size:13px;margin:12px 4px 0;}</style></head>
<body><div class="wrap"><div class="mode" id="mode">FOCUS</div><div class="timer" id="timer">25:00</div><div><button class="btn" id="btn" onclick="toggle()">Start</button><button class="btn" onclick="reset()">Reset</button></div></div>
<script>let work=25*60,brk=5*60,time=work,running=false,interval=null,isWork=true;function fmt(s){return String(Math.floor(s/60)).padStart(2,'0')+':'+String(s%60).padStart(2,'0');}function render(){document.getElementById('timer').textContent=fmt(time);document.getElementById('btn').textContent=running?'Pause':'Start';document.getElementById('mode').textContent=isWork?'FOCUS':'BREAK';}function toggle(){running=!running;if(running){interval=setInterval(tick,1000);}else{clearInterval(interval);}render();}function tick(){time--;if(time<=0){clearInterval(interval);running=false;isWork=!isWork;time=isWork?work:brk;}render();}function reset(){clearInterval(interval);running=false;isWork=true;time=work;render();}render();</script></body></html>"""

private val CRYPTO_CODE = """<!DOCTYPE html>
<html><head><meta name="viewport" content="width=device-width,initial-scale=1">
<style>* {margin:0;padding:0;box-sizing:border-box;} body {background:#0c0a09;height:100vh;font-family:'Courier New',monospace;color:#fef3c7;display:flex;flex-direction:column;justify-content:center;padding:16px;} h3 {font-size:11px;letter-spacing:3px;color:#d97706;margin-bottom:12px;} .coin {display:flex;justify-content:space-between;align-items:center;padding:8px 0;border-bottom:1px solid #1c1917;} .sym {font-weight:bold;font-size:13px;} .price {font-size:13px;} .change {font-size:11px;padding:2px 5px;border-radius:4px;} .up {color:#4ade80;background:rgba(74,222,128,0.1);} .down {color:#f87171;background:rgba(248,113,113,0.1);}</style></head>
<body><h3>CRYPTO</h3><div id="coins"></div>
<script>const data=[{sym:'BTC',price:67240,change:2.3},{sym:'ETH',price:3820,change:-1.1},{sym:'SOL',price:178,change:5.7},{sym:'BNB',price:412,change:0.8}];function fmt(n){return n>=1000?'$'+(n/1000).toFixed(1)+'k':'$'+n.toFixed(0);}function render(){document.getElementById('coins').innerHTML=data.map(c=>`<div class="coin"><span class="sym">\${c.sym}</span><div><span class="price">\${fmt(c.price)}</span> <span class="change \${c.change>=0?'up':'down'}">\${c.change>=0?'+':''}\${c.change}%</span></div></div>`).join('');}function tick(){data.forEach(c=>{c.price=Math.max(1,c.price+(Math.random()-0.48)*c.price*0.002);c.change=+(c.change+(Math.random()-0.5)*0.2).toFixed(2);});render();}render();setInterval(tick,2000);</script></body></html>"""

private val VUE_CODE = """<!DOCTYPE html>
<html><head><meta name="viewport" content="width=device-width,initial-scale=1"><script src="https://unpkg.com/vue@3/dist/vue.global.prod.js"><\/script>
<style>* {margin:0;padding:0;box-sizing:border-box;} body {background:linear-gradient(135deg,#064e3b,#065f46);height:100vh;display:flex;align-items:center;justify-content:center;font-family:-apple-system,sans-serif;color:white;} #app {text-align:center;} h3 {font-size:13px;letter-spacing:2px;color:#6ee7b7;margin-bottom:16px;} .count {font-size:72px;font-weight:200;} .controls {display:flex;gap:16px;justify-content:center;margin-top:24px;} button {width:48px;height:48px;border-radius:50%;border:2px solid #6ee7b7;background:transparent;color:#6ee7b7;font-size:24px;cursor:pointer;transition:all 0.2s;} button:active {background:#6ee7b7;color:#064e3b;transform:scale(0.9);}</style></head>
<body><div id="app"><h3>COUNTER</h3><div class="count">{{ count }}</div><div class="controls"><button @click="count>0&&count--">-</button><button @click="count++">+</button></div></div>
<script>Vue.createApp({data(){return{count:0}}}).mount('#app');<\/script></body></html>"""

private val REACT_CODE = """<!DOCTYPE html>
<html><head><meta name="viewport" content="width=device-width,initial-scale=1"><script src="https://unpkg.com/react@18/umd/react.production.min.js"><\/script><script src="https://unpkg.com/react-dom@18/umd/react-dom.production.min.js"><\/script><script src="https://unpkg.com/@babel/standalone/babel.min.js"><\/script>
<style>* {margin:0;padding:0;box-sizing:border-box;} body {background:linear-gradient(135deg,#4c1d95,#5b21b6);height:100vh;display:flex;align-items:center;justify-content:center;font-family:Georgia,serif;overflow:hidden;}</style></head>
<body><div id="root"></div>
<script type="text/babel">const quotes=[{text:"The only way to do great work is to love what you do.",author:"Steve Jobs"},{text:"Code is poetry.",author:"WordPress"},{text:"First solve the problem, then write the code.",author:"John Johnson"}];function App(){const[i,setI]=React.useState(0);const q=quotes[i];return(<div style={{textAlign:'center',padding:'24px',color:'white'}}><div style={{fontSize:'28px',marginBottom:'16px'}}>✦</div><p style={{fontSize:'15px',lineHeight:1.6,fontStyle:'italic',color:'#e9d5ff',marginBottom:'16px'}}>"{q.text}"</p><p style={{fontSize:'11px',letterSpacing:'2px',color:'#c4b5fd'}}>— {q.author}</p><button onClick={()=>setI(n=>(n+1)%quotes.length)} style={{marginTop:'20px',background:'transparent',border:'1px solid rgba(255,255,255,0.3)',color:'white',padding:'8px 20px',borderRadius:'20px',cursor:'pointer',fontSize:'12px'}}>Next</button></div>);}ReactDOM.createRoot(document.getElementById('root')).render(<App/>);<\/script></body></html>"""