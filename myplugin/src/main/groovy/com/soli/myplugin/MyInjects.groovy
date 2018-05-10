package com.soli.myplugin

import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.gradle.api.Project

/**
 * Created by soli on 18-3-30.
 */

public class MyInjects {

    final static def pool = ClassPool.getDefault()
    /**
     *
     * @param path
     * @param project
     */
    static def inject(String path, Project project) {
        //将当前路径加入类池,不然找不到这个类
        pool.insertClassPath(path)
        //project.android.bootClasspath 加入android.jar，不然找不到android相关的所有类
        pool.insertClassPath(project.android.bootClasspath[0].toString())
        //AppCompatActivity
        pool.insertClassPath("/home/soli/.gradle/caches/transforms-1/files-1.1/appcompat-v7-27.1.0.aar/c5f9b9afd2c30e21e9b7cc142f7b1e61/jars/classes.jar")
        pool.insertClassPath("/home/soli/.gradle/caches/transforms-1/files-1.1/support-fragment-27.1.0.aar/969eff80804fc158faf654470daf699b/jars/classes.jar")
        pool.insertClassPath("/home/soli/.gradle/caches/transforms-1/files-1.1/support-compat-27.1.0.aar/a38b48c82f573c8e82895a88e83c2dfa/jars/classes.jar")
        pool.insertClassPath("/home/soli/.gradle/caches/modules-2/files-2.1/android.arch.lifecycle/common/1.1.0/edf3f7bfb84a7521d0599efa3b0113a0ee90f85/common-1.1.0.jar")
        pool.insertClassPath("/home/soli/.gradle/caches/transforms-1/files-1.1/viewmodel-1.1.0.aar/df0d2e289c6ec3851027412d87fbde3c/jars/classes.jar")
        pool.insertClassPath("/home/soli/.gradle/caches/transforms-1/files-1.1/support-core-utils-27.1.0.aar/6f96448d359647fff68216aeef064e9a/jars/classes.jar")
        //引入android.os.Bundle包，因为onCreate方法参数有Bundle
        pool.importPackage("android.os.Bundle")

        def dir = new File(path)
        if (dir.isDirectory()) {
            dir.eachFileRecurse { File file ->
                def filePath = file.absolutePath
                println("filepath: $filePath")
                if (file.name.equals("MainActivity.class")) {
                    //获取MainActivity.class
                    CtClass ctClass = pool.getCtClass("com.soli.gradlestudy.MainActivity")
                    println("ctclass:$ctClass")
                    //解冻
                    if (ctClass.isFrozen())
                        ctClass.defrost()

                    CtMethod ctMethod = ctClass.getDeclaredMethod("onCreate")
                    println("方法名：$ctMethod")

                    String insetBeforeStr = """ android.widget.Toast.makeText(this,"我是通过Transfrom被插入的Toast代码~!!数量的看来是打开sakdssdksldkskddsdlsllaldlsdksldsdlksdl",android.widget.Toast.LENGTH_LONG).show();"""
                    ctMethod.insertAfter(insetBeforeStr)
                    ctClass.writeFile(path)
                    ctClass.detach()///释放
                }
            }
        }
    }
}
