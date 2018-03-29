package com.soli.myplugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.*
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskState

public class PluginImpl implements Plugin<Project> {

    //统计每个任务执行的时间
    private class TimingsListener implements TaskExecutionListener, BuildListener {
        private long clock
        private timings = []

        @Override
        void beforeExecute(Task task) {
            clock = System.currentTimeMillis()
        }

        @Override
        void afterExecute(Task task, TaskState taskState) {
            def ms = System.currentTimeMillis() - clock
            timings.add([ms, task.path])
            task.project.logger.warn "${task.path} took ${ms}ms"
        }

        @Override
        void buildFinished(BuildResult result) {
            println "Task timings:"
            for (timing in timings) {
//            if (timing[0] >= 50) {
                printf "%7sms  %s\n", timing
//            }
            }
        }

        @Override
        void buildStarted(Gradle gradle) {}

        @Override
        void projectsEvaluated(Gradle gradle) {}

        @Override
        void projectsLoaded(Gradle gradle) {}

        @Override
        void settingsEvaluated(Settings settings) {}
    }

    @Override
    void apply(Project project) {
        println "配置阶段-----自定义的插件开始运行"
        println("统计task的执行时间---注册")
        project.gradle.addListener(new TimingsListener())
        println("自定义参数获取------")
        def closureExtension = project.extensions.create("ClosureInput", CloserExtension)
        closureExtension.with {
            apiUrl = "https://www.baidu.com/api/test/ds/sds"
        }
        project.gradle.addProjectEvaluationListener(new projectEvalu(closureExtension))

        project.extensions.create('pluginExt', PluginExtension)
        project.pluginExt.extensions.create('nestExt', PluginNestExtension)
        project.pluginExt.nestExt.extensions.create('other', AgainExtension)
        CustomTask task = project.task(type: CustomTask, CustomTask.NAME)
        def task1 = project.task("testTask") << {
            println("运行-----Hello gradle plugin")
        }
        task1.dependsOn task

//        project.afterEvaluate {
//            println("121212----------------------------通过Project Closure来获取嵌套的参数输入")
//            println("输入的参数：${closureExtension.toString()}")
//        }

        def isApp = project.plugins.hasPlugin(AppPlugin)
        if (isApp) {
            println("==================$project.name transformAPI===============")
            def android = project.extensions.findByType(AppExtension)
            def transformImpl = new MyClassTransform(project)
            android.registerTransform(transformImpl)

            android.applicationVariants.all { variant ->
                //获取到scope,作用域
                def variantData = variant.variantData
                def scope = variantData.scope
                //拿到build.gradle中创建的Extension的值
                def config = project.extensions.findByName("pluginExt")
                //创建一个task
                def createTaskName = scope.getTaskName("CeShi", "MyTestPlugin")
                def createTask = project.task(createTaskName)
                createTask << {
                    createJavaTest(variant, config)
                }
                //设置task依赖于生成BuildConfig的task，然后在生成BuildConfig后生成我们的类
                String generateBuildConfigTaskName = variant.getVariantData().getScope().getGenerateBuildConfigTask().name
                def generateBuildConfigTask = project.tasks.getByName(generateBuildConfigTaskName)
                if (generateBuildConfigTask) {
                    createTask.dependsOn generateBuildConfigTask
                    generateBuildConfigTask.finalizedBy createTask
                }
            }
        }
    }

    static def createJavaTest(variant, config) {
        //要生成的内容
        def content = "package com.soli.gradlestudy;\n" +
                " /**\n" +
                "   * Created by Soli on 2018/3/29.\n" +
                "   */\n" +
                " public class MyPlguinTestClass {\n" +
                " \tpublic static final String param1 = \"${config.param1}\";\n" +
                " }"
        //获取到BuildConfig类的路径
        File outputDir = variant.getVariantData().getScope().getBuildConfigSourceOutputDir()
        def javaFile = new File(outputDir, "MyPluginTestClass.java")
        javaFile.write(content, javaFile)
    }
    /**
     *
     */
    class projectEvalu implements ProjectEvaluationListener {

        private CloserExtension extension

        projectEvalu(CloserExtension extension) {
            this.extension = extension
        }

        @Override
        void beforeEvaluate(Project project) {

        }

        @Override
        void afterEvaluate(Project project, ProjectState projectState) {
            println("-------------------------------通过Project Closure来获取嵌套的参数输入")
            println("输入的参数：${extension.toString()}")
        }
    }
}