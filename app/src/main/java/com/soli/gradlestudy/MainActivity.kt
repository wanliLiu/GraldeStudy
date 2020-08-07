package com.soli.gradlestudy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gradHello.text = "Gradle Study"


        if (BuildConfig.DEBUG && gradHello.isVisible) {
            gradHello.append("\nVisiable")
        }

//        gradHello.append("\n 通过Gradle 插件自动生成的类： ${MyPluginTestClass::class.java.name} \n 类的内容：${MyPluginTestClass.param1}")
    }
}
