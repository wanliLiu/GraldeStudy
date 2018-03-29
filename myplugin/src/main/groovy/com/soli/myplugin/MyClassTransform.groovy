import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import org.gradle.api.Project

public class MyClassTransform extends Transform{

    private Project project

    public MyClassTransform(Project mProject){
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
        super.transform(transformInvocation)

        println("自定义的TransFrom开始=================================================================")
        println("transform{ context= \n${transformInvocation.context}\n" +
                "inputs=\n${transformInvocation.inputs}\n" +
                "referencedInputs=\n${transformInvocation.referencedInputs}\n" +
                "secondaryInputs=\n${transformInvocation.secondaryInputs}\n" +
                "outputProvider=\n${transformInvocation.outputProvider}\n" +
                "incremental=${transformInvocation.incremental}")
        println("自定义的TransFrom结束=================================================================")
    }
}