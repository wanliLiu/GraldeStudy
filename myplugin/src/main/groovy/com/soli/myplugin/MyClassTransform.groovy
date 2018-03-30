import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import com.soli.myplugin.MyInjects
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

class MyClassTransform extends Transform {

    private Project project

    public MyClassTransform(Project mProject) {
        this.project = mProject
    }

    //transformClassesWith + getName() + For + Debug或Release
    @Override
    String getName() {
        return "MyClassTransform"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
//        super.transform(transformInvocation)
        println("自定义的TransFrom开始=================================================================")
//        println("transform{ context= \n${transformInvocation.context}\n" +
//                "inputs=\n${transformInvocation.inputs}\n" +
//                "referencedInputs=\n${transformInvocation.referencedInputs}\n" +
//                "secondaryInputs=\n${transformInvocation.secondaryInputs}\n" +
//                "outputProvider=\n${transformInvocation.outputProvider}\n" +
//                "incremental=${transformInvocation.incremental}")

        println("bootClasspath-------------${project.android.bootClasspath}")
        //遍历input
        transformInvocation.inputs.each { TransformInput input ->
            //遍历文件夹
            input.directoryInputs.each { DirectoryInput directoryInput ->
                println("DirectoryInput---->:$directoryInput.file.absolutePath")
                //注入代码
                MyInjects.inject(directoryInput.file.absolutePath, project)
                // 获取output目录

                def dest = transformInvocation.outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                println("DirectoryOuput---->:$dest.absolutePath")
                // 将input的目录复制到output指定目录
                //这里理解好像必须输出到制定目录
                FileUtils.copyDirectory(directoryInput.file, dest)
            }

            //遍历jar文件 对jar不操作，但是要输出到out路径
            input.jarInputs.each { JarInput jarInput ->
                // 重命名输出文件（同目录copyFile会冲突）
                def jarName = jarInput.name
                println("jar:$jarInput.file.absolutePath")
                def md5Name = DigestUtils.md5Hex(jarInput.file.absolutePath)
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                def dest = transformInvocation.outputProvider.getContentLocation(jarName + md5Name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                println("outjar:$dest.absolutePath")
                FileUtils.copyFile(jarInput.file, dest)
            }
        }
        println("自定义的TransFrom结束=================================================================")
    }
}