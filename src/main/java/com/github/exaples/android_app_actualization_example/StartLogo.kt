package com.github.exaples.android_app_actualization_example


import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.*
import android.net.ConnectivityManager
import android.os.*
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.widget.TextView
import android.view.animation.TranslateAnimation
import android.support.constraint.ConstraintLayout
import android.support.v4.content.LocalBroadcastManager
import android.view.ViewTreeObserver
import android.widget.Toast
import com.github.exaples.android_app_actualization_example.NetworkStateChangeReceiver.IS_NETWORK_AVAILABLE
import com.github.exaples.android_app_actualization_example.httpcall.HttpCall
import com.github.exaples.android_app_actualization_example.httpcall.HttpRequest
import com.github.exaples.android_app_actualization_example.httpcall.UpdateApp
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class StartLogo : AppCompatActivity() {

    private var global: Global? = null
    private var link: String? = null
    private var updateApp: UpdateApp? = null
    private var httpCallPost: HttpCall? = null
    private var alert: AlertDialog? = null
    private var progressBar: ProgressDialog? = null
    private var builder: AlertDialog.Builder? = null

    var app: TextView? = null
    var logo: TextView? = null

    var width : Float = 0f
    var newWidth : Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_logo)

        app = findViewById(R.id.textView4)
        logo = findViewById(R.id.textView3)

        //Start logo animation
        animationText()

        //Start actualization check
        verCheck()
    }


    private fun animationText(){
        val parent = findViewById<ConstraintLayout>(R.id.startLogoLayout)
        parent.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {

                val availableWidth = parent.measuredWidth
                parent.viewTreeObserver.removeGlobalOnLayoutListener(this)

                val summaryWidth = app!!.measuredWidth + logo!!.measuredWidth

                    width = (availableWidth - summaryWidth)/2f
                    newWidth = 0 - width

                val appAnimation = TranslateAnimation(350f, newWidth, 0f, 0f)
                appAnimation.duration = 2000
                appAnimation.fillAfter = true
                appAnimation.setAnimationListener(MyAnimationListener())
                app!!.startAnimation(appAnimation)

                val logoAnimation = TranslateAnimation(-350f, width, 0f, 0f)
                logoAnimation.duration = 2000
                logoAnimation.fillAfter = true
                logoAnimation.setAnimationListener(MyAnimationListener())
                logo!!.startAnimation(logoAnimation)
            }
        })

    }

    private inner class MyAnimationListener : AnimationListener {

        override fun onAnimationEnd(animation: Animation) { }

        override fun onAnimationRepeat(animation: Animation) {}

        override fun onAnimationStart(animation: Animation) {

        }

    }

    fun actStart(context: Activity){
        val newActivity = Intent(context, MainActivity::class.java)
        newActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(newActivity)
        context.finish()
    }


    private fun verCheck() {

        val appVer = BuildConfig.VERSION_NAME

        progresBarStart(resources.getString(R.string.str_actualization), 30000, true, this@StartLogo)


        if (httpCallPost == null) {
            httpCallPost = HttpCall()
        }

        httpCallPost!!.methodtype = HttpCall.POST
        httpCallPost!!.url = "http://" + global!!.ip + "/YOUR_PROJECT_NAME/versionUpd"

        val paramsPost = HashMap<String, String>()
        paramsPost["version"] = appVer
        httpCallPost!!.params = paramsPost

        object : HttpRequest() {
            override fun onResponse(response: String) {
                super.onResponse(response)
                if (response !== "") {

                    try {

                        progressBarStop()
                        val jsonobject = JSONObject(response)
                        val verId = jsonobject.getString("verId")
                        link = jsonobject.getString("link")

                        val builder = AlertDialog.Builder(this@StartLogo)
                        builder.setTitle(resources.getString(R.string.ver) + " " + verId +" "+ resources.getString(R.string.already_available))
                        builder.setMessage(resources.getString(R.string.do_you_want_to_update_the_app))
                                .setCancelable(false)

                                .setNegativeButton(resources.getString(R.string.later)) { dialog, id ->

                                    dialog.dismiss()

                                    Handler().postDelayed(Runnable {
                                        kotlin.run {
                                            actStart(this@StartLogo)
                                        }
                                    }, 1000)
                                }
                        builder.setPositiveButton(resources.getString(R.string.update)) { dialog, which ->

                            updateApp = UpdateApp()
                            updateApp!!.setContext(this@StartLogo)
                            updateApp!!.execute(link)
                            progresBarStart(resources.getString(R.string.str_actualization), 300000, true, this@StartLogo)
                        }

                        alert = builder.create()
                        alert!!.show()

                    } catch (e: JSONException) {
                        e.printStackTrace()

                    }

                } else {
                    object : CountDownTimer(1000, 1000) {

                        override fun onTick(millisUntilFinished: Long) {

                        }

                        override fun onFinish() {
                            progressBarStop()
                            actStart(this@StartLogo)
                        }

                    }.start()


                }

            }
        }.execute(httpCallPost)
    }

    fun progresBarStart(str: String, interval: Int?, mess: Boolean, act: Activity) {

        progressBarStop()

        progressBar = ProgressDialog(this)
        progressBar!!.setCancelable(false)
        progressBar!!.setMessage(str)
        progressBar!!.show()

        if (progressBar!!.isShowing) {

            object : CountDownTimer(interval!!.toLong(), 1000) {

                override fun onTick(millisUntilFinished: Long) {

                }

                override fun onFinish() {

                    if (progressBar != null) {

                        if (progressBar!!.isShowing) {
                            progressBar!!.dismiss()

                            if (mess) {
                                makeToast(act.resources.getString(R.string.connection_problem))
                                isNetworkAvailable()
                            }

                        } else {
                            progressBar = null
                        }
                    }

                }

            }.start()


        }


    }

    fun progressBarStop() {

        if (progressBar != null) {

            if (progressBar!!.isShowing) {

                progressBar!!.dismiss()
            }

            progressBar = null
        }


    }

    fun isNetworkAvailable(): Boolean {

        val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
            true
        } else {
            modal()
            false
        }
    }

    private fun modal() {

        if (alert == null && builder == null) {
            progressBarStop()
            builder = AlertDialog.Builder(this)
            builder!!.setTitle(resources.getString(R.string.no_connection))
            builder!!.setMessage(resources.getString(R.string.please_connect_to_the_internet))
                    .setCancelable(false)
                    .setNegativeButton(resources.getString(R.string.go_to_settings), { dialog, id -> this.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) })
            alert = builder!!.create()
            if (!alert!!.isShowing) {
                alert!!.show()
            }


        } else {
            if (!alert!!.isShowing) {
                alert!!.show()
            }

        }
        checkLan()
    }

    private fun checkLan() {
        val intentFilter = IntentFilter(NetworkStateChangeReceiver.NETWORK_AVAILABLE_ACTION)
        LocalBroadcastManager.getInstance(this).registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val isNetworkAvailable = intent.getBooleanExtra(IS_NETWORK_AVAILABLE, false)

                if (isNetworkAvailable && alert!!.isShowing) {
                    alert!!.dismiss()
                }
            }
        }, intentFilter)
    }

    fun makeToast(str: String) {
        Toast.makeText(this, str, Toast.LENGTH_LONG).show()
    }
}
