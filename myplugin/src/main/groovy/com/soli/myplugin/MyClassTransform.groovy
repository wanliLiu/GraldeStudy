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
        return true
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)

        println("=================================================================")
        println("transform{ context= ${transformInvocation.context}\n" +
                "inputs=${transformInvocation.inputs}\n" +
                "referencedInputs=${transformInvocation.referencedInputs}\n" +
                "secondaryInputs=${transformInvocation.secondaryInputs}\n" +
                "outputProvider=${transformInvocation.outputProvider}\n" +
                "incremental=${transformInvocation.incremental}")
    }
}