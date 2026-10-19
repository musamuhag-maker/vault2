package com.calc.calculator

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

class VaultActivity : AppCompatActivity() {
    class App(val pkg: String, val label: String, val icon: Drawable)

    private lateinit var prefs: SharedPreferences
    private lateinit var content: LinearLayout
    private lateinit var adapter: ArrayAdapter<App>
    private lateinit var toggle: Button
    private var all = listOf<App>()
    private val shown = ArrayList<App>()
    private var edit = false
    private var unlocked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        val bg = Color.parseColor("#202124")
        window.statusBarColor = bg
        window.navigationBarColor = bg
        prefs = getSharedPreferences("c", 0)

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            visibility = View.INVISIBLE
        }
        val bar = LinearLayout(this).apply { setPadding(16, 24, 16, 8) }
        toggle = Button(this).apply {
            isAllCaps = false
            text = "Add / remove apps"
            setOnClickListener {
                edit = !edit
                text = if (edit) "Done" else "Add / remove apps"
                refresh()
            }
        }
        val codeBtn = Button(this).apply {
            isAllCaps = false
            text = "Change code"
            setOnClickListener { changeCode() }
        }
        bar.addView(toggle, LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f))
        bar.addView(codeBtn, LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f))
        content.addView(bar, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))

        val list = ListView(this).apply { divider = null }
        val empty = TextView(this).apply {
            text = "Vault is empty.\nTap \"Add / remove apps\" and tick the apps you want here."
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        }
        val frame = FrameLayout(this)
        frame.addView(list, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
        frame.addView(empty, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
        list.emptyView = empty
        content.addView(frame, LinearLayout.LayoutParams(MATCH_PARENT, 0, 1f))

        adapter = object : ArrayAdapter<App>(this, 0, shown) {
            override fun getView(p: Int, v: View?, parent: ViewGroup): View {
                val a = shown[p]
                val row = LinearLayout(context).apply {
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(32, 18, 32, 18)
                }
                row.addView(ImageView(context).apply { setImageDrawable(a.icon) }, LinearLayout.LayoutParams(112, 112))
                row.addView(TextView(context).apply {
                    text = a.label
                    setTextColor(Color.WHITE)
                    textSize = 17f
                    setPadding(36, 0, 0, 0)
                }, LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f))
                if (edit) row.addView(CheckBox(context).apply {
                    isChecked = a.pkg in protectedSet()
                    isClickable = false
                    isFocusable = false
                })
                return row
            }
        }
        list.adapter = adapter
        list.setOnItemClickListener { _, _, pos, _ ->
            val a = shown[pos]
            if (edit) {
                val s = protectedSet().toMutableSet()
                if (!s.add(a.pkg)) s.remove(a.pkg)
                prefs.edit().putStringSet("p", s).apply()
                adapter.notifyDataSetChanged()
            } else {
                packageManager.getLaunchIntentForPackage(a.pkg)?.let { startActivity(it) }
            }
        }
        setContentView(content)
        authenticate()
    }

    private fun protectedSet(): Set<String> = prefs.getStringSet("p", emptySet())!!.toSet()

    private fun authenticate() {
        val bm = BiometricManager.from(this)
        if (bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) != BiometricManager.BIOMETRIC_SUCCESS) {
            Toast.makeText(this, "Set up a fingerprint in phone Settings first", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock")
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
        BiometricPrompt(this, ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    unlocked = true
                    content.visibility = View.VISIBLE
                    load()
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    finish()
                }
            }).authenticate(info)
    }

    private fun load() {
        val i = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        all = packageManager.queryIntentActivities(i, 0)
            .map { App(it.activityInfo.packageName, it.loadLabel(packageManager).toString(), it.loadIcon(packageManager)) }
            .filter { it.pkg != packageName }
            .distinctBy { it.pkg }
            .sortedBy { it.label.lowercase() }
        refresh()
    }

    private fun refresh() {
        val p = protectedSet()
        shown.clear()
        shown.addAll(if (edit) all else all.filter { it.pkg in p })
        adapter.notifyDataSetChanged()
    }

    private fun changeCode() {
        val et = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            hint = "New code (digits, 3+)"
        }
        AlertDialog.Builder(this)
            .setTitle("Secret code (type it, then press =)")
            .setView(et)
            .setPositiveButton("Save") { _, _ ->
                val c = et.text.toString()
                if (c.length >= 3) {
                    prefs.edit().putString("code", c).apply()
                    Toast.makeText(this, "Code saved", Toast.LENGTH_SHORT).show()
                } else Toast.makeText(this, "Too short", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onStop() {
        super.onStop()
        if (unlocked) finish()
    }
}
