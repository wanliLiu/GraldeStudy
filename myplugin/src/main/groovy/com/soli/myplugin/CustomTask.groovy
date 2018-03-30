import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * 定一个task
 */
class CustomTask extends DefaultTask {
    static final String NAME = 'CustomTask'

    @TaskAction
    void output() {
        println "param1 is ${project.pluginExt.param1}"
        println "param2 is ${project.pluginExt.param2}"
        println "param3 is ${project.pluginExt.param3}"

        println "nestParams1 is ${project.pluginExt.nestExt.nestParams1}"
        println "nestParams2 is ${project.pluginExt.nestExt.nestParams2}"
        println "nestParams3 is ${project.pluginExt.nestExt.nestParams3}"

        println "url is ${project.pluginExt.nestExt.other.url}"
        println "path is ${project.pluginExt.nestExt.other.path}"
    }
}